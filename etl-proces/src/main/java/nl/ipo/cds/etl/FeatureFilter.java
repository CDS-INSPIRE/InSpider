package nl.ipo.cds.etl;

public interface FeatureFilter<InputType extends Feature, OutputType extends Feature> {

	void processFeature (InputType feature, FeatureOutputStream<OutputType> outputStream, FeatureOutputStream<Feature> errorOutputStream);

	/**
	 * Responsible for cleaning up resources, no post processing.
	 */
	void finish ();

	/**
	 * Responsible for post processing, not cleaning up resources.
	 * @return Returns true iff the post processing checks/validation completed successfully.
	 */
	boolean postProcess();
}
