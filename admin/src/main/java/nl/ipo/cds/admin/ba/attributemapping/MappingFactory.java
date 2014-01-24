package nl.ipo.cds.admin.ba.attributemapping;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.ipo.cds.admin.ba.controller.beans.mapping.ConditionOperation;
import nl.ipo.cds.admin.ba.controller.beans.mapping.InputAttribute;
import nl.ipo.cds.admin.ba.controller.beans.mapping.Mapping;
import nl.ipo.cds.admin.ba.controller.beans.mapping.Operation;
import nl.ipo.cds.admin.ba.controller.beans.mapping.TransformOperation;
import nl.ipo.cds.attributemapping.operations.OperationInput;
import nl.ipo.cds.dao.attributemapping.InputOperationDTO;
import nl.ipo.cds.dao.attributemapping.OperationDTO;
import nl.ipo.cds.dao.attributemapping.TransformOperationDTO;
import nl.ipo.cds.domain.FeatureType;
import nl.ipo.cds.etl.operations.transform.ConditionalTransform;
import nl.ipo.cds.etl.theme.AttributeDescriptor;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public class MappingFactory {
	private final OperationDTO rootOperation;
	private final FeatureType featureType;
	
	public MappingFactory (final OperationDTO rootOperation, final FeatureType featureType) {
		this.rootOperation = rootOperation;
		this.featureType = featureType;
	}
	
	public Mapping buildMapping () {
		final Mapping mapping = new Mapping ();

		// Validate the input:
		if (!(rootOperation.getOperationType () instanceof AttributeDescriptor<?>)) {
			throw new IllegalStateException ("Root operation is not an attribute descriptor");
		}
		if (rootOperation.getInputs ().size () != 1 || !(rootOperation.getInputs ().get (0).getOperation () instanceof TransformOperationDTO)) {
			throw new IllegalStateException ("Root operation is expected to have exactly one conditional input of type TransformOperation");
		}
		
		// Get the attribute descriptor:
		final AttributeDescriptor<?> attributeDescriptor = (AttributeDescriptor<?>)rootOperation.getOperationType ();

		mapping.setAttributeName (attributeDescriptor.getName ());
		mapping.setFeatureTypeName (featureType.getName ().getLocalPart ());
		mapping.setFeatureTypeNamespace (featureType.getName ().getNamespace ());
		
		// Set operations:
		mapping.setOperations (buildConditionalOperations ((TransformOperationDTO)rootOperation.getInputs ().get (0).getOperation ()));
		
		return mapping;
	}
	
	public List<Operation> buildConditionalOperations (final TransformOperationDTO conditionalOperation) {
		final List<Operation> result = new ArrayList<Operation> ();
		final ConditionalTransform.Settings settings = (ConditionalTransform.Settings)conditionalOperation.getOperationProperties ();
		final List<OperationInput> inputs = conditionalOperation.getInputs ();
		
		for (int i = 0; i < inputs.size (); ++ i) {
			final OperationDTO inputOperation = (OperationDTO)inputs.get (i).getOperation ();
			final ConditionalTransform.Condition condition = settings != null && i < settings.getConditions ().size () ? settings.getConditions ().get (i) : null;
			
			result.add (buildCondition (inputOperation, condition));
		}
		
		return result;
	}
	
	public ConditionOperation buildCondition (final OperationDTO inputOperation, final ConditionalTransform.Condition condition) {
		final ConditionOperation operation = new ConditionOperation ();
		final Map<String, Object> settings = new HashMap<String, Object> ();
		final List<Operation> inputs = new ArrayList<Operation> (1);
		
		// Construct settings:
		if (condition != null) {
			settings.put ("attribute", condition.getAttribute ());
			settings.put ("operator", getOperatorName (condition.getOperation ()));
			if (condition.getValues ().length > 0) {
				settings.put ("value", condition.getValues ()[0]);
			}
		}
		
		// Create the input:
		if (inputOperation != null) {
			inputs.add (buildOperation (inputOperation));
		}
		
		operation.setName ("[condition]");
		operation.setOperationInputs (inputs);
		operation.setSettings (settings);
		
		return operation;
	}
	
	public Operation buildOperation (final OperationDTO original) {
		if (original == null) {
			return null;
		} else if (original instanceof InputOperationDTO) {
			return buildInput ((InputOperationDTO)original);
		} else if (original instanceof TransformOperationDTO) {
			return buildTransform ((TransformOperationDTO)original);
		}
		
		throw new IllegalStateException (String.format ("Invalid operation type %s", original.getClass ().getCanonicalName ()));
	}
	
	public InputAttribute buildInput (final InputOperationDTO original) {
		final InputAttribute result = new InputAttribute ();
		
		result.setName (original.getAttributeName ());
		result.setInputAttributeType (original.getAttributeType().getJavaType().toString ());
		result.setInputAttributeNamespace (featureType.getName ().getNamespace ());
		
		return result;
	}
	
	public TransformOperation buildTransform (final TransformOperationDTO original) {
		final TransformOperation result = new TransformOperation ();
		final Map<String, Object> settings = new HashMap<String, Object> ();
		final Object properties = original.getOperationProperties ();
		
		// Convert settings:
		if (properties != null) {
			final BeanWrapper wrapper = new BeanWrapperImpl (properties);

			for (final PropertyDescriptor pd: wrapper.getPropertyDescriptors ()) {
				if (pd.getReadMethod () != null && pd.getWriteMethod () != null) {
					settings.put (pd.getName (), wrapper.getPropertyValue (pd.getName ()));
				}
			}
		}
		
		// Convert inputs:
		final List<Operation> inputs = new ArrayList<Operation> ();
		for (final OperationInput input: original.getInputs()) {
			inputs.add (buildOperation ((OperationDTO)input.getOperation ()));
		}
		
		result.setName (original.getOperationType ().getName ());
		result.setSettings (settings);
		result.setOperationInputs (inputs);
		
		return result;
	}
	
	private static String getOperatorName (final ConditionalTransform.Operation operation) {
		switch (operation) {
		case IN:
			return "in";
		case IS_EMPTY:
			return "empty";
		case IS_NOT_EMPTY:
			return "not_empty";
		case IS_NOT_NULL:
			return "not_null";
		case IS_NULL:
			return "null";
		case NOT_IN:
			return "not_in";
		default:
			return null;
		}
	}
}