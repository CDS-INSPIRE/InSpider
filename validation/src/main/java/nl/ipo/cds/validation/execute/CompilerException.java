package nl.ipo.cds.validation.execute;

public class CompilerException extends Exception {

	private static final long serialVersionUID = -3916229560735034476L;

	public CompilerException () {
	}

	public CompilerException (String message) {
		super (message);
	}

	public CompilerException (Throwable cause) {
		super (cause);
	}

	public CompilerException (String message, Throwable cause) {
		super (message, cause);
	}

	public CompilerException (String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
