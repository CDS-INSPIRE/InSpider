package nl.ipo.cds.etl.featurecollection;

import nl.ipo.cds.domain.FeatureType;
import nl.ipo.cds.etl.GenericFeature;

import org.deegree.geometry.Envelope;

public interface FeatureCollection extends Iterable<GenericFeature> {
	Envelope getBoundedBy();
	FeatureType getFeatureType ();
}
