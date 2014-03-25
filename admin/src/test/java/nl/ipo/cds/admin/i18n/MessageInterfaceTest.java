package nl.ipo.cds.admin.i18n;

import static org.junit.Assert.assertEquals;

import javax.inject.Inject;

import nl.ipo.cds.admin.i18n.config.TestConfig;
import nl.ipo.cds.admin.i18n.messages.Login;

import org.junit.Test;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(classes = TestConfig.class)
public class MessageInterfaceTest extends AbstractJUnit4SpringContextTests {	
	
	@Inject
	Login login;

	@Test
	public void testInterface() {
		String username = login.username();
		assertEquals("Username", username);
		assertEquals("Logged in as: John Doe", login.loggedIn("John Doe"));
	}
}
