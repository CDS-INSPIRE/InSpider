/**
 * 
 */
package nl.ipo.cds.admin.ba.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import nl.idgis.commons.jobexecutor.AbstractJob;
import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobLogger.LogLevel;
import nl.ipo.cds.admin.reporting.ReportConfiguration;
import nl.idgis.commons.velocity.ToolContext;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.DatasetType;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.etl.reporting.DefaultLogWriterContext;
import nl.ipo.cds.etl.reporting.LogWriterContext;
import nl.ipo.cds.etl.reporting.velocity.VelocityJobFaseLogWriter;
import nl.ipo.cds.nagios.NagiosStatusService;
import nl.ipo.cds.nagios.ast.HostStatusNode;
import nl.ipo.cds.nagios.ast.ServiceStatusNode;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.velocity.VelocityConfigurer;

/**
 * @author Rob
 *
 */
@Controller
@RequestMapping("/ba/monitoring")
public class MonitoringController {

	private int statusData = 3;
	private int statusServices = 3;
	
	// service statuses 0,1,2,3 stand for OK, WARNING, CRITICAL and UNKNOWN respectively 
	private static String [] statusColor = new String[]{"GREEN","YELLOW","RED","RED"};
	private static String [] statusString = new String[]{"OK","WARNING","CRITICAL","UNKNOWN"};
	
	@Autowired
	private VelocityConfigurer velocityConfigurer;
	
	@Autowired
	private ReportConfiguration reportConfiguration;
	
	@Autowired
	private MonitoringConfiguration monitoringConfiguration;

	@Autowired
	private ManagerDao managerDao;

	@Autowired
	private NagiosStatusService nagiosStatusService;

	@ModelAttribute("roleFunction")
	String getRoleFunction(){
		return "beheerder";
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public String index (Model model) {
		System.err.println("monitoring");
		
		model.addAttribute("StatusData", getStatusLight(statusData));
		model.addAttribute("StatusServices", getStatusLight(statusServices));
		
		String statusDataTitle = 
			"Aborted ETL Jobs: UNKNOWN, ETL Job Age: UNKNOWN, Service jars: UNKNOWN, Database replication: UNKNOWN";
		model.addAttribute("StatusDataTitle", statusDataTitle);
		String statusServicesTitle = 
			"WFS Availability: UNKNOWN, WMS Availability: UNKNOWN, WMS Performance: UNKNOWN";
		model.addAttribute("StatusServicesTitle", statusServicesTitle);

		// Build a list of awstats service urls and names
		List<AWstats> awstatsList = new ArrayList<AWstats>();
		String[] awstatsUrls  = monitoringConfiguration.getAwstatsUrls();
		String[] awstatsNames = monitoringConfiguration.getAwstatsNames();
		for (int i=0;i<Math.min(awstatsUrls.length, awstatsNames.length);i++){
			AWstats awstats = new AWstats(awstatsUrls[i], awstatsNames[i]);
			awstatsList.add(awstats);
			System.err.println(awstats);
		}
		                                            
		model.addAttribute("nagiosService", monitoringConfiguration.getNagiosUrl());
		model.addAttribute("muninService", monitoringConfiguration.getMuninUrl());
		model.addAttribute("awstatsServices", awstatsList);
		model.addAttribute("nagiosHosts", monitoringConfiguration.getNagiosHosts());

		// add nagiosCDSHost for WFS/ WMS availability. 
		// For dev there is only one host, for prod there will be three
		String[] nagiosHosts = monitoringConfiguration.getNagiosHosts();
		switch (nagiosHosts.length) {
		case 1:
			model.addAttribute("nagiosCDSHost", nagiosHosts[0]);
			break;
		case 3:
			model.addAttribute("nagiosCDSHost", nagiosHosts[2]);
			break;
		default: 
			model.addAttribute("nagiosCDSHost", "");
			break;
		}
		
		model.addAttribute("nagiosHostgroup", monitoringConfiguration.getNagiosHostgroup());
		return "/ba/monitoring";
	}

	private String getStatusLight(int status) {
		//statuses 0,1,2,3 stand for OK, WARNING, CRITICAL and UNKNOWN respectively 
		return statusColor[(status % 4)];
	}

	private String getStatusString(int status) {
		//statuses 0,1,2,3 stand for OK, WARNING, CRITICAL and UNKNOWN respectively 
		return statusString[(status % 4)];
	}

	@RequestMapping(value ="getTrafficLights/", method = RequestMethod.GET)
	public void getTrafficLight (Model model) {
		String nagiosHostDb, nagiosHostEtl,nagiosHostCds;
		String[] nagiosHosts;
		ServiceStatusNode hostServiceAbortedEtlJobs 		= null;
		ServiceStatusNode hostServiceEtlJobAge 				= null;
		ServiceStatusNode hostServiceServiceJars 			= null;
		ServiceStatusNode hostServiceWfsAvailability 		= null;
		ServiceStatusNode hostServiceWmsAvailability 		= null;
		ServiceStatusNode hostServiceWmsPerformance 		= null;
		nagiosHosts = monitoringConfiguration.getNagiosHosts();
		
		switch (nagiosHosts.length) {
		case 1:
			nagiosHostDb = nagiosHosts[0];
			nagiosHostEtl = nagiosHosts[0];
			nagiosHostCds = nagiosHosts[0];
			break;
		case 3:
			nagiosHostDb = nagiosHosts[0];
			nagiosHostEtl = nagiosHosts[1];
			nagiosHostCds = nagiosHosts[2];
			break;
		default: // This should not happen, will give nullpointer exceptions
					// below
			nagiosHostDb = "";
			nagiosHostEtl = "";
			nagiosHostCds = "";
			break;
		}
		
		// the array nagiosHosts should be cleared and have only one valu nagiosHostsCds
		
		
		HostStatusNode hostAStatus = nagiosStatusService.getHostStatus (nagiosHostDb);
		hostServiceAbortedEtlJobs = nagiosStatusService.getServiceStatus (nagiosHostDb, "Aborted ETL jobs");    
		hostServiceEtlJobAge = nagiosStatusService.getServiceStatus (nagiosHostDb, "ETL Job age");
		hostServiceServiceJars 	= nagiosStatusService.getServiceStatus (nagiosHostEtl, "Service JARS");
		hostServiceWfsAvailability = nagiosStatusService.getServiceStatus (nagiosHostCds, "WFS availability");     
		hostServiceWmsAvailability = nagiosStatusService.getServiceStatus (nagiosHostCds, "WMS availability");     
		hostServiceWmsPerformance = nagiosStatusService.getServiceStatus (nagiosHostCds, "WMS performance");      
		
		//		System.out.println("Hosts:    [" + nagiosStatusService.getAvailableHosts() + "]");
//		System.out.println("Services: [" + nagiosStatusService.getAvailableServices() + "]");

		/** Bepaal de 'traffic light' status
		 *  Voor elk onderdeel het gemiddelde van de score per host
		 *  Eind score is het maximum van de afzonderlijke scores
		 */
		// status Data
		int serviceAbortedEtlJobs 		= hostServiceAbortedEtlJobs.getCurrentState();
		int serviceEtlJobAge 			= hostServiceEtlJobAge.getCurrentState();
		int serviceServiceJars 			= hostServiceServiceJars.getCurrentState() ;
	//	int serviceDatabaseReplication 	= Math.max(hostAServiceDatabaseReplication.getCurrentState(), hostBServiceDatabaseReplication.getCurrentState());
		statusData =  Math.max(Math.max(serviceAbortedEtlJobs, serviceEtlJobAge), serviceServiceJars);
		model.addAttribute("StatusData", getStatusLight(statusData));
		String statusDataTitle = "Aborted ETL Jobs: " + getStatusString(serviceAbortedEtlJobs) + 
			", ETL Job Age: " 			+ getStatusString(serviceEtlJobAge) + 
			", Service jars: " 			+ getStatusString(serviceServiceJars) ;
			
		model.addAttribute("StatusDataTitle", statusDataTitle);

		// status Services
		int serviceWfsAvailabilityStatus = hostServiceWfsAvailability.getCurrentState();
		int serviceWmsAvailabilityStatus = hostServiceWmsAvailability.getCurrentState() ;
		int serviceWmsPerformanceStatus  = hostServiceWmsPerformance.getCurrentState();
		statusServices = Math.max(Math.max(serviceWfsAvailabilityStatus, serviceWmsAvailabilityStatus), serviceWmsPerformanceStatus);
		model.addAttribute("StatusServices", getStatusLight(statusServices));
		String statusServicesTitle = 
			"WFS Availability status: " 	+ getStatusString(serviceWfsAvailabilityStatus) + 
			", WMS Availability status: " 	+ getStatusString(serviceWmsAvailabilityStatus) + 
			", WMS Performance status: " 	+ getStatusString(serviceWmsPerformanceStatus);
		model.addAttribute("StatusServicesTitle", statusServicesTitle);
		
	}
	
	@RequestMapping("status/{hostName}")
	@ResponseBody
	public HostStatusNode hostStatusAction (final @PathVariable String hostName) {
		return nagiosStatusService.getHostStatus (hostName);
	}
	
	@RequestMapping ("status/{hostName}/{serviceDescription}")
	@ResponseBody
	public ServiceStatusNode serviceStatusAction (final @PathVariable String hostName, final @PathVariable String serviceDescription) {
		return nagiosStatusService.getServiceStatus (hostName, serviceDescription);
	}

	@RequestMapping ("jobs/")
	@ResponseBody
	public List<EtlJob> getJobs() {
		List<EtlJob> jobs = this.managerDao.getJobsByStatus(Job.Status.ABORTED);
		jobs.addAll(this.managerDao.getJobsByStatus(Job.Status.STARTED));
		jobs.addAll(this.managerDao.getJobsByStatus(Job.Status.CREATED));
		return jobs;
	}	
	
	@RequestMapping (value = "jobs/{jobId}", method = RequestMethod.GET)
	public String showBronhouderDatasetJob (
			@PathVariable long jobId, 
			Model model,
			Principal principal) {
		final AbstractJob abstractJob = managerDao.getJob (jobId);

		if (abstractJob == null || !(abstractJob instanceof EtlJob)) {
			model.asMap ().clear ();
			return "redirect:/ba/monitoring";
		}
		
		final EtlJob job = (EtlJob)abstractJob;
		final Bronhouder bronhouder = job.getBronhouder();
		
		final DatasetType datasetType = job.getDatasetType();
		
		// Setup the log writer. Use the velocity engine for the current web application context:
		final VelocityEngine velocityEngine = velocityConfigurer.getVelocityEngine ();
		final VelocityJobFaseLogWriter writer = new VelocityJobFaseLogWriter (velocityEngine);
		
		writer.setDefaultContext ("html");
		writer.setDefaultEncoding ("UTF-8");
		writer.setDefaultTemplate ("default");
		writer.setTemplatePath ("ba/joblog");
		writer.setContextClass (ToolContext.class);
		
		final LogWriterContext writerContext = new DefaultLogWriterContext (managerDao.findJobLog(job));

		final long errorCount = managerDao.getJobLogCount (job, LogLevel.ERROR);
		final long warningCount = managerDao.getJobLogCount (job, LogLevel.WARNING);
		
		model.addAttribute ("bronhouder", bronhouder);
		model.addAttribute ("datasetType", datasetType);
		model.addAttribute ("job", job);
		model.addAttribute ("lastJob", managerDao.getLastSuccessfullImportJob (job));
		model.addAttribute ("writer", writer);
		model.addAttribute ("writerContext", writerContext);
		model.addAttribute ("pgrBaseUrl", reportConfiguration.getPgrBaseUrl ());
		model.addAttribute ("messageCount", errorCount + warningCount);
		model.addAttribute ("errorCount", errorCount);
		model.addAttribute ("warningCount", warningCount);
		
		return "/ba/jobdetails";
	}

}

