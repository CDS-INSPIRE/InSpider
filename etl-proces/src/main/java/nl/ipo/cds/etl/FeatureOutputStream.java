package nl.ipo.cds.etl;

public interface FeatureOutputStream<T extends Feature> {

	void writeFeature (T feature);
}
