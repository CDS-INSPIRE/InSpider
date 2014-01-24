package nl.ipo.cds.etl.theme.protectedSite;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import nl.idgis.commons.jobexecutor.JobLogger;
import nl.ipo.cds.attributemapping.operations.discover.OperationDiscoverer;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.etl.DatasetHandlers;
import nl.ipo.cds.etl.DefaultDatasetHandlers;
import nl.ipo.cds.etl.Validator;
import nl.ipo.cds.etl.filtering.FeatureClipper;
import nl.ipo.cds.etl.theme.ThemeConfig;
import nl.ipo.cds.etl.theme.ThemeConfigException;
import nl.ipo.cds.validation.execute.CompilerException;

import org.deegree.geometry.Geometry;

public class ProtectedSiteThemeConfig extends ThemeConfig<ProtectedSite> {

	public final static String THEME_NAME = "Protected sites";

	private final Map<Object, Object> validatorMessages;

	private final OperationDiscoverer operationDiscoverer;

	public ProtectedSiteThemeConfig( final Map<Object, Object> validatorMessages, final OperationDiscoverer operationDiscoverer) {
		super(THEME_NAME, ProtectedSite.class);
		this.validatorMessages = validatorMessages == null ? new HashMap<Object, Object>()
				: new HashMap<Object, Object>(validatorMessages);
		this.operationDiscoverer = operationDiscoverer;
	}

	public Map<Object, Object> getValidatorMessages() {
		return Collections.unmodifiableMap(validatorMessages);
	}

	public OperationDiscoverer getOperationDiscoverer() {
		return operationDiscoverer;
	}

	@Override
	public Validator<ProtectedSite> getValidator() throws ThemeConfigException {
		try {
			return new ProtectedSiteValidator(validatorMessages);
		} catch (CompilerException e) {
			throw new ThemeConfigException(e);
		}
	}

	@Override
	public boolean isJobSupported(final EtlJob job) {
		return job.getDatasetType() != null
				&& job.getDatasetType().getThema() != null
				&& THEME_NAME.equals(job.getDatasetType().getThema().getNaam());
	}

	@Override
	public DatasetHandlers<ProtectedSite> createDatasetHandlers(final EtlJob job, final ManagerDao managerDao) {
		return new DefaultDatasetHandlers<ProtectedSite>(operationDiscoverer, this, managerDao) {
			@Override
			public FeatureClipper getFeatureClipper(final EtlJob job, final JobLogger logger) {
				final Geometry geometry = managerDao.getBronhouderGeometry(job.getBronhouder());
				if (geometry == null) {
					return null;
				}
				return new FeatureClipper(geometry, getBeanClass());
			}
		};
	}
}
