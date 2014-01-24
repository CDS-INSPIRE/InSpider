package nl.ipo.cds.etl.operations.transform;

import nl.ipo.cds.attributemapping.operations.annotation.Execute;
import nl.ipo.cds.attributemapping.operations.annotation.Input;
import nl.ipo.cds.attributemapping.operations.annotation.MappingOperation;

@MappingOperation
public class MakeStringArrayTransform {

	@Execute
	public String[] execute (final @Input("values") String ... values) {
		return values;
	}
}
