package nl.ipo.cds.dao.attributemapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nl.ipo.cds.attributemapping.operations.OperationInput;
import nl.ipo.cds.attributemapping.operations.OperationType;
import nl.ipo.cds.attributemapping.operations.OutputOperationType;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.AttributeMapping;
import nl.ipo.cds.domain.AttributeType;
import nl.ipo.cds.domain.Dataset;
import nl.ipo.cds.domain.FeatureTypeAttribute;
import nl.ipo.cds.domain.MappingOperation;
import nl.ipo.cds.domain.MappingOperation.MappingOperationType;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

public class AttributeMappingDao {

	private final ManagerDao managerDao;
	private final List<FeatureTypeAttribute> inputAttributes;
	private final List<OperationType> operationTypes;

	public AttributeMappingDao (final ManagerDao managerDao) {
		this (managerDao, new ArrayList<FeatureTypeAttribute> (), new ArrayList<OperationType> ());
	}
	
	public AttributeMappingDao (final ManagerDao managerDao, final Collection<OperationType> operationTypes) {
		this (managerDao, new ArrayList<FeatureTypeAttribute> (), operationTypes);
	}
	
	public AttributeMappingDao (final ManagerDao managerDao, final Collection<FeatureTypeAttribute> inputAttributes, final Collection<OperationType> operationTypes) {
		this.managerDao = managerDao;
		this.inputAttributes = new ArrayList<FeatureTypeAttribute> (inputAttributes);
		this.operationTypes = new ArrayList<OperationType> (operationTypes);
	}

	@Transactional
	public OperationDTO getAttributeMapping (final Dataset dataset, final OutputOperationType attribute) {
		final AttributeMapping attributeMapping = managerDao.getAttributeMapping (dataset, attribute.getName ());
		
		// Attribute mapping hasn't been persisted yet:
		if (attributeMapping == null || attributeMapping.getRootOperation () == null) {
			return null;
		}
		
		
		return unmarshalOperation (attributeMapping.getRootOperation ());
	}

	private OperationDTO unmarshalOperation (final MappingOperation mappingOperation) {
		if (mappingOperation == null) {
			return null;
		} else if (mappingOperation.getOperationType () == MappingOperationType.INPUT_OPERATION) {
			return unmarshalInputOperation (mappingOperation);
		} else if (mappingOperation.getOperationType () == MappingOperationType.TRANSFORM_OPERATION) {
			return unmarshalTransformOperation (mappingOperation);
		} else {
			throw new IllegalArgumentException (String.format ("Mapping operation has an invalid type: %s", mappingOperation.getOperationType ()));
		}
	}
	
	private InputOperationDTO unmarshalInputOperation (final MappingOperation mappingOperation) {
		if (mappingOperation == null) {
			throw new NullPointerException ("mappingOperation cannot be null");
		}
		if (mappingOperation.getOperationName () == null) {
			throw new NullPointerException ("operation name is null");
		}
		if (mappingOperation.getInputAttributeType () == null) {
			throw new NullPointerException ("attribute type is null");
		}
		
		return new InputOperationDTO (
				findFeatureTypeAttribute (mappingOperation.getOperationName (), mappingOperation.getInputAttributeType ()),
				mappingOperation.getOperationName (), 
				mappingOperation.getInputAttributeType ()
			);
	}
	
	private FeatureTypeAttribute findFeatureTypeAttribute (final String name, final AttributeType type) {
		for (final FeatureTypeAttribute attr: inputAttributes) {
			if (attr.getName ().getLocalPart ().equals (name) && attr.getType ().equals (type)) {
				return attr;
			}
		}
		
		return null;
	}
	
	private TransformOperationDTO unmarshalTransformOperation (final MappingOperation mappingOperation) {
		if (mappingOperation == null || mappingOperation.getOperationName () == null) {
			throw new NullPointerException ();
		}
		
		// Unmarshal properties:
		
		final OperationType operationType = findOperationType (mappingOperation.getOperationName ()); 
		return new TransformOperationDTO (
				operationType, 
				unmarshalInputs (mappingOperation.getInputs ()), 
				unmarshalProperties (mappingOperation.getProperties(), operationType)
			);
	}
	
	private List<OperationInputDTO> unmarshalInputs (final List<MappingOperation> inputs) {
		if (inputs == null) {
			throw new NullPointerException ("inputs cannot be null");
		}
		
		final List<OperationInputDTO> result = new ArrayList<OperationInputDTO> ();
		
		for (final MappingOperation input: inputs) {
			result.add (new OperationInputDTO (unmarshalOperation (input)));
		}
		
		return result;
	}
	
	private Object unmarshalProperties (final String properties, final OperationType operationType) {
		if (properties == null || operationType.getPropertyBeanClass () == null) {
			return null;
		}
		
		try {
			return new ObjectMapper ().readValue (properties, operationType.getPropertyBeanClass ());
		} catch (JsonParseException e) {
			throw new IllegalArgumentException (String.format ("Invalid JSON string %s", properties), e);
		} catch (JsonMappingException e) {
			throw new IllegalArgumentException (String.format ("Invalid JSON string %s", properties), e);
		} catch (IOException e) {
			throw new IllegalArgumentException (String.format ("Invalid JSON string %s", properties), e);
		}
	}
	
	private OperationType findOperationType (final String name) {
		for (final OperationType ot: operationTypes) {
			if (ot.getName ().equals (name)) {
				return ot;
			}
		}
		
		throw new IllegalArgumentException (String.format ("No operation named %s could be found", name));
	}
	
	@Transactional
	public void putAttributeMapping (final Dataset dataset, final OutputOperationType attribute, final OperationDTO operation, final boolean isValid) {
		final AttributeMapping attributeMapping = getOrCreateAttributeMapping (dataset, attribute);

		attributeMapping.setValid (isValid);
		
		// Persist the root operation:
		attributeMapping.setRootOperation (persistOperation (operation, attributeMapping.getRootOperation ()));
		managerDao.update (attributeMapping);
	}
	
	private MappingOperation persistOperation (final OperationDTO operation, final MappingOperation existingMappingOperation) {
		if (operation == null) {
			// Delete an existing mapping operation:
			if (existingMappingOperation != null) {
				deleteChild (existingMappingOperation);
			}
			
			return null;
		}
		
		final MappingOperation mappingOperation = existingMappingOperation != null
				? existingMappingOperation
				: new MappingOperation ();
		
		// Set properties of this mapping operation:
		mergeOperation (operation, mappingOperation);
		
		// Set children:
		
		// Persist:
		if (mappingOperation.getId () == null) {
			managerDao.create (mappingOperation);
		} else {
			managerDao.update (mappingOperation);
		}
		
		return mappingOperation;
	}
	
	private void mergeOperation (final OperationDTO operation, final MappingOperation mappingOperation) {
		if (operation instanceof InputOperationDTO) {
			mergeInputOperation ((InputOperationDTO)operation, mappingOperation);
		} else if (operation instanceof TransformOperationDTO) {
			mergeTransformOperation ((TransformOperationDTO)operation, mappingOperation);
		} else {
			throw new IllegalArgumentException (String.format ("Invalid operation type %s", operation.getClass ().getCanonicalName ()));
		}
	}
	
	private void mergeInputOperation (final InputOperationDTO inputOperation, final MappingOperation operation) {
		// If the operation previously had children, delete them:
		for (final MappingOperation input: operation.getInputs ()) {
			deleteChild (input);
		}

		// Set properties:
		operation.setOperationType (MappingOperationType.INPUT_OPERATION);
		operation.setInputAttributeType (inputOperation.getAttributeType ());
		operation.setOperationName (inputOperation.getAttributeName ());
		operation.setInputs (new ArrayList<MappingOperation> ());
		operation.setProperties (null);
	}
	
	private void mergeTransformOperation (final TransformOperationDTO transformOperation, final MappingOperation operation) {
		// Set properties:
		operation.setOperationType (MappingOperationType.TRANSFORM_OPERATION);
		operation.setOperationName (transformOperation.getOperationType ().getName ());
		operation.setInputAttributeType (null);

		// Serialize configuration properties:
		operation.setProperties (serializeProperties (transformOperation.getOperationProperties ()));
		
		// Update children:
		final List<OperationInput> operationInputs = transformOperation.getInputs ();
		final List<MappingOperation> oldChildren = new ArrayList<MappingOperation> (operation.getInputs ());
		final List<MappingOperation> newChildren = new ArrayList<MappingOperation> ();
		int i;
		
		for (i = 0; i < operationInputs.size (); ++ i) {
			final OperationInputDTO operationInput = (OperationInputDTO)operationInputs.get (i);
			final MappingOperation childOperation = i < oldChildren.size () ? oldChildren.get (i) : null;
			
			newChildren.add (persistOperation ((OperationDTO)operationInput.getOperation (), childOperation));
		}
		
		// Remove unused children:
		for (; i < oldChildren.size (); ++ i) {
			deleteChild (oldChildren.get (i));
		}
		
		operation.setInputs (newChildren);
	}
	
	private void deleteChild (final MappingOperation mappingOperation) {
		if (mappingOperation == null) {
			return;
		}
		
		// Delete children of this operation:
		final List<MappingOperation> children = mappingOperation.getInputs ();
		if (children != null) {
			for (final MappingOperation child: children) {
				deleteChild (child);
			}
		}
		
		// Delete this operation:
		managerDao.delete (mappingOperation);
	}
	
	private String serializeProperties (final Object properties) {
		if (properties == null) {
			return null;
		}
		
		try {
			return new ObjectMapper ().writeValueAsString (properties);
		} catch (JsonGenerationException e) {
			throw new RuntimeException (e);
		} catch (JsonMappingException e) {
			throw new RuntimeException (e);
		} catch (IOException e) {
			throw new RuntimeException (e);
		}
	}
	
	/**
	 * Returns an existing mapping for the given attribute and dataset, or creates and persists a new one if
	 * it hasn't been created yet.
	 * 
	 * @param dataset
	 * @param attribute
	 * @return An attribute mapping for the given dataset and attribute.
	 */
	private AttributeMapping getOrCreateAttributeMapping (final Dataset dataset, final OutputOperationType attribute) {
		final AttributeMapping existingMapping = managerDao.getAttributeMapping (dataset, attribute.getName ());
		
		if (existingMapping == null) {
			final AttributeMapping mapping = new AttributeMapping (dataset, attribute.getName ());
			managerDao.create (mapping);
			return mapping;
		}
		
		return existingMapping;
	}
}
