package nl.ipo.cds.nagios.ast;

import nl.ipo.cds.nagios.parser.ParserContext;
import nl.ipo.cds.nagios.parser.Token;

public class KVPNode extends ASTNode {
	private static final long serialVersionUID = 3953568830875326148L;
	
	private Token key;
	private Token value;
	
	public KVPNode (final ParserContext parserContext, int line, int column, final Token key, final Token value) {
		super (parserContext, line, column);
		
		this.key = key;
		this.value = value;
	}
	
	public Token getKey () {
		return key;
	}
	
	public Token getValue () {
		return value;
	}
}
