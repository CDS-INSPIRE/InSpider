package nl.ipo.cds.admin.i18n;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import nl.ipo.cds.admin.i18n.config.TestConfig;
import nl.ipo.cds.admin.i18n.messages.Login;

import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(classes = {TestConfig.class, VelocityAdapterTest.Config.class})
public class VelocityAdapterTest extends AbstractJUnit4SpringContextTests {
	
	@Inject
	VelocityAdapter velocityAdapter;
	
	@Configuration
	static class Config {
		
		@Bean
		public VelocityAdapter velocityAdapter() {
			return new VelocityAdapter();
		}
	}

	@Test
	public void testLoginUsername() {
		Object login = velocityAdapter.get("login");
		assertNotNull(login);
		assertTrue(login instanceof Login);
		assertEquals("Username", ((Login)login).username());
	}
	
	@Test
	public void testNotDefined() {		
		assertNull(velocityAdapter.get("notDefined"));
	}
}
