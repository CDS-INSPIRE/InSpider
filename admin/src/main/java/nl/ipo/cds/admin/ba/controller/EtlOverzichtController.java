/**
 * 
 */
package nl.ipo.cds.admin.ba.controller;

import java.security.Principal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import nl.idgis.commons.jobexecutor.AbstractJob;
import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobCreator;
import nl.idgis.commons.jobexecutor.JobLogger.LogLevel;
import nl.idgis.commons.velocity.ToolContext;
import nl.ipo.cds.admin.ba.util.GebruikerAuthorization;
import nl.ipo.cds.admin.reporting.ReportConfiguration;
import nl.ipo.cds.dao.DatasetCriteria;
import nl.ipo.cds.dao.JobCriteria;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.dao.SortOrder;
import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.Dataset;
import nl.ipo.cds.domain.DatasetType;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.domain.ImportJob;
import nl.ipo.cds.domain.Thema;
import nl.ipo.cds.domain.TransformJob;
import nl.ipo.cds.domain.TypeGebruik;
import nl.ipo.cds.domain.ValidateJob;
import nl.ipo.cds.etl.reporting.DefaultLogWriterContext;
import nl.ipo.cds.etl.reporting.LogWriterContext;
import nl.ipo.cds.etl.reporting.velocity.VelocityJobFaseLogWriter;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.velocity.VelocityConfigurer;

/**
 * @author Rob
 *
 */
@Controller
public class EtlOverzichtController {

	@Autowired
	private ManagerDao managerDao;

	@Inject
	private JobCreator jobCreator;

	@Autowired
	private VelocityConfigurer velocityConfigurer;
	
	@Autowired
	private ReportConfiguration reportConfiguration;

	@ModelAttribute("roleFunction")
	String getRoleFunction(){
		return "bronhouder";
	}

	@ModelAttribute("pgrBaseUrl")
	String getPgrBaseUrl(){
		return reportConfiguration.getPgrBaseUrl();
	}

	/**
	 * Returns the dataset types that exist for the given thema. This method
	 * operates similary to the DAO method with the same name, except that the
	 * "thema" parameter can be null. In which case an empty list is returned.
	 * 
	 * @param thema	The theme whose dataset types are returned, or null.
	 * @return		Returns the dataset types for the given theme, or an empty list of theme is null.
	 */
	private List<DatasetType> getDatasetTypesByThema (final Thema thema) {
		if (thema == null) {
			return Collections.emptyList ();
		}
		
		return this.managerDao.getDatasetTypesByThema (thema);
	}
	
	/**
	 * Datasets
	 */
	@RequestMapping(value = "/ba/etloverzicht", method = RequestMethod.GET)
	public String etlOverzicht (@RequestParam(value="bronhouderId", required=false) Long bronhouderId,
								@RequestParam(value="themaId", required=false) Long themaId,
								@RequestParam(value="datasetTypeId", required=false) Long datasetTypeId,
								@RequestParam(value="datasetStatusImport", required=false) String datasetStatusImport,
								@RequestParam(value="progress", required=false) String progress,
								Model model,
								Principal principal
							   ) {
		
		final GebruikerAuthorization gebruikerAuthorization = new GebruikerAuthorization (managerDao.getGebruiker (principal.getName ()), TypeGebruik.RAADPLEGER, managerDao);
		final Bronhouder bronhouder = gebruikerAuthorization.getAuthorizedBronhouder (bronhouderId);
		final Thema thema = gebruikerAuthorization.getAuthorizedThema (themaId);

		model.addAttribute ("bronhouders", gebruikerAuthorization.getAuthorizedBronhouders ());
		model.addAttribute ("themas", gebruikerAuthorization.getAuthorizedThemas ());
		model.addAttribute ("bronhouder", bronhouder);
		model.addAttribute ("thema", thema);
		model.addAttribute ("datasetTypes", getDatasetTypesByThema (thema));
		model.addAttribute ("themaId", thema == null ? null : thema.getId());
		model.addAttribute ("datasetTypeId", datasetTypeId);
		model.addAttribute ("datasetStatusImport", datasetStatusImport);
		model.addAttribute ("progress", progress);

		// Datasets
		Map<String, Object> datasetInfoMap = getDatasetInfoMap(datasetTypeId, thema, datasetStatusImport, progress, bronhouder);
		model.addAttribute("datasetInfoMap", datasetInfoMap);

		model.addAttribute("contextTitle", "Datasets");
		
		return "/ba/etloverzicht";
	}

	private Map<String, Object> getDatasetInfoMap(Long datasetTypeId, final Thema thema, String datasetStatusImport, String progress, Bronhouder bronhouder) {

		DatasetCriteria criteria = new DatasetCriteria(bronhouder);
		if(datasetTypeId != null){
			DatasetType datasetType = this.managerDao.getDatasetType(datasetTypeId);
			criteria.setDatasetType(datasetType);
		}
		
		if (thema != null) {
			criteria.setThema (thema);
		}
		
		List<Dataset> datasets = this.managerDao.findDataset(criteria);
		final List<DatasetInfo> datasetInfos = new ArrayList<DatasetInfo> (); 
		for (Dataset ds: datasets) {			
			datasetInfos.add (new DatasetInfo (ds));			
		}

		for (DatasetInfo di: datasetInfos) {
			final EtlJob lastValidationJob = managerDao.getLastJobThatValidated(di.getBronhouder(), di.getDatasetType(), di.getUuid());
			final EtlJob lastImportJob = managerDao.getLastSuccessfullImportJob(di.getBronhouder(), di.getDatasetType(), di.getUuid());

			JobInfo lastValidationJobInfo = null;
			if (lastValidationJob != null) {
				lastValidationJobInfo = new JobInfo(lastValidationJob);
			}
			JobInfo lastImportJobInfo = null;
			if (lastImportJob != null) {
				lastImportJobInfo = new JobInfo(lastImportJob);
			}

			if (lastImportJobInfo != null) {
				// Load last import job for each dataset (if any):
				lastImportJobInfo.setErrorCount (managerDao.getJobLogCount (lastImportJob, LogLevel.ERROR));
				lastImportJobInfo.setWarningCount (managerDao.getJobLogCount (lastImportJob, LogLevel.WARNING));
				di.setLastImportJobInfo(lastImportJobInfo);
				di.setLastTransformJob(managerDao.getLastTransformJob(Job.Status.FINISHED));
			}
			if (lastValidationJobInfo != null) {
				// Load last validation job for each dataset (if any):
				lastValidationJobInfo.setErrorCount (managerDao.getJobLogCount (lastValidationJob, LogLevel.ERROR));
				lastValidationJobInfo.setWarningCount (managerDao.getJobLogCount (lastValidationJob, LogLevel.WARNING));
				di.setLastValidationJobInfo(lastValidationJobInfo);
			}
			// Load the pending job for each dataset:
			di.setPendingJob (managerDao.getPendingJob (di.getBronhouder(), di.getDatasetType(), di.getUuid()));
		}
		
		List<DatasetInfo> filteredDatasetInfos = new ArrayList<DatasetInfo>();
		// Filtering
		int totalValidationLogCount = 0;
		for (DatasetInfo di: datasetInfos) {
			// Filter on datasetStatusImport cq valid
			if(!StringUtils.isBlank(datasetStatusImport) && !datasetStatusImport.equals(di.getStatusImport())){
				continue;
			}
			// Filter on progress
			if(!StringUtils.isBlank(progress) && !progress.equals(di.getProgress())){
				continue;
			}
			filteredDatasetInfos.add(di);
			
			// Calculate all Validation-warnings and -errors of all datasetInfos
			if(di.getLastValidationJobInfo() != null){
				totalValidationLogCount += di.getLastValidationJobInfo().getWarningCount() + di.getLastValidationJobInfo().getErrorCount();
			}
		}

		Map<String, Object> filteredDatasetInfoMap = new HashMap<String, Object>();
		filteredDatasetInfoMap.put("datasetInfos", filteredDatasetInfos);
		filteredDatasetInfoMap.put("totalValidationLogCount", totalValidationLogCount);
		return filteredDatasetInfoMap;
	}

	/**
	 * Jobs
	 */
	@RequestMapping(value = "/ba/etloverzicht/jobs", method = RequestMethod.GET)
	public String etlOverzichtJobs (@RequestParam(value="bronhouderId", required=false) Long bronhouderId,
									@RequestParam(value="themaId", required=false) Long themaId,
									@RequestParam(value="datasetTypeId", required=false) Long datasetTypeId,
									@RequestParam(value="datasetStatusImport", required=false) String datasetStatusImport,
									@RequestParam(value="progress", required=false) String progress,
									@RequestParam(value="jobStatus", required=false) String jobStatus,
									@RequestParam(value="jobrefreshed", required=false) Boolean jobrefreshed,
									Model model,
									Principal principal) {
		
		final GebruikerAuthorization gebruikerAuthorization = new GebruikerAuthorization (managerDao.getGebruiker (principal.getName ()), TypeGebruik.RAADPLEGER, managerDao);
		final Bronhouder bronhouder = gebruikerAuthorization.getAuthorizedBronhouder (bronhouderId);
		final Thema thema = gebruikerAuthorization.getAuthorizedThema (themaId);
		
		model.addAttribute ("bronhouders", gebruikerAuthorization.getAuthorizedBronhouders ());
		model.addAttribute ("themas", gebruikerAuthorization.getAuthorizedThemas ());
		model.addAttribute ("bronhouder", bronhouder);
		model.addAttribute ("thema", thema);
		model.addAttribute ("datasetTypes", getDatasetTypesByThema (thema));
		model.addAttribute ("themaId", thema == null ? null : thema.getId());
		model.addAttribute ("datasetTypeId", datasetTypeId);
		model.addAttribute ("datasetStatusImport", datasetStatusImport);
		model.addAttribute ("progress", progress);
		
		// Datasets
		Map<String, Object> datasetInfoMap = getDatasetInfoMap(datasetTypeId, thema, datasetStatusImport, progress, bronhouder);
		@SuppressWarnings("unchecked")
		List<DatasetInfo> datasetInfos = ((List<DatasetInfo>)datasetInfoMap.get("datasetInfos"));
//		Assert.isTrue(datasetInfos.size() == 1, "Expected exactly one dataset, but there are \"" + datasetInfos.size() + "\" datasets found.");
		if (datasetInfos.size() == 1){
//			model.addAttribute("dataset", datasetInfos.get(0).getDataset());
			model.addAttribute("uuid", datasetInfos.get(0).getUuid());
			model.addAttribute("datasetTypeExists", true);
		}else{
			model.addAttribute("datasetTypeExists", false);
		}
		
		model.addAttribute("datasetType", this.managerDao.getDatasetType(datasetTypeId));
		model.addAttribute("bronhouder", bronhouder);
		model.addAttribute("jobStatus", jobStatus);
		model.addAttribute("jobrefreshed", jobrefreshed);
		model.addAttribute("contextTitle", "Jobs");
		model.addAttribute("jobsMode", true);
		
		return "/ba/etloverzicht";
	}

	/**
	 * JSON datasetInfo response of all datasetInfos belonging to requested bronhouder
	 * @param bronhouderId
	 * @param datasetId
	 * @param model
	 * @param principal
	 * @return
	 */
	@RequestMapping(value = "/ba/bronhouders/{bronhouderId}/datasetInfos", method = RequestMethod.GET)
	public Map<String, Object> getDatasetInfosFromBronhouder (@PathVariable long bronhouderId,
										@RequestParam(value="datasetTypeId", required=false) Long datasetTypeId,
										@RequestParam(value="datasetStatusImport", required=false) String datasetStatusImport,
										@RequestParam(value="progress", required=false) String progress,
										Model model,
										Principal principal) {
		final GebruikerAuthorization gebruikerAuthorization = new GebruikerAuthorization (managerDao.getGebruiker (principal.getName ()), TypeGebruik.RAADPLEGER, managerDao);
		final Bronhouder bronhouder = gebruikerAuthorization.getAuthorizedBronhouder (bronhouderId);
		
		return this.getDatasetInfoMap(datasetTypeId, null, datasetStatusImport, progress, bronhouder);
	}
	
	/**
	 * JSON jobs response of all jobs belonging to requested dataset
	 * @param bronhouderId
	 * @param datasetId
	 * @param model
	 * @param principal
	 * @return
	 */
	@RequestMapping(value = "/ba/bronhouders/{bronhouderId}/datasetTypes/{datasetTypeId}/jobs", method = RequestMethod.GET)
	public List<EtlJob> getJobsFromDataset (@PathVariable long bronhouderId,
										 @PathVariable long datasetTypeId,
										 @RequestParam(value="jobStatus", required=false) String jobStatus,
										 @RequestParam(value="jobrefreshed", required=false) Boolean jobrefreshed,
										 Model model,
										 Principal principal) {
		final Bronhouder bronhouder = new GebruikerAuthorization (managerDao.getGebruiker (principal.getName ()), TypeGebruik.RAADPLEGER, managerDao)
			.getAuthorizedBronhouder (bronhouderId);
		
		DatasetType datasetType = this.managerDao.getDatasetType(datasetTypeId);
		List<Dataset> datasetsByDatasetTypeList = this.managerDao.getDatasetsByDatasetType(datasetType);
		boolean match = false;
		for (Dataset datasetT : datasetsByDatasetTypeList) {
			if (datasetT.getBronhouder().equals(bronhouder)){
				match=true;
			}
			
		}
		Assert.isTrue(match, "Requested dataset doesn't belong to requested bronhouder");

		// Jobs of bronhouder
		JobCriteria criteria = new JobCriteria(datasetType);
		criteria.setBronhouder(bronhouder);
		// Only recent job information
		Calendar dateInThePast = Calendar.getInstance();
		dateInThePast.add(Calendar.MONTH, -4);
		criteria.setCreatieTijd(new Timestamp(dateInThePast.getTimeInMillis()));
		// Filter on jobstatus
		if(!StringUtils.isBlank(jobStatus)){
			criteria.setJobStatus(Job.Status.valueOf(jobStatus));
		}

		if(jobrefreshed != null){
			criteria.setVerversen(jobrefreshed);
		}
		// Order newest first
		criteria.setSortOrder(SortOrder.DESCENDING);

		List<EtlJob> jobs = this.managerDao.findJob(criteria);
		return jobs;
	}
	
	@RequestMapping(value = "/ba/bronhouders/{bronhouderId}/validate", method = RequestMethod.GET)
	public ValidateJob doValidate (@PathVariable long bronhouderId,
							@RequestParam(value="datasetType", required=true) long datasetIdType,
							@RequestParam(value="uuid", required=true) String uuid,
							Model model,
							Principal principal
							) {
		final GebruikerAuthorization gebruikerAuthorization = new GebruikerAuthorization (managerDao.getGebruiker (principal.getName ()), TypeGebruik.DATABEHEERDER, managerDao); 
		final Bronhouder bronhouder = gebruikerAuthorization.getAuthorizedBronhouder (bronhouderId);
		
		final DatasetType datasetType = managerDao.getDatasetType (datasetIdType);
		Assert.notNull(datasetType, "DataSet with id \"" + datasetIdType + "\", could not be found.");
//		Assert.isTrue(bronhouder.equals(datasetType.getBronhouder()), "Requested dataset(" + datasetIdType + ") doesn't belong to requested bronhouder("+ bronhouderId +")");
		Assert.isTrue(managerDao.getPendingJob (bronhouder, datasetType, uuid) == null, "There is already a pending validation-job for this dataset(" + datasetIdType + ") of this bronhouder("+ bronhouderId +")");

		// Test permissions:
		if (bronhouder == null || !gebruikerAuthorization.getAuthorizedThemas (bronhouder).contains (datasetType.getThema ())) {
			return null;
		}
		
		// make validate job
		final ValidateJob job = new ValidateJob ();
		// copy properties to job
		job.setBronhouder(bronhouder);
		job.setDatasetType(datasetType);
		job.setUuid(uuid);
		job.setForceExecution(true);
		jobCreator.putJob (job);
		
		return job;
	}

	@Transactional
	@RequestMapping(value = "/ba/bronhouders/{bronhouderId}/import", method = RequestMethod.GET)
	public ImportJob doImport (@PathVariable long bronhouderId,
							@RequestParam(value="datasetType", required=true) long datasetIdType,
							@RequestParam(value="uuid", required=true) String uuid,
							Model model,
							Principal principal
							) {
		final GebruikerAuthorization gebruikerAuthorization = new GebruikerAuthorization (managerDao.getGebruiker (principal.getName ()), TypeGebruik.DATABEHEERDER, managerDao); 
		final Bronhouder bronhouder = gebruikerAuthorization.getAuthorizedBronhouder (bronhouderId);
		
		
		final DatasetType datasetType = managerDao.getDatasetType (datasetIdType);
		Assert.notNull(datasetType, "DataSet with id \"" + datasetIdType + "\", could not be found.");
//		Assert.isTrue(bronhouder.equals(datasetType.getBronhouder()), "Requested dataset(" + datasetIdType + ") doesn't belong to requested bronhouder("+ bronhouderId +")");
		Assert.isTrue(managerDao.getPendingJob (bronhouder, datasetType, uuid) == null, "There is already a pending import-job for this dataset(" + datasetIdType + ") of this bronhouder("+ bronhouderId +")");

		// Test permissions:
		if (bronhouder == null || !gebruikerAuthorization.getAuthorizedThemas (bronhouder).contains (datasetType.getThema ())) {
			return null;
		}
		
		// make import job
		final ImportJob job = new ImportJob ();
		// copy properties to job
		job.setBronhouder(bronhouder);
		job.setDatasetType(datasetType);
		job.setUuid(uuid);
		job.setForceExecution(true);		
		jobCreator.putJob (job);
		
		/* Check whether to create a transform Job, by checking if there is already a TRANSFORM job that
		 * hasn't started yet
		 */
		if(this.managerDao.getLastTransformJob(Job.Status.CREATED) == null){
			final TransformJob transformJob = new TransformJob ();
			managerDao.create (transformJob);
		}
		return job;
	}
	
	@RequestMapping (value = "/ba/bronhouders/{bronhouderId}/datasetTypes/{datasetTypeId}/jobs/{jobId}", method = RequestMethod.GET)
	public String showBronhouderDatasetJob (
			@PathVariable long bronhouderId, 
			@PathVariable long datasetTypeId, 
			@PathVariable long jobId, 
			Model model,
			Principal principal) {
		
		final Bronhouder bronhouder = new GebruikerAuthorization (managerDao.getGebruiker (principal.getName ()), TypeGebruik.RAADPLEGER, managerDao)
			.getAuthorizedBronhouder (bronhouderId);
		
		if (bronhouder == null) {
			model.asMap ().clear ();
			return "redirect:/ba/bronhouders";
		}
		
		final DatasetType datasetType = managerDao.getDatasetType (datasetTypeId);
		if (datasetType == null) {
			model.asMap ().clear ();
			return "redirect:/ba/bronhouders/" + bronhouder.getId () + "/datasetTypes";
		}
		
		final AbstractJob job = managerDao.getJob (jobId);
		if (job == null) {
			model.asMap ().clear ();
			return "redirect:/ba/bronhouders/" + bronhouder.getId () + "/datasetTypes/" + datasetType.getId () +  "/jobs";
		}
		
		// Setup the log writer. Use the velocity engine for the current web application context:
		final VelocityEngine velocityEngine = velocityConfigurer.getVelocityEngine ();
		final VelocityJobFaseLogWriter writer = new VelocityJobFaseLogWriter (velocityEngine);
		
		writer.setDefaultContext ("html");
		writer.setDefaultEncoding ("UTF-8");
		writer.setDefaultTemplate ("default");
		writer.setTemplatePath ("ba/joblog");
		writer.setContextClass (ToolContext.class);
		
		// Create a job log context:
		final LogWriterContext writerContext = new DefaultLogWriterContext (managerDao.findJobLog(job));
		final long errorCount = managerDao.getJobLogCount (job, LogLevel.ERROR);
		final long warningCount = managerDao.getJobLogCount (job, LogLevel.WARNING);
		
		model.addAttribute ("bronhouder", bronhouder);
		model.addAttribute ("datasetType", datasetType);
		model.addAttribute ("job", job);
		model.addAttribute ("lastJob", managerDao.getLastSuccessfullImportJob ((EtlJob)job));
		model.addAttribute ("writer", writer);
		model.addAttribute ("writerContext", writerContext);
		model.addAttribute ("pgrBaseUrl", reportConfiguration.getPgrBaseUrl ());
		model.addAttribute ("messageCount", errorCount + warningCount);
		model.addAttribute ("errorCount", errorCount);
		model.addAttribute ("warningCount", warningCount);
		
		return "/ba/jobdetails";
	}
}
