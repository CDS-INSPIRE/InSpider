package nl.ipo.cds.nagios.parser;

import java.io.Serializable;

public class Token implements Serializable {

	private static final long serialVersionUID = -8659570641733721131L;
	
	private LexerContext lexerContext;
	private String value;
	private TokenType tokenType;
	private int line;
	private int column;
	
	public Token (final LexerContext lexerContext, final TokenType tokenType, final String value, int line, int column) {
		this.lexerContext = lexerContext;
		this.tokenType = tokenType;
		this.value = value;
		this.line = line;
		this.column = column;
	}

	public LexerContext getLexerContext() {
		return lexerContext;
	}

	public String getValue() {
		return value;
	}

	public TokenType getTokenType() {
		return tokenType;
	}

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}
}
