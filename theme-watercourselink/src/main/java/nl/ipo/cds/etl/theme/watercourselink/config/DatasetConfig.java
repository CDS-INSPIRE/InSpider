package nl.ipo.cds.etl.theme.watercourselink.config;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

import nl.ipo.cds.attributemapping.operations.discover.OperationDiscoverer;
import nl.ipo.cds.etl.Transformer;
import nl.ipo.cds.etl.theme.DefaultThemeConfig;
import nl.ipo.cds.etl.theme.ThemeConfig;
import nl.ipo.cds.etl.theme.watercourselink.WatercourseLink;
import nl.ipo.cds.etl.theme.watercourselink.WatercourseLinkValidator;
import nl.ipo.cds.etl.util.ScriptExecutor;
import nl.ipo.cds.etl.util.ScriptTransformer;
import nl.ipo.cds.validation.execute.CompilerException;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration (value = "watercourseLink.DatasetConfig")
public class DatasetConfig {

	private static final String THEME_NAME = "WatercourseLink"; 	
	
	@Bean
	@Inject
	public ThemeConfig<WatercourseLink> watercourseLinkThemeConfig (
			final WatercourseLinkValidator validator,
			final OperationDiscoverer operationDiscoverer) {
		return new DefaultThemeConfig<WatercourseLink> (THEME_NAME, WatercourseLink.class, validator, operationDiscoverer);
	}

	@Bean
	public Transformer watercourseLinkTransformer(final ScriptExecutor scriptExecutor) {
		return new ScriptTransformer(scriptExecutor, new ClassPathResource ("nl/ipo/cds/etl/theme/watercourselink/transform-bron-to-inspire.sql"), THEME_NAME);
	}

	@Configuration (value = "watercourseLink.Validators")
	public static class Validators {
		@Bean
		@Inject
		public WatercourseLinkValidator watercourseLinkValidator (
				final @Named ("watercourseLinkValidationMessages") Properties validatorMessages) throws CompilerException {
			return new WatercourseLinkValidator (validatorMessages);
		}
	}

	@Configuration (value = "watercourseLink.Messages")
	public static class Messages {
		@Bean
		public PropertiesFactoryBean watercourseLinkValidationMessages () {
			final PropertiesFactoryBean properties = new PropertiesFactoryBean ();
			properties.setLocation (new ClassPathResource ("nl/ipo/cds/etl/theme/watercourselink/validator.messages"));
			return properties;
		}
	}
}
