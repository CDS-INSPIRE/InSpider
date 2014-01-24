package nl.ipo.cds.etl;

import java.io.StringWriter;
import java.util.Properties;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.Job.Status;
import nl.idgis.commons.jobexecutor.JobLogger.LogLevel;
import nl.idgis.commons.jobexecutor.JobMail;
import nl.idgis.commons.jobexecutor.JobTypeIntrospector;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.Bronhouder;
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

	private String smtpHost, from, subject, host;
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
		
		StringWriter writer = new StringWriter();
        template.merge(context, writer);
		
		return writer.toString();
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
					
					Email mail = new SimpleEmail();
					mail.setHostName(smtpHost);
					mail.setSmtpPort(smtpPort);
					mail.setFrom(from);
					mail.setSubject(subject);
					mail.setMsg(createMsg(job));					
					mail.addTo(address);
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
}
