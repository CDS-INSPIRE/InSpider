package nl.ipo.cds.etl.attributemapping;

import java.util.Map;
import java.util.Properties;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobLogger;
import nl.ipo.cds.dao.attributemapping.OperationDTO;
import nl.ipo.cds.domain.FeatureType;
import nl.ipo.cds.etl.PersistableFeature;
import nl.ipo.cds.etl.theme.AttributeDescriptor;
import nl.ipo.cds.etl.theme.ThemeConfig;

public class AttributeMappingFactory {

	private final Properties properties;
	
	public AttributeMappingFactory (final Properties properties) {
		this.properties = properties;
	}
	
	public <T extends PersistableFeature> AttributeMapper<T> createAttributeMapper (
			final Job job,
			final ThemeConfig<T> themeConfig, 
			final FeatureType featureType,
			final Map<AttributeDescriptor<?>, OperationDTO> attributeMappings,
			final JobLogger jobLogger) {
		
		return new AttributeMapper<T> (
				job,
				themeConfig, 
				featureType,
				attributeMappings,
				jobLogger,
				properties
			);
	}
}
