package nl.ipo.cds.etl.operations.transform;

import nl.ipo.cds.attributemapping.operations.annotation.Execute;
import nl.ipo.cds.attributemapping.operations.annotation.Input;
import nl.ipo.cds.attributemapping.operations.annotation.MappingOperation;

import org.deegree.commons.tom.ows.CodeType;

@MappingOperation
public class ToCodeTypeTransform {

	@Execute
	public CodeType execute (final @Input("code") String input, final @Input("codeSpace") String codeSpace) {
		if (input == null) {
			return null;
		}
		return new CodeType (input, codeSpace);
	}
}
