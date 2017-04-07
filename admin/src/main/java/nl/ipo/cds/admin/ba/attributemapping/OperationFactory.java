package nl.ipo.cds.admin.ba.attributemapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import nl.ipo.cds.admin.ba.controller.MappingParserException;
import nl.ipo.cds.admin.ba.controller.beans.mapping.ConditionOperation;
import nl.ipo.cds.admin.ba.controller.beans.mapping.InputAttribute;
import nl.ipo.cds.admin.ba.controller.beans.mapping.Mapping;
import nl.ipo.cds.admin.ba.controller.beans.mapping.Operation;
import nl.ipo.cds.admin.ba.controller.beans.mapping.TransformOperation;
import nl.ipo.cds.attributemapping.operations.OperationType;
import nl.ipo.cds.dao.attributemapping.InputOperationDTO;
import nl.ipo.cds.dao.attributemapping.OperationDTO;
import nl.ipo.cds.dao.attributemapping.OperationInputDTO;
import nl.ipo.cds.dao.attributemapping.TransformOperationDTO;
import nl.ipo.cds.domain.AttributeType;
import nl.ipo.cds.domain.FeatureType;
import nl.ipo.cds.domain.FeatureTypeAttribute;
import nl.ipo.cds.etl.operations.transform.ConditionalTransform;
import nl.ipo.cds.etl.theme.AttributeDescriptor;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.convert.ConversionService;

public class OperationFactory {
	private final AttributeDescriptor<?> attributeDescriptor;
	private final Collection<OperationType> operationTypes;
	private final FeatureType featureType;
	private final ConversionService conversionService;
	
	public OperationFactory (final AttributeDescriptor<?> attributeDescriptor, final Collection<OperationType> operationTypes, final FeatureType featureType, final ConversionService conversionService) {
		this.attributeDescriptor = attributeDescriptor;
		this.operationTypes = operationTypes;
		this.featureType = featureType;
		this.conversionService = conversionService;
	}
	
	public OperationDTO buildOperationCommand (final Mapping mapping) throws MappingParserException {
		final TransformOperationDTO conditionalCommand = buildConditionalTransformOperation (mapping.getOperations ());
		final List<OperationInputDTO> inputs = new ArrayList<OperationInputDTO> (1);
		
		inputs.add (new OperationInputDTO (conditionalCommand));
		
		return new TransformOperationDTO (attributeDescriptor, inputs, null); 
	}
	
	public TransformOperationDTO buildConditionalTransformOperation (final List<Operation> operations) throws MappingParserException {
		final List<OperationInputDTO> inputs = new ArrayList<OperationInputDTO> ();
		final List<ConditionalTransform.Condition> conditions = new ArrayList<ConditionalTransform.Condition> ();
		
		// Build inputs and conditions:
		for (final Operation operation: operations) {
			if (!(operation instanceof ConditionOperation)) {
				throw new MappingParserException ("Expected condition operation");
			}
			
			// Add the input:
			if (((ConditionOperation)operation).getOperationInputs ().size () != 0) {
				if (((ConditionOperation)operation).getOperationInputs ().size () > 1) {
					throw new MappingParserException ("Conditional operation has more than 1 input");
				}
				inputs.add (buildInput (((ConditionOperation)operation).getOperationInputs ().get (0)));
			} else {
				inputs.add (new OperationInputDTO (null));
			}
			
			// Add condition, if present:
			final Map<String, Object> settings = ((ConditionOperation)operation).getSettings ();
			
			if (settings == null) {
				continue;
			}
			if (!settings.containsKey ("attribute")) {
				continue;
			}
			if (!settings.containsKey ("operator") || settings.get ("operator") == null) {
				continue;
			}
			
			final String referenceAttributeName = settings.get ("attribute") == null ? null : settings.get ("attribute").toString ();
			final String operator = settings.get ("operator").toString ();
			final String value = settings.get ("value") == null ? null : settings.get ("value").toString ();
			final ConditionalTransform.Operation op = getConditionalOperation (operator);
			
			if (op == null) {
				throw new MappingParserException (String.format ("Unknown conditional operation: %s", operator));
			}
			if ((op == ConditionalTransform.Operation.IN || op == ConditionalTransform.Operation.NOT_IN) && value == null) {
				throw new MappingParserException (String.format ("No value provided for conditional operation %s", operator));
			}
			
			final ConditionalTransform.Condition condition = new ConditionalTransform.Condition ();
			
			condition.setAttribute (referenceAttributeName);
			condition.setOperation (op);
			condition.setValues (value == null ? new String[0] : new String[] { value });
			
			conditions.add (condition);
		}
		
		// Validate the number of inputs:
		if (!(conditions.size () == 0 && inputs.size () == 0) && conditions.size () !=  inputs.size () - 1) {
			throw new MappingParserException (String.format ("The number of inputs does not match the number of conditions (%d conditions, %d inputs)", conditions.size (), inputs.size ()));
		}
		
		// Build a settings object:
		final ConditionalTransform.Settings settings = new ConditionalTransform.Settings ();
		
		settings.setConditions (conditions);
		
		return new TransformOperationDTO (getConditionalTransformOperationType (), inputs, settings);
	}
	
	public OperationInputDTO buildInput (final Operation operation) throws MappingParserException {
		if (operation == null) {
			return new OperationInputDTO (null);
		} else {
			return new OperationInputDTO (buildOperation (operation));
		}
	}
	
	public OperationDTO buildOperation (final Operation operation) throws MappingParserException {
		if (operation instanceof InputAttribute) {
			return buildInputOperation ((InputAttribute)operation);
		} else {
			return buildTransformOperation ((TransformOperation)operation);
		}
	}
	
	public InputOperationDTO buildInputOperation (final InputAttribute inputAttribute) throws MappingParserException {
		final FeatureTypeAttribute attribute = getInputAttribute (featureType, inputAttribute.getName (), inputAttribute.getInputAttributeType ());
		final AttributeType attributeType = AttributeType.fromString (inputAttribute.getInputAttributeType ());
		
		if (attributeType == null) {
			throw new MappingParserException (String.format ("Attribute type not found: %s", inputAttribute.getInputAttributeType ()));
		}
		
		// attributeType is mandatory
		return new InputOperationDTO (attribute, inputAttribute.getName (), attribute != null ? attribute.getType () : attributeType);
	}
	
	public TransformOperationDTO buildTransformOperation (final TransformOperation transformOperation) throws MappingParserException {
		final OperationType operationType = getTransformOperationType (transformOperation.getName ());
		final Object properties = buildOperationProperties (operationType, transformOperation.getSettings ());
		final List<OperationInputDTO> inputs = new ArrayList<OperationInputDTO> ();
		
		for (final Operation operation: transformOperation.getOperationInputs ()) {
			inputs.add (buildInput (operation));
		}
		
		return new TransformOperationDTO (operationType, inputs, properties);
	}
	
	public Object buildOperationProperties (final OperationType operationType, final Map<String, Object> settings) {
		if (operationType == null || settings == null || operationType.getPropertyBeanClass () == null) {
			return null;
		}
		
		final BeanWrapper wrapper;
		try {
			wrapper = new BeanWrapperImpl (operationType.getPropertyBeanClass ().newInstance ());
		} catch (InstantiationException e) {
			return null;
		} catch (IllegalAccessException e) {
			return null;
		}

		wrapper.setConversionService (conversionService);
		
		for (final Map.Entry<String, Object> entry: settings.entrySet ()) {
			wrapper.setPropertyValue (entry.getKey (), entry.getValue ());
		}
		
		return wrapper.getWrappedInstance ();
	}
	
	private FeatureTypeAttribute getInputAttribute (final FeatureType featureType, final String attributeName, final String attributeType) {
		for (final FeatureTypeAttribute attribute: featureType.getAttributes ()) {
			final String typeString = attribute.getType ().getJavaType ().toString ();
			if (attribute.getName ().getLocalPart ().equals (attributeName) && typeString.equals (attributeType)) {
				return attribute;
			}
		}
		
		return null;
	}
	
	private OperationType getTransformOperationType (final String operationName) {
		for (final OperationType operationType: operationTypes) {
			if (operationType.getName ().equals (operationName)) {
				return operationType;
			}
		}
		
		return null;
	}
	
	private ConditionalTransform.Operation getConditionalOperation (final String operationName) {
		if ("in".equals (operationName)) {
			return ConditionalTransform.Operation.IN;
		} else if ("not_in".equals (operationName)) {
			return ConditionalTransform.Operation.NOT_IN;
		} else if ("empty".equals (operationName)) {
			return ConditionalTransform.Operation.IS_EMPTY;
		} else if ("not_empty".equals (operationName)) {
			return ConditionalTransform.Operation.IS_NOT_EMPTY;
		}
		
		return null;
	}
	
	private OperationType getConditionalTransformOperationType () throws MappingParserException {
		for (final OperationType operationType: operationTypes) {
			if (operationType.getPropertyBeanClass () != null && operationType.getPropertyBeanClass ().equals (ConditionalTransform.Settings.class)) {
				return operationType;
			}
		}
		
		throw new MappingParserException ("No conditional transform operation type found");
	}
}