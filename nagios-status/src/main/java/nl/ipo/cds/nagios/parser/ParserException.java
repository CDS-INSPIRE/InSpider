package nl.ipo.cds.nagios.parser;

public class ParserException extends Exception {

	private static final long serialVersionUID = 2939646019582345692L;
	
	private ParserContext parserContext;
	private int line;
	private int column;

	public ParserException (final ParserContext parserContext, int line, int column) {
		this.parserContext = parserContext;
		this.line = line;
		this.column = column;
	}
	
	public ParserException (final ParserContext parserContext, int line, int column, String message) {
		super (message);
		
		this.parserContext = parserContext;
		this.line = line;
		this.column = column;
	}
	
	public ParserException (final ParserContext parserContext, int line, int column, Throwable cause) {
		super (cause);
		
		this.parserContext = parserContext;
		this.line = line;
		this.column = column;
	}
	
	public ParserException (final ParserContext parserContext, int line, int column, String message, Throwable cause) {
		super (message, cause);
		
		this.parserContext = parserContext;
		this.line = line;
		this.column = column;
	}
	
	public ParserException (final ParserContext parserContext, final LexerException lexerException) {
		this (parserContext, lexerException.getLine (), lexerException.getColumn (), lexerException.getMessage (), lexerException);
	}

	public ParserContext getParserContext() {
		return parserContext;
	}

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}
}
