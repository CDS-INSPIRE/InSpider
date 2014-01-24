package nl.ipo.cds.etl.log;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;

import nl.idgis.commons.jobexecutor.JobLogger.LogLevel;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.domain.ImportJob;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(classes = { LogStringBuilderTest.Config.class })
public class LogStringBuilderTest extends AbstractJUnit4SpringContextTests {
	
	@Configuration
	public static class Config {
		@Bean
		@Inject
		public LogStringBuilder<MessageKey> eventLogger (
				final @Named ("listStringLogger") ListStringLogger listStringLogger,
				final @Named ("testLogProperties") Properties logProperties) {
			final LogStringBuilder<MessageKey> builder = new LogStringBuilder<MessageKey> ();
			
			builder.setJobLogger (listStringLogger);
			builder.setProperties (logProperties);
			
			return builder;
		}
		
		@Bean
		public ListStringLogger listStringLogger () {
			return new ListStringLogger ();
		}
		
		@Configuration
		public static class Messages {
			
			@Bean
			public PropertiesFactoryBean testLogProperties () {
				final PropertiesFactoryBean properties = new PropertiesFactoryBean ();
				
				properties.setLocation (new ClassPathResource ("nl/ipo/cds/etl/log/test.messages"));

				return properties;				
			}
		}
	}
	
	@Autowired
	private LogStringBuilder<MessageKey> eventLogger;

	@Autowired
	private ListStringLogger listStringLogger;

	private EtlJob job;
	
	enum MessageKey {
		MESSAGE0, MESSAGE1;
	}

	@Before
	public void setUp() {
		job = new ImportJob();
		this.listStringLogger.reset();
	}
	
	@Test
	public void testNoProperty() {
		assertEquals(0, listStringLogger.getLog().size());
		
		eventLogger.logEvent(job, MessageKey.MESSAGE0, LogLevel.ERROR);
		assertEquals(1, listStringLogger.getLog().size());
		assertEquals("MESSAGE0", listStringLogger.getLog().get(0));
		
		eventLogger.logEvent(job, MessageKey.MESSAGE0, LogLevel.ERROR, "param0");
		assertEquals(2, listStringLogger.getLog().size());
		assertEquals("MESSAGE0: param0", listStringLogger.getLog().get(1));
		
		eventLogger.logEvent(job, MessageKey.MESSAGE0, LogLevel.ERROR, "param0", "param1");
		assertEquals(3, listStringLogger.getLog().size());
		assertEquals("MESSAGE0: param0, param1", listStringLogger.getLog().get(2));
	}
	
	@Test
	public void testProperty() {		
		eventLogger.logEvent(job, MessageKey.MESSAGE1, LogLevel.ERROR, "value0", "value1");
		assertEquals(1, listStringLogger.getLog().size());
		assertEquals("A nice text string for MESSAGE1, param1: value1, param0: value0, $", listStringLogger.getLog().get(0));
	}
}
