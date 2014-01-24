package nl.ipo.cds.etl.process;

import java.net.URL;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import junit.framework.Assert;
import nl.idgis.commons.jobexecutor.AbstractJob;
import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobDao;
import nl.idgis.commons.jobexecutor.JobLogger;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.dao.attributemapping.AttributeMappingDao;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.domain.ImportJob;
import nl.ipo.cds.domain.Thema;
import nl.ipo.cds.etl.DatasetHandlers;
import nl.ipo.cds.etl.FeatureProcessor;
import nl.ipo.cds.etl.PersistableFeature;
import nl.ipo.cds.etl.featurecollection.FeatureCollection;
import nl.ipo.cds.executor.ConfigDir;

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

@ContextConfiguration(classes = ProcessTransactionTest.Config.class)
/**
 * NOT extending AbstractTransactionalJUnit4SpringContextTests to be able to test transactionality
 */
public class ProcessTransactionTest extends AbstractTransactionalJUnit4SpringContextTests {
	
	protected static final String ABORT_MESSAGE = "Abort this Job";

	private EntityManager entityManager;

	private Long jobId;
	private Long themaId;

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
	}
	
	@PersistenceContext(unitName = "cds")
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Autowired
	private ManagerDao managerDao;
	
	@Autowired
	private HarvesterFactory harvesterFactory;
	
	@Inject
	private JobDao jobDao;
	
	private FeatureProcessor featureProcessor;
	
	private Properties logProperties;
	
	private JobLogger logger;
	
	@Before
	public void testInitialization(){
		Assert.assertNotNull(this.entityManager);
		Assert.assertNotNull(this.managerDao);
		
		logProperties = new Properties ();
		featureProcessor = new FeatureProcessor() {
			@Override
			public boolean requiresFeatureProcessing(EtlJob job) {
				return false;
			}
			
			@Override
			public URL processUrl(EtlJob job) {
				return null;
			}
			
			@Override
			public int processFeatures(EtlJob job,
					DatasetHandlers<PersistableFeature> datasetHandlers,
					FeatureCollection features, JobLogger logger)
					throws ValidationException {
				return 0;
			}
		};
		logger = new JobLogger () {
			@Override
			public void logString(Job job, String key, LogLevel logLevel, String message) {
			}

			@Override
			public void logString(Job job, String key, LogLevel logLevel, String message, Map<String,Object> context) {
			}
		};
	}
	
	@Test
	/**
	 * Make sure that the process runs within an transaction and that when a RuntimeException
	 * is raised, the transaction is rolled back
	 */
	public void testProcessJobAbortion() throws Exception {
    	ImportJob job = createJob();
    	this.jobId = job.getId();

    	ImportProcess process = new ImportProcess(managerDao, featureProcessor, logProperties) {
			@Override
			@Transactional(propagation = Propagation.REQUIRES_NEW)
			public boolean process(ImportJob job, final JobLogger logger) throws Exception {
				// Simulate here the process is writing stuff to the database by inserting an Thema.
				themaId = createThema();
				// Simulate a RuntimeException
				throw new RuntimeException(ABORT_MESSAGE);
			}
		};

		try {
			process.process(job, logger);
		} catch (RuntimeException re) {
			// Assert the Job is not rolled-back
			AbstractJob reFoundJob = this.managerDao.getJob(this.jobId);
			Assert.assertNotNull("Job could not be found again.", reFoundJob);
			// Assert that insertion of the Thema is rolled-back
			Thema refoundThema = managerDao.getThema(this.themaId);
			Assert.assertNull("It's not correct that the Thema could be found again", refoundThema);
			return;
		}
		// If we get here there was no RuntimeException
		Assert.fail();
	}

	@Test
	/**
	 * Make sure that the process runs within an transaction. If not,
	 * an javax.persistence.TransactionRequiredException is raised: "no transaction is in progress"
	 */
	public void testProcessJobSuccess() throws Exception {
    	ImportJob job = createJob();
    	this.jobId = job.getId();

    	ImportProcess process = new ImportProcess(managerDao, featureProcessor, logProperties) {
			@Override
			@Transactional(propagation = Propagation.REQUIRES_NEW)
			public boolean process(ImportJob job, final JobLogger logger) {
				// Simulate here the process is writing stuff to the database by inserting a Thema.
				themaId = createThema();
				// Do NOT Simulate a RuntimeException
				return false;
			}
		};

		process.process(job, logger);

		// Assert the Job is succesfull
		Job reFoundJob = this.jobDao.getJob (this.jobId);
		Assert.assertNotNull("Job could not be found again.", reFoundJob);
		// Assert that insertion of the Thema is succeeded
		Thema refoundThema = managerDao.getThema(this.themaId);
		
		Assert.assertNotNull("The Thema could NOT be found again", refoundThema);
	}

	private ImportJob createJob() {
		ImportJob job = new ImportJob();
		job.setCreateTime(new Timestamp(System.currentTimeMillis()));
		managerDao.create(job);
//		entityManager.persist(job);
    	Assert.assertNotNull(job.getId());
		Job reFoundJob = managerDao.getJob (job.getId());
		Assert.assertNotNull("Job could not be found again.", reFoundJob);
//    	this.entityManager.flush();
		return job;
	}

	private Long createThema() {
		Thema thema = new Thema();
		thema.setNaam("dummy");
		managerDao.create(thema);
//		entityManager.persist(thema);
		// Assert the Thema is saved
		Assert.assertNotNull(thema.getId());
		// make sure Thema.naam stays unique
		thema.setNaam(""+thema.getId());
		// Make sure the Thema is committed to the database before it's reread again
		managerDao.update(thema);
		Thema reFoundthema = managerDao.getThema (thema.getId());
		Assert.assertNotNull("Thema could not be found again.", reFoundthema);
//		this.entityManager.flush();
		
		return thema.getId();
	}
}
