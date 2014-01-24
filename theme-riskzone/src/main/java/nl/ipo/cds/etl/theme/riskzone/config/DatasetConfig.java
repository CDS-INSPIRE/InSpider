package nl.ipo.cds.etl.theme.riskzone.config;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

import nl.ipo.cds.attributemapping.operations.discover.OperationDiscoverer;
import nl.ipo.cds.etl.theme.riskzone.RiskZoneThemeConfig;
import nl.ipo.cds.etl.theme.riskzone.RiskZoneValidator;
import nl.ipo.cds.validation.execute.CompilerException;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration (value = "riskZone.DatasetConfig")
public class DatasetConfig {

	@Bean
	@Inject
	public RiskZoneThemeConfig riskZoneThemeConfig (
			final RiskZoneValidator validator,
			final OperationDiscoverer operationDiscoverer) {

		return new RiskZoneThemeConfig (validator, operationDiscoverer);
	}

	@Configuration (value = "riskZone.Validators")
	public static class Validators {
		@Bean
		@Inject
		public RiskZoneValidator riskZoneValidator (
				final @Named ("riskZoneValidationMessages") Properties validatorMessages) throws CompilerException {
			return new RiskZoneValidator (validatorMessages);
		}
	}

	@Configuration (value = "riskZone.Messages")
	public static class Messages {
		@Bean
		public PropertiesFactoryBean riskZoneValidationMessages () {
			final PropertiesFactoryBean properties = new PropertiesFactoryBean ();
			properties.setLocation (new ClassPathResource ("nl/ipo/cds/etl/theme/riskzone/validator.messages"));
			return properties;
		}
	}
}
