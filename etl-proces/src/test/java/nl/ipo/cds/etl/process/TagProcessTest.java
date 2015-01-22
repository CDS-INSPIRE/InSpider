package nl.ipo.cds.etl.process;

import junit.framework.Assert;
import nl.idgis.commons.jobexecutor.*;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.*;
import nl.ipo.cds.etl.DatasetHandlers;
import nl.ipo.cds.etl.FeatureProcessor;
import nl.ipo.cds.etl.PersistableFeature;
import nl.ipo.cds.etl.featurecollection.FeatureCollection;
import nl.ipo.cds.executor.ConfigDir;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Properties;

@ContextConfiguration(classes = TagProcessTest.Config.class)
/**
 * NOT extending AbstractTransactionalJUnit4SpringContextTests to be able to test transactionality
 */
public class TagProcessTest extends AbstractTransactionalJUnit4SpringContextTests {
	

	private EntityManager entityManager;


	@Configuration
	@ComponentScan (basePackageClasses = { nl.ipo.cds.etl.config.Package.class })
	@ImportResource ({
		"classpath:/nl/ipo/cds/dao/dataSource-applicationContext.xml",
		"classpath:/nl/ipo/cds/dao/dao-applicationContext.xml",
		"classpath:/nl/ipo/cds/context/propertyConfigurer-test.xml" // Override ConfigDirPropertyPlaceholderConfigurer
	})
	public static class Config {
		@Bean
		public ConfigDir configDir () {
			return new ConfigDir ("classpath:");
		}

		@Bean
		@Inject
		public TagProcess tagProcess (final DataSource dataSource) {
			return new TagProcess(dataSource);
		}
	}
	
	@PersistenceContext(unitName = "cds")
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Autowired
	private ManagerDao managerDao;
	


	@Inject
	private JobCreator jobCreator;

	@Inject
	private TagProcess tagProcess;

	private Properties logProperties;
	
	private JobLogger logger;

	private TagJob tagJob;
	private Bronhouder bronhouder;
	private Dataset dataset;
	private DatasetType datasetType;
	private Thema thema;

	@Before
	public void setUp(){
		Assert.assertNotNull(this.entityManager);
		Assert.assertNotNull(this.managerDao);
		
		logProperties = new Properties ();
		logger = new JobLogger () {
			@Override
			public void logString(Job job, String key, LogLevel logLevel, String message) {
			}

			@Override
			public void logString(Job job, String key, LogLevel logLevel, String message, Map<String,Object> context) {
			}
		};

		bronhouder = new Bronhouder();
		bronhouder.setCode("9930");
		bronhouder.setCommonName("noordbrabant");
		bronhouder.setContactAdres("");
		bronhouder.setContactEmailadres("inspire@idgis.nl");
		bronhouder.setContactNaam("N. Brabant");
		bronhouder.setContactPlaats("");
		bronhouder.setContactPostcode("");
		bronhouder.setContactTelefoonnummer("");
		bronhouder.setNaam("Noord-Brabant");
		managerDao.create(bronhouder);


		// TODO: Remove Geodan references.
		thema = new Thema();
		thema.setNaam("LandelijkGebiedBeheer");
		managerDao.create(thema);
		datasetType = new DatasetType();
		datasetType.setNaam("LandelijkGebiedBeheer");
		datasetType.setRefreshPolicy(RefreshPolicy.IF_MODIFIED_METADATA);
		datasetType.setThema(thema);
		managerDao.create(datasetType);

		dataset = new Dataset();
		dataset.setActief(true);
		dataset.setNaam("test");
		dataset.setUuid("http://ftp.geodan.nl/vrn/data/test/geometrie/test.gml;http://ftp.geodan.nl/vrn/data/test/geometrie/geometries-test.xsd;GebiedBeheerNM");
		dataset.setBronhouder(bronhouder);
		dataset.setDatasetType(datasetType);
		managerDao.create(dataset);


		tagJob = new TagJob();
		tagJob.setBronhouder(bronhouder);
		tagJob.setDatasetType(dataset.getDatasetType());
		tagJob.setUuid(dataset.getUuid());
		tagJob.setTag("test-case");

		jobCreator.putJob (tagJob);

	}

	@After
	public void tearDown() {
		managerDao.delete(tagJob);
		managerDao.delete(dataset);
		managerDao.delete(datasetType);
		managerDao.delete(thema);
		managerDao.delete(bronhouder);
	}

	@Test
	public void testTagProcessor() {
		tagProcess.process(tagJob, logger);
	}
	
}
