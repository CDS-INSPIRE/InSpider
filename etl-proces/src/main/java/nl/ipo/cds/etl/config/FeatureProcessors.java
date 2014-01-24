package nl.ipo.cds.etl.config;

import javax.inject.Inject;
import javax.sql.DataSource;

import nl.idgis.commons.jobexecutor.JobLogger;
import nl.ipo.cds.etl.FileCache;
import nl.ipo.cds.etl.attributemapping.AttributeMappingFactory;
import nl.ipo.cds.etl.filtering.DatasetFiltererFactory;
import nl.ipo.cds.etl.process.ImportFeatureProcessor;
import nl.ipo.cds.etl.process.ValidateFeatureProcessor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.support.nativejdbc.CommonsDbcpNativeJdbcExtractor;

@Configuration
public class FeatureProcessors {
	private @Inject JobLogger logger;
	
	@Bean
	@Inject
	public ValidateFeatureProcessor validateFeatureProcessor (final AttributeMappingFactory attributeMappingFactory, final DatasetFiltererFactory datasetFiltererFactory) {
		final ValidateFeatureProcessor processor = new ValidateFeatureProcessor (attributeMappingFactory, datasetFiltererFactory);
		
		return processor;
	}
	
	@Bean
	@Inject
	public ImportFeatureProcessor importFeatureProcessor (final DataSource dataSource, final FileCache fileCache, final AttributeMappingFactory attributeMappingFactory, final DatasetFiltererFactory datasetFiltererFactory) {
		final ImportFeatureProcessor processor = new ImportFeatureProcessor (
				attributeMappingFactory,
				datasetFiltererFactory,
				dataSource, 
				new CommonsDbcpNativeJdbcExtractor (),
				fileCache
			);
		
		return processor;
	}
}