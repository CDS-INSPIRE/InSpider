package nl.ipo.cds.etl.process;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;
import javax.xml.stream.XMLStreamReader;

import nl.idgis.commons.jobexecutor.JobLogger;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.etl.DatasetHandlers;
import nl.ipo.cds.etl.Feature;
import nl.ipo.cds.etl.FeatureFilter;
import nl.ipo.cds.etl.FeatureOutputStream;
import nl.ipo.cds.etl.FileCache;
import nl.ipo.cds.etl.PersistableFeature;
import nl.ipo.cds.etl.attributemapping.AttributeMappingFactory;
import nl.ipo.cds.etl.db.DBWriter;
import nl.ipo.cds.etl.db.DBWriterFactory;
import nl.ipo.cds.etl.filtering.DatasetFiltererFactory;
import nl.ipo.cds.etl.process.helpers.HttpGetUtil;
import nl.ipo.cds.etl.util.CopyInOutputStream;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.postgresql.copy.CopyIn;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractorAdapter;

public class ImportFeatureProcessor extends ValidateFeatureProcessor {
	
	private static final Log technicalLog = LogFactory.getLog(ImportFeatureProcessor.class); // developer log

	private final DataSource dataSource;
	private final NativeJdbcExtractorAdapter nativeJdbcExtractorAdapter;
	private final FileCache fileCache;

	public ImportFeatureProcessor (
			final AttributeMappingFactory attributeMappingFactory, 
			final DatasetFiltererFactory datasetFiltererFactory,
			final DataSource dataSource, 
			final NativeJdbcExtractorAdapter nativeJdbcExtractorAdapter,
			final FileCache fileCache) {
		super (attributeMappingFactory, datasetFiltererFactory);
		
		this.dataSource = dataSource;
		this.nativeJdbcExtractorAdapter = nativeJdbcExtractorAdapter;
		this.fileCache = fileCache;
	}

	/**
	 * Adds a filter to the list of standard filters. A filter is appended to the list that writes features to the dbWriter.
	 */
	@Override
	protected List<FeatureFilter<PersistableFeature, PersistableFeature>> createFilters (final EtlJob job, final DatasetHandlers<PersistableFeature> datasetHandlers, final JobLogger logger) {
		final List<FeatureFilter<PersistableFeature, PersistableFeature>> filters = new ArrayList<FeatureFilter<PersistableFeature, PersistableFeature>> (super.createFilters (job, datasetHandlers, logger));
		
		// Add a DBWriter filter at the end of the pipeline:
		filters.add (createDBWriterFilter (job, datasetHandlers, logger));
	
		return filters;
	}
	
	private FeatureFilter<PersistableFeature, PersistableFeature> createDBWriterFilter (final EtlJob job, final DatasetHandlers<PersistableFeature> datasetHandlers, final JobLogger logger) {
		technicalLog.debug("initializing");
		
		final Connection connection = DataSourceUtils.getConnection(dataSource);
		
		technicalLog.debug("database connection established");
		
		DBWriterFactory<PersistableFeature> dbWriterFactory = datasetHandlers.getDBWriterFactory("job_id", "" + job.getId());
		
		try {
           String query =
                   "delete from " + dbWriterFactory.getTableName() + " " +
                   "where job_id in (" +
                       "select j0.id from manager.etljob j0 where j0.uuid = ? )";
               PreparedStatement stmt = connection.prepareStatement(query);
               stmt.setString(1, job.getUuid());                   
               technicalLog.debug("delete query: " + query + ", uuid = " + job.getUuid());
               int count = stmt.executeUpdate();
               technicalLog.debug("# of deleted features: " + count);
               stmt.close();
   		} catch(SQLException e) {
			throw new RuntimeException("Couldn't remove existing data", e);
		}

		technicalLog.debug("existing data removed");
		
		CopyManager copyManager;
		try {
			copyManager = new CopyManager((BaseConnection)
				nativeJdbcExtractorAdapter.getNativeConnection(connection));		
		} catch(SQLException e) {
			throw new RuntimeException("Couldn't construct CopyManager");
		}
		
		technicalLog.debug("CopyManager constructed");
		
		final java.io.OutputStream outputStream;
		try {
			CopyIn copyIn = copyManager.copyIn(dbWriterFactory.getQuery());			
			 outputStream = new CopyInOutputStream(copyIn);
		} catch(SQLException e) {
			throw new RuntimeException("Couldn't start copy", e);
		}
		
		technicalLog.debug("copy started");
		
		final DBWriter<PersistableFeature> dbWriter;
		try {
			dbWriter = dbWriterFactory.getDBWriter(outputStream, "utf-8");
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException("Couldn't construct DBWriter", e);
		}
		
		technicalLog.debug("dbwriter constructed");
		
		return new FeatureFilter<PersistableFeature, PersistableFeature> () {
			@Override
			public void processFeature (final PersistableFeature feature, final FeatureOutputStream<PersistableFeature> outputStream, final FeatureOutputStream<Feature> errorOutputStream) {
				technicalLog.debug("writing feature: " + feature.getId());
				dbWriter.writeObject(feature);
			}

			@Override
			public void finish () {
				dbWriter.close();
				technicalLog.debug("dbWriter closed");
				DataSourceUtils.releaseConnection(connection, dataSource);
			}
		};
	}
	
	@Override
	public URL processUrl(EtlJob job) {

		URL processedUrl = null;
		HttpGetUtil wfsHttpGetUtil = null;

		try {			
			processedUrl = new URL(job.getDatasetUrl());
			
			technicalLog.debug("trying to download " + processedUrl);
			
			wfsHttpGetUtil = new HttpGetUtil(processedUrl.toExternalForm());			
			if(!wfsHttpGetUtil.isValidResponse()) { 
				// invalid dataset urls are to be properly handled elsewhere,
				// in this method we only try to persist the data in the cache
				
				return processedUrl;
			}
			
			technicalLog.debug("writing to file cache");

			XMLStreamReader xmlStream = wfsHttpGetUtil.getEntityOMElement().getXMLStreamReaderWithoutCaching();
			XMLStreamUtils.skipStartDocument(xmlStream);
			processedUrl = fileCache.storeToCache(job, xmlStream);
			xmlStream.close();			
		} catch (Exception e) {
			throw new RuntimeException("Not be able to write to fileCache" ,e);
		} finally{
			if(wfsHttpGetUtil != null) {
				wfsHttpGetUtil.close();
			}
		}
		
		return processedUrl;
	}

	public FileCache getFileCache() {
		return fileCache;
	}

	@Override
	public boolean requiresFeatureProcessing(EtlJob job) {
		return job.getVerversen();
	}
}
