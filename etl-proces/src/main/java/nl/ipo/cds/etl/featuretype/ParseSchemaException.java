package nl.ipo.cds.etl.featuretype;

public class ParseSchemaException extends Exception {

	private static final long serialVersionUID = -2137810084922219463L;

	public ParseSchemaException() {
	}

	public ParseSchemaException(String message) {
		super(message);
	}

	public ParseSchemaException(Throwable cause) {
		super(cause);
	}

	public ParseSchemaException(String message, Throwable cause) {
		super(message, cause);
	}
}
