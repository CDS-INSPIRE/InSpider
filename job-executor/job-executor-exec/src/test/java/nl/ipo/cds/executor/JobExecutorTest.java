package nl.ipo.cds.executor;

import junit.framework.Assert;
import nl.ipo.cds.categories.IntegrationTests;
import nl.ipo.cds.dao.ManagerDao;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

@ContextConfiguration (classes = JobExecutorTest.Config.class)
@Category(IntegrationTests.class)
public class JobExecutorTest extends AbstractTransactionalJUnit4SpringContextTests{

	@Autowired
	private CdsJobExecuter jobExecuter;
	
	@Autowired
	private ManagerDao managerDao;
	
	@Configuration
	@ComponentScan (basePackageClasses = nl.ipo.cds.executor.config.Package.class)
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
	
	@Before
	public void testInitialization(){
		Assert.assertNotNull(jobExecuter);
	}
	
	@Test
	public void test () {
	}
	
	/*
	@Test
	public void testProcessSuccessfullJob() {
    	CdsJob job = createJob();
    	
    	Map<String, Process<?>> processes = new HashMap<String, Process<?>>();
    	ImportProcess testProcess = new ImportProcess (managerDao, null, null, null) {
			@Override
			public boolean process(final ImportJob job, final JobLogger logger) {
				// Do NOT Simulate a RuntimeException
				return true;
			}
		};
		
    	processes.put(job.getJobType().getNaam(), testProcess);
    	jobExecuter.setProcesses(processes);
    	jobExecuter.processJob(job);
    	
    	Assert.assertEquals(Job.CdsJob.FINISHED, job.getStatus());
	}

	@Test
	public void testProcessExceptionJob() {
    	CdsJob job = createJob();
    	
    	Map<String, Process> processes = new HashMap<String, Process>();
    	Process testProcess = new AbstractProcess() {
			@Override
			public boolean executeJob(CdsJob job) {
				// Do NOT Simulate a RuntimeException
				throw new RuntimeException("Forced Exception");
			}
		};
		
    	processes.put(job.getJobType().getNaam(), testProcess);
    	jobExecuter.setProcesses(processes);
    	jobExecuter.processJob(job);
    	
    	Assert.assertEquals(Job.CdsJob.ABORTED, job.getStatus());
	}

	private CdsJob createJob() {
		CdsJob job = new CdsJob();
    	job.setJobType(createJobType());
		return job;
	}

	private CdsJobType createJobType() {
		return this.managerDao.getJobTypeByName("VALIDATE");
	}
	*/
}
