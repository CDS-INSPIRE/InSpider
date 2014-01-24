package nl.ipo.cds.etl;

public class FilteringFeatureOutputStream<InputType extends Feature, OutputType extends Feature> implements FeatureOutputStream<InputType> {

	private final FeatureFilter<InputType, OutputType> filter;
	private final FeatureOutputStream<OutputType> outputStream;
	private final FeatureOutputStream<Feature> errorStream;
	
	public FilteringFeatureOutputStream (final FeatureFilter<InputType, OutputType> filter, final FeatureOutputStream<OutputType> outputStream, final FeatureOutputStream<Feature> errorStream) {
		this.filter = filter;
		this.outputStream = outputStream;
		this.errorStream = errorStream;
	}

	@Override
	public void writeFeature (final InputType feature) {
		filter.processFeature (feature, outputStream, errorStream);
	}

}
