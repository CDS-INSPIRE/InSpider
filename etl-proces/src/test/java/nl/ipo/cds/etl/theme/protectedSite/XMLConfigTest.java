package nl.ipo.cds.etl.theme.protectedSite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.xml.stream.XMLStreamReader;

import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.Dataset;
import nl.ipo.cds.domain.DatasetType;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.domain.ImportJob;
import nl.ipo.cds.domain.Thema;
import nl.ipo.cds.etl.DatasetHandlers;
import nl.ipo.cds.etl.GenericFeature;
import nl.ipo.cds.etl.featurecollection.FeatureCollection;
import nl.ipo.cds.etl.featurecollection.WFSResponse;
import nl.ipo.cds.etl.featurecollection.WFSResponseReader;
import nl.ipo.cds.etl.test.TestData;
import nl.ipo.cds.etl.util.BlockingExecutor;
import nl.ipo.cds.executor.ConfigDir;

import org.deegree.feature.types.AppSchema;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = XMLConfigTest.Config.class)
public class XMLConfigTest {

	@Autowired
	private ProtectedSiteThemeConfig xmlConfig;
	
	private @Inject ManagerDao managerDao;
	
	private Bronhouder bronhouder;
	private Dataset dataset;
	private EtlJob job;	

	private XMLStreamReader streamReader;	
	private AppSchema appSchema;
	private String featureTypeName;

	@Configuration
	@ComponentScan (basePackageClasses = {nl.ipo.cds.etl.config.Package.class, nl.ipo.cds.etl.theme.protectedSite.config.Package.class })
	@ImportResource ({
		"classpath:/nl/ipo/cds/context/propertyConfigurer-test.xml",
		"classpath:/nl/ipo/cds/dao/dao-applicationContext.xml",
		"classpath:/nl/ipo/cds/dao/dataSource-applicationContext.xml"
	})
	public static class Config {
		@Bean
		public Executor executer () {
			return new BlockingExecutor (2);
		}
		
		@Bean
		public ConfigDir configDir () {
			return new ConfigDir ("classpath:");
		}
	}
	
	@Before
	public void setUp() throws Exception {		
		bronhouder = new Bronhouder();
		bronhouder.setCode("9931");
		
		Thema thema = new Thema ();
		thema.setNaam ("Protected sites");
		
		DatasetType datasetType = new DatasetType();
		datasetType.setNaam("ST");
		datasetType.setThema (thema);
		
		dataset = new Dataset();
		dataset.setBronhouder(bronhouder);
		dataset.setDatasetType(datasetType);
		
		job = new ImportJob();
		// copy properties from dataset to job
		job.setBronhouder(dataset.getBronhouder());
		job.setDatasetType(dataset.getDatasetType());
		job.setUuid(dataset.getUuid());
				
		TestData testData = new TestData();
		streamReader = testData.getXMLStreamReader();
		appSchema = testData.getAppSchema ();
		featureTypeName = testData.getFeatureTypeName ();
	}
	
	@Test
	public void testParseFeatureCollection() throws Exception {
		DatasetHandlers<ProtectedSite> datasetHandlers = xmlConfig.createDatasetHandlers(job, managerDao);
		assertNotNull(datasetHandlers);
		
		WFSResponseReader wfsResponseReader = new WFSResponseReader();
		WFSResponse wfsResponse = wfsResponseReader.parseWFSResponse(streamReader, appSchema, featureTypeName);
		FeatureCollection featureCollection = wfsResponse.getFeatureCollection();			
		
		int count = 0;
		for(final GenericFeature feature : featureCollection) {
			assertNotNull(feature);
			count++;
		}
		assertEquals(33, count);
	}
}
