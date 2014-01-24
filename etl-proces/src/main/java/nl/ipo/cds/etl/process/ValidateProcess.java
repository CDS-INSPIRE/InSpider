package nl.ipo.cds.etl.process;

import java.util.Properties;

import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.ValidateJob;
import nl.ipo.cds.etl.AbstractProcess;
import nl.ipo.cds.etl.FeatureProcessor;

public class ValidateProcess extends AbstractProcess<ValidateJob> {

	public ValidateProcess(final ManagerDao managerDao,  
			final FeatureProcessor featureProcessor, 
			final Properties logProperties) {
		super (ValidateJob.class, managerDao, featureProcessor, logProperties);
	}

}
