package nl.ipo.cds.attributemapping.executer;

public class OperationExecutionException extends Exception {

	private static final long serialVersionUID = -8584001461635890214L;

	public OperationExecutionException () {
	}
	
	public OperationExecutionException (final Throwable cause) {
		super (cause);
	}
	
	public OperationExecutionException (final String message) {
		super (message);
	}
	
	public OperationExecutionException (final String message, final Throwable cause) {
		super (message, cause);
	}
}
