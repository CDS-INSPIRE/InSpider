package nl.ipo.cds.etl.theme.protectedSite.config;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

import nl.ipo.cds.attributemapping.operations.discover.OperationDiscoverer;
import nl.ipo.cds.etl.theme.protectedSite.ProtectedSiteThemeConfig;
import nl.ipo.cds.executor.ConfigDir;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;

@Configuration
@Import (GeneralizationConfig.class)
public class DatasetConfig {

	@Bean
	@Inject
	public ProtectedSiteThemeConfig protectedSite (
			final ConfigDir configDir,
			final @Named("protectedSiteValidatorMessages") Properties validatorMessages,
			final OperationDiscoverer operationDiscoverer) {	
		final ProtectedSiteThemeConfig config = new ProtectedSiteThemeConfig (validatorMessages, operationDiscoverer);		
		return config;
	}
	
	@Configuration
	public static class Messages {
		@Bean
		public PropertiesFactoryBean protectedSiteValidatorMessages () {
			final PropertiesFactoryBean properties = new PropertiesFactoryBean ();
			
			properties.setLocation (new ClassPathResource ("nl/ipo/cds/etl/protectedSite/validator.messages"));

			return properties;
		}
	}
}
