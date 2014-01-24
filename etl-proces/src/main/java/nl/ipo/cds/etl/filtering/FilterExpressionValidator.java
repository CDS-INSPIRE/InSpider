package nl.ipo.cds.etl.filtering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobLogger.LogLevel;
import nl.ipo.cds.domain.AttributeExpression;
import nl.ipo.cds.domain.AttributeType;
import nl.ipo.cds.domain.DatasetFilter;
import nl.ipo.cds.domain.FeatureType;
import nl.ipo.cds.domain.FeatureTypeAttribute;
import nl.ipo.cds.domain.FilterExpression;
import nl.ipo.cds.domain.OperatorExpression;
import nl.ipo.cds.domain.OperatorExpression.OperatorType;
import nl.ipo.cds.domain.ValueExpression;
import nl.ipo.cds.domain.ValueExpression.ValueType;
import nl.ipo.cds.etl.log.EventLogger;

public class FilterExpressionValidator {

	public enum MessageKey {
		DATASET_FILTER_TECHNICAL_ERROR,		// Technical errors.
		
		DATASET_FILTER_MISSING,				// There is no dataset filter (filter is null).
		DATASET_FILTER_NO_DATASET,			// The dataset filter has no dataset.
		DATASET_FILTER_NO_ROOT_EXPRESSION,	// The dataset filter has no root expression.
		
		DATASET_FILTER_LOGICAL_OPERATOR_WITHOUT_INPUTS,	// Logical operator must have at least one input.
		DATASET_FILTER_LOGICAL_OPERATOR_MISSING_INPUT,	// Logical operator has a missing (null) input.
		DATASET_FILTER_LOGICAL_OPERATOR_WRONG_TYPE,		// Logical operator input is not of type OperatorExpression.
		
		DATASET_FILTER_OPERATOR_NO_ATTRIBUTE,// Comparison operator must have an attribute.
		DATASET_FILTER_OPERATOR_NO_VALUE,	// Comparison operator has no value.
		DATASET_FILTER_OPERATOR_INCOMPATIBLE_TYPES, // Incompatible types in comparison operator.
		DATASET_FILTER_OPERATOR_INPUT_COUNT,			// Operator must have exactly two inputs.
		
		DATASET_FILTER_OPERATOR_MISSING_ATTRIBUTE				// An attribute is missing from the feature type.
	}
	
	private class Logger {
		private int messageCount = 0;
		private final Job job;
		
		public Logger (final Job job) {
			this.job = job;
		}
		
		public void report (final MessageKey messageKey, final String ... parameters) {
			final List<String> params = new ArrayList<String> ();
			final Map<String, Object> context = new HashMap<String, Object> ();
			
			context.put ("featureTypeName", featureType.getName ().getLocalPart ());
			context.put ("featureTypeNamespace", featureType.getName ().getNamespace ());
			
			logger.logEvent (job, messageKey, LogLevel.ERROR, context, params.toArray (new String[params.size ()]));
			++ messageCount;
		}
		
		public int getMessageCount () {
			return messageCount;
		}
	}
	
	private final FeatureType featureType;
	private final EventLogger<MessageKey> logger;
	
	public FilterExpressionValidator (final FeatureType featureType, final EventLogger<MessageKey> logger) {
		if (featureType == null) {
			throw new NullPointerException ("featureType cannot be null");
		}
		if (logger == null) {
			throw new NullPointerException ("logger cannot be null");
		}
		
		this.featureType = featureType;
		this.logger = logger;
	}
	
	public boolean isValid (final Job job, final DatasetFilter filter) {
		final Logger log = new Logger (job);
		
		validateDatasetFilter (filter, log);
		
		return log.getMessageCount () == 0;
	}
	
	private void validateDatasetFilter (final DatasetFilter filter, final Logger log) {
		if (filter == null) {
			log.report (MessageKey.DATASET_FILTER_MISSING);
			return;
		}
		
		// Filter must have a dataset:
		if (filter.getDataset () == null) {
			log.report (MessageKey.DATASET_FILTER_NO_DATASET);
		}
				
		// Filter must have a root expression:
		final FilterExpression rootExpression = filter.getRootExpression ();
		if (rootExpression == null) {
			log.report (MessageKey.DATASET_FILTER_NO_ROOT_EXPRESSION);
			return;
		}
				
		// Root expression must be of type OperatorExpression:
		if (rootExpression == null || !(rootExpression instanceof OperatorExpression)) {
			log.report (MessageKey.DATASET_FILTER_TECHNICAL_ERROR, "The root expression must be of type OperatorExpression");
			return;
		}
		
		validateRootOperation ((OperatorExpression)rootExpression, log);
	}
	
	private void validateRootOperation (final OperatorExpression rootExpression, final Logger log) {
		validateOperatorExpression (rootExpression, log);
	}
	
	private void validateOperatorExpression (final OperatorExpression expression, final Logger log) {
		// Expression must have an operator:
		final OperatorType operatorType = expression.getOperatorType ();
		if (operatorType == null) {
			log.report (MessageKey.DATASET_FILTER_TECHNICAL_ERROR, "An operator expression must have an operator type.");
			return;
		}

		// Operator specific validation: 
		switch (operatorType) {
		
		// Logical operators:
		case AND:
		case OR:
			validateLogicalOperator (expression, log);
			break;
			
		// Comparison operators:
		case EQUALS:
		case GREATER_THAN:
		case GREATER_THAN_EQUAL:
		case LESS_THAN:
		case LESS_THAN_EQUAL:
		case NOT_EQUALS:
			validateComparisonOperator (expression, log);
			break;

		// In-operator:
		case IN:
			validateInOperator (expression, log);
			break;

		// Like-operator:
		case LIKE:
			validateLikeOperator (expression, log);
			break;
			
		case NOT_NULL:
			validateNotNullOperator (expression, log);
			break;
			
		default:
			log.report (MessageKey.DATASET_FILTER_TECHNICAL_ERROR, String.format ("Unknown operator type `%s`", operatorType));
			break;
		}
	}
	
	private void validateLogicalOperator (final OperatorExpression expression, final Logger log) {
		final List<FilterExpression> inputs = expression.getInputs ();
		
		// The "and" operator must have inputs, the "or" operator is allowed without inputs (to allow empty filters):
		if (inputs == null || inputs.size () == 0) {
			if (expression.getOperatorType () != OperatorType.OR) {
				log.report (MessageKey.DATASET_FILTER_LOGICAL_OPERATOR_WITHOUT_INPUTS);
			}
			return;
		}
		
		// Validate inputs:
		for (final FilterExpression input: inputs) {
			// Input must exist:
			if (input == null) {
				log.report (MessageKey.DATASET_FILTER_LOGICAL_OPERATOR_MISSING_INPUT);
				continue;
			}
			
			// Input must be of type OperatorExpression:
			if (input == null || !(input instanceof OperatorExpression)) {
				log.report (MessageKey.DATASET_FILTER_LOGICAL_OPERATOR_WRONG_TYPE);
				continue;
			}
			
			validateOperatorExpression ((OperatorExpression)input, log);
		}
	}
	
	private void validateComparisonOperator (final OperatorExpression expression, final Logger log) {
		if (!validateOperatorInputs (expression, log)) {
			return;
		}
		
		final List<FilterExpression> inputs = expression.getInputs ();
		final AttributeExpression attributeExpression = (AttributeExpression)inputs.get (0);
		final FeatureTypeAttribute attribute = findAttribute (attributeExpression.getAttributeName (), attributeExpression.getAttributeType ());
		
		// The attribute and value must have matching types:
		if (!compareAttributeType (attribute, ((ValueExpression)inputs.get (1)).getValueType ())) {
			log.report (MessageKey.DATASET_FILTER_OPERATOR_INCOMPATIBLE_TYPES);
		}
	}

	private void validateInOperator (final OperatorExpression expression, final Logger log) {
		if (!validateOperatorInputs (expression, log)) {
			return;
		}
		
		final List<FilterExpression> inputs = expression.getInputs ();
		final ValueExpression valueExpression = (ValueExpression)inputs.get (1);
		
		if (valueExpression.getValueType () != ValueType.STRING) {
			log.report (MessageKey.DATASET_FILTER_OPERATOR_INCOMPATIBLE_TYPES);
		}
	}
	
	private void validateLikeOperator (final OperatorExpression expression, final Logger log) {
		if (!validateOperatorInputs (expression, log)) {
			return;
		}
		
		final List<FilterExpression> inputs = expression.getInputs ();
		final AttributeExpression attributeExpression = (AttributeExpression)inputs.get (0);
		final FeatureTypeAttribute attribute = findAttribute (attributeExpression.getAttributeName (), attributeExpression.getAttributeType ());
		final ValueExpression valueExpression = (ValueExpression)inputs.get (1);
		
		// The attribute and value must have matching types:
		if (!compareAttributeType (attribute, ((ValueExpression)inputs.get (1)).getValueType ()) || valueExpression.getValueType() != ValueType.STRING) {
			log.report (MessageKey.DATASET_FILTER_OPERATOR_INCOMPATIBLE_TYPES);
		}
	}
	
	private boolean validateNotNullOperator (final OperatorExpression expression, final Logger log) {
		final List<FilterExpression> inputs = expression.getInputs ();
		
		// A not null operator must have one input:
		if (inputs == null || inputs.size () < 1 || inputs.size () > 2) {
			log.report (MessageKey.DATASET_FILTER_OPERATOR_INPUT_COUNT);
			return false;
		}
		
		// First input must be an instance of AttributeExpression:
		if (inputs.get (0) == null || !(inputs.get (0) instanceof AttributeExpression) || ((AttributeExpression)inputs.get (0)).getAttributeName () == null || ((AttributeExpression)inputs.get (0)).getAttributeType () == null) {
			log.report (MessageKey.DATASET_FILTER_OPERATOR_NO_ATTRIBUTE);
			return false;
		}
		
		// The attribute must exist:
		final AttributeExpression attributeExpression = (AttributeExpression)inputs.get (0);
		final FeatureTypeAttribute attribute = findAttribute (attributeExpression.getAttributeName (), attributeExpression.getAttributeType ());
		if (attribute == null) {
			log.report (MessageKey.DATASET_FILTER_OPERATOR_MISSING_ATTRIBUTE, attributeExpression.getAttributeName ());
			return false;
		}
		
		return true;
	}

	private boolean validateOperatorInputs (final OperatorExpression expression, final Logger log) {
		final List<FilterExpression> inputs = expression.getInputs ();
		
		// A comparison operator must have exactly two inputs:
		if (inputs == null || inputs.size () != 2) {
			log.report (MessageKey.DATASET_FILTER_OPERATOR_INPUT_COUNT);
			return false;
		}
		
		// First input must be an instance of AttributeExpression:
		boolean invalidInputs = false;
		if (inputs.get (0) == null || !(inputs.get (0) instanceof AttributeExpression) || ((AttributeExpression)inputs.get (0)).getAttributeName () == null || ((AttributeExpression)inputs.get (0)).getAttributeType () == null) {
			log.report (MessageKey.DATASET_FILTER_OPERATOR_NO_ATTRIBUTE);
			invalidInputs = true;
		}
		
		// Second input must be an instance of ValueExpression:
		if (inputs.get (1) == null || !(inputs.get (1) instanceof ValueExpression) || ((ValueExpression)inputs.get (1)).getStringValue () == null || ((ValueExpression)inputs.get (1)).getValueType () == null) {
			log.report (MessageKey.DATASET_FILTER_OPERATOR_NO_VALUE);
			invalidInputs = true;
		}
		
		if (invalidInputs) {
			return false;
		}
		
		// The attribute must exist:
		final AttributeExpression attributeExpression = (AttributeExpression)inputs.get (0);
		final FeatureTypeAttribute attribute = findAttribute (attributeExpression.getAttributeName (), attributeExpression.getAttributeType ());
		if (attribute == null) {
			log.report (MessageKey.DATASET_FILTER_OPERATOR_MISSING_ATTRIBUTE, attributeExpression.getAttributeName ());
		}
		
		return true;
	}
	
	private boolean compareAttributeType (final FeatureTypeAttribute attribute, final ValueType valueType) {
		if (attribute == null) {
			return false;
		}
		
		final AttributeType at = attribute.getType ();
		
		switch (valueType) {
		case DATE:
			return at == AttributeType.DATE;
		case DATE_TIME:
			return at == AttributeType.DATE_TIME;
		case DOUBLE:
			return at == AttributeType.DECIMAL || at == AttributeType.DOUBLE || at == AttributeType.FLOAT || at == AttributeType.GEOMETRY;
		case INTEGER:
			return at == AttributeType.INTEGER || at == AttributeType.GEOMETRY;
		case STRING:
			return at == AttributeType.STRING;
		case TIME:
			return at == AttributeType.TIME;
			
		default:
			return false;
		}
	}
	
	private FeatureTypeAttribute findAttribute (final String attributePath, final AttributeType attributeType) {
		final String attributeName = stripPath (attributePath);
		
		for (final FeatureTypeAttribute attr: featureType.getAttributes ()) {
			if (attr.getName ().getLocalPart ().equals (attributeName) && attr.getType ().equals (attributeType)) {
				return attr;
			}
		}
		
		return null;
	}
	
	private final String stripPath (final String attributePath) {
		if (attributePath == null) {
			return null;
		}
		
		final int offset = attributePath.indexOf ('/');
		if (offset >= 0) {
			return attributePath.substring (0, offset);
		}
		
		return attributePath;
	}
}
