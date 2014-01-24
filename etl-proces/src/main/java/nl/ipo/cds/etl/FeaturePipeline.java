package nl.ipo.cds.etl;

import java.util.Arrays;
import java.util.List;

public class FeaturePipeline<InputType extends Feature, OutputType extends Feature> implements FeatureFilter<InputType, OutputType> {

	private final FeatureFilter<Feature, Feature>[] filters;
	
	@SuppressWarnings("unchecked")
	public FeaturePipeline (final FeatureFilter<?, ?> ... filters) {
		this.filters = (FeatureFilter<Feature, Feature>[])Arrays.copyOf (filters, filters.length);
	}
	
	@SuppressWarnings("unchecked")
	public <A extends Feature, B extends Feature> FeaturePipeline (final List<FeatureFilter<A, B>> filters) {
		this.filters = filters.toArray (new FeatureFilter[filters.size ()]);
	}

	@Override
	public void processFeature (final InputType feature, final FeatureOutputStream<OutputType> outputStream, final FeatureOutputStream<Feature> errorStream) {
		if (feature == null) {
			throw new NullPointerException ("feature cannot be null");
		}
		if (outputStream == null) {
			throw new NullPointerException ("outputStream cannot be null");
		}
		
		doProcessFeature (feature, outputStream, errorStream, 0);
	}
	
	private void doProcessFeature (final Feature feature, final FeatureOutputStream<OutputType> pipelineOutputStream, final FeatureOutputStream<Feature> errorStream, final int filterNumber) {
		if (filterNumber >= filters.length) {
			@SuppressWarnings("unchecked")
			final OutputType outputFeature = (OutputType)feature;
			pipelineOutputStream.writeFeature (outputFeature);
		} else {
			filters[filterNumber].processFeature (feature, new FeatureOutputStream<Feature> () {
				@Override
				public void writeFeature (final Feature f) {
					doProcessFeature (f, pipelineOutputStream, errorStream, filterNumber + 1);
				}
			}, errorStream);
		}
	}

	@Override
	public void finish () {
		for (final FeatureFilter<Feature, Feature> filter: filters) {
			filter.finish ();
		}
	}
}
