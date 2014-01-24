package nl.ipo.cds.etl;

import java.util.Map;

import nl.idgis.commons.jobexecutor.JobLogger;
import nl.ipo.cds.dao.attributemapping.OperationDTO;
import nl.ipo.cds.domain.DatasetFilter;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.domain.FeatureType;
import nl.ipo.cds.etl.db.DBWriterFactory;
import nl.ipo.cds.etl.featurecollection.FeatureCollection;
import nl.ipo.cds.etl.filtering.FeatureClipper;
import nl.ipo.cds.etl.log.EventLogger;
import nl.ipo.cds.etl.process.DatasetMetadata;
import nl.ipo.cds.etl.theme.AttributeDescriptor;
import nl.ipo.cds.etl.theme.ThemeConfig;
import nl.ipo.cds.validation.gml.codelists.CodeListFactory;

public interface DatasetHandlers<T extends PersistableFeature> {

	Class<T> getBeanClass();

	DBWriterFactory<T> getDBWriterFactory(String... constColumns);
	
	ThemeConfig<T> getThemeConfig ();
	
	Map<AttributeDescriptor<?>, OperationDTO> getAttributeMappings (EtlJob job, FeatureType featureType);
	
	/**
	 * Returns a feature clipper for the given job, or null if the geometry doesn't need clipping.
	 * 
	 * @param job The job to create a feature clipper.
	 * @param logger The logger that can be used by the feature clipper to report errors and warnings.
	 * @return A feature clipper, or null.
	 */
	FeatureClipper getFeatureClipper (EtlJob job, JobLogger logger);
	
	/**
	 * Returns a dataset filter expression for the given job, or null if the dataset doesn't need filtering.
	 * 
	 * @param job The job to create a filter expression for.
	 * @return A dataset filter, or null.
	 */
	DatasetFilter getDatasetFilter (EtlJob job);
	
	/**
	 * Returns a codelist factory for this dataset. The codelist factory is invoked whenever
	 * a codelist is needed during validation.
	 * 
	 * @return A codelist factory that is able to return codelists for each codespace in this
	 * theme.
	 */
	CodeListFactory getCodeListFactory (EtlJob job);

	FeatureCollection retrieveFeaturesFromBronhouder(EtlJob job, FeatureProcessor featureProcessor, final EventLogger<AbstractProcess.MessageKey> userLog, DatasetMetadata md);
}
