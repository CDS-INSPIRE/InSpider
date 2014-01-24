package nl.ipo.cds.etl.filtering;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import nl.ipo.cds.domain.AttributeExpression;
import nl.ipo.cds.domain.FeatureType;
import nl.ipo.cds.domain.FilterExpression;
import nl.ipo.cds.domain.OperatorExpression;
import nl.ipo.cds.domain.ValueExpression;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.filter.Expression;
import org.deegree.filter.Filter;
import org.deegree.filter.MatchAction;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.BinaryComparisonOperator;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.comparison.PropertyIsGreaterThan;
import org.deegree.filter.comparison.PropertyIsGreaterThanOrEqualTo;
import org.deegree.filter.comparison.PropertyIsLessThan;
import org.deegree.filter.comparison.PropertyIsLessThanOrEqualTo;
import org.deegree.filter.comparison.PropertyIsLike;
import org.deegree.filter.comparison.PropertyIsNotEqualTo;
import org.deegree.filter.comparison.PropertyIsNull;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.logical.And;
import org.deegree.filter.logical.Not;
import org.deegree.filter.logical.Or;

public class FilterFactory {

	private final FeatureType featureType;
	
	public FilterFactory (final FeatureType featureType) {
		this.featureType = featureType;
	}
	
	public Filter createFilter (final FilterExpression expression) {
		if (!(expression instanceof OperatorExpression)) {
			throw new IllegalArgumentException ("expression must be an instance of FilterExpression");
		}
		return createFilter ((OperatorExpression)expression);
	}
	
	public Filter createFilter (final OperatorExpression inputFilter) {
		final Operator rootOperator = createOperator (inputFilter);
		
		return new OperatorFilter (rootOperator);
	}
	
	private Operator createOperator (final OperatorExpression expression) {
		final List<FilterExpression> inputs = expression.getInputs ();
		
		switch (expression.getOperatorType ()) {
		case AND:
			return new And (createChildrenArray (inputs));
		case OR:
			return new Or (createChildrenArray (inputs));
			
		case EQUALS:
			return createBinaryComparisonOperator (expression, PropertyIsEqualTo.class);
		case GREATER_THAN:
			return createBinaryComparisonOperator (expression, PropertyIsGreaterThan.class);
		case GREATER_THAN_EQUAL:
			return createBinaryComparisonOperator (expression, PropertyIsGreaterThanOrEqualTo.class);
		case LESS_THAN:
			return createBinaryComparisonOperator (expression, PropertyIsLessThan.class);
		case LESS_THAN_EQUAL:
			return createBinaryComparisonOperator (expression, PropertyIsLessThanOrEqualTo.class);
		case NOT_EQUALS:
			return createBinaryComparisonOperator (expression, PropertyIsNotEqualTo.class);
			
		case LIKE:
			return createLikeOperator (expression);
			
		case IN:
			return createInOperator (expression);
			
		case NOT_NULL:
			return createNotNullOperator (expression);
			
		default:
			throw new IllegalArgumentException (String.format ("Invalid operator type `%s`", expression.getOperatorType ()));
		}
	}
	
	/**
	 * Turns the given list of filter expressions into an array of Operators.
	 * 
	 * @param children The list of filter expressions to convert.
	 * @return An array of operators
	 */
	private Operator[] createChildrenArray (final List<FilterExpression> children) {
		final List<Operator> operators = new ArrayList<Operator> ();
		
		for (final FilterExpression childExpression: children) {
			if (!(childExpression instanceof OperatorExpression)) {
				throw new IllegalArgumentException ("Only operator expressions can be used as the child of a logical expression");
			}
			
			operators.add (createOperator ((OperatorExpression)childExpression));
		}
		
		return operators.toArray (new Operator[0]);
	}
	
	/**
	 * Turns the given operator expression into a binary comparison operator of the given type.
	 * 
	 * @param operatorExpression The expression to turn into a comparison.
	 * @return A binary comparison operator of the given type.
	 */
	private BinaryComparisonOperator createBinaryComparisonOperator (final OperatorExpression operatorExpression, final Class<? extends BinaryComparisonOperator> operatorClass) {
		final Constructor<? extends BinaryComparisonOperator> constructor;
		final List<FilterExpression> inputs = operatorExpression.getInputs ();
		
		// Check preconditions:
		if (inputs == null || inputs.size () != 2) {
			throw new IllegalArgumentException (String.format ("Binary comparison operator expects exactly two inputs"));
		}
		if (!(inputs.get (0) instanceof AttributeExpression)) {
			throw new IllegalArgumentException (String.format ("Binary comparison operator expects the first input to be of type AttributeExpression"));
		}
		if (!(inputs.get (1) instanceof ValueExpression)) {
			throw new IllegalArgumentException (String.format ("Binary comparison operator expects the second input to be of type ValueExpression"));
		}
		
		// Locate the constructor:
		try {
			constructor = operatorClass.getConstructor (Expression.class, Expression.class, Boolean.class, MatchAction.class);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException (String.format ("Comparison operator class `%s` doesn't have the expected constructor", operatorClass.getCanonicalName ()));
		} catch (SecurityException e) {
			throw new IllegalArgumentException (String.format ("Comparison operator class `%s` is not accessible", operatorClass.getCanonicalName ()));
		}
		
		try {
			return constructor.newInstance (
						createValueReference ((AttributeExpression)inputs.get (0)),
						createLiteral ((ValueExpression)inputs.get (1)),
						operatorExpression.isCaseSensitive (),
						MatchAction.ANY
					);
		} catch (InstantiationException e) {
			throw new RuntimeException (e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException (e);
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (InvocationTargetException e) {
			throw new RuntimeException (e);
		}
	}
	
	/**
	 * Turns the given expression into a PropertyIsLike operator.
	 * 
	 * @param expression The expression.
	 * @return An instance of PropertyIsLike. 
	 */
	private PropertyIsLike createLikeOperator (final OperatorExpression expression) {
		final List<FilterExpression> inputs = expression.getInputs ();
		
		// Check preconditions:
		if (inputs == null || inputs.size () != 2) {
			throw new IllegalArgumentException ("The like operator requires two inputs");
		}
		if (!(inputs.get (0) instanceof AttributeExpression)) {
			throw new IllegalArgumentException ("The first argument to the like operator must be a AttributeExpression");
		}
		if (!(inputs.get (1) instanceof ValueExpression)) {
			throw new IllegalArgumentException ("The second argument to the like operator must be a ValueExpression");
		}
		
		return new PropertyIsLike (
				createValueReference ((AttributeExpression)inputs.get (0)), 
				createLiteral ((ValueExpression)inputs.get (1)), 
				"*", 
				"?", 
				"\\", 
				expression.isCaseSensitive (), 
				MatchAction.ANY
			);
	}

	/**
	 * Turns the given operator into a sequence of operators that are equivalent in semantics
	 * to the 'in' operator. The first input expression must be an attribute reference and
	 * the second input is assumed to be a list of values. 
	 * 
	 * @param expression The expression
	 * @return The Or operator representing this expression.
	 */
	private Or createInOperator (final OperatorExpression expression) {
		final List<FilterExpression> inputs = expression.getInputs ();
		
		// Check preconditions:
		if (inputs == null || inputs.size () != 2) {
			throw new IllegalArgumentException ("The `in` operator requires two inputs");
		}
		if (!(inputs.get (0) instanceof AttributeExpression)) {
			throw new IllegalArgumentException ("The first argument to the in operator must be a AttributeExpression");
		}
		if (!(inputs.get (1) instanceof ValueExpression)) {
			throw new IllegalArgumentException ("The second argument to the in operator must be a ValueExpression");
		}
		if (((ValueExpression)inputs.get (1)).getStringValue () == null) {
			throw new IllegalArgumentException ("In operator needs a value");
		}
		
		// Create values:
		final String[] values = ((ValueExpression)inputs.get (1)).getStringValue ().split (",");
		final List<Operator> valueOperators = new ArrayList<Operator> ();
		final ValueReference valueReference = createValueReference ((AttributeExpression)inputs.get (0));

		for (final String value: values) {
			final String trimmedValue = value.trim ();
			
			valueOperators.add (new PropertyIsEqualTo (
					valueReference, 
					new Literal<TypedObjectNode> (trimmedValue), 
					expression.isCaseSensitive (), 
					MatchAction.ANY
				));
		}
		
		return new Or (valueOperators.toArray (new Operator[0]));
	}

	/**
	 * Turns the given expression into a Not Null expression (combination of not and propertyIsNull).
	 * 
	 * @param expression
	 * @return A not expression that contains a propertyIsNull expression.
	 */
	private Not createNotNullOperator (final OperatorExpression expression) {
		final List<FilterExpression> inputs = expression.getInputs ();
		
		// Check preconditions:
		if (inputs == null || inputs.size () < 1 || inputs.size () > 2) {
			throw new IllegalArgumentException ("The not null operator must have one input");
		}
		if (!(inputs.get (0) instanceof AttributeExpression)) {
			throw new IllegalArgumentException ("The first argument to the not null operator must be a AttributeExpression");
		}
		
		// Get the value reference:
		final ValueReference valueReference = createValueReference ((AttributeExpression)inputs.get (0));
		
		return new Not (new PropertyIsNull (valueReference, MatchAction.ANY));
	}

	/**
	 * Creates a value reference based on the given AttributeExpression.
	 *  
	 * @param expression
	 * @return A ValueReference for the requested attributes.
	 */
	private ValueReference createValueReference (final AttributeExpression expression) {
		return new ValueReference (new QName (
				featureType.getName ().getNamespace (), 
				expression.getAttributeName ()
			));
	}
	
	/**
	 * Creates a literal based on the given ValueExpression.
	 * 
	 * @param expression The expression
	 * @return A literal instance containg the value in the expression.
	 */
	private Literal<?> createLiteral (final ValueExpression expression) {
		return new Literal<TypedObjectNode> (expression.getStringValue ());
	}
}
