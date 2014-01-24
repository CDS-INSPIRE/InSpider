package nl.ipo.cds.dao.attributemapping;

import java.util.ArrayList;
import java.util.List;

import nl.ipo.cds.attributemapping.operations.OperationInput;
import nl.ipo.cds.attributemapping.operations.OperationType;
import nl.ipo.cds.domain.AttributeType;
import nl.ipo.cds.domain.FeatureTypeAttribute;
import nl.ipo.cds.etl.attributemapping.FeatureTypeAttributeOperationType;

public class InputOperationDTO extends OperationDTO {

	private final String attributeName;
	private final AttributeType attributeType;
	private final FeatureTypeAttribute attribute; 
	private final OperationType operationType;
	
	public InputOperationDTO (final FeatureTypeAttribute attribute, final String attributeName, final AttributeType attributeType) {
		this.attribute = attribute;
		this.operationType = new FeatureTypeAttributeOperationType (attribute);
		this.attributeName = attributeName;
		this.attributeType = attributeType;
	}

	@Override
	public Object getOperationProperties () {
		return null;
	}

	@Override
	public List<OperationInput> getInputs () {
		return new ArrayList<OperationInput> ();
	}

	@Override
	public OperationType getOperationType () {
		return operationType;
	}
	
	public FeatureTypeAttribute getAttribute () {
		return attribute;
	}
	
	public String getAttributeName () {
		return attributeName;
	}
	
	public AttributeType getAttributeType () {
		return attributeType;
	}
}