package nl.ipo.cds.attributemapping.operations.discover;

import java.util.Collection;

import nl.ipo.cds.attributemapping.operations.InputOperationType;
import nl.ipo.cds.attributemapping.operations.OperationType;
import nl.ipo.cds.attributemapping.operations.OutputOperationType;
import nl.ipo.cds.attributemapping.operations.TransformOperationType;

public interface OperationDiscoverer {

	Collection<OperationType> getOperationTypes ();
	Collection<OperationType> getPublicOperationTypes ();
	Collection<TransformOperationType> getTransformOperationTypes ();
	Collection<InputOperationType> getInputOperationTypes ();
	Collection<OutputOperationType> getOutputOperationTypes ();
}
