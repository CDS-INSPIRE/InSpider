package nl.ipo.cds.etl.theme.hydronode.config;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

import nl.ipo.cds.attributemapping.operations.discover.OperationDiscoverer;
import nl.ipo.cds.etl.Transformer;
import nl.ipo.cds.etl.theme.DefaultThemeConfig;
import nl.ipo.cds.etl.theme.ThemeConfig;
import nl.ipo.cds.etl.theme.hydronode.HydroNode;
import nl.ipo.cds.etl.theme.hydronode.HydroNodeValidator;
import nl.ipo.cds.etl.util.ScriptExecutor;
import nl.ipo.cds.etl.util.ScriptTransformer;
import nl.ipo.cds.validation.execute.CompilerException;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration (value = "hydroNode.DatasetConfig")
public class DatasetConfig {

	private static final String THEME_NAME = "HydroNode"; 	
	
	@Bean
	@Inject
	public ThemeConfig<HydroNode> hydroNodeThemeConfig (
			final HydroNodeValidator validator,
			final OperationDiscoverer operationDiscoverer) {
		return new DefaultThemeConfig<HydroNode> (THEME_NAME, HydroNode.class, validator, operationDiscoverer);
	}

	@Bean
	public Transformer hydroNodeTransformer(final ScriptExecutor scriptExecutor) {
		return new ScriptTransformer(scriptExecutor, new ClassPathResource ("nl/ipo/cds/etl/theme/hydronode/transform-bron-to-inspire.sql"), THEME_NAME);
	}

	@Configuration (value = "hydroNode.Validators")
	public static class Validators {
		@Bean
		@Inject
		public HydroNodeValidator hydroNodeValidator (
				final @Named ("hydroNodeValidationMessages") Properties validatorMessages) throws CompilerException {
			return new HydroNodeValidator (validatorMessages);
		}
	}

	@Configuration (value = "hydroNode.Messages")
	public static class Messages {
		@Bean
		public PropertiesFactoryBean hydroNodeValidationMessages () {
			final PropertiesFactoryBean properties = new PropertiesFactoryBean ();
			properties.setLocation (new ClassPathResource ("nl/ipo/cds/etl/theme/hydronode/validator.messages"));
			return properties;
		}
	}
}
