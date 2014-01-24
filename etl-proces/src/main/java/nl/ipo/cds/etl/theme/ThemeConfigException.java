package nl.ipo.cds.etl.theme;

public class ThemeConfigException extends Exception {

	private static final long serialVersionUID = -1281801876448864772L;

	public ThemeConfigException () {
	}

	public ThemeConfigException (final String message) {
		super(message);
	}

	public ThemeConfigException (final Throwable cause) {
		super(cause);
	}

	public ThemeConfigException (final String message, final Throwable cause) {
		super(message, cause);
	}

	public ThemeConfigException (final String message, final Throwable cause,
			final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
