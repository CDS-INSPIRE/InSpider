package nl.ipo.cds.admin.ba;

public class ControllerException extends Exception {

	private static final long serialVersionUID = 5387526852082774782L;
	
	private final int statusCode;
	
	public ControllerException () {
		this (500);
	}
	
	public ControllerException (final int statusCode) {
		this.statusCode = statusCode;
	}

	public ControllerException (final String message) {
		this (500, message);
	}

	public ControllerException (final int statusCode, final String message) {
		super (message);
		
		this.statusCode = statusCode;
	}
	
	public ControllerException (final Throwable cause) {
		this (500, cause);
	}
	
	public ControllerException (final int statusCode, final Throwable cause) {
		super (cause);
		
		this.statusCode = statusCode;
	}

	public ControllerException (final String message, final Throwable cause) {
		this (500, message, cause);
	}
	
	public ControllerException (final int statusCode, final String message, final Throwable cause) {
		super (message, cause);
		
		this.statusCode = statusCode;
	}
	
	public int getStatusCode () {
		return statusCode;
	}
}
