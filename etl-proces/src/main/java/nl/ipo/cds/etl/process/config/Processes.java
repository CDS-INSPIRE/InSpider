package nl.ipo.cds.etl.process.config;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import nl.idgis.commons.jobexecutor.JobLogger;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.etl.process.ImportFeatureProcessor;
import nl.ipo.cds.etl.process.ImportProcess;
import nl.ipo.cds.etl.process.RemoveProcess;
import nl.ipo.cds.etl.process.TransformProcess;
import nl.ipo.cds.etl.process.ValidateFeatureProcessor;
import nl.ipo.cds.etl.process.ValidateProcess;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Processes {

	private @Inject ManagerDao managerDao;
	private @Inject JobLogger logger;
	private @Inject @Named("processMessages") Properties processMessages;

	@Bean
	@Inject
	public ValidateProcess validateProcess (final @Named("validateFeatureProcessor") ValidateFeatureProcessor processor) {
		
		return new ValidateProcess (
				managerDao,
				processor,
				processMessages
			);
	}

	@Bean
	@Inject
	public ImportProcess importProcess (final @Named("importFeatureProcessor") ImportFeatureProcessor processor) {
		return new ImportProcess (
				managerDao,
				processor,
				processMessages
			);
	}

	@Bean
	public TransformProcess transformProcess () {
		return new TransformProcess (managerDao);
	}

	@Bean
	@Inject
	public RemoveProcess removeProcess (final DataSource dataSource) {
		final RemoveProcess process = new RemoveProcess (dataSource);

		return process;
	}
}