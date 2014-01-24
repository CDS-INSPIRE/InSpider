package nl.ipo.cds.etl.theme.productioninstallation.config;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

import nl.ipo.cds.attributemapping.operations.discover.OperationDiscoverer;
import nl.ipo.cds.etl.Transformer;
import nl.ipo.cds.etl.theme.DefaultThemeConfig;
import nl.ipo.cds.etl.theme.ThemeConfig;
import nl.ipo.cds.etl.theme.productioninstallation.ProductionInstallation;
import nl.ipo.cds.etl.theme.productioninstallation.ProductionInstallationValidator;
import nl.ipo.cds.etl.util.ScriptExecutor;
import nl.ipo.cds.etl.util.ScriptTransformer;
import nl.ipo.cds.validation.execute.CompilerException;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration (value = "productionInstallation.DatasetConfig")
public class DatasetConfig {

	private static final String THEME_NAME = "ProductionInstallation";	
	
	@Bean
	@Inject
	public ThemeConfig<ProductionInstallation> productionInstallationThemeConfig (
			final ProductionInstallationValidator validator,
			final OperationDiscoverer operationDiscoverer) {
		return new DefaultThemeConfig<ProductionInstallation> (THEME_NAME, ProductionInstallation.class, validator, operationDiscoverer);
	}

	@Bean
	public Transformer productionInstallationTransformer(final ScriptExecutor scriptExecutor) {
		return new ScriptTransformer(scriptExecutor, new ClassPathResource ("nl/ipo/cds/etl/theme/productioninstallation/transform-bron-to-inspire.sql"), THEME_NAME);
	}

	@Configuration (value = "productionInstallation.Validators")
	public static class Validators {
		@Bean
		@Inject
		public ProductionInstallationValidator productionInstallationValidator (
				final @Named ("productionInstallationValidationMessages") Properties validatorMessages) throws CompilerException {
			return new ProductionInstallationValidator (validatorMessages);
		}
	}

	@Configuration (value = "productionInstallation.Messages")
	public static class Messages {
		@Bean
		public PropertiesFactoryBean productionInstallationValidationMessages () {
			final PropertiesFactoryBean properties = new PropertiesFactoryBean ();
			properties.setLocation (new ClassPathResource ("nl/ipo/cds/etl/theme/productioninstallation/validator.messages"));
			return properties;
		}
	}
}
