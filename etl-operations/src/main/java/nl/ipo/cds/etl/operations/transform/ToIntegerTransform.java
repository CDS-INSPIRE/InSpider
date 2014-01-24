package nl.ipo.cds.etl.operations.transform;

import nl.ipo.cds.attributemapping.operations.annotation.Execute;
import nl.ipo.cds.attributemapping.operations.annotation.Input;
import nl.ipo.cds.attributemapping.operations.annotation.MappingOperation;

@MappingOperation
public class ToIntegerTransform {

	@Execute
	public Integer execute (final @Input ("value") String input) {
		if (input == null) {
			return 0;
		}
		
		try {
			return Integer.parseInt (input);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
