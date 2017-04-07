package nl.ipo.cds.etl.filtering;

import java.util.Properties;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobLogger;
import nl.ipo.cds.domain.DatasetFilter;
import nl.ipo.cds.domain.FeatureType;
import nl.ipo.cds.etl.Feature;
import nl.ipo.cds.etl.FeatureFilter;
import nl.ipo.cds.etl.FeatureOutputStream;
import nl.ipo.cds.etl.GenericFeature;
import nl.ipo.cds.etl.log.LogStringBuilder;

import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;

public class DatasetFilterer implements FeatureFilter<GenericFeature, GenericFeature> {

	private final Filter filter;
	private final boolean valid;
	private GenericFeatureXPathEvaluator xpathEvaluator = new GenericFeatureXPathEvaluator ();
	
	public DatasetFilterer (final Job job, final FeatureType featureType, final DatasetFilter datasetFilter, final JobLogger jobLogger, final Properties properties) {
		// Don't filter if there is no dataset filter:
		if (datasetFilter == null) {
			valid = true;
			filter = null;
			return;
		}
		
		// Create a logger for validation:
		final LogStringBuilder<FilterExpressionValidator.MessageKey> logger = new LogStringBuilder<FilterExpressionValidator.MessageKey> ();
		logger.setJobLogger (jobLogger);
		logger.setProperties (properties);
		
		// Validate the filter expression:
		final FilterExpressionValidator validator = new FilterExpressionValidator (featureType, logger);
		if (!validator.isValid (job, datasetFilter)) {
			valid = false;
			filter = null;
			return;
		}
		
		// Convert the dataset filter into a deegree filter:
		final FilterFactory factory = new FilterFactory (featureType);
		this.filter = factory.createFilter (datasetFilter.getRootExpression ());
		this.valid = true;
	}
	
	protected DatasetFilterer (final Filter filter) {
		this.filter = filter;
		this.valid = true;
	}
	
	public boolean isValid () {
		return valid;
	}
	
	public boolean hasFilter () {
		return filter != null;
	}
	
	public Filter getFilter () {
		return filter;
	}

	@Override
	public void processFeature (final GenericFeature feature, final FeatureOutputStream<GenericFeature> outputStream, final FeatureOutputStream<Feature> errorOutputStream) {
		if (!isValid () || !hasFilter ()) {
			outputStream.writeFeature (feature);
			return;
		}
		
		try {
			if (filter.evaluate (feature, xpathEvaluator)) {
				outputStream.writeFeature (feature);
			}
		} catch (FilterEvaluationException e) {
			errorOutputStream.writeFeature (feature);
		}
	}

	@Override
	public void finish () {
	}

	@Override
	public boolean postProcess() {
		return true;
	}
}
