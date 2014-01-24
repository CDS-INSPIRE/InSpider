package nl.ipo.cds.dao;

import nl.idgis.commons.jobexecutor.AbstractJob;

public class LogInfo {

	private AbstractJob job;
	private String messageKey;
	private String messageSample;
	private long messageCount;
	
	public LogInfo (AbstractJob job, String messageKey, String messageSample, long messageCount) {
		this.job = job;
		this.messageKey = messageKey;
		this.messageSample = messageSample;
		this.messageCount = messageCount;
	}

	public AbstractJob getJob () {
		return job;
	}
	
	public String getMessageKey () {
		return messageKey;
	}
	
	public String getMessageSample () {
		return messageSample;
	}
	
	public long getMessageCount () {
		return messageCount;
	}
}
