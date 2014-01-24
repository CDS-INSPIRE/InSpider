package nl.ipo.cds.etl.filtering;

import java.util.Arrays;

import nl.ipo.cds.domain.AttributeExpression;
import nl.ipo.cds.domain.AttributeType;
import nl.ipo.cds.domain.FilterExpression;
import nl.ipo.cds.domain.OperatorExpression;
import nl.ipo.cds.domain.OperatorExpression.OperatorType;
import nl.ipo.cds.domain.ValueExpression;
import nl.ipo.cds.domain.ValueExpression.ValueType;

public class FilterExpressionFactory {

	public static OperatorExpression equal (final AttributeExpression attribute, final ValueExpression value) {
		return operatorExpression (OperatorType.EQUALS, attribute, value);
	}

	public static OperatorExpression notEqual (final AttributeExpression attribute, final ValueExpression value) {
		return operatorExpression (OperatorType.NOT_EQUALS, attribute, value);
	}

	public static OperatorExpression lessThan (final AttributeExpression attribute, final ValueExpression value) {
		return operatorExpression (OperatorType.LESS_THAN, attribute, value);
	}

	public static OperatorExpression lessThanEqual (final AttributeExpression attribute, final ValueExpression value) {
		return operatorExpression (OperatorType.LESS_THAN_EQUAL, attribute, value);
	}
	
	public static OperatorExpression greaterThan (final AttributeExpression attribute, final ValueExpression value) {
		return operatorExpression (OperatorType.GREATER_THAN, attribute, value);
	}
	
	public static OperatorExpression greaterThanEqual (final AttributeExpression attribute, final ValueExpression value) {
		return operatorExpression (OperatorType.GREATER_THAN_EQUAL, attribute, value);
	}

	public static OperatorExpression like (final AttributeExpression attribute, final String value) {
		return operatorExpression (OperatorType.LIKE, attribute, stringValue (value));
	}

	public static OperatorExpression in (final AttributeExpression attribute, final String value) {
		return operatorExpression (OperatorType.IN, attribute, stringValue (value));
	}
	
	public static OperatorExpression and (final OperatorExpression ... children) {
		return operatorExpression (OperatorType.AND, children);
	}
	
	public static OperatorExpression or (final OperatorExpression ... children) {
		return operatorExpression (OperatorType.OR, children);
	}
	
	public static OperatorExpression notNull (final AttributeExpression child) {
		return operatorExpression (OperatorType.NOT_NULL, new FilterExpression[] { child });
	}
	
	public static OperatorExpression operatorExpression (final OperatorType operatorType, final FilterExpression ... children) {
		return operatorExpression (operatorType, true, children);
	}
	
	public static OperatorExpression operatorExpression (final OperatorType operatorType, final boolean caseSensitive, final FilterExpression ... children) {
		final OperatorExpression exp = new OperatorExpression ();
		
		exp.setOperatorType (operatorType);
		exp.setCaseSensitive (caseSensitive);
		
		if (children.length > 0) {
			exp.setInputs (Arrays.asList (children));
		}
		
		return exp;
	}
	
	public static AttributeExpression attribute (final String attributeName, final AttributeType attributeType) {
		final AttributeExpression attributeExpression = new AttributeExpression ();
		
		attributeExpression.setAttributeName (attributeName);
		attributeExpression.setAttributeType (attributeType);
		
		return attributeExpression;
	}
	
	public static ValueExpression integerValue (final int value) {
		return valueExpression (Integer.toString (value), ValueType.INTEGER);
	}
	
	public static ValueExpression stringValue (final String value) {
		return valueExpression (value, ValueType.STRING);
	}
	
	public static ValueExpression doubleValue (final double value) {
		return valueExpression (Double.toString (value), ValueType.DOUBLE);
	}
	
	public static ValueExpression valueExpression (final String value, final ValueType valueType) {
		final ValueExpression exp = new ValueExpression ();
		
		exp.setStringValue (value);
		exp.setValueType (valueType);
		
		return exp;
	}
}