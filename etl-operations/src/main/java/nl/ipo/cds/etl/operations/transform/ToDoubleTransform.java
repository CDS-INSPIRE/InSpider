package nl.ipo.cds.etl.operations.transform;

import nl.ipo.cds.attributemapping.operations.annotation.Execute;
import nl.ipo.cds.attributemapping.operations.annotation.Input;
import nl.ipo.cds.attributemapping.operations.annotation.MappingOperation;

@MappingOperation
public class ToDoubleTransform {

	@Execute
	public Double execute (final @Input("value") String input) {
		if (input == null) {
			return Double.NaN;
		}
		
		try {
			return Double.parseDouble (input);
		} catch (NumberFormatException e) {
			return Double.NaN;
		}
	}
}
