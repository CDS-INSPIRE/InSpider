package nl.ipo.cds.etl.operations.transform;

import nl.ipo.cds.attributemapping.operations.annotation.Execute;
import nl.ipo.cds.attributemapping.operations.annotation.Input;
import nl.ipo.cds.attributemapping.operations.annotation.MappingOperation;

@MappingOperation
public class NumberToIntegerTransform {

	@Execute
	public Integer execute (final @Input ("number") Number number) {
		if (number == null) {
			return null;
		}
		
		return number.intValue ();
	}

}
