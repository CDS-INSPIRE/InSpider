package nl.ipo.cds.attributemapping.executer;

public class MappingValidationException extends Exception {

	private static final long serialVersionUID = -6355201763639272203L;

	public MappingValidationException() {
	}

	public MappingValidationException(String message) {
		super(message);
	}

	public MappingValidationException(Throwable cause) {
		super(cause);
	}

	public MappingValidationException(String message, Throwable cause) {
		super(message, cause);
	}
}
