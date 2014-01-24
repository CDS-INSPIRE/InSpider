package nl.ipo.cds.attributemapping.operations.discover.annotation.operations;

import nl.ipo.cds.attributemapping.operations.annotation.Input;
import nl.ipo.cds.attributemapping.operations.annotation.MappingOperation;

@MappingOperation
public class TestOperationVarargs {

	public String execute (final @Input("a") int a, final @Input("b") String ... inputs) {
		final StringBuilder builder = new StringBuilder ();
		
		builder.append (String.valueOf (a));
		
		for (final String input: inputs) {
			builder.append (input);
		}
		
		return builder.toString ();
	}
}