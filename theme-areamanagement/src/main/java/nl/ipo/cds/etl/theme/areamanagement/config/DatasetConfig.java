package nl.ipo.cds.etl.theme.areamanagement.config;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

import nl.ipo.cds.attributemapping.operations.discover.OperationDiscoverer;
import nl.ipo.cds.etl.Transformer;
import nl.ipo.cds.etl.theme.DefaultThemeConfig;
import nl.ipo.cds.etl.theme.ThemeConfig;
import nl.ipo.cds.etl.theme.areamanagement.AreaManagement;
import nl.ipo.cds.etl.theme.areamanagement.AreaManagementValidator;
import nl.ipo.cds.etl.util.ScriptExecutor;
import nl.ipo.cds.etl.util.ScriptTransformer;
import nl.ipo.cds.validation.execute.CompilerException;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration (value = "areamanagement.DatasetConfig")
public class DatasetConfig {

	private static final String THEME_NAME = "AreaManagement"; 
	
	@Bean
	@Inject
	public ThemeConfig<AreaManagement> areaManagementThemeConfig (
			final AreaManagementValidator validator,
			final OperationDiscoverer operationDiscoverer) {
		return new DefaultThemeConfig<AreaManagement> (THEME_NAME, AreaManagement.class, validator, operationDiscoverer);
	}
	
	@Bean
	public Transformer areaManagementTransformer(final ScriptExecutor scriptExecutor) {
		return new ScriptTransformer(scriptExecutor, new ClassPathResource ("nl/ipo/cds/etl/theme/areamanagement/transform-bron-to-inspire.sql"), THEME_NAME);
	}
	
	@Configuration (value = "areamanagement.Validators")
	public static class Validators {
		@Bean
		@Inject
		public AreaManagementValidator areaManagementValidator (
				final @Named ("areaManagementValidationMessages") Properties validatorMessages) throws CompilerException {
			return new AreaManagementValidator (validatorMessages);
		}
	}
	
	@Configuration (value = "areamanagement.Messages")
	public static class Messages {
		@Bean
		public PropertiesFactoryBean areaManagementValidationMessages () {
			final PropertiesFactoryBean properties = new PropertiesFactoryBean ();
			
			properties.setLocation (new ClassPathResource ("nl/ipo/cds/etl/theme/areamanagement/validator.messages"));

			return properties;
		}
	}
}
