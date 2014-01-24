package nl.ipo.cds.etl.reporting;

/**
 * Subclasses of the JobFaseLogWriter class throw this exception in response to exceptions thrown by the underlying
 * template engine. The "cause" is set to the exception thrown by the template engine.
 * 
 * @author Erik Orbons
 */
public class WriterException extends Exception {

	private static final long serialVersionUID = -230037439413367133L;

	public WriterException () {
	}
	
	public WriterException (Throwable cause) {
		super (cause);
	}
	
	public WriterException (String message) {
		super (message);
	}
	
	public WriterException (String message, Throwable cause) {
		super (message, cause);
	}
}
