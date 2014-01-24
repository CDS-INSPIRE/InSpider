package nl.ipo.cds.validation.execute;

public class ExecutorException extends Exception {

	private static final long serialVersionUID = 4045917223362370708L;

	public ExecutorException () {
	}

	public ExecutorException (final String message) {
		super(message);
	}

	public ExecutorException (final Throwable cause) {
		super(cause);
	}

	public ExecutorException (final String message, final Throwable cause) {
		super(message, cause);
	}

	public ExecutorException (final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
