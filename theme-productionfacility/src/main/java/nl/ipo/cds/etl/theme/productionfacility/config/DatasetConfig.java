package nl.ipo.cds.etl.theme.productionfacility.config;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

import nl.ipo.cds.attributemapping.operations.discover.OperationDiscoverer;
import nl.ipo.cds.etl.Transformer;
import nl.ipo.cds.etl.theme.DefaultThemeConfig;
import nl.ipo.cds.etl.theme.ThemeConfig;
import nl.ipo.cds.etl.theme.productionfacility.ProductionFacility;
import nl.ipo.cds.etl.theme.productionfacility.ProductionFacilityValidator;
import nl.ipo.cds.etl.util.ScriptExecutor;
import nl.ipo.cds.etl.util.ScriptTransformer;
import nl.ipo.cds.validation.execute.CompilerException;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration (value = "productionFacility.DatasetConfig")
public class DatasetConfig {

	private static final String THEME_NAME = "ProductionFacility"; 	
	
	@Bean
	@Inject
	public ThemeConfig<ProductionFacility> productionFacilityThemeConfig (
			final ProductionFacilityValidator validator,
			final OperationDiscoverer operationDiscoverer) {
		return new DefaultThemeConfig<ProductionFacility> (THEME_NAME, ProductionFacility.class, validator, operationDiscoverer);
	}

	@Bean
	public Transformer productionFacilityTransformer(final ScriptExecutor scriptExecutor) {
		return new ScriptTransformer(scriptExecutor, new ClassPathResource ("nl/ipo/cds/etl/theme/productionfacility/transform-bron-to-inspire.sql"), THEME_NAME);
	}

	@Configuration (value = "productionFacility.Validators")
	public static class Validators {
		@Bean
		@Inject
		public ProductionFacilityValidator productionFacilityValidator (
				final @Named ("productionFacilityValidationMessages") Properties validatorMessages) throws CompilerException {
			return new ProductionFacilityValidator (validatorMessages);
		}
	}

	@Configuration (value = "productionFacility.Messages")
	public static class Messages {
		@Bean
		public PropertiesFactoryBean productionFacilityValidationMessages () {
			final PropertiesFactoryBean properties = new PropertiesFactoryBean ();

			properties.setLocation (new ClassPathResource ("nl/ipo/cds/etl/theme/productionfacility/validator.messages"));

			return properties;
		}
	}
}
