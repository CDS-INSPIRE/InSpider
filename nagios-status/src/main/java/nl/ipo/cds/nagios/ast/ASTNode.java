package nl.ipo.cds.nagios.ast;

import java.io.Serializable;

import nl.ipo.cds.nagios.parser.ParserContext;

public abstract class ASTNode implements Serializable {
	private static final long serialVersionUID = 1124341428837550408L;
	
	private transient ParserContext parserContext;
	private int line;
	private int column;
	
	public ASTNode (final ParserContext parserContext, final int line, final int column) {
		this.parserContext = parserContext;
		this.line = line;
		this.column = column;
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
