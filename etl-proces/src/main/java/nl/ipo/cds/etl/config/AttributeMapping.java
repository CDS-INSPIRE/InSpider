package nl.ipo.cds.etl.config;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

import nl.ipo.cds.attributemapping.operations.discover.OperationDiscoverer;
import nl.ipo.cds.attributemapping.operations.discover.annotation.AnnotationOperationDiscoverer;
import nl.ipo.cds.etl.attributemapping.AttributeMappingFactory;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@ComponentScan (basePackageClasses = nl.ipo.cds.etl.operations.Package.class)
public class AttributeMapping {

	@Bean
	public OperationDiscoverer operationDiscoverer () {
		return new AnnotationOperationDiscoverer ();
	}
	
	@Bean
	@Inject
	public AttributeMappingFactory attributeMappingFactory (final @Named("attributeMappingValidatorMessages") Properties messages) {
		return new AttributeMappingFactory (messages);
	}
	
	@Configuration
	public static class Messages {
		@Bean
		public PropertiesFactoryBean attributeMappingValidatorMessages () {
			final PropertiesFactoryBean properties = new PropertiesFactoryBean ();
			
			properties.setLocation (new ClassPathResource ("nl/ipo/cds/etl/attributemapping/validator.messages"));

			return properties;
		}
	}

}
