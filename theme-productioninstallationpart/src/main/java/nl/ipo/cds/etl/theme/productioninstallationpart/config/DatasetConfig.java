package nl.ipo.cds.etl.theme.productioninstallationpart.config;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

import nl.ipo.cds.attributemapping.operations.discover.OperationDiscoverer;
import nl.ipo.cds.etl.Transformer;
import nl.ipo.cds.etl.theme.DefaultThemeConfig;
import nl.ipo.cds.etl.theme.ThemeConfig;
import nl.ipo.cds.etl.theme.productioninstallationpart.ProductionInstallationPart;
import nl.ipo.cds.etl.theme.productioninstallationpart.ProductionInstallationPartValidator;
import nl.ipo.cds.etl.util.ScriptExecutor;
import nl.ipo.cds.etl.util.ScriptTransformer;
import nl.ipo.cds.validation.execute.CompilerException;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration (value = "productionInstallationPart.DatasetConfig")
public class DatasetConfig {

	private static final String THEME_NAME = "ProductionInstallationPart";

	@Bean
	@Inject
	public ThemeConfig<ProductionInstallationPart> productionInstallationPartThemeConfig (
			final ProductionInstallationPartValidator validator,
			final OperationDiscoverer operationDiscoverer) {
		return new DefaultThemeConfig<ProductionInstallationPart> (THEME_NAME, ProductionInstallationPart.class, validator, operationDiscoverer);
	}

	@Bean
	public Transformer productionInstallationPartTransformer(final ScriptExecutor scriptExecutor) {
		return new ScriptTransformer(scriptExecutor, new ClassPathResource ("nl/ipo/cds/etl/theme/productioninstallationpart/transform-bron-to-inspire.sql"), THEME_NAME);
	}

	@Configuration (value = "productionInstallationPart.Validators")
	public static class Validators {
		@Bean
		@Inject
		public ProductionInstallationPartValidator productionInstallationPartValidator (
				final @Named ("productionInstallationPartValidationMessages") Properties validatorMessages) throws CompilerException {
			return new ProductionInstallationPartValidator (validatorMessages);
		}
	}

	@Configuration (value = "productionInstallationPart.Messages")
	public static class Messages {
		@Bean
		public PropertiesFactoryBean productionInstallationPartValidationMessages () {
			final PropertiesFactoryBean properties = new PropertiesFactoryBean ();
			properties.setLocation (new ClassPathResource ("nl/ipo/cds/etl/theme/productioninstallationpart/validator.messages"));
			return properties;
		}
	}
}
