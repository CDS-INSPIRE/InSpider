package nl.ipo.cds.etl.config;

import javax.inject.Inject;
import javax.inject.Named;

import nl.idgis.commons.jobexecutor.JobLogger;
import nl.ipo.cds.etl.log.LogStringBuilder;
import nl.ipo.cds.etl.process.HarvesterFactory;
import nl.ipo.cds.etl.process.HarvesterMessageKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class Harvester {

	@Bean
	@Inject
	public HarvesterFactory harvesterFactory (
			final @Value ("${pgrBaseUrl}") String pgrBaseUrl, 
			final JobLogger logger,
			final @Named("harvesterMessages") java.util.Properties properties) {
		
		final HarvesterFactory harvesterFactory = new HarvesterFactory ();
		final LogStringBuilder<HarvesterMessageKey> logStringBuilder = new LogStringBuilder<HarvesterMessageKey> ();
		
		logStringBuilder.setJobLogger (logger);
		logStringBuilder.setProperties (properties);
		
		harvesterFactory.setUserLog (logStringBuilder);
		harvesterFactory.setPgrBaseUrl (pgrBaseUrl);
		
		return harvesterFactory;
	}
	
	@Bean
	public PropertiesFactoryBean harvesterMessages () {
		final PropertiesFactoryBean properties = new PropertiesFactoryBean ();
		
		properties.setLocation (new ClassPathResource ("nl/ipo/cds/etl/harvest/harvest.messages"));

		return properties;
	}
}
