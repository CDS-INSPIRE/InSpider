package nl.ipo.cds.etl.generalization;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.concurrent.Executor;

import nl.ipo.cds.categories.IntegrationTests;
import nl.ipo.cds.etl.util.BlockingExecutor;
import nl.ipo.cds.executor.ConfigDir;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = GeneralizerTest.Config.class)
@Category(IntegrationTests.class)
public class GeneralizerTest {
	
	@Autowired
	private GeneralizeReader generalizeReader;
	
	@Autowired
	private GeneralizerConfig generalizerConfig;

	@Configuration
	@ComponentScan (basePackageClasses = nl.ipo.cds.executor.config.Package.class)
	@ImportResource ({
		"classpath:/nl/ipo/cds/dao/dao-applicationContext.xml",
		"classpath:/nl/ipo/cds/dao/dataSource-applicationContext.xml",
		"classpath:/nl/ipo/cds/context/propertyConfigurer-test.xml"
	})
	public static class Config {
		@Bean
		public Executor executer () {
			return new BlockingExecutor (2);
		}
		
		@Bean
		public ConfigDir configDir () {
			return new ConfigDir ("classpath:");
		}
	}
	
	@Test
	public void testQuery() throws Exception {
		for(GeneralizeJob job : generalizerConfig.getGeneralizeJobs()) {
			String query = job.getQuery();
			String destination = job.getDestination();
			
			assertNotNull(query);
			assertNotNull(destination);
			
			assertFalse(query.trim().length() == 0);
			assertFalse(destination.trim().length() == 0);
		}
	}
	
	//@Test
	public void testExecute() throws Exception {
		generalizeReader.populate();
	}
}
