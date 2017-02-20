package nl.ipo.cds.etl;

import static java.lang.Integer.MAX_VALUE;
import static nl.ipo.cds.domain.RefreshPolicy.*;
import static nl.ipo.cds.utils.UrlUtils.getLastModifiedHeader;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Properties;

import javax.inject.Inject;
import javax.xml.stream.XMLStreamException;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobLogger;
import nl.idgis.commons.jobexecutor.JobLogger.LogLevel;
import nl.idgis.commons.jobexecutor.JobTypeIntrospector;
import nl.idgis.commons.jobexecutor.Process;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.Dataset;
import nl.ipo.cds.domain.DatasetType;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.etl.FeatureProcessor.ValidationException;
import nl.ipo.cds.etl.featurecollection.FeatureCollection;
import nl.ipo.cds.etl.featurecollection.NumberLimitedFeatureCollection;
import nl.ipo.cds.etl.log.LogStringBuilder;
import nl.ipo.cds.etl.process.DatasetMetadata;
import nl.ipo.cds.etl.process.Harvester;
import nl.ipo.cds.etl.process.HarvesterException;
import nl.ipo.cds.etl.process.HarvesterFactory;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class of all processes.
 * 
 * Exceptionhandling is done by catching all exception. Checked Exceptions are caught (and swallowed) and
 * written as warning to the log. RuntimeExceptions are caught and written as error to the log,
 * but also propagated to make sure transaction are rolled back.
 * 
 */
public abstract class AbstractProcess<T extends EtlJob> implements Process<T>, ApplicationContextAware {
	
	public static enum MessageKey {
		NO_FEATURES,
		XML_FEATURES_HTTP_ERROR,
		XML_FEATURES_EXCEPTION,
		NO_DATASET_HANDLERS,
		WFS_EXCEPTIONREPORT,
		XML_BLOCKING_FEATURE_ERROR,
		WFS_UNPARSEBLE_RESPONSE
	}

	private static final Log technicalLog = LogFactory.getLog(AbstractProcess.class); // developer log
	private final static LogLevel LOG_LEVEL = LogLevel.ERROR;

	private ApplicationContext applicationContext;
	
	private final Class<? extends Job> jobType;
	private final ManagerDao managerDao;
	private final Properties logProperties;
	private final FeatureProcessor featureProcessor;
	
	//private EventLogger<AbstractProcess.MessageKey> userLog; // user log

	@Inject
	private HarvesterFactory harvesterFactory;
	
	public AbstractProcess (
			final Class<? extends Job> jobType,
			final ManagerDao managerDao,
			final FeatureProcessor featureProcessor, 
			final Properties logProperties) {
		
		this.jobType = jobType;
		this.managerDao = managerDao;
		this.logProperties = logProperties;
		this.featureProcessor = featureProcessor;
	}

	@Override
	public Class<? extends Job> getJobType () {
		return jobType;
	}
	
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected DatasetHandlers<PersistableFeature> createDatasetHandlers(EtlJob job) throws XMLStreamException, IOException {
		Collection<DatasetHandlersFactory> factories = applicationContext.getBeansOfType(DatasetHandlersFactory.class).values();
		for(DatasetHandlersFactory<PersistableFeature> factory : factories) {
			if (!factory.isJobSupported (job)) {
				continue;
			}			
			DatasetHandlers<PersistableFeature> datasetHandlers = factory.createDatasetHandlers(job, managerDao);
			if(datasetHandlers != null) {
				return datasetHandlers;
			}
		}		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see nl.ipo.cds.etl.Process#executeJob(nl.ipo.cds.domain.Job)
	 */
	@Transactional
	@Override
	public boolean process (final T job, final JobLogger jobLogger) throws Exception {
		return process (job, featureProcessor, jobLogger);
	}
	
	public boolean process(final T job, final FeatureProcessor featureProcessor, final JobLogger jobLogger) throws Exception {
		return process(job, featureProcessor, jobLogger, MAX_VALUE);
	}

	public boolean process(final T job, final FeatureProcessor featureProcessor, final JobLogger jobLogger, int featureLimit) throws Exception {
		boolean error = true;		

		technicalLog.debug("START " + JobTypeIntrospector.getJobTypeName(job) + " Job");		
		final LogStringBuilder<AbstractProcess.MessageKey> userLog = new LogStringBuilder<AbstractProcess.MessageKey>();
		userLog.setProperties(logProperties);
		userLog.setJobLogger(jobLogger);

		DatasetHandlers<PersistableFeature> datasetHandlers = createDatasetHandlers(job);
		if (datasetHandlers == null) {
			String message = userLog.logEvent(job, AbstractProcess.MessageKey.NO_DATASET_HANDLERS, LOG_LEVEL,
					job.getUuid(), job.getBronhouder().getNaam(), job.getBronhouder().getCode());
			job.setResult(message);
			technicalLog.warn(message);
			return error;
		}
		
		DatasetMetadata md = fetchDatasetMetadataAndAugmentJob(job);
		if (md != null) {
			updateVerversen (job);
			if (job.getVerversen()) {
				if (featureProcessor.requiresFeatureProcessing(job)) {
					FeatureCollection fc = datasetHandlers.retrieveFeaturesFromBronhouder(job, featureProcessor, userLog, md);
					if (fc != null) {
						fc = new NumberLimitedFeatureCollection(fc, featureLimit);
						error = processFeatures(featureProcessor, job, datasetHandlers, fc, jobLogger, userLog);
					} else{
						job.setFeatureCount(0);	
					}
				} else {
					technicalLog.debug("feature processing phase skipped");
				}			
			}
		} else {
			technicalLog.debug("fetching of metadata failed for " + JobTypeIntrospector.getJobTypeName(job) + " Job");
		}

		technicalLog.debug("END " + JobTypeIntrospector.getJobTypeName(job) + " Job");
		return error;
	}

	private DatasetMetadata fetchDatasetMetadataAndAugmentJob(T job) throws HarvesterException {
		Harvester mdHarvester = harvesterFactory.createHarvester(job);
		if (!mdHarvester.updateJobWithDatasetUrl()) {
			technicalLog.error("Unable to fetch dataset metadata for job " + job);
		}
		return mdHarvester.getMetadata();
	}

	private void updateVerversen(T job) {
		//w1502 019 refreshpolicy from dataset entity class
		technicalLog.debug("abstract process managerDao +++++++++++++++++++++++ " + this.managerDao);
		technicalLog.debug("abstract process managerDao:  " + managerDao);
		Dataset dataset = managerDao.getDatasetBy(job.getBronhouder(), job.getDatasetType(), job.getUuid());
		
		//DatasetType datasetType = job.getDatasetType();
		if ((dataset.getRefreshPolicy() == IF_MODIFIED_HTTP_HEADER)) {
			setMetadataUpdateDatumFromLastModifiedHeader (job);
		}		
		RefreshPolicyGuard guard = new RefreshPolicyGuard(this.managerDao);
		EtlJob lastSuccess = managerDao.getLastSuccessfullImportJob(job);		
		boolean needsRefresh =	guard.isRefreshAllowed(job, lastSuccess);
		job.setVerversen(needsRefresh);
	}

	private void setMetadataUpdateDatumFromLastModifiedHeader(T job) {
		long lastModified = 0;
		try {
			final URL datasetUrl = new URL (job.getDatasetUrl());			
			lastModified = getLastModifiedHeader(datasetUrl);
		} catch (Exception e ) {
			throw new RuntimeException("Unable to retrieve last-modified header from " + job.getDatasetUrl());
		}
		technicalLog.debug("lastModifiedHeader: " + lastModified);
		job.setMetadataUpdateDatum(new Timestamp(lastModified));	
	}

	private boolean processFeatures(FeatureProcessor processor, final T job, final DatasetHandlers<PersistableFeature> datasetHandlers, final FeatureCollection fc, final JobLogger jobLogger, final LogStringBuilder<AbstractProcess.MessageKey> userLog ) {	
		boolean error = false;
		try {
			int num = processFeatures(processor, job, datasetHandlers, fc, jobLogger);
			job.setFeatureCount(num);
			if (num == 0) {
				String message = userLog.logEvent(job, AbstractProcess.MessageKey.NO_FEATURES, LOG_LEVEL);
				technicalLog.warn(message);
				job.setResult(message);
				error = true;
			}				
		} catch (ValidationException e) {
			String message = userLog.logEvent(job, AbstractProcess.MessageKey.XML_BLOCKING_FEATURE_ERROR, LogLevel.ERROR);
			technicalLog.warn(message);
			job.setResult(message);
			job.setFeatureCount(0);
			error = true;
		} catch (Exception e) {
			String message = userLog.logEvent(job, AbstractProcess.MessageKey.XML_FEATURES_EXCEPTION, LOG_LEVEL, ExceptionUtils.getRootCauseMessage(e));
			job.setResult(message);
			job.setFeatureCount(0);
			error = true;
			technicalLog.error("Error while parsing/processing features", e);
		}
		technicalLog.debug("processing features finished");		
		return error;		
	}

	@Transactional(propagation=REQUIRES_NEW, rollbackFor=Throwable.class)	
	private int processFeatures(FeatureProcessor processor, final T job, final DatasetHandlers<PersistableFeature> datasetHandlers, final FeatureCollection fc, final JobLogger jobLogger ) throws ValidationException {	
		return processor.processFeatures(job, datasetHandlers, fc, jobLogger);
	}

}
