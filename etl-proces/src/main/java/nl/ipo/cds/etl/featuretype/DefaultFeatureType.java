package nl.ipo.cds.etl.featuretype;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import nl.ipo.cds.domain.FeatureType;
import nl.ipo.cds.domain.FeatureTypeAttribute;
import nl.ipo.cds.domain.QName;

public class DefaultFeatureType implements FeatureType {

	private final QName qname;
	private final Set<DefaultFeatureTypeAttribute> attributes;
	
	DefaultFeatureType (final QName qname, final Set<DefaultFeatureTypeAttribute> attributes) {
		if (qname == null) {
			throw new NullPointerException ("qname is null");
		}
		if (attributes == null) {
			throw new NullPointerException ("attributes is null");
		}
		
		this.qname = new DefaultQName (qname);
		this.attributes = new HashSet<DefaultFeatureTypeAttribute> (attributes);
	}

	@Override
	public QName getName() {
		return qname;
	}

	@Override
	public Set<FeatureTypeAttribute> getAttributes() {
		return Collections.<FeatureTypeAttribute>unmodifiableSet (attributes);
	}

	@Override
	public String toString () {
		final StringBuilder builder = new StringBuilder ();
		
		for (final FeatureTypeAttribute attr: getAttributes ()) {
			if (builder.length () > 0) {
				builder.append (", ");
			}
			builder.append (attr.toString ());
		}
		
		return getName () + "(" + builder.toString () + ")";
	}
}
