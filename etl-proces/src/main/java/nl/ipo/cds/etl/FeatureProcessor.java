package nl.ipo.cds.etl;

import java.net.URL;

import nl.idgis.commons.jobexecutor.JobLogger;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.etl.featurecollection.FeatureCollection;

public interface FeatureProcessor {

	public class ValidationException extends Exception {
		
		private static final long serialVersionUID = -7824251779291018499L;

		public ValidationException(String message, Throwable cause) {
			super(message, cause);
		}
		
		public ValidationException(String message) {
			super(message);
		}
	}
	
	URL processUrl(EtlJob job);
	int processFeatures(EtlJob job, DatasetHandlers<PersistableFeature> datasetHandlers, FeatureCollection features, JobLogger logger) throws ValidationException;
	boolean requiresFeatureProcessing(EtlJob job);
}