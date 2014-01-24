/**
 * 
 */
package nl.ipo.cds.admin.ba.controller;

import nl.idgis.commons.jobexecutor.Job;
import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.Dataset;
import nl.ipo.cds.domain.DatasetType;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.domain.ImportJob;

/**
 * @author Rob
 *
 */
public class DatasetInfo {
	private static final String PROGRESS_NO_PENDING_JOB = "NO_PENDING_JOB";
	private static final String PROGRESS_WAIT_FOR_VALIDATION = "WAIT_FOR_VALIDATION";
	private static final String PROGRESS_WAIT_FOR_IMPORT = "WAIT_FOR_IMPORT";
	private static final String PROGRESS_VALIDATING = "VALIDATING";
	private static final String PROGRESS_IMPORTING = "IMPORTING";

	private static final String STATUS_IMPORT_NOT_IMPORTED = "NOT_IMPORTED";
	private static final String STATUS_IMPORT_IMPORTED = "IMPORTED";
	private static final String STATUS_IMPORT_IMPORTED_WARNINGS = "IMPORTED_WARNINGS";
	private static final String STATUS_IMPORT_TRANSFORMED = "TRANSFORMED";
	private static final String STATUS_IMPORT_TRANSFORMED_WARNINGS = "TRANSFORMED_WARNINGS";
	
	private Dataset dataset;
	private EtlJob lastCompletedJob;
	private JobInfo lastValidationJobInfo;
	private JobInfo lastImportJobInfo;
	private EtlJob lastTransformJob;
	private EtlJob pendingJob;
	private EtlJob currentJob;
	
	public DatasetInfo (Dataset dataset) {
		this.dataset = dataset;
	}

	/**
	 * @return the currentJob
	 */
	public EtlJob getCurrentJob() {
		return currentJob;
	}

	/**
	 * @param currentJob the currentJob to set
	 */
	public void setCurrentJob(EtlJob currentJob) {
		this.currentJob = currentJob;
	}

	public EtlJob getLastCompletedJob() {
		return lastCompletedJob;
	}

	public void setLastCompletedJob(EtlJob lastCompletedJob) {
		this.lastCompletedJob = lastCompletedJob;
	}

	public EtlJob getPendingJob() {
		return pendingJob;
	}

	public void setPendingJob(EtlJob pendingJob) {
		this.pendingJob = pendingJob;
	}

	public String getProgress() {
		String progress = null;
		EtlJob pendingJob = this.getPendingJob();
		if(pendingJob != null) {
			if(pendingJob.getStatus() == Job.Status.CREATED){
				if(pendingJob instanceof ImportJob){
					progress = PROGRESS_WAIT_FOR_IMPORT;
				} else {
					progress = PROGRESS_WAIT_FOR_VALIDATION;
				}
			} else {
				if(pendingJob instanceof ImportJob){
					progress = PROGRESS_IMPORTING;
				} else {
					progress = PROGRESS_VALIDATING;
				}
			}
		} else {
			progress = PROGRESS_NO_PENDING_JOB;
		}
		
		return progress; 
	}

	public EtlJob getLastTransformJob() {
		return lastTransformJob;
	}

	public void setLastTransformJob(EtlJob lastTransformJob) {
		this.lastTransformJob = lastTransformJob;
	}

	public JobInfo getLastValidationJobInfo() {
		return lastValidationJobInfo;
	}

	public void setLastValidationJobInfo(JobInfo lastValidationJobInfo) {
		this.lastValidationJobInfo = lastValidationJobInfo;
	}

	public JobInfo getLastImportJobInfo() {
		return lastImportJobInfo;
	}

	public void setLastImportJobInfo(JobInfo lastImportJobInfo) {
		this.lastImportJobInfo = lastImportJobInfo;
	}

	public Boolean isTransformed(){
		return	this.lastTransformJob != null && 
				this.lastImportJobInfo != null && 
				this.lastTransformJob.getFinishTime() != null &&
				this.lastImportJobInfo.getEindTijd() != null && 
				this.lastTransformJob.getFinishTime().after(this.lastImportJobInfo.getEindTijd());
	}
	
	public String getStatusImport(){
		String status = STATUS_IMPORT_NOT_IMPORTED;
		
		if(this.lastImportJobInfo != null){
			boolean importWarnings = this.lastImportJobInfo.getWarningCount() > 0;
			if(importWarnings){
				if(this.isTransformed()){
					status = STATUS_IMPORT_TRANSFORMED_WARNINGS;
				} else {
					status = STATUS_IMPORT_IMPORTED_WARNINGS;
				}
			} else{
				if(this.isTransformed()){
					status = STATUS_IMPORT_TRANSFORMED;
				} else {
					status = STATUS_IMPORT_IMPORTED;
				}
			}
		}
		
		return status;
	}
	
	
	
	
	/*
	 * Getters for dataset
	 */
	
	
	/**
	 * @return id
	 */
	public Long getId() {
		return dataset.getId();
	}

	/**
	 * @return datasetType
	 */
	public DatasetType getDatasetType() {
		return dataset.getDatasetType();
	}

	/**
	 * @return bronhouder
	 */
	public Bronhouder getBronhouder() {
		return dataset.getBronhouder();
	}

	/**
	 * @return uuid
	 */
	public String getUuid() {
		return dataset.getUuid();
	}

	/**
	 * @return actief
	 */
	public Boolean getActief() {
		return dataset.getActief();
	}
	
	public String getNaam() {
		return dataset.getNaam();
	}	
}