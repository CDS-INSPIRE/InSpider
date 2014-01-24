package nl.ipo.cds.admin.ba;

public class UnauthorizedException extends ControllerException {

	private static final long serialVersionUID = -1012975653491656581L;

	public UnauthorizedException () {
	}

	public UnauthorizedException (final String message) {
		super (403, message);
	}

	public UnauthorizedException (final Throwable cause) {
		super (403, cause);
	}

	public UnauthorizedException (final String message, final Throwable cause) {
		super (403, message, cause);
	}
}
