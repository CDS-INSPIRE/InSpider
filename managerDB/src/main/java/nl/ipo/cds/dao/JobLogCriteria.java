package nl.ipo.cds.dao;

import nl.idgis.commons.jobexecutor.AbstractJob;

public final class JobLogCriteria extends BaseSearchCriteria {

	private AbstractJob job;
	
	public JobLogCriteria (AbstractJob job) {
		this.job = job;
	}
	
	public AbstractJob getJob() {
		return job;
	}
	
	public void setJob(AbstractJob job) {
		this.job = job;
	}
	
	public boolean hasJob () {
		return job != null;
	}
}
