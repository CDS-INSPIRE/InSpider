package nl.ipo.cds.etl.theme;

import nl.ipo.cds.attributemapping.operations.discover.OperationDiscoverer;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.etl.DatasetHandlers;
import nl.ipo.cds.etl.DefaultDatasetHandlers;
import nl.ipo.cds.etl.PersistableFeature;
import nl.ipo.cds.etl.Validator;

public class DefaultThemeConfig<T extends PersistableFeature> extends ThemeConfig<T> {

	private final Validator<T> validator;

	private final OperationDiscoverer operationDiscoverer;
	
	public DefaultThemeConfig (String themeName, Class<T> featureClass, final Validator<T> validator, final OperationDiscoverer operationDiscoverer) {
		super (themeName, featureClass);
		this.validator = validator;
		this.operationDiscoverer = operationDiscoverer;
	}

	@Override
	public DatasetHandlers<T> createDatasetHandlers (final EtlJob job, final ManagerDao managerDao){		
		return new DefaultDatasetHandlers<T>(operationDiscoverer, this, managerDao);
	}

	@Override
	public Validator<T> getValidator () throws ThemeConfigException {
		return validator;
	}
}
