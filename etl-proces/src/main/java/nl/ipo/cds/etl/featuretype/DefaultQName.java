package nl.ipo.cds.etl.featuretype;

import nl.ipo.cds.domain.QName;

public class DefaultQName implements QName {

	private final String namespace;
	private final String localPart;
	
	public DefaultQName (final String namespace, final String localPart) {
		if (namespace == null) {
			throw new NullPointerException ("namespace is null");
		}
		if (localPart == null) {
			throw new NullPointerException ("localPart is null");
		}
		
		this.namespace = namespace;
		this.localPart = localPart;
	}
	
	public DefaultQName (final QName qname) {
		this (qname.getNamespace (), qname.getLocalPart ());
	}
	
	public DefaultQName (final javax.xml.namespace.QName qname) {
		this (qname.getNamespaceURI (), qname.getLocalPart ());
	}

	@Override
	public String getNamespace() {
		return namespace;
	}

	@Override
	public String getLocalPart() {
		return localPart;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((localPart == null) ? 0 : localPart.hashCode());
		result = prime * result
				+ ((namespace == null) ? 0 : namespace.hashCode());
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
		DefaultQName other = (DefaultQName) obj;
		if (localPart == null) {
			if (other.localPart != null)
				return false;
		} else if (!localPart.equals(other.localPart))
			return false;
		if (namespace == null) {
			if (other.namespace != null)
				return false;
		} else if (!namespace.equals(other.namespace))
			return false;
		return true;
	}
	
	@Override
	public String toString () {
		return "{" + getNamespace () + "}" + getLocalPart ();
	}

	@Override
	public int compareTo (final QName o) {
		final int result = getLocalPart ().compareTo (o.getLocalPart ());
		return result == 0 ? getNamespace ().compareTo (o.getNamespace ()) : result;
	}
}
