package nl.ipo.cds.executor;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobExecutor;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource(description="CDS-INSPIRE JobExecutor")
public class JobExecuterMBean {
	
	private final JobExecutor jobExecutor;	
	private final Date startTime;
	
	public JobExecuterMBean (JobExecutor jobExecutor) {
		this.jobExecutor = jobExecutor;
		this.startTime = new Date();
	}
	
	@ManagedAttribute(description="A list of IDs of running jobs")
	public List<Long> getRunningJobs() {
		final Job runningJob = jobExecutor.getRunningJob();
		
		if(runningJob == null) {
			return Collections.emptyList();
		}			
		
		return Arrays.asList(runningJob.getId());
	}
	
	@ManagedAttribute(description="The start time of this JobExecuter")
	public Date getStartTime() {
		return startTime;
	}
	
	@ManagedOperation(description="Terminate JobExecuter")
	public void terminate() {
		jobExecutor.terminate();
	}
	
	@ManagedAttribute(description="The number of processed jobs")
	public Long getProcessedJobs() { 
		return jobExecutor.getProcessedJobs();
	}

	@ManagedAttribute(description="Indicates whether this JobExecuter is terminating")
	public boolean isTerminating() {
		return jobExecutor.isTerminating();
	}
}