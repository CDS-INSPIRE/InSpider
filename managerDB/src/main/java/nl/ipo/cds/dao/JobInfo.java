package nl.ipo.cds.dao;

import nl.idgis.commons.jobexecutor.AbstractJob;

public class JobInfo {
	private AbstractJob		job;
	private long	logMessageCount;
	
	public JobInfo (AbstractJob job, long logMessageCount) {
		this.job = job;
		this.logMessageCount = logMessageCount;
	}

	public AbstractJob getJob() {
		return job;
	}

	public long getLogMessageCount() {
		return logMessageCount;
	}
}
