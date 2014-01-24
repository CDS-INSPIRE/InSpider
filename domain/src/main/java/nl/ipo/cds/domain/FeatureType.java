package nl.ipo.cds.domain;

import java.util.Set;

public interface FeatureType {
	QName getName ();
	Set<FeatureTypeAttribute> getAttributes ();
}
