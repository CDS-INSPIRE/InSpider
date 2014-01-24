package nl.ipo.cds.etl.theme.protectedSite.config;

import javax.inject.Inject;
import javax.sql.DataSource;

import nl.ipo.cds.etl.generalization.GeneralizeReader;
import nl.ipo.cds.etl.theme.protectedSite.ProtectedSiteTransformer;
import nl.ipo.cds.etl.util.ScriptExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class TransformerConfig {

	@Bean
	@Inject
	public ProtectedSiteTransformer protectedSiteTransformer (final ScriptExecutor scriptExecuter, final GeneralizeReader generalizeReader) {
		final ProtectedSiteTransformer transformer = new ProtectedSiteTransformer ();
		
		transformer.setScriptExecuter (scriptExecuter);
		transformer.setTransformScript (new ClassPathResource ("nl/ipo/cds/etl/protectedSite/transform-bron-to-inspire.sql"));
		transformer.setDeleteScript (new ClassPathResource ("nl/ipo/cds/etl/protectedSite/delete-inspire-schema.sql"));
		transformer.setGeneralizeReader (generalizeReader);
		
		return transformer;
	}
}
