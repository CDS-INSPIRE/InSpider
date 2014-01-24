package nl.ipo.cds.etl.process;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import junit.framework.Assert;
import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobDao;
import nl.idgis.commons.jobexecutor.JobLogger;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.DatasetType;
import nl.ipo.cds.domain.RemoveJob;
import nl.ipo.cds.domain.Thema;
import nl.ipo.cds.etl.FeatureProcessor;
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

@ContextConfiguration(classes = RemoveProcessTest.Config.class)
/**
 * NOT extending AbstractTransactionalJUnit4SpringContextTests to be able to test transactionality
 */
public class RemoveProcessTest extends AbstractTransactionalJUnit4SpringContextTests {
	
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
	private DataSource dataSource;
	
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
		Assert.assertNotNull(this.dataSource);
	}
	

	@Test
	/**
	 * Make sure that the process runs within an transaction. If not,
	 * an javax.persistence.TransactionRequiredException is raised: "no transaction is in progress"
	 */
	public void testProcessJobSuccess() throws Exception {
    	RemoveJob job = createJob();
    	this.jobId = job.getId();

    	RemoveProcess process = new RemoveProcess(dataSource);

    	JobLogger  logger = new JobLogger () {
			@Override
			public void logString(Job job, String key, LogLevel logLevel, String message) {
			}

			@Override
			public void logString(Job job, String key, LogLevel logLevel, String message, Map<String,Object> context) {
			}
		};
		
		process.process(job, logger);

		// Assert the Job is succesfull
		Job reFoundJob = this.jobDao.getJob (this.jobId);
		Assert.assertNotNull("Job could not be found again.", reFoundJob);
	}

	private RemoveJob createJob() {
		RemoveJob job = new RemoveJob();
		job.setCreateTime(new Timestamp(System.currentTimeMillis()));
		Bronhouder bh = new Bronhouder();
		managerDao.create(bh);
		job.setBronhouder(bh);
		DatasetType dt = new DatasetType();
		managerDao.create(dt);
		job.setDatasetType(dt);
		job.setUuid("uuid");
		managerDao.create(job);
//		entityManager.persist(job);
    	Assert.assertNotNull(job.getId());
		Job reFoundJob = managerDao.getJob (job.getId());
		Assert.assertNotNull("Job could not be found again.", reFoundJob);
//    	this.entityManager.flush();
		return job;
	}

}
