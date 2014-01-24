package nl.ipo.cds.etl.filtering;

import java.util.Properties;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobLogger;
import nl.ipo.cds.domain.DatasetFilter;
import nl.ipo.cds.domain.FeatureType;

public class DatasetFiltererFactory {

	private final Properties properties;
	
	public DatasetFiltererFactory (final Properties properties) {
		this.properties = properties;
	}

	public DatasetFilterer createAttributeMapper (
			final Job job,
			final FeatureType featureType,
			final DatasetFilter datasetFilter,
			final JobLogger jobLogger) {
		
		return new DatasetFilterer (job, featureType, datasetFilter, jobLogger, properties);
	}
}
