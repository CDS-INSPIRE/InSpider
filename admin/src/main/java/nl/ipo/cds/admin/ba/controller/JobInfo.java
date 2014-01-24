package nl.ipo.cds.admin.ba.controller;

import java.sql.Timestamp;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobTypeIntrospector;
import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.DatasetType;
import nl.ipo.cds.domain.EtlJob;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class JobInfo /*extends EtlJob*/ {

	private EtlJob job;
	private long warningCount;
	private long errorCount;
	
	public JobInfo(EtlJob job) {
		this.job = job;
	}

	public boolean equals(Object obj) {
	       if (obj == null) {
	            return false;
	        }

	        if (this == obj) {
	            return true;
	        }

	        if (!(obj instanceof JobInfo)) {
	            return false;
	        }

	        JobInfo jobInfo = (JobInfo) obj;

	        return new EqualsBuilder().append(this.getId(), jobInfo.getId()).isEquals();
	}

	public Object get(String value){
		return null;
	}

	public int hashCode() {
	      return new HashCodeBuilder(17, 37)  
	         .append(job.getId())  
	         .toHashCode();
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

	public Timestamp getCreatieTijd() {
		return job.getCreateTime();
	}

	public Bronhouder getBronhouder() {
		return job.getBronhouder();
	}

	public DatasetType getDatasetType() {
		return job.getDatasetType();
	}

	public String getUuid() {
		return job.getUuid();
	}


	public String getDatasetUrl() {
		return job.getDatasetUrl();
	}

	public Timestamp getEindTijd() {
		return job.getFinishTime();
	}

	public Integer getFeatureCount() {
		return job.getFeatureCount();
	}

	public Long getId() {
		return job.getId();
	}

	public String getJobType() {
		return JobTypeIntrospector.getJobTypeName (job);
	}

	public Timestamp getMetadataUpdateDatum() {
		return job.getMetadataUpdateDatum();
	}

	public String getWfsUrl() {
		return job.getWfsUrl();
	}

	public Integer getPrioriteit() {
		return job.getPriority();
	}

	public String getResultaat() {
		return job.getResult();
	}

	public Timestamp getStartTijd() {
		return job.getStartTime();
	}

	public Job.Status getStatus() {
		return job.getStatus();
	}

	public Boolean getVerversen() {
		return job.getVerversen();
	}

	public long getWarningCount() {
		return this.warningCount;
	}

	public void setWarningCount(long warningCount) {
		this.warningCount = warningCount;
	}

	public long getErrorCount() {
		return this.errorCount;
	}

	public void setErrorCount(long errorCount) {
		this.errorCount = errorCount;
	}

}
