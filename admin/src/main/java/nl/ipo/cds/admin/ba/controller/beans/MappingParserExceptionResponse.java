package nl.ipo.cds.admin.ba.controller.beans;

import nl.ipo.cds.admin.ba.controller.MappingParserException;

public class MappingParserExceptionResponse extends ExceptionResponse<MappingParserException> {

	public MappingParserExceptionResponse (final MappingParserException exception) {
		super(exception);
	}
}
