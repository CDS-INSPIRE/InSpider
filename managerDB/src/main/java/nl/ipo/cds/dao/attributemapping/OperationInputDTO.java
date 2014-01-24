package nl.ipo.cds.dao.attributemapping;

import nl.ipo.cds.attributemapping.operations.Operation;
import nl.ipo.cds.attributemapping.operations.OperationInput;

public class OperationInputDTO implements OperationInput {

	private final OperationDTO operation;
	
	public OperationInputDTO (final OperationDTO operation) {
		this.operation = operation;
	}
	
	@Override
	public Operation getOperation () {
		return operation;
	}
}
