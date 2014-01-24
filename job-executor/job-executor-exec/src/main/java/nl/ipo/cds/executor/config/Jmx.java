package nl.ipo.cds.executor.config;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import nl.idgis.commons.jobexecutor.JobExecutor;
import nl.ipo.cds.executor.JobExecuterMBean;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.annotation.AnnotationJmxAttributeSource;
import org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler;
import org.springframework.jmx.export.metadata.JmxAttributeSource;
import org.springframework.jmx.support.RegistrationPolicy;

@Configuration
public class Jmx {

	@Configuration
	public static class MBeans {
		
		@Bean
		@Inject
		public JobExecuterMBean jobExecuterMBean (final JobExecutor jobExecutor) {
			final JobExecuterMBean mbean = new JobExecuterMBean (jobExecutor);
			
			return mbean;
		}
		
		@Bean
		@Inject
		public MetadataMBeanInfoAssembler assembler (final JmxAttributeSource attributeSource) {
			final MetadataMBeanInfoAssembler assembler = new MetadataMBeanInfoAssembler ();
			
			assembler.setAttributeSource (attributeSource);
			
			return assembler;
		}
		
		@Bean
		public AnnotationJmxAttributeSource jmxAttributeSource () {
			return new AnnotationJmxAttributeSource ();
		}
	}
	
	@Bean
	@Inject
	public MBeanExporter exporter (final JobExecuterMBean jobExecuterMBean, final MetadataMBeanInfoAssembler assembler) {
		final MBeanExporter exporter = new MBeanExporter ();
		final Map<String, Object> beans = new HashMap<String, Object> ();
		
		beans.put ("nl.ipo.cds.etl:type=JobExecuter", jobExecuterMBean);
		
		exporter.setBeans (beans);
		exporter.setAssembler (assembler);
		exporter.setRegistrationPolicy(RegistrationPolicy.REPLACE_EXISTING);
		
		return exporter;
	}
}
