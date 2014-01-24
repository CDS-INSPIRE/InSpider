package nl.ipo.cds.etl.config;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

import nl.ipo.cds.etl.filtering.DatasetFiltererFactory;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class Filtering {

	@Bean
	@Inject
	public DatasetFiltererFactory datasetFiltererFactory (final @Named("datasetFilterValidatorMessages") Properties messages) {
		return new DatasetFiltererFactory (messages);
	}
	
	@Configuration
	public static class Messages {
		@Bean
		public PropertiesFactoryBean datasetFilterValidatorMessages () {
			final PropertiesFactoryBean properties = new PropertiesFactoryBean ();
			
			properties.setLocation (new ClassPathResource ("nl/ipo/cds/etl/filtering/validator.messages"));

			return properties;
		}
	}
}
