/**
 * 
 */
package nl.ipo.cds.executor;

import nl.idgis.commons.jobexecutor.AbstractJob;
import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.Job.Status;
import nl.idgis.commons.jobexecutor.JobCollector;
import nl.ipo.cds.dao.ManagerDao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Rob
 *
 */
public class JobCollectorImpl implements JobCollector {
	private static Log log = LogFactory.getLog(JobCollector.class);
	
	private ManagerDao managerDao;
	public JobCollectorImpl(final ManagerDao managerDao){
		this.managerDao = managerDao;		
	}
	
	@Override
	@Transactional (propagation = Propagation.REQUIRED)
	public Job nextJob() {
		log.debug("Fetching next job");
		
		AbstractJob job = managerDao.getLastJob();
		if (job==null){
			log.debug("No job");
			return null;
		}
		
		log.debug("Job found: setting status to PREPARED");
		
		job.setStatus(Status.PREPARED);
		managerDao.update(job);
		return job;
	}

}
