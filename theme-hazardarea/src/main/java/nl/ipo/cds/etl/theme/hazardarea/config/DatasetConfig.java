package nl.ipo.cds.etl.theme.hazardarea.config;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

import nl.ipo.cds.attributemapping.operations.discover.OperationDiscoverer;
import nl.ipo.cds.etl.theme.hazardarea.HazardAreaThemeConfig;
import nl.ipo.cds.etl.theme.hazardarea.HazardAreaValidator;
import nl.ipo.cds.validation.execute.CompilerException;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration (value = "hazardArea.DatasetConfig")
public class DatasetConfig {

	@Bean
	@Inject
	public HazardAreaThemeConfig hazardAreaThemeConfig (
			final HazardAreaValidator validator,
			final OperationDiscoverer operationDiscoverer) {

		return new HazardAreaThemeConfig (validator, operationDiscoverer);
	}

	@Configuration (value = "hazardArea.Validators")
	public static class Validators {
		@Bean
		@Inject
		public HazardAreaValidator hazardAreaValidator (
				final @Named ("hazardAreaValidationMessages") Properties validatorMessages) throws CompilerException {
			return new HazardAreaValidator (validatorMessages);
		}
	}

	@Configuration (value = "hazardArea.Messages")
	public static class Messages {
		@Bean
		public PropertiesFactoryBean hazardAreaValidationMessages () {
			final PropertiesFactoryBean properties = new PropertiesFactoryBean ();
			properties.setLocation (new ClassPathResource ("nl/ipo/cds/etl/theme/hazardarea/validator.messages"));
			return properties;
		}
	}
}
