package nl.ipo.cds.etl;

public interface FeatureFilter<InputType extends Feature, OutputType extends Feature> {

	void processFeature (InputType feature, FeatureOutputStream<OutputType> outputStream, FeatureOutputStream<Feature> errorOutputStream);
	void finish ();
}
