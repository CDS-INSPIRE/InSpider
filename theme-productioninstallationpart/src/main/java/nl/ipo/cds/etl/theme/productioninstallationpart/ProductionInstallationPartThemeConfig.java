package nl.ipo.cds.etl.theme.productioninstallationpart;

import nl.ipo.cds.attributemapping.operations.discover.OperationDiscoverer;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.etl.DatasetHandlers;
import nl.ipo.cds.etl.DefaultDatasetHandlers;
import nl.ipo.cds.etl.Validator;
import nl.ipo.cds.etl.theme.ThemeConfig;
import nl.ipo.cds.etl.theme.ThemeConfigException;

public class ProductionInstallationPartThemeConfig extends ThemeConfig<ProductionInstallationPart> {

	private final Validator<ProductionInstallationPart> validator;

	private final OperationDiscoverer operationDiscoverer;
	
	public ProductionInstallationPartThemeConfig (final Validator<ProductionInstallationPart> validator, final OperationDiscoverer operationDiscoverer) {
		super ("ProductionInstallationPart", ProductionInstallationPart.class);
		this.validator = validator;
		this.operationDiscoverer = operationDiscoverer;
	}

	@Override
	public DatasetHandlers<ProductionInstallationPart> createDatasetHandlers (final EtlJob job, final ManagerDao managerDao) {
		return new DefaultDatasetHandlers<> (operationDiscoverer, this, managerDao);
	}

	@Override
	public Validator<ProductionInstallationPart> getValidator () throws ThemeConfigException {
		return validator;
	}
}
