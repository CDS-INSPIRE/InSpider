package nl.ipo.cds.admin.config;

import java.util.Map;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobLogger;
import nl.ipo.cds.admin.ba.attributemapping.FeatureTypeCache;
import nl.ipo.cds.attributemapping.operations.discover.PropertyBeanIntrospector;
import nl.ipo.cds.etl.config.AttributeMapping;
import nl.ipo.cds.etl.config.FeatureProcessors;
import nl.ipo.cds.etl.config.FileCacheConfiguration;
import nl.ipo.cds.etl.config.Filtering;
import nl.ipo.cds.etl.config.Harvester;
import nl.ipo.cds.etl.config.Messages;
import nl.ipo.cds.etl.config.ScriptExecutorConfiguration;
import nl.ipo.cds.etl.config.ThemeConfiguration;
import nl.ipo.cds.etl.process.HarvesterFactory;
import nl.ipo.cds.etl.process.config.Processes;
import nl.ipo.cds.etl.theme.ThemeDiscoverer;
import nl.ipo.cds.etl.util.BlockingExecutor;
import nl.ipo.cds.executor.ConfigDir;
import nl.ipo.cds.metadata.config.MetadataManagerConfiguration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource (value = {
   	"classpath:nl/ipo/cds/admin/admin-applicationContext.xml",
   	"classpath:nl/ipo/cds/admin/admin-securityContext.xml",
   	"classpath:nl/ipo/cds/dao/dataSource-applicationContext.xml",
   	"classpath:nl/ipo/cds/dao/dao-applicationContext.xml",
   	"classpath:nl/ipo/cds/dao/metadata/dao-applicationContext.xml",
   	"classpath:nl/ipo/cds/nagios/nagios-status-client.xml",
   	"classpath:nl/ipo/cds/etl/reporting/geom/geometry-applicationContext.xml"
})
@Import (value = {
	AttributeMapping.class,
	Filtering.class,
	Harvester.class,
	ThemeConfiguration.class,
	FeatureProcessors.class,
	Processes.class,
	FileCacheConfiguration.class,
	Messages.class,
	ScriptExecutorConfiguration.class,
	MetadataManagerConfiguration.class
})
public class AdminConfig {
	
	@Bean
	public JobLogger stringLogger () {
		return new JobLogger() {
			
			@Override
			public void logString(Job job, String key, LogLevel logLevel, String message) {
			}

			@Override
			public void logString(Job job, String key, LogLevel logLevel,
					String message, Map<String, Object> context) {
			}
		};
	}
	
	@Bean
	public ConfigDir configDir (final @Value("file:${CONFIGDIR}") String configDirPath) {
		return new ConfigDir (configDirPath);
	}
	
	@Bean
	public Executor executer (final @Value("${numberOfThreads}") int numberOfThreads) {
		return new BlockingExecutor (numberOfThreads);
	}
	
	@Bean
	public PropertyBeanIntrospector propertyBeanIntrospector () {
		return new PropertyBeanIntrospector ();
	}
	
	@Bean
	@Inject
	public FeatureTypeCache featureTypeCache (final HarvesterFactory harvesterFactory, final ThemeDiscoverer themeDiscoverer) {
		return new FeatureTypeCache (harvesterFactory, themeDiscoverer);
	}
}
