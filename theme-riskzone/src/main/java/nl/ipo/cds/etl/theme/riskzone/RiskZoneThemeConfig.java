package nl.ipo.cds.etl.theme.riskzone;

import nl.ipo.cds.attributemapping.operations.discover.OperationDiscoverer;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.etl.DatasetHandlers;
import nl.ipo.cds.etl.DefaultDatasetHandlers;
import nl.ipo.cds.etl.Validator;
import nl.ipo.cds.etl.theme.ThemeConfig;
import nl.ipo.cds.etl.theme.ThemeConfigException;

public class RiskZoneThemeConfig extends ThemeConfig<RiskZone> {

	private final Validator<RiskZone> validator;

	private final OperationDiscoverer operationDiscoverer;

	public RiskZoneThemeConfig (final Validator<RiskZone> validator, final OperationDiscoverer operationDiscoverer) {
		super ("RiskZone", RiskZone.class);
		this.validator = validator;
		this.operationDiscoverer = operationDiscoverer;
	}

	@Override
	public DatasetHandlers<RiskZone> createDatasetHandlers (final EtlJob job,	final ManagerDao managerDao) {
		return new DefaultDatasetHandlers<> (operationDiscoverer, this, managerDao);
	}

	@Override
	public Validator<RiskZone> getValidator () throws ThemeConfigException {
		return validator;
	}
}
