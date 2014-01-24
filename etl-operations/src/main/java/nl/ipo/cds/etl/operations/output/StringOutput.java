package nl.ipo.cds.etl.operations.output;

import nl.ipo.cds.attributemapping.MappingDestination;
import nl.ipo.cds.attributemapping.operations.annotation.Execute;
import nl.ipo.cds.attributemapping.operations.annotation.Input;
import nl.ipo.cds.attributemapping.operations.annotation.MappingOperation;

@MappingOperation (internal = true)
public class StringOutput extends AbstractOutput<String> {

	@Override
	@Execute
	public void execute (final @Input("value") String input, final MappingDestination destination) {
		destination.setValue (input.toString ());
	}

}
