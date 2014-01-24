package nl.ipo.cds.etl.config;

import javax.inject.Inject;

import nl.idgis.commons.jobexecutor.JobDao;
import nl.idgis.commons.jobexecutor.JobLogger;
import nl.ipo.cds.etl.log.DBLogger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Logger {

	@Bean
	@Inject
	public JobLogger dbLogger (final JobDao jobDao) {
		final DBLogger logger = new DBLogger (jobDao);
		
		return logger;
	}
}
