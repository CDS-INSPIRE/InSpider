package nl.ipo.cds.etl.attributemapping;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobLogger.LogLevel;
import nl.ipo.cds.attributemapping.AttributeMapperUtils;
import nl.ipo.cds.attributemapping.operations.Operation;
import nl.ipo.cds.attributemapping.operations.OperationInput;
import nl.ipo.cds.attributemapping.operations.OperationInputType;
import nl.ipo.cds.attributemapping.operations.OperationType;
import nl.ipo.cds.dao.attributemapping.InputOperationDTO;
import nl.ipo.cds.dao.attributemapping.OperationDTO;
import nl.ipo.cds.dao.attributemapping.OperationInputDTO;
import nl.ipo.cds.dao.attributemapping.TransformOperationDTO;
import nl.ipo.cds.domain.FeatureType;
import nl.ipo.cds.domain.FeatureTypeAttribute;
import nl.ipo.cds.etl.log.EventLogger;
import nl.ipo.cds.etl.operations.transform.ConditionalTransform;
import nl.ipo.cds.etl.theme.AttributeDescriptor;

public class AttributeMappingValidator {

	public enum MessageKey {
		// Attribute mapping is incorrect, but due to a technical error:
		ATTRIBUTE_MAPPING_TECHNICAL_ERROR,
		
		ATTRIBUTE_MAPPING_MISSING,
		
		ATTRIBUTE_MAPPING_CONDITION_INVALID_ATTRIBUTE,
		ATTRIBUTE_MAPPING_CONDITION_NO_VALUE,
		
		ATTRIBUTE_MAPPING_TYPES_INCOMPATIBLE,
		
		ATTRIBUTE_MAPPING_INPUT_COUNT_INVALID,
		
		ATTRIBUTE_MAPPING_INPUT_NOT_FOUND,
		ATTRIBUTE_MAPPING_INPUT_NO_LONGER_AVAILABLE,
		ATTRIBUTE_MAPPING_INPUT_TYPE_CHANGED,
		
		ATTRIBUTE_MAPPING_RUNTIME_ERROR
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
			
			params.add (attributeDescriptor.getLabel (Locale.getDefault ()));
			params.addAll (Arrays.asList (parameters));

			context.put ("attribute", attributeDescriptor.getName ());
			context.put ("attributeLabel", attributeDescriptor.getLabel (Locale.getDefault ()));
			
			logger.logEvent (job, messageKey, LogLevel.ERROR, context, params.toArray (new String[params.size ()]));
			++ messageCount;
		}
		
		public int getMessageCount () {
			return messageCount;
		}
	}
	
	private final EventLogger<MessageKey> logger;
	private final AttributeDescriptor<?> attributeDescriptor;
	private final FeatureType featureType;
	
	public AttributeMappingValidator (final AttributeDescriptor<?> attributeDescriptor, final FeatureType featureType, final EventLogger<MessageKey> logger) {
		this.logger = logger;
		this.attributeDescriptor = attributeDescriptor;
		this.featureType = featureType;
	}
	
	public boolean isValid (final Job job, final OperationDTO rootOperation) {
		final Logger logger = new Logger (job);
		
		validateRoot (rootOperation, logger);
		
		return logger.getMessageCount () == 0;
	}
	
	public void validateRoot (final OperationDTO rootOperation, final Logger logger) {
		if (rootOperation == null) {
			logger.report (MessageKey.ATTRIBUTE_MAPPING_MISSING);
			return;
		}
		
		// Must be an instance of TransformOperationDTO:
		if (!(rootOperation instanceof TransformOperationDTO)) {
			logger.report (MessageKey.ATTRIBUTE_MAPPING_TECHNICAL_ERROR, "Root operation must be an instance of TransformOperationDTO");
			return;
		}
		
		// Validate the operation type:
		if (!validateOperationType (rootOperation, logger)) {
			return;
		}
		
		// Root must be an attribute descriptor:
		if (!(rootOperation instanceof TransformOperationDTO)) {
			logger.report (MessageKey.ATTRIBUTE_MAPPING_TECHNICAL_ERROR, "Root operation must be of type ConditionalTransform");
			return;
		}
		
		// Must be of type attribute descriptor:
		final TransformOperationDTO operation = (TransformOperationDTO)rootOperation;

		if (!ConditionalTransform.Settings.class.equals (operation.getOperationType ().getPropertyBeanClass ())) {
			logger.report (MessageKey.ATTRIBUTE_MAPPING_TECHNICAL_ERROR, "Root operation must be of type ConditionalTransform");
			return;
		}
		
		// Test conditions:
		final ConditionalTransform.Settings settings = (ConditionalTransform.Settings)operation.getOperationProperties ();
		final List<ConditionalTransform.Condition> conditions = settings.getConditions ();
		final List<OperationInput> inputs = operation.getInputs ();
		
		if (!(conditions.size () == 0 && inputs.size () == 0) && conditions.size () != inputs.size () - 1) {
			logger.report (MessageKey.ATTRIBUTE_MAPPING_TECHNICAL_ERROR, String.format ("Invalid number of conditions %d for root operation, expected %d", conditions.size (), inputs.size () - 1));
		}
		
		for (int i = 0; i < conditions.size (); ++ i) {
			final ConditionalTransform.Condition condition = conditions.get (i);
			
			if (condition.getAttribute () == null || !hasFeatureTypeAttribute (condition.getAttribute ())) {
				logger.report (MessageKey.ATTRIBUTE_MAPPING_CONDITION_INVALID_ATTRIBUTE, String.valueOf (i + 1), condition.getAttribute () == null ? "" : condition.getAttribute ());
			}
			
			if ((condition.getOperation () == ConditionalTransform.Operation.IN || condition.getOperation () == ConditionalTransform.Operation.NOT_IN) && (condition.getValues () == null || condition.getValues ().length == 0)) {
				logger.report (MessageKey.ATTRIBUTE_MAPPING_CONDITION_NO_VALUE, String.valueOf (i + 1));
			}
		}
		
		for (int i = 0; i < inputs.size (); ++ i) {
			validateInput (inputs.get (i), operation, i, logger);
			
			// Test whether the inputs of the condition are compatible with the result attribute:
			if (inputs.get (i) != null && inputs.get (i).getOperation () != null && inputs.get (i).getOperation ().getOperationType () != null) {
				if (!AttributeMapperUtils.areTypesAssignable (inputs.get (i).getOperation ().getOperationType ().getReturnType (), attributeDescriptor.getAttributeType ())) {
					logger.report (MessageKey.ATTRIBUTE_MAPPING_TYPES_INCOMPATIBLE);
				}
			}
		}
	}
	
	public void validateInput (final OperationInput operationInput, final TransformOperationDTO parent, final int index, final Logger logger) {
		if (operationInput == null || !(operationInput instanceof OperationInputDTO)) {
			logger.report (MessageKey.ATTRIBUTE_MAPPING_TECHNICAL_ERROR, "Invalid operation input type");
			return;
		}
		
		final OperationInputDTO input = (OperationInputDTO)operationInput;
		final Operation operation = input.getOperation ();

		if (operation == null) {
			logger.report (MessageKey.ATTRIBUTE_MAPPING_INPUT_COUNT_INVALID);
			return;
		}
		
		if (!(operation instanceof OperationDTO)) {
			logger.report (MessageKey.ATTRIBUTE_MAPPING_TECHNICAL_ERROR, String.format ("Invalid operation instance type %s, expected %s", operation == null ? "NULL" : operation.getClass ().getCanonicalName (), OperationDTO.class.getCanonicalName ()));
			return;
		}
		
		validateOperation ((OperationDTO)operation, parent, index, logger); 
	}
	
	public void validateOperation (final OperationDTO operation, final TransformOperationDTO parent, final int index, final Logger logger) {
		// Validate the operation type:
		if (!validateOperationType (operation, logger)) {
			return;
		}
		
		// Check whether the operation is assignable to the parent:
		final List<OperationInputType> inputTypes = parent.getOperationType ().getInputs ();
		final Type inputType = inputTypes.get (Math.min (index, inputTypes.size () - 1)).getInputType ();
		final OperationType operationType = operation.getOperationType ();

		if (operationType == null || !AttributeMapperUtils.areTypesAssignable (operationType.getReturnType (), inputType)) {
			logger.report (MessageKey.ATTRIBUTE_MAPPING_TYPES_INCOMPATIBLE);
		}
		
		if (operation instanceof InputOperationDTO) {
			validateInputOperation ((InputOperationDTO)operation, parent, index, logger);
		} else if (operation instanceof TransformOperationDTO) {
			validateTransformOperation ((TransformOperationDTO)operation, parent, index, logger);
		} else {
			logger.report (MessageKey.ATTRIBUTE_MAPPING_TECHNICAL_ERROR, "Wrong operation type");
		}
	}
	
	public void validateInputOperation (final InputOperationDTO operation, final TransformOperationDTO parent, final int index, final Logger logger) {
		// Test whether the attribute is still valid:
		if (operation.getAttribute () == null) {
			logger.report(MessageKey.ATTRIBUTE_MAPPING_INPUT_NOT_FOUND, operation.getAttributeName ());
			return;
		}
		
		final FeatureTypeAttribute attribute = operation.getAttribute ();
		
		if (!attribute.getName ().getLocalPart ().equals (operation.getAttributeName ())) {
			logger.report (MessageKey.ATTRIBUTE_MAPPING_INPUT_NO_LONGER_AVAILABLE, operation.getAttributeName ());
		}
		if (!attribute.getType ().equals (operation.getAttributeType ())) {
			logger.report (MessageKey.ATTRIBUTE_MAPPING_INPUT_TYPE_CHANGED, operation.getAttributeName ());
		}
	}
	
	public void validateTransformOperation (final TransformOperationDTO operation, final TransformOperationDTO parent, final int index, final Logger logger) {
		final List<OperationInput> inputs = operation.getInputs ();
		final OperationType operationType = operation.getOperationType ();
		final List<OperationInputType> inputTypes = operationType.getInputs ();
		final boolean hasVariableInputs = inputTypes.size () == 0 ? false : inputTypes.get (inputTypes.size () - 1).isVariableInputCount ();

		if (hasVariableInputs) {
			if (inputs.size () < inputTypes.size () - 1) {
				logger.report (MessageKey.ATTRIBUTE_MAPPING_INPUT_COUNT_INVALID);
				return;
			}
		} else {
			if (inputs.size () != inputTypes.size ()) {
				logger.report (MessageKey.ATTRIBUTE_MAPPING_INPUT_COUNT_INVALID);
			}
		}
		
		for (int i = 0; i < inputs.size (); ++ i) {
			if (inputs.get (i) == null || inputs.get (i).getOperation () == null) {
				logger.report (MessageKey.ATTRIBUTE_MAPPING_INPUT_COUNT_INVALID);
				break;
			}
			validateInput (inputs.get (i), operation, i, logger);
		}
	}
	
	public boolean validateOperationType (final OperationDTO operation, final Logger logger) {
		if (operation.getOperationType () == null) {
			logger.report (MessageKey.ATTRIBUTE_MAPPING_TECHNICAL_ERROR, String.format ("Operation %s has no operation type", operation));
			return false;
		}
		
		if (operation.getOperationType ().getPropertyBeanClass () != null) {
			if (operation.getOperationProperties () == null) {
				logger.report (MessageKey.ATTRIBUTE_MAPPING_TECHNICAL_ERROR, String.format ("Operation %s has no properties object", operation));
				return false;
			}
			
			if (!operation.getOperationProperties ().getClass ().equals (operation.getOperationType ().getPropertyBeanClass ())) {
				logger.report (MessageKey.ATTRIBUTE_MAPPING_TECHNICAL_ERROR, String.format ("Operation %s must have a properties object of type %s", operation, operation.getOperationType ().getPropertyBeanClass ().getCanonicalName ()));
				return false;
			}
		}
		
		return true;
	}
	
	private boolean hasFeatureTypeAttribute (final String name) {
		for (final FeatureTypeAttribute attr: featureType.getAttributes ()) {
			if (attr.getName ().getLocalPart ().equals (name)) {
				return true;
			}
		}
		
		return false;
	}
}
