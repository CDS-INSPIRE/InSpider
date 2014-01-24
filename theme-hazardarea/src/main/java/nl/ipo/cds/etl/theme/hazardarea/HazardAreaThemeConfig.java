package nl.ipo.cds.etl.theme.hazardarea;

import nl.ipo.cds.attributemapping.operations.discover.OperationDiscoverer;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.etl.DatasetHandlers;
import nl.ipo.cds.etl.DefaultDatasetHandlers;
import nl.ipo.cds.etl.Validator;
import nl.ipo.cds.etl.theme.ThemeConfig;
import nl.ipo.cds.etl.theme.ThemeConfigException;

public class HazardAreaThemeConfig extends ThemeConfig<HazardArea> {

	private final Validator<HazardArea> validator;

	private final OperationDiscoverer operationDiscoverer;

	public HazardAreaThemeConfig (final Validator<HazardArea> validator, final OperationDiscoverer operationDiscoverer) {
		super ("HazardArea", HazardArea.class);
		this.validator = validator;
		this.operationDiscoverer = operationDiscoverer;
	}

	@Override
	public DatasetHandlers<HazardArea> createDatasetHandlers (final EtlJob job,	final ManagerDao managerDao) {
		return new DefaultDatasetHandlers<> (operationDiscoverer, this, managerDao);
	}

	@Override
	public Validator<HazardArea> getValidator () throws ThemeConfigException {
		return validator;
	}
}
