package nl.ipo.cds.nagios.parser;

public class ParserError {

	private ParserContext parserContext;
	private int line;
	private int column;
	private String message;
	
	public ParserError (final ParserContext parserContext, final int line, final int column, final String message) {
		this.parserContext = parserContext;
		this.line = line;
		this.column = column;
		this.message = message;
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

	public String getMessage() {
		return message;
	}
}
