package nl.ipo.cds.dao.attributemapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.ipo.cds.attributemapping.operations.OperationInput;
import nl.ipo.cds.attributemapping.operations.OperationType;

public class TransformOperationDTO extends OperationDTO {

	private final OperationType operationType;
	private final List<OperationInputDTO> inputs;
	private final Object properties;
	
	public TransformOperationDTO (final OperationType operationType, final List<OperationInputDTO> inputs, final Object properties) {
		this.operationType = operationType;
		this.inputs = new ArrayList<OperationInputDTO> (inputs);
		this.properties = properties;
	}
	
	@Override
	public Object getOperationProperties () {
		return properties;
	}
	
	@Override
	public OperationType getOperationType () {
		return operationType;
	}
	
	@Override
	public List<OperationInput> getInputs () {
		return Collections.<OperationInput>unmodifiableList (inputs);
	}
}
