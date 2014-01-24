package nl.ipo.cds.executor.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.sql.DataSource;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobCollector;
import nl.idgis.commons.jobexecutor.JobCreator;
import nl.idgis.commons.jobexecutor.JobDao;
import nl.idgis.commons.jobexecutor.JobExecutor;
import nl.idgis.commons.jobexecutor.JobLogger;
import nl.idgis.commons.jobexecutor.JobMail;
import nl.idgis.commons.jobexecutor.JobProcessor;
import nl.idgis.commons.jobexecutor.Process;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.dao.impl.JobDaoImpl;
import nl.ipo.cds.etl.EtlJobMail;
import nl.ipo.cds.etl.util.BlockingExecutor;
import nl.ipo.cds.executor.CdsJobExecuter;
import nl.ipo.cds.executor.JobCollectorImpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobExecuterConfig {

	public class ProcessHolder {
		private final Map<Class<? extends Job>, Process<Job>> processes;
		
		public ProcessHolder (final Map<Class<? extends Job>, Process<Job>> processes) {
			this.processes = new HashMap<Class<? extends Job>, Process<Job>> (processes);
		}
		
		public Map<Class<? extends Job>, Process<Job>> getProcessJobTypes () {
			return Collections.unmodifiableMap (processes);
		}
	}
	
	@Bean
	@Inject
	public ProcessHolder processJobTypesHolder (final Set<Process<Job>> processes) {

		final Map<Class<? extends Job>, Process<Job>> processMap = new HashMap<Class<? extends Job>, Process<Job>> ();
		
		for (final Process<Job> process: processes) {
			final Class<? extends Job> key = process.getJobType ();
			
			if (processMap.containsKey (key)) {
				throw new IllegalStateException (String.format ("Duplicate process type: %s", key));
			}
			
			processMap.put (key, process);
		}
		
		return new ProcessHolder (processMap);
	}
	
	@Bean
	@Inject
	public JobProcessor jobProcessor (
			final ProcessHolder processes,
			final JobDao jobDao,
			final JobMail jobMail,
			final JobLogger jobLogger) {
		
		return new JobProcessor (
				Collections.<Class<?>, Process<?>>unmodifiableMap (processes.getProcessJobTypes ()),
				jobDao,
				jobMail,
				jobLogger
			);
	}
	
	@Bean
	@Inject
	public JobCollector JobCollectorImpl (final ManagerDao managerDao) {
		return new JobCollectorImpl(managerDao); 
	}
	
	@Bean
	@Inject
	public JobCreator jobDaoImpl(final ManagerDao managerDao) {
		return new JobDaoImpl(managerDao); 
	}
	
	
	@Bean
	@Inject
	public JobExecutor jobExecutor (final JobCollector jobCollector, final JobProcessor jobProcessor) {
		JobExecutor jobExecutor = new JobExecutor (jobProcessor);
		jobExecutor.setJobCollector(jobCollector);
		jobExecutor.setWaitTime(5000);
		return jobExecutor;
	}
	
	@Bean
	@Inject
	public CdsJobExecuter jobExecuter (final DataSource dataSource, final JobExecutor jobExecutor) {
		
		final CdsJobExecuter jobExecuter = new CdsJobExecuter (dataSource, jobExecutor);
		
		return jobExecuter;
	}
	
	@Bean
	public Executor executer (final @Value("${numberOfThreads}") int numberOfThreads) {
		return new BlockingExecutor (numberOfThreads);
	}
	
	@Configuration
	public static class EmailConfiguration {
		private @Value("${mail.smtpHost}") String mailSmtpHost;
		private @Value("${mail.smtpPort}") String mailSmtpPort;
		private @Value("${mail.from}") String mailFrom;
		private @Value("${mail.host}") String mailHost;
		private String mailSubject = "Melding Centrale Data- en Services omgeving";
		
		@Bean
		@Inject
		public JobMail jobMail (final ManagerDao managerDao) {
			final EtlJobMail jobMail = new EtlJobMail ();
			
			jobMail.setFrom (mailFrom);
			jobMail.setHost (mailHost);
			jobMail.setManagerDao (managerDao);
			jobMail.setSmtpHost (mailSmtpHost);
			jobMail.setSmtpPort (Integer.parseInt (mailSmtpPort));
			jobMail.setSubject (mailSubject);
			
			return jobMail;
		}
	}
}
