package nl.ipo.cds.attributemapping.operations.discover.annotation.operations;

import nl.ipo.cds.attributemapping.operations.annotation.Execute;
import nl.ipo.cds.attributemapping.operations.annotation.Input;
import nl.ipo.cds.attributemapping.operations.annotation.MappingOperation;

@MappingOperation
public class TestOperationWithMultipleMethods {
	@Execute
	public String execute (final @Input("a") String a, final @Input("b") String b) {
		return String.format ("%s:%s", a, b);
	}

	public String execute2 (final String a, final String b) {
		return null;
	}
}
