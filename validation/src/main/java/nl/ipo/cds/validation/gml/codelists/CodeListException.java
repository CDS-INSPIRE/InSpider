package nl.ipo.cds.validation.gml.codelists;

public class CodeListException extends Exception {

	private static final long serialVersionUID = 8593501965769446621L;
	public final String codeSpace;
	public final String url;
	
	public CodeListException (final String codeSpace, final String url) {
		this.codeSpace = codeSpace;
		this.url = url;
	}

	public CodeListException (final String codeSpace, final String url, final String message) {
		super(message);
		
		this.codeSpace = codeSpace;
		this.url = url;
	}

	public CodeListException (final String codeSpace, final String url, final Throwable cause) {
		super(cause);
		
		this.codeSpace = codeSpace;
		this.url = url;
	}

	public CodeListException (final String codeSpace, final String url, final String message, final Throwable cause) {
		super(message, cause);
		
		this.codeSpace = codeSpace;
		this.url = url;
	}

	public CodeListException (final String codeSpace, final String url, final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		
		this.codeSpace = codeSpace;
		this.url = url;
	}
}
