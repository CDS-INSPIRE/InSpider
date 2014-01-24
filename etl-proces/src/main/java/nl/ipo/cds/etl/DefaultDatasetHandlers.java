package nl.ipo.cds.etl;

import static nl.idgis.commons.jobexecutor.JobLogger.LogLevel.ERROR;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import nl.idgis.commons.jobexecutor.JobLogger;
import nl.idgis.commons.jobexecutor.JobLogger.LogLevel;
import nl.ipo.cds.attributemapping.operations.OperationType;
import nl.ipo.cds.attributemapping.operations.discover.OperationDiscoverer;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.dao.attributemapping.AttributeMappingDao;
import nl.ipo.cds.dao.attributemapping.OperationDTO;
import nl.ipo.cds.domain.CodeListMapping;
import nl.ipo.cds.domain.Dataset;
import nl.ipo.cds.domain.DatasetFilter;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.domain.FeatureType;
import nl.ipo.cds.etl.db.DBWriterFactory;
import nl.ipo.cds.etl.featurecollection.ExceptionReport;
import nl.ipo.cds.etl.featurecollection.FeatureCollection;
import nl.ipo.cds.etl.featurecollection.WFSResponse;
import nl.ipo.cds.etl.featurecollection.WFSResponseReader;
import nl.ipo.cds.etl.featuretype.FeatureTypeNotFoundException;
import nl.ipo.cds.etl.filtering.FeatureClipper;
import nl.ipo.cds.etl.log.EventLogger;
import nl.ipo.cds.etl.process.DatasetMetadata;
import nl.ipo.cds.etl.process.HarvesterException;
import nl.ipo.cds.etl.theme.AttributeDescriptor;
import nl.ipo.cds.etl.theme.ThemeConfig;
import nl.ipo.cds.utils.UrlUtils;
import nl.ipo.cds.validation.gml.codelists.AtomCodeListFactory;
import nl.ipo.cds.validation.gml.codelists.CodeListFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.types.AppSchema;

public class DefaultDatasetHandlers<T extends PersistableFeature> implements
		DatasetHandlers<T> {

	private final ThemeConfig<T> themeConfig;

	protected final ManagerDao managerDao;

	private final Collection<OperationType> operationTypes;

	private static final Log technicalLog = LogFactory.getLog(AbstractProcess.class);

	private final static LogLevel LOG_LEVEL = LogLevel.ERROR;

	public DefaultDatasetHandlers(
			final OperationDiscoverer operationDiscoverer,
			final ThemeConfig<T> themeConfig, final ManagerDao managerDao) {
		this.themeConfig = themeConfig;
		this.managerDao = managerDao;
		this.operationTypes = operationDiscoverer.getOperationTypes();
	}

	@Override
	public Class<T> getBeanClass() {
		return themeConfig.getFeatureTypeClass();
	}

	@Override
	public DBWriterFactory<T> getDBWriterFactory(String... constColumns) {
		return new DBWriterFactory<T>(getBeanClass(), constColumns);
	}

	@Override
	public ThemeConfig<T> getThemeConfig() {
		return themeConfig;
	}

	@Override
	public Map<AttributeDescriptor<?>, OperationDTO> getAttributeMappings(
			final EtlJob job, final FeatureType featureType) {
		final Dataset dataset = getDatasetForJob(job);
		final AttributeMappingDao attributeMappingDao = new AttributeMappingDao(
				managerDao, featureType.getAttributes(), operationTypes);

		if (dataset == null) {
			return new HashMap<AttributeDescriptor<?>, OperationDTO>();
		}

		final Map<AttributeDescriptor<?>, OperationDTO> mappings = new HashMap<AttributeDescriptor<?>, OperationDTO>();

		for (final AttributeDescriptor<?> ad : themeConfig
				.getAttributeDescriptors()) {
			mappings.put(ad,
					attributeMappingDao.getAttributeMapping(dataset, ad));
		}

		return mappings;
	}

	@Override
	public FeatureClipper getFeatureClipper(final EtlJob job,
			final JobLogger logger) {
		return null;
	}

	@Override
	public DatasetFilter getDatasetFilter(final EtlJob job) {
		final Dataset dataset = getDatasetForJob(job);

		if (dataset == null) {
			return null;
		}

		return managerDao.getDatasetFilter(dataset);
	}

	@Override
	public CodeListFactory getCodeListFactory(final EtlJob job) {
		final List<CodeListMapping> mappings = managerDao.getCodeListMappings();
		final Map<String, String> codeListMappings = new HashMap<String, String>();

		for (final CodeListMapping mapping : mappings) {
			codeListMappings.put(mapping.getCodeSpace(), mapping.getUrl());
		}

		return new AtomCodeListFactory(codeListMappings);
	}

	private Dataset getDatasetForJob(final EtlJob job) {
		for (final Dataset dataset : managerDao.getDatasetsByUUID(job.getUuid())) {
			if (dataset.getBronhouder().getId().equals(job.getBronhouder().getId())
					&& dataset.getDatasetType().getId().equals(job.getDatasetType().getId())) {
				return dataset;
			}
		}
		return null;
	}

	@Override
	public FeatureCollection retrieveFeaturesFromBronhouder(EtlJob job,
			FeatureProcessor featureProcessor,
			final EventLogger<AbstractProcess.MessageKey> userLog,
			final DatasetMetadata md) {

		AppSchema appSchema = null;
		try {
			appSchema = themeConfig.getSchemaHarvester()
					.parseApplicationSchema(md);
		} catch (HarvesterException e) {
			technicalLog.debug(String.format(
					"Failed to get application schema from WFS: %s",
					e.getMessage()), e);
			return null;
		}

		InputStream is = null;
		try {
			is = initConnection(job, job.getDatasetUrl(), userLog);
			if (is == null) {
				return null;
			}
			String featureTypeName = md.getFeatureTypeName();
			XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance()
					.createXMLStreamReader(is);
			return retrieveFeaturesFromWfs(job, featureTypeName, userLog,
					xmlStreamReader, appSchema);
		} catch (Exception e) {
			technicalLog.debug(String.format(
					"Failed to initiate GetFeature request to WFS: %s",
					e.getMessage()), e);
			userLog.logEvent(job,
					AbstractProcess.MessageKey.XML_FEATURES_EXCEPTION,
					LogLevel.ERROR, e.getMessage());
		}
		return null;
	}

	protected InputStream initConnection(EtlJob job, String url,
			final EventLogger<AbstractProcess.MessageKey> userLog)
			throws IOException {
		try {
			return UrlUtils.open(new URL (url));
		} catch (IOException e) {
			userLog.logEvent(job,
					AbstractProcess.MessageKey.XML_FEATURES_HTTP_ERROR, ERROR,
					e.getMessage());
			throw e;
		}
	}

	private FeatureCollection retrieveFeaturesFromWfs(EtlJob job,
			final String featureTypeName,
			final EventLogger<AbstractProcess.MessageKey> userLog,
			XMLStreamReader xmlStreamReader, final AppSchema appSchema)
			throws XMLStreamException, FeatureTypeNotFoundException,
			UnknownCRSException {
		WFSResponseReader wfsResponseReader = new WFSResponseReader();
		WFSResponse wfsResponse = wfsResponseReader.parseWFSResponse(
				xmlStreamReader, appSchema, featureTypeName);
		if (wfsResponse.isFeatureCollection()) {
			return wfsResponse.getFeatureCollection();
		}
		String message = null;
		if (wfsResponse.isExceptionReport()) {
			ExceptionReport exceptionReport = wfsResponse.getExceptionReport();
			message = userLog.logEvent(
					job,
					AbstractProcess.MessageKey.WFS_EXCEPTIONREPORT,
					LOG_LEVEL,
					exceptionReport.hasExceptionCode() ? exceptionReport
							.getExceptionCode() : "onbekend",
					exceptionReport.hasLocator() ? exceptionReport.getLocator()
							: "onbekend",
					exceptionReport.hasExceptionText() ? exceptionReport
							.getExceptionText() : "onbekend");
		} else {
			message = userLog.logEvent(job,
					AbstractProcess.MessageKey.WFS_UNPARSEBLE_RESPONSE,
					LOG_LEVEL);
		}
		technicalLog.warn(message);
		return null;
	}

}
