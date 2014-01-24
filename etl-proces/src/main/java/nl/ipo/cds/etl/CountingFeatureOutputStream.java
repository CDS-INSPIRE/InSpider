package nl.ipo.cds.etl;

public class CountingFeatureOutputStream<T extends Feature> implements FeatureOutputStream<T> {

	int count = 0;

	@Override
	public void writeFeature (final T feature) {
		++ count;
	}
	
	public int getFeatureCount () {
		return count;
	}
}
