package nl.ipo.cds.attributemapping.operations.discover.annotation.operations;

import nl.ipo.cds.attributemapping.NullReference;
import nl.ipo.cds.attributemapping.operations.annotation.Execute;
import nl.ipo.cds.attributemapping.operations.annotation.MappingOperation;

@MappingOperation
public class TestNullReference {
	@Execute
	public NullReference execute () {
		return NullReference.VALUE;
	}
}
