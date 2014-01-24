package nl.ipo.cds.etl.theme.exposedelements.config;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

import nl.ipo.cds.attributemapping.operations.discover.OperationDiscoverer;
import nl.ipo.cds.etl.theme.exposedelements.ExposedElementsThemeConfig;
import nl.ipo.cds.etl.theme.exposedelements.ExposedElementsValidator;
import nl.ipo.cds.validation.execute.CompilerException;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration (value = "exposedElements.DatasetConfig")
public class DatasetConfig {

	@Bean
	@Inject
	public ExposedElementsThemeConfig exposedElementsThemeConfig (
			final ExposedElementsValidator validator,
			final OperationDiscoverer operationDiscoverer) {

		return new ExposedElementsThemeConfig (validator, operationDiscoverer);
	}

	@Configuration (value = "exposedElements.Validators")
	public static class Validators {
		@Bean
		@Inject
		public ExposedElementsValidator exposedElementsValidator (
				final @Named ("exposedElementsValidationMessages") Properties validatorMessages) throws CompilerException {
			return new ExposedElementsValidator (validatorMessages);
		}
	}

	@Configuration (value = "exposedElements.Messages")
	public static class Messages {
		@Bean
		public PropertiesFactoryBean exposedElementsValidationMessages () {
			final PropertiesFactoryBean properties = new PropertiesFactoryBean ();
			properties.setLocation (new ClassPathResource ("nl/ipo/cds/etl/theme/exposedelements/validator.messages"));
			return properties;
		}
	}
}
