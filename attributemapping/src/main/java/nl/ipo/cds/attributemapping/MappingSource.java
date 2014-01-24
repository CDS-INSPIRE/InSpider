package nl.ipo.cds.attributemapping;

public interface MappingSource {
	boolean hasAttribute (String name);
	Object getAttributeValue (String name);
}
