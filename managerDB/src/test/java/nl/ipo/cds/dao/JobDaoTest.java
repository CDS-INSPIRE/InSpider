package nl.ipo.cds.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobDao;
import nl.ipo.cds.categories.IntegrationTests;
import nl.ipo.cds.domain.Dataset;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.domain.ImportJob;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(defaultRollback = true)
@Category(IntegrationTests.class)
public class JobDaoTest extends BaseManagerDaoTest {

    @PersistenceContext(unitName = "cds")
    private EntityManager entityManager;

    @Inject
    private JobDao jobDao;
    
    /**
     * build up a database with base tables to be used in the testcases
     * @throws Exception 
     */
    @Before
    public void buildDB() throws Exception {
    	super.buildDB ();
    }
    
    private <T extends EtlJob> T createJob (final T job) {
    	final Dataset dataset = managerDao.getAllDatasets().get(0);
    	
    	job.setBronhouder (dataset.getBronhouder ());
    	job.setDatasetType (dataset.getDatasetType ());
    	job.setUuid (dataset.getUuid ());
    	
    	return job;
    }
    
    @Test
    public void testCreate () {
    	final ImportJob importJob = createJob (new ImportJob ());
    	
    	jobDao.create (importJob);
    	
    	assertNotNull (importJob.getId ());
    	
    	assertNotNull (entityManager.getReference (ImportJob.class, importJob.getId ()));
    }
    
    @Test
    public void testGetJob () {
    	final ImportJob importJob = createJob (new ImportJob ());
    	jobDao.create (importJob);
    	
    	final Job fetchedJob = jobDao.getJob (importJob.getId ());
    	
    	assertNotNull (fetchedJob);
    	assertEquals (importJob.getId (), fetchedJob.getId ());
    	
    }
    
    @Test
    public void testUpdate () {
    	final ImportJob importJob = createJob (new ImportJob ());
    	importJob.setVerversen (true);
    	jobDao.create (importJob);
    	
    	final Job fetchedJob = jobDao.getJob (importJob.getId ());
    	
    	assertTrue (((ImportJob)fetchedJob).getVerversen ());

    	((ImportJob)fetchedJob).setVerversen (false);
    	
    	jobDao.update (fetchedJob);
    	
    	assertFalse (((ImportJob)jobDao.getJob (importJob.getId ())).getVerversen ());
    }
    
    @Test
    public void testDelete () {
    	final ImportJob importJob = createJob (new ImportJob ());
    	jobDao.create (importJob);
    	
    	final Job fetchedJob = jobDao.getJob (importJob.getId ());
    	
    	assertNotNull (fetchedJob);
    	
    	jobDao.delete (fetchedJob);
    	
    	assertNull (jobDao.getJob (importJob.getId ()));
    	try {
    		entityManager.getReference (EtlJob.class, importJob.getId ());
    	} catch (JpaObjectRetrievalFailureException e) {
    		return;
    	} catch (EntityNotFoundException e) {
    		return;
    	}
    	
    	fail ();
    }

}
