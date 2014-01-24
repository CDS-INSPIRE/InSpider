package nl.ipo.cds.validation;

public class ExpressionEvaluationException extends RuntimeException {
	private static final long serialVersionUID = 8705087696417355921L;

	public ExpressionEvaluationException () {
	}

	public ExpressionEvaluationException (final String message) {
		super (message);
	}

	public ExpressionEvaluationException (final Throwable cause) {
		super (cause);
	}

	public ExpressionEvaluationException (final String message, final Throwable cause) {
		super(message, cause);
	}

	public ExpressionEvaluationException (final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
