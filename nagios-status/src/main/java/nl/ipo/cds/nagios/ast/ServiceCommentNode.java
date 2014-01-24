package nl.ipo.cds.nagios.ast;

import java.util.List;

import nl.ipo.cds.nagios.parser.ParserContext;

public class ServiceCommentNode extends ObjectNode {
	private static final long serialVersionUID = -5439486134560989537L;

	public ServiceCommentNode (final ParserContext parserContext, int line, int column, final List<KVPNode> kvps) {
		super (parserContext, line, column, kvps);
	}
}
