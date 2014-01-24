package nl.ipo.cds.etl.process;

import java.util.Properties;

import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.ImportJob;
import nl.ipo.cds.etl.AbstractProcess;
import nl.ipo.cds.etl.FeatureProcessor;

public class ImportProcess extends AbstractProcess<ImportJob> {

	public ImportProcess (final ManagerDao managerDao,
			final FeatureProcessor featureProcessor, 
			final Properties logProperties) {
		super (ImportJob.class, managerDao, featureProcessor, logProperties);
	}

}
