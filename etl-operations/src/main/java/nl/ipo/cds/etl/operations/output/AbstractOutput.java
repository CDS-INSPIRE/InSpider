package nl.ipo.cds.etl.operations.output;

import nl.ipo.cds.attributemapping.MappingDestination;


public abstract class AbstractOutput<T> {

	public abstract void execute (T input, MappingDestination destination); 
}
