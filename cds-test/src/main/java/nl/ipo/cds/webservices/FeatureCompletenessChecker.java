package nl.ipo.cds.webservices;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;

import junit.framework.Assert;
import nl.idgis.commons.jobexecutor.JobDao;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.domain.Dataset;
import nl.ipo.cds.etl.file.FileCacheImpl;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

public class FeatureCompletenessChecker {

	private static final Log logger = LogFactory.getLog(FeatureCompletenessChecker.class);

	private ManagerDao managerDao;
	private JobDao jobDao;

	private FileCacheImpl fileCache;

	private URL inspireGetFeatureRequest;
	
	public static void main(String[] args) throws Exception {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
				"classpath:/nl/ipo/cds/dao/dao-applicationContext.xml",
				"classpath:/nl/ipo/cds/dao/dataSource-applicationContext.xml",
				"classpath:/nl/ipo/cds/webservices/webservices-applicationContext.xml"
				);

		FeatureCompletenessChecker featureCompletenessChecker = applicationContext.getBean(FeatureCompletenessChecker.class);
		featureCompletenessChecker.compareBronhouderVSInspireServices();
	}

	public void compareBronhouderVSInspireServices() throws IOException, TransformerException, XMLStreamException, FactoryConfigurationError, JaxenException{

		List<Resource> getFeatureResourcesAllDatasets = createGetFeatureResourcesAllDatasets();
		
		FeatureCollectionComparator comparator = new FeatureCollectionComparator();
		
		Resource featureCollectionInspire = new UrlResource(inspireGetFeatureRequest);
		logger.debug("Make string-id collection of the inspire service from url: " + featureCollectionInspire.getDescription());
		List<String> allInspireFeatures = FeatureCollectionComparator.createFeatureIdStringCollectionFlat(featureCollectionInspire);
		logger.debug("Done making string-id collection of the inspire service");

		FeatureCollectionComparisonResult result = comparator.compareFeatureCollectionsByResources(getFeatureResourcesAllDatasets, allInspireFeatures);
		
		logger.debug(ReflectionToStringBuilder.toString(result.getMessages().toArray(), ToStringStyle.MULTI_LINE_STYLE));
		Assert.assertTrue("Collection's should be equal", result.isSuccess());
	}

	private List<Resource> createGetFeatureResourcesAllDatasets()
			throws JaxenException, XMLStreamException,
			FactoryConfigurationError, IOException {
		List<Resource> getFeatureResourcesAllDatasets = new ArrayList<Resource>();
		List<Dataset> datasets = managerDao.getAllDatasets();
		int quantityDatasetsLoadedInInspireSchema = 0;
		for (Iterator<Dataset> datasetIterator = datasets.iterator(); datasetIterator.hasNext();) {
			Dataset dataset = (Dataset) datasetIterator.next();
			EtlJob job = managerDao.getLastSuccessfullImportJob(dataset.getBronhouder(), dataset.getDatasetType(), dataset.getUuid());
			if(job != null){
				String fileCacheDir = null;//fileCache.getFiledir(job);
				String file = null;//fileCache.getFilename(job);
				Resource cachedResource = new FileSystemResource(fileCacheDir + System.getProperty("file.separator") + file);
				logger.debug("Adding cachedResource(" + quantityDatasetsLoadedInInspireSchema + ") " + "\"" + cachedResource.getDescription() + "\" to the cached-resources-list.");
				getFeatureResourcesAllDatasets.add(cachedResource);
				quantityDatasetsLoadedInInspireSchema++;
			}
		}
		logger.debug("Quantity of datasets that resulted in features in inspire schema:" + quantityDatasetsLoadedInInspireSchema);
		return getFeatureResourcesAllDatasets;
	}

	public JobDao getJobDao () {
		return jobDao;
	}
	
	public void setJobDao (final JobDao jobDao) {
		this.jobDao = jobDao;
	}
	
	public ManagerDao getManagerDao() {
		return managerDao;
	}

	public void setManagerDao(ManagerDao managerDao) {
		this.managerDao = managerDao;
	}

	public FileCacheImpl getFileCache() {
		return fileCache;
	}

	public void setFileCache(FileCacheImpl fileCache) {
		this.fileCache = fileCache;
	}

	public URL getInspireGetFeatureRequest() {
		return inspireGetFeatureRequest;
	}

	public void setInspireGetFeatureRequest(URL inspireGetFeatureRequest) {
		this.inspireGetFeatureRequest = inspireGetFeatureRequest;
	}
}
