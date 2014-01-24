package nl.ipo.cds.etl.theme.exposedelements;

import nl.ipo.cds.attributemapping.operations.discover.OperationDiscoverer;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.etl.DatasetHandlers;
import nl.ipo.cds.etl.DefaultDatasetHandlers;
import nl.ipo.cds.etl.Validator;
import nl.ipo.cds.etl.theme.ThemeConfig;
import nl.ipo.cds.etl.theme.ThemeConfigException;

public class ExposedElementsThemeConfig extends ThemeConfig<ExposedElements> {

	private final Validator<ExposedElements> validator;

	private final OperationDiscoverer operationDiscoverer;

	public ExposedElementsThemeConfig (final Validator<ExposedElements> validator, final OperationDiscoverer operationDiscoverer) {
		super ("ExposedElements", ExposedElements.class);
		this.validator = validator;
		this.operationDiscoverer = operationDiscoverer;
	}

	@Override
	public DatasetHandlers<ExposedElements> createDatasetHandlers (final EtlJob job,	final ManagerDao managerDao) {
		return new DefaultDatasetHandlers<> (operationDiscoverer, this, managerDao);
	}

	@Override
	public Validator<ExposedElements> getValidator () throws ThemeConfigException {
		return validator;
	}
}
