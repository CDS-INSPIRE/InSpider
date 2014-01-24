package nl.ipo.cds.attributemapping.executer;

import java.util.List;

import nl.ipo.cds.attributemapping.MappingDestination;
import nl.ipo.cds.attributemapping.MappingSource;

public interface OperationExecuter {
	void before () throws OperationExecutionException;
	Object execute (MappingSource source, MappingDestination destination, List<Object> inputs) throws OperationExecutionException;
	void after () throws OperationExecutionException;
}
