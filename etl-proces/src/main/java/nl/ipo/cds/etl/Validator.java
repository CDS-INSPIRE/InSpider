package nl.ipo.cds.etl;

import nl.idgis.commons.jobexecutor.JobLogger;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.validation.gml.codelists.CodeListFactory;


public interface Validator<T extends PersistableFeature> {

	FeatureFilter<T, T> getFilterForJob (EtlJob etlJob, CodeListFactory codeListFactory, JobLogger logger);
}
