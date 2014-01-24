package nl.ipo.cds.nagios.parser;

public class LexerException extends Exception {

	private static final long serialVersionUID = 582167202831567836L;

	private LexerContext lexerContext;
	private int line;
	private int column;
	
	public LexerException (final LexerContext lexerContext, int line, int column) {
		this.lexerContext = lexerContext;
		this.line = line;
		this.column = column;
	}
	
	public LexerException (final LexerContext lexerContext, int line, int column, String message) {
		super (message);
		
		this.lexerContext = lexerContext;
		this.line = line;
		this.column = column;
	}
	
	public LexerException (final LexerContext lexerContext, int line, int column, Throwable cause) {
		super (cause);
		
		this.lexerContext = lexerContext;
		this.line = line;
		this.column = column;
	}
	
	public LexerException (final LexerContext lexerContext, int line, int column, String message, Throwable cause) {
		super (message, cause);
		
		this.lexerContext = lexerContext;
		this.line = line;
		this.column = column;
	}

	public LexerContext getLexerContext() {
		return lexerContext;
	}

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}
}
