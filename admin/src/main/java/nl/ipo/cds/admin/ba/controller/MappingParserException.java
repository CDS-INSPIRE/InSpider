package nl.ipo.cds.admin.ba.controller;

public class MappingParserException extends Exception {
	private static final long serialVersionUID = 3383257187438479057L;

	public MappingParserException () {
	}

	public MappingParserException (final String message) {
		super (message);
	}

	public MappingParserException (final Throwable cause) {
		super (cause);
	}

	public MappingParserException (final String message, final Throwable cause) {
		super (message, cause);
	}
}