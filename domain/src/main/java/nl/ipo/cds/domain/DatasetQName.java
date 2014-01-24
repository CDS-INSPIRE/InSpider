package nl.ipo.cds.domain;

public class DatasetQName implements QName {

	private final String namespace;
	private final String localPart;

	DatasetQName (final String namespace, final String localPart) {
		this.namespace = namespace;
		this.localPart = localPart;
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
	public int compareTo (final QName o) {
		final int result = getLocalPart ().compareTo (o.getLocalPart ());
		return result == 0 ? getNamespace ().compareTo (o.getNamespace ()) : result;
	}
}
