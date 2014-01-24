package nl.ipo.cds.etl.config;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class Messages {
	@Bean
	public PropertiesFactoryBean processMessages () {
		final PropertiesFactoryBean properties = new PropertiesFactoryBean ();
		
		properties.setLocation (new ClassPathResource ("nl/ipo/cds/etl/process/process.messages"));

		return properties;
	}
}