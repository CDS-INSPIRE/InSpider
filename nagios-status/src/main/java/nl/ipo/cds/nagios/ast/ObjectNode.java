package nl.ipo.cds.nagios.ast;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import nl.ipo.cds.nagios.parser.ParserContext;

public class ObjectNode extends ASTNode {

	private static final long serialVersionUID = 3886947863425585991L;
	
	private Map<String, KVPNode> keyValuePairs;
	
	public ObjectNode (final ParserContext parserContext, int line, int column, final Collection<KVPNode> kvps) {
		super (parserContext, line, column);
		
		this.keyValuePairs = new HashMap<String, KVPNode> ();
		
		for (KVPNode node: kvps) {
			keyValuePairs.put(node.getKey ().getValue (), node);
		}
	}
	
	public Collection<KVPNode> getKeyValuePairs () {
		return keyValuePairs.values ();
	}
	
	public Set<String> getKeys () {
		return keyValuePairs.keySet ();
	}
	
	public boolean hasValue (final String key) {
		return keyValuePairs.containsKey (key);
	}
	
	public KVPNode getKeyValuePair (final String key) {
		return keyValuePairs.get (key);
	}
	
	public String getValue (final String key) {
		final KVPNode node = keyValuePairs.get (key);
		
		if (node == null) {
			return null;
		}
		
		return node.getValue ().getValue ();
	}
}