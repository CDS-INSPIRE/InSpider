package nl.ipo.cds.etl.config;

import nl.ipo.cds.properties.ConfigDirPropertyPlaceholderConfigurer;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Properties {

	@Bean
	public PropertyPlaceholderConfigurer propertyPlaceholderConfigurer () {
		final ConfigDirPropertyPlaceholderConfigurer properties = new ConfigDirPropertyPlaceholderConfigurer ();
		return properties;
	}
}
