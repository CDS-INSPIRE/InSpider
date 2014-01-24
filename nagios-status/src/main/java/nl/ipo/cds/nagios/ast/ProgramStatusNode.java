package nl.ipo.cds.nagios.ast;

import java.util.List;

import nl.ipo.cds.nagios.parser.ParserContext;

public class ProgramStatusNode extends ObjectNode {
	private static final long serialVersionUID = 4617451975066712196L;

	public ProgramStatusNode (final ParserContext parserContext, int line, int column, final List<KVPNode> kvps) {
		super (parserContext, line, column, kvps);
	}
}
