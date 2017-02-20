package nl.ipo.cds.etl;

public class CountingFeatureFilter<T extends Feature> implements FeatureFilter<T, T> {

	int count = 0;

	@Override
	public void processFeature (final T feature, final FeatureOutputStream<T> outputStream, final FeatureOutputStream<Feature> errorOutputStream) {
		++ count;
		outputStream.writeFeature (feature);
	}
	
	public int getFeatureCount () {
		return count;
	}

	@Override
	public void finish () {
	}

	@Override
	public boolean postProcess() {
		return true;
	}
}
