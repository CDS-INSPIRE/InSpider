package nl.ipo.cds.etl;

import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.Properties;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.Job.Status;
import nl.idgis.commons.jobexecutor.JobLogger.LogLevel;
import nl.idgis.commons.jobexecutor.JobMail;
import nl.idgis.commons.jobexecutor.JobTypeIntrospector;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.Dataset;
import nl.ipo.cds.domain.EtlJob;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public class EtlJobMail implements JobMail {
	
	private static final Log technicalLog = LogFactory.getLog(EtlJobMail.class);

	private String smtpHost, from, subject, host, hostProto;
	private int smtpPort;
	
	private ManagerDao managerDao;
	
	protected String createMsg(EtlJob job) {
		Properties velocityProperties = new Properties();
		velocityProperties.put("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");
		velocityProperties.put("resource.loader", "class");
		velocityProperties.put("class.resource.loader.class", 
			"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		
		VelocityEngine velocityEngine = new VelocityEngine(velocityProperties);		
		velocityEngine.init();
		
		Template template = velocityEngine.getTemplate("nl/ipo/cds/etl/mail.vm");
		
		VelocityContext context = new VelocityContext();
		context.put("job", job);
		context.put("host", host);
		context.put("hostProto", hostProto);
		
		StringWriter writer = new StringWriter();
        template.merge(context, writer);
		
		return writer.toString();
	}

	protected String createSuccessMsg(EtlJob job) {
		String emailTeksten = job.getDatasetType().getThema().getEmailteksten();
		
		if(emailTeksten.contains("[DATUM/TIJD]")){
			final Timestamp now = new Timestamp(new java.util.Date().getTime());
			emailTeksten = emailTeksten.replace("[DATUM/TIJD]", now.toString());
		}
		if(emailTeksten.contains("[NAAM-DATASET]")){
			Dataset dataset = managerDao.getDataset(job.getBronhouder(), job.getDatasetType(), job.getUuid());
			emailTeksten = emailTeksten.replace("[NAAM-DATASET]", dataset.getNaam());  
		}
		if(emailTeksten.contains("[THEMA]")){
			emailTeksten = emailTeksten.replace("[THEMA]", job.getDatasetType().getThema().getNaam());
		}
		if(emailTeksten.contains("[TYPE]")){
			emailTeksten = emailTeksten.replace("[TYPE]", job.getDatasetType().getNaam());
		}
		if(emailTeksten.contains("[BRONHOUDER]")){
			emailTeksten = emailTeksten.replace("[BRONHOUDER]", job.getBronhouder().getNaam());
		}
		if(emailTeksten.contains("[NAW]")){
			Bronhouder bronhouder = job.getBronhouder();
			String naw = ""; 
			if(bronhouder.getContactAdres() != null && !bronhouder.getContactAdres().isEmpty()){
				naw =  naw + bronhouder.getContactAdres();
			}
			if(bronhouder.getContactPostcode() != null && !bronhouder.getContactPostcode().isEmpty()){
				naw =  naw + ", " + bronhouder.getContactPostcode();
			}
			if(bronhouder.getContactPlaats() != null && !bronhouder.getContactPlaats().isEmpty()){
				naw =  naw + ", " + bronhouder.getContactPlaats();
			}
			if(bronhouder.getContactTelefoonnummer() != null && !bronhouder.getContactTelefoonnummer().isEmpty()){
				naw =  naw + ", tel:" + bronhouder.getContactTelefoonnummer();
			}
			emailTeksten = emailTeksten.replace("[NAW]", naw);
		}
		if(emailTeksten.contains("[URL]")){
			String url = "" + hostProto + "://"  + host + "/admin/ba/bronhouders/" + job.getBronhouder().getId() + "/datasetTypes/" + job.getDatasetType().getId() + "/jobs/" + job.getId();
			emailTeksten = emailTeksten.replace("[URL]", url);
		}
		return emailTeksten;
	}
	@Override
	public void sendMail(Job originalJob) throws EmailException {
		if (!(originalJob instanceof EtlJob)) {
			return;
		}
		
		final EtlJob job = (EtlJob)originalJob;
		if(job.getStatus() == Status.ABORTED || managerDao.getJobLogCount(job, LogLevel.ERROR) > 0) {
			technicalLog.debug("Job is aborted or resulted in errors, trying to send e-mail");
			
			String jobtype = JobTypeIntrospector.getJobTypeName (job);
			if (!jobtype.equals("TRANSFORM")) {
				Bronhouder bronhouder = job.getBronhouder();
				if(bronhouder != null) {					
					String address = bronhouder.getContactEmailadres();
					String extraAddress = bronhouder.getContactExtraEmailadres();
					
					Email mail = new SimpleEmail();
					mail.setHostName(smtpHost);
					mail.setSmtpPort(smtpPort);
					mail.setFrom(from);
					mail.setSubject(subject);
					mail.setMsg(createMsg(job));					
					mail.addTo(address);
					if(extraAddress != null && !extraAddress.isEmpty()){
						mail.addTo(extraAddress);
					}
					mail.send();
					
					technicalLog.debug("E-mail sent to: " + address);
				} else {
					technicalLog.error("Couldn't send e-mail, no bronhouder found");
				}
			} else {
				// happens for transform jobs
				technicalLog.debug("Couldn't send e-mail, transform job");
			}
		} else {
			technicalLog.debug("Job is not aborted and doesn't contain errors");			
			
			String jobtype = JobTypeIntrospector.getJobTypeName (job);			
			technicalLog.debug("jobtype: " + jobtype);
			if (jobtype.equals("ImportJob")) {
				if(job.getVerversen()){
					String emailTeksten = job.getDatasetType().getThema().getEmailteksten();
					if(emailTeksten == null || emailTeksten.isEmpty()){
						technicalLog.debug("No Email Teksten template defined in Emailteksten tab");
						return;
					}
					Bronhouder bronhouder = job.getBronhouder();
					if(bronhouder != null) {	
						String address = bronhouder.getContactEmailadres();
						String extraAddress = bronhouder.getContactExtraEmailadres();
						
						Email mail = new SimpleEmail();
						mail.setHostName(smtpHost);
						mail.setSmtpPort(smtpPort);
						mail.setFrom(from);
						mail.setSubject(subject);
						mail.setMsg(createSuccessMsg(job));					
						mail.addTo(address);
						if(extraAddress != null && !extraAddress.isEmpty()){
							mail.addTo(extraAddress);
						}
						mail.send();
						
						technicalLog.debug("E-mail sent to: " + address);
					} else {
						technicalLog.error("Couldn't send e-mail, no bronhouder found");
					}
				}
			}
		}
	}

	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setSmtpPort(int smtpPort) {
		this.smtpPort = smtpPort;
	}	
	
	public void setManagerDao(ManagerDao managerDao) {
		this.managerDao = managerDao;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setHostProto(String proto) {
		this.hostProto = proto;
	}
}
