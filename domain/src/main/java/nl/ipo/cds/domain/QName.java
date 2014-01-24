package nl.ipo.cds.domain;

public interface QName extends Comparable<QName> {
	String getNamespace ();
	String getLocalPart ();
	
	boolean equals (Object other);
	int hashCode ();
}
