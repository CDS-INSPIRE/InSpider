package nl.ipo.cds.etl.featuretype;

import nl.ipo.cds.domain.AttributeType;
import nl.ipo.cds.domain.FeatureTypeAttribute;
import nl.ipo.cds.domain.QName;

public class DefaultFeatureTypeAttribute implements FeatureTypeAttribute {

	private final QName qname;
	private final AttributeType type;
	
	DefaultFeatureTypeAttribute (final QName qname, final AttributeType type) {
		if (qname == null) {
			throw new NullPointerException ("qname is null");
		}
		if (type == null) {
			throw new NullPointerException ("type is null");
		}
		
		this.qname = new DefaultQName (qname);
		this.type = type;
	}

	@Override
	public QName getName() {
		return qname;
	}

	@Override
	public AttributeType getType() {
		return type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((qname == null) ? 0 : qname.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultFeatureTypeAttribute other = (DefaultFeatureTypeAttribute) obj;
		if (qname == null) {
			if (other.qname != null)
				return false;
		} else if (!qname.equals(other.qname))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
	
	@Override
	public String toString () {
		return getName ().getLocalPart () + ": " + getType ();
	}

	@Override
	public int compareTo (final FeatureTypeAttribute o) {
		final int result = getName ().compareTo (o.getName ());
		
		return result == 0 ? getType ().compareTo (o.getType ()) : result;
	}
}
