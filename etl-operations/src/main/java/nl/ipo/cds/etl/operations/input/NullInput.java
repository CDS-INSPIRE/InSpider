package nl.ipo.cds.etl.operations.input;

import nl.ipo.cds.attributemapping.NullReference;
import nl.ipo.cds.attributemapping.operations.annotation.Execute;
import nl.ipo.cds.attributemapping.operations.annotation.MappingOperation;

@MappingOperation
public class NullInput {

	@Execute
	public NullReference execute () {
		return NullReference.VALUE;
	}
}
