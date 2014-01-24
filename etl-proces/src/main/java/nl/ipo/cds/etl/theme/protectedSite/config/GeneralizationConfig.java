package nl.ipo.cds.etl.theme.protectedSite.config;

import java.io.IOException;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.xml.bind.JAXBException;

import nl.ipo.cds.etl.generalization.DefaultGeneralizer;
import nl.ipo.cds.etl.generalization.GeneralizeReader;
import nl.ipo.cds.etl.generalization.GeneralizeWriter;
import nl.ipo.cds.etl.generalization.GeneralizerConfig;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.support.nativejdbc.CommonsDbcpNativeJdbcExtractor;

@Configuration
public class GeneralizationConfig {
	
	@Bean
	public GeneralizerConfig generalizerConfig () throws IOException, JAXBException {
		final GeneralizerConfig config = new GeneralizerConfig ();
		
		config.setConfigResource (new ClassPathResource ("nl/ipo/cds/etl/protectedSite/config/generalize.xml"));
		
		return config;
	}
	
	@Bean
	@Scope (ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public DefaultGeneralizer generalizer () {
		final DefaultGeneralizer generalizer = new DefaultGeneralizer ();

		return generalizer;
	}
	
	@Bean
	@Scope (ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	public GeneralizeWriter generalizeWriter () {
		final GeneralizeWriter writer = new GeneralizeWriter ();
		
		writer.setNativeJdbcExtractorAdapter (new CommonsDbcpNativeJdbcExtractor ());
		
		return writer;
	}
	
	@Bean
	@Inject
	public GeneralizeReader generalizeReader (final DataSource dataSource, final GeneralizerConfig generalizerConfig, final Executor executor) {
		final GeneralizeReader reader = new GeneralizeReader ();
		
		reader.setDataSource (dataSource);
		reader.setGeneralizerConfig (generalizerConfig);
		reader.setExecuter (executor);
		
		return reader;
	}
}
