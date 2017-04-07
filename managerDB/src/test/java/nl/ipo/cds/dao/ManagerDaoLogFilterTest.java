package nl.ipo.cds.dao;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.List;

import nl.ipo.cds.categories.IntegrationTests;
import nl.ipo.cds.domain.JobLog;
import nl.ipo.cds.domain.ValidateJob;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTests.class)
public class ManagerDaoLogFilterTest extends BaseManagerDaoTest {

	private static String[] logMessages = {
		"Message 1",
		"Message 2",
		"Message 3",
		"Message 4"
	};
	
	protected ValidateJob job;
	protected ValidateJob emptyJob;
	
	@Override @Before
	public void buildDB () throws Exception {
		super.buildDB ();
		
    	job = new ValidateJob();
    	managerDao.create(job);
    	
    	emptyJob = new ValidateJob ();
    	managerDao.create (emptyJob);
    	
    	for (String message: logMessages) {
    		JobLog log = new JobLog ();
    		log.setJob (job);
    		log.setMessage (message);
        	managerDao.create(log);
    	}
    	
    	entityManager.flush();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testFindJobFaseLogForJobInvalidCriteria () throws Exception {
		final JobLogCriteria criteria = new JobLogCriteria (job);
		
		criteria.setJob (null);
		
		managerDao.findJobLog (criteria);
	}
	
	public @Test void testFindJobLogForJob () throws Exception {
		
		final List<JobLog> logItems = managerDao.findJobLog (job);
		
		assertNotNull ("Must return a list", logItems);
		assertEquals ("Job must have " + logMessages.length + " messages.", logMessages.length, logItems.size ());

		for (int i = 0; i < logMessages.length; ++ i) {
			assertEquals (logMessages[i], logItems.get (i).getMessage ());
		}
	}
	
	public @Test void testFindJobLogForJobEmpty () throws Exception {
		final List<JobLog> logItems = managerDao.findJobLog (emptyJob);
		
		assertNotNull ("Must return a list", logItems);
		assertTrue ("Must return an empty list", logItems.isEmpty ());
	}
	
	public @Test void testFindJobLogOrder () throws Exception {
		final JobLogCriteria criteria = new JobLogCriteria (job);
		
		criteria.setSortField (JobLogField.MESSAGE);
		criteria.setSortOrder (SortOrder.DESCENDING);
		
		final List<JobLog> logItems = managerDao.findJobLog (criteria);
		
		assertNotNull (logItems);
		assertEquals (logMessages.length, logItems.size ());
		
		for (int i = 0; i < logMessages.length; ++ i) {
			assertEquals (logMessages[logMessages.length - 1 - i], logItems.get(i).getMessage ());
		}
	}
	
	public @Test void testFindJobFaseLogLimit () throws Exception {
		
		final JobLogCriteria criteria = new JobLogCriteria (job);
		
		criteria.setOffset (1);
		criteria.setLimit (2);
		
		final List<JobLog> logItems = managerDao.findJobLog (criteria);
		
		assertNotNull (logItems);
		assertEquals (2, logItems.size ());
		
		assertEquals (logMessages[1], logItems.get (0).getMessage ());
		assertEquals (logMessages[2], logItems.get (1).getMessage ());
	}
}
