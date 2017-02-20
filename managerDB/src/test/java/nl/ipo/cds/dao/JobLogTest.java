package nl.ipo.cds.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobLogger;
import nl.idgis.commons.jobexecutor.JobLogger.LogLevel;
import nl.ipo.cds.categories.IntegrationTests;
import nl.ipo.cds.dao.impl.JobDaoImpl;
import nl.ipo.cds.domain.ImportJob;
import nl.ipo.cds.domain.JobLog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:/nl/ipo/cds/dao/dao-applicationContext.xml",
	"classpath:/nl/ipo/cds/dao/dataSource-applicationContext.xml",
	"classpath:/nl/ipo/cds/context/propertyConfigurer-test.xml" })
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Category(IntegrationTests.class)
public class JobLogTest  {
	private static final Log logger = LogFactory.getLog(JobLogTest.class); // developer log
	
    @PersistenceContext(unitName = "cds")
    private EntityManager entityManager;

    @Inject
    private JobDaoImpl jobDao;

    @Inject
	private ManagerDao managerDao;
    
    private Job currentJob;
    
    public void createJob()  {
    	currentJob = new ImportJob ();
    	jobDao.create (currentJob);
    	logger.debug("\t### after create job.id: " + currentJob.getId());
    	assertNotNull (currentJob.getId ());
    }
    
//    @After
//    public void deleteJob() throws Exception {
//    	if (currentJob!=null) 
//    		jobDao.delete(currentJob);
//    }
    
    @Test
    @Transactional
    public void testPutLogItem () {
    	createJob();
    	
    	String contextStr = "{\"GMLID\":31415269,\"X\":0.0,\"Y\":1.0}";// JSON coding of gml_id value
		JobLog joblog = jobDao.createLogItem (currentJob, "Hello, world!", "A", LogLevel.ERROR, contextStr);
		managerDao.create(joblog);
    	
    	final JobLog log = entityManager
    		.createQuery ("from JobLog as log where log.job = ?1", JobLog.class)
    		.setParameter (1, currentJob)
    		.getSingleResult ();
    	
    	assertNotNull (log);
    	assertEquals (currentJob.getId (), log.getJob ().getId ());
    	assertEquals ("Hello, world!", log.getMessage ());
    	assertEquals ("A", log.getKey ());
    	assertEquals (LogLevel.ERROR, log.getLogLevel ());
		System.out.println(String.format ("\tCtx=%s", log.getContext()));
    	assertTrue(log.getContext ().contains("31415269"));
    }

    @Test
    @Transactional
    public void testLogitemEmpty(){
    	createJob();
    	try {
    		JobLog joblog = jobDao.createLogItem (currentJob, "MSG", "KEY", JobLogger.LogLevel.WARNING, "");    	
    		managerDao.create(joblog);
		} catch (Exception e) {
			e.printStackTrace();
	    	fail ();
		}    	
    }
    
    @Test
    @Transactional
    public void testLogitemNull(){
    	createJob();
    	try {
    		JobLog joblog = jobDao.createLogItem (currentJob, "MSG", "KEY", JobLogger.LogLevel.ERROR, null);
    		managerDao.create(joblog);
		} catch (Exception e) {
			e.printStackTrace();
	    	fail ();
		}    	
    }
    
@Test
public void test(){
	
}

}
