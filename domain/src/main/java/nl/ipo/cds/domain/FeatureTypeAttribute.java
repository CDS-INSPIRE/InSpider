package nl.ipo.cds.domain;

public interface FeatureTypeAttribute extends Comparable<FeatureTypeAttribute>{
	QName getName ();
	AttributeType getType ();
	
	boolean equals (Object other);
	int hashCode ();
}
