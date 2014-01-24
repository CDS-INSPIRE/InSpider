/**
 * 
 */
package nl.ipo.cds.etl.process;

import nl.idgis.commons.jobexecutor.JobLogger.LogLevel;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.etl.log.EventLogger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Rob
 *
 * Default Exceptionhandling is done by catching all exception even RuntimeExceptions. Reason for this is that
 * when Harvesting does not succeed for the current Job, caused by a RunTimeexception, this does not automatically mean
 * that another Job after this one will fail too. Therefore catch all Exceptions, and propagate them. The (Abstract)Process-class
 * will make sure that the checked Exceptions are caught and the RuntimeExceptions propagated.
 */
public class Harvester {

	private static final Log technicalLog = LogFactory.getLog(Harvester.class); // developer log
	
	private final static LogLevel LOG_LEVEL = LogLevel.ERROR;

	private final EventLogger<HarvesterMessageKey> userLog; // user log
	private final EtlJob job;
	
	// private OMElement featureTypeElement = null;
	private DatasetMetadata metadata = null;
	
	private final MetadataHarvester metadataHarvester;
	
	public Harvester(EventLogger<HarvesterMessageKey> userlog, EtlJob job, final MetadataHarvester metadataHarvester){
		this.metadataHarvester = metadataHarvester;
		this.userLog = userlog;
		this.job = job;
	}
	
	public boolean fetchMetadata() {
		
		if(job.getDatasetUrl() == null) {
			technicalLog.debug("determining metadataUrl");
			
			metadata = parseMetadataFromPgr ();
			if (metadata == null || !metadata.isValid ()) {
				return false;
			}
		} else {
			technicalLog.debug("using existing datasetUrl");
		}
		
		return true;
	}

	public DatasetMetadata getMetadata() {
		return metadata;
	}	
	
	public boolean updateJobWithDatasetUrl() {
		if (metadata == null) {
			if (!fetchMetadata()) {
				return false;
			}
		}
		
		if(metadata.isValid ()) {
			technicalLog.debug("determining datasetUrl");
			
			final String url;
			
			try {
				url = metadataHarvester.getFeatureCollectionUrl (metadata);
			} catch (HarvesterException e) {
				logException (e);
				return false;
			}
			
			if(url == null){
				return false;
			}
			
			storeDatasetUrl(metadata.getFeatureTypeName (), url);
		}
		
		return true;
	}
	
	protected MetadataHarvester getMetadataHarvester () {
		return metadataHarvester;
	}

	/**
	 * get data
	 * @param jobFase
	 * @return A boolean indicating whether the metadata is valid.
	 */
	private DatasetMetadata parseMetadataFromPgr() {
		final DatasetMetadata metadata;
		
		// Store the metadata URL in the job:
		job.setMetadataUrl (metadataHarvester.getMetadataUrl (job.getUuid ()));
		
		try {
			metadata = metadataHarvester.parseMetadata (job.getUuid ());
		} catch (HarvesterException e) {
			logException (e);
			return null;
		}
		
		job.setMetadataUpdateDatum (metadata.getTimestamp ());
		job.setWfsUrl (metadata.getFeatureCollectionUrl ());

		return metadata;
	}
	
	public void logException (final HarvesterException e) {
		final String[] params = e.getParameters ();
		final String[] eventParams = new String[1 + params.length];

		System.arraycopy (params, 0, eventParams, 1, params.length);
		eventParams[0] = e.getUrl ();
		
		final String message = userLog.logEvent (job, e.getMessageKey (), LOG_LEVEL, eventParams);
		technicalLog.warn (message);
	}

	/**
	 * Get wfs feature data and store to cache.
	 * @param job
	 * @param ftElement
	 * @param urlElement
	 */
	private void storeDatasetUrl(String ftName, String url) {

		String typeName = ftName;
		// some url's end with '?service=wfs', others do not
		url = createWfsGetFeatureUrl(url, typeName);
		technicalLog.debug("datasetUrl: " + url);
		job.setDatasetUrl(url);
	}

	protected String createWfsGetFeatureUrl(final String url, final String typeName) {
		final String separator = url.indexOf("?") == -1 ? "?" 
			: url.endsWith("?") || url.endsWith("&") ? "" : "&";
		
		return url + separator + "request=GetFeature"
			+ "&typename=" + typeName
			+ "&service=WFS"
			+ "&version=1.1.0"
			+ (job.getMaxFeatures () != null ? ("&maxfeatures=" + job.getMaxFeatures ()) : "");
	}
}
