package nl.ipo.cds.nagios.ast;

import java.util.List;

import nl.ipo.cds.nagios.parser.ParserContext;

public class ContactStatusNode extends ObjectNode {
	private static final long serialVersionUID = 8940958549755849243L;

	public ContactStatusNode (final ParserContext parserContext, int line, int column, final List<KVPNode> kvps) {
		super (parserContext, line, column, kvps);
	}
}
