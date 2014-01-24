package nl.ipo.cds.attributemapping.operations;

import java.util.List;

public interface Operation {
	Object getOperationProperties ();
	List<OperationInput> getInputs ();
	OperationType getOperationType ();
}
