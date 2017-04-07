package nl.ipo.cds.dao;

import java.sql.Timestamp;

import nl.idgis.commons.jobexecutor.Job.Status;
import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.DatasetType;

public final class JobCriteria extends BaseSearchCriteria {

	private DatasetType datasetType;
	
	private Bronhouder bronhouder;
	
	/**
	 * @return the bronhouder
	 */
	public Bronhouder getBronhouder() {
		return bronhouder;
	}

	/**
	 * @param bronhouder the bronhouder to set
	 */
	public void setBronhouder(Bronhouder bronhouder) {
		this.bronhouder = bronhouder;
	}

	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * @param uuid the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	private String uuid;
	
	private Timestamp creatieTijd;
	
	private Status jobStatus;
	
	// Show by default only jobs that did something
	private Boolean verversen;

	private String parameter;
	
	public JobCriteria (DatasetType datasetType) {
		this.datasetType = datasetType;
	}
	
	public DatasetType getDatasetType() {
		return datasetType;
	}
	
	public void setDatasetType(DatasetType datasetType) {
		this.datasetType = datasetType;
	}
	
	public boolean hasDatasetType () {
		return datasetType != null;
	}

	public Timestamp getCreatieTijd() {
		return creatieTijd;
	}

	public void setCreatieTijd(Timestamp creatieTijd) {
		this.creatieTijd = creatieTijd;
	}

	public Boolean isVerversen() {
		return verversen;
	}

	public void setVerversen(Boolean verversen) {
		this.verversen = verversen;
	}

	public Status getJobStatus() {
		return jobStatus;
	}

	public void setJobStatus(Status jobStatus) {
		this.jobStatus = jobStatus;
	}

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}
	
}
