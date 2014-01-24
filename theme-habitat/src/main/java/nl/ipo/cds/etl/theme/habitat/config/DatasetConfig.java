package nl.ipo.cds.etl.theme.habitat.config;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

import nl.ipo.cds.attributemapping.operations.discover.OperationDiscoverer;
import nl.ipo.cds.etl.Transformer;
import nl.ipo.cds.etl.theme.DefaultThemeConfig;
import nl.ipo.cds.etl.theme.ThemeConfig;
import nl.ipo.cds.etl.theme.habitat.Habitat;
import nl.ipo.cds.etl.theme.habitat.HabitatValidator;
import nl.ipo.cds.etl.util.ScriptExecutor;
import nl.ipo.cds.etl.util.ScriptTransformer;
import nl.ipo.cds.validation.execute.CompilerException;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration (value = "habitat.DatasetConfig")
public class DatasetConfig {

	private static final String THEME_NAME = "Habitat"; 
	
	@Bean
	@Inject
	public ThemeConfig<Habitat> habitatThemeConfig (
			final HabitatValidator validator,
			final OperationDiscoverer operationDiscoverer) {		
		return new DefaultThemeConfig<Habitat> (THEME_NAME, Habitat.class, validator, operationDiscoverer);
	}
	
	@Bean
	public Transformer habitatTransformer(final ScriptExecutor scriptExecutor) {
		return new ScriptTransformer(scriptExecutor, new ClassPathResource ("nl/ipo/cds/etl/theme/habitat/transform-bron-to-inspire.sql"), THEME_NAME);
	}
	
	@Configuration (value = "habitat.Validators")
	public static class Validators {
		@Bean
		@Inject
		public HabitatValidator habitatValidator (
				final @Named ("habitatValidationMessages") Properties validatorMessages) throws CompilerException {
			return new HabitatValidator (validatorMessages);
		}
	}
	
	@Configuration (value = "habitat.Messages")
	public static class Messages {
		@Bean
		public PropertiesFactoryBean habitatValidationMessages () {
			final PropertiesFactoryBean properties = new PropertiesFactoryBean ();
			
			properties.setLocation (new ClassPathResource ("nl/ipo/cds/etl/theme/habitat/validator.messages"));

			return properties;
		}
	}
}
