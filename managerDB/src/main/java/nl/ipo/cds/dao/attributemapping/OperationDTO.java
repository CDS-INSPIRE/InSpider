package nl.ipo.cds.dao.attributemapping;

import java.util.List;

import nl.ipo.cds.attributemapping.operations.Operation;
import nl.ipo.cds.attributemapping.operations.OperationInput;
import nl.ipo.cds.attributemapping.operations.OperationType;

public abstract class OperationDTO implements Operation {

	@Override
	public abstract Object getOperationProperties ();

	@Override
	public abstract List<OperationInput> getInputs ();

	@Override
	public abstract OperationType getOperationType ();
}
