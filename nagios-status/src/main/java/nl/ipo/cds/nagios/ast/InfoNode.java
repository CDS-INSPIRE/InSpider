package nl.ipo.cds.nagios.ast;

import java.util.List;

import nl.ipo.cds.nagios.parser.ParserContext;

public class InfoNode extends ObjectNode {
	private static final long serialVersionUID = -6586878836882607658L;

	public InfoNode (final ParserContext parserContext, int line, int column, final List<KVPNode> kvps) {
		super (parserContext, line, column, kvps);
	}
}
