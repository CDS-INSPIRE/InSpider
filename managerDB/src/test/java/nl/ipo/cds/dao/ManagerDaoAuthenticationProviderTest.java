package nl.ipo.cds.dao;

import static org.junit.Assert.fail;
import nl.ipo.cds.categories.IntegrationTests;
import nl.ipo.cds.domain.BronhouderThema;
import nl.ipo.cds.domain.Gebruiker;
import nl.ipo.cds.domain.TypeGebruik;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * Test cases for the {@link ManagerDaoAuthenticationProvider}. Tests whether authentication succeeds
 * for various types of users and whether the correct roles are assigned.
 */
@Category(IntegrationTests.class)
public class ManagerDaoAuthenticationProviderTest extends BaseLdapManagerDaoTest {

	private ManagerDaoAuthenticationProvider provider;
	
	/**
	 * Overriden to create a {@link ManagerDaoAuthenticationProvider} instance.
	 */
	@Override
	public void buildDB() throws Exception {
		super.buildDB ();
		
		provider = new ManagerDaoAuthenticationProvider (managerDao);
	}

	/**
	 * Test whether a superuser can authenticate and has the correct roles.
	 */
	@Test
	public void testAuthenticateSuperuser () {
		createUser ("test-admin", "test", true);
		
		final Authentication authentication = authenticate ("test-admin", "test");

		assertAuthority (authentication, "ROLE_USER");
		assertAuthority (authentication, "ROLE_SUPERUSER");
		assertAuthority (authentication, "ROLE_RAADPLEGER");
		assertAuthority (authentication, "ROLE_DATABEHEERDER");
		assertAuthority (authentication, "ROLE_VASTSTELLER");
	}

	/**
	 * Test whether providing a bad password for a superuser causes authentication to fail.
	 */
	@Test (expected = BadCredentialsException.class)
	public void testAuthenticateSuperuserFail () {
		createUser ("test-admin", "test", true);
		authenticate ("test-admin", "tset");
	}
	
	/**
	 * Test whether a normal can authenticate and has the correct roles.
	 */
	@Test
	public void testAuthenticateUser () {
		createUser ("test-user", "test", false);
		
		final Authentication authentication = authenticate ("test-user", "test");
		
		assertAuthority (authentication, "ROLE_USER");
		assertNotAuthority (authentication, "ROLE_SUPERUSER");
		assertNotAuthority (authentication, "ROLE_RAADPLEGER");
		assertNotAuthority (authentication, "ROLE_DATABEHEERDER");
		assertNotAuthority (authentication, "ROLE_VASTSTELLER");
	}
	
	/**
	 * Test whether providing a bad password for a normal user causes authentication to fail.
	 */
	@Test (expected = BadCredentialsException.class)
	public void testAuthenticateUserFail () {
		createUser ("test-user", "test", false);
		authenticate ("test-user", "tset");
	}
	
	/**
	 * Test whether a normal user with additiona authorization can authenticate and has the correct roles.
	 */
	@Test
	public void testAuthenticateDatabeheerder () {
		createUser ("test-user", "test", false);
		
		createGebruikerThemaAutorisatie ("Protected sites", "Limburg", "test-user", TypeGebruik.DATABEHEERDER);
		
		final Authentication authentication = authenticate ("test-user", "test");
		
		assertAuthority (authentication, "ROLE_USER");
		assertNotAuthority (authentication, "ROLE_SUPERUSER");
		assertAuthority (authentication, "ROLE_RAADPLEGER");
		assertAuthority (authentication, "ROLE_DATABEHEERDER");
		assertNotAuthority (authentication, "ROLE_VASTSTELLER");
	}
	
	/**
	 * Asserts that when a superuser is authenticated and there are no bronhouder-thema relations
	 * stored the superuser shouldn't be assigned the RAADPLEGER, DATABEHEERDER or VASTSTELLER roles.
	 */
	@Test
	public void testAuthenticateSuperuserWithoutBronhouderThema () {
		// Delete all BronhouderThema instances:
		for (final BronhouderThema bt: managerDao.getBronhouderThemas ()) {
			managerDao.delete (bt);
		}
		
		entityManager.flush ();
		
		createUser ("test-admin", "test", true);
		
		final Authentication authentication = authenticate ("test-admin", "test");
		
		assertAuthority (authentication, "ROLE_USER");
		assertAuthority (authentication, "ROLE_SUPERUSER");
		assertNotAuthority (authentication, "ROLE_RAADPLEGER");
		assertNotAuthority (authentication, "ROLE_DATABEHEERDER");
		assertNotAuthority (authentication, "ROLE_VASTSTELLER");
	}
	
    /**
     * Asserts that the user can login using the same credentials after saving the user. 
     */
    @Test
    public void testPasswordAfterSave () {
    	createUser ("test-admin", "test", true);
    	
    	final Authentication authentication = authenticate ("test-admin", "test");
    	
    	assertAuthority (authentication, "ROLE_VASTSTELLER");
    	
    	final Gebruiker user = managerDao.getGebruiker ("test-admin");
    	managerDao.update (user);
    	
    	entityManager.flush ();
    	
    	final Authentication authentication2 = authenticate ("test-admin", "test");
    	
    	assertAuthority (authentication2, "ROLE_VASTSTELLER");
    	
    }
	
	
	/**
	 * Asserts whether the given authentication token doesn't have the provided authority.
	 * 
	 * @param authentication	The authentication token to test.
	 * @param authority			The authority to look for.
	 */
	private static void assertNotAuthority (final Authentication authentication, final String authority) {
		for (final GrantedAuthority ga: authentication.getAuthorities ()) {
			if (ga.getAuthority ().equals (authority)) {
				fail ("Unexpected authority: " + authority);
			}
		}
	}
	
	/**
	 * Asserts whether the given authentication token has the provided authority.
	 * 
	 * @param authentication	The authentication token to test.
	 * @param authority			The authority to look for.
	 */
	private static void assertAuthority (final Authentication authentication, final String authority) {
		for (final GrantedAuthority ga: authentication.getAuthorities ()) {
			if (ga.getAuthority ().equals (authority)) {
				return;
			}
		}
		
		fail ("Missing authority: " + authority);
	}

	/**
	 * Convenience method to perform authentication on the authentication provider.
	 * 
	 * @param username	The username
	 * @param password	The password
	 * @return			The resulting authentication token.
	 */
	private Authentication authenticate (final String username, final String password) {
		return provider.authenticate (new UsernamePasswordAuthenticationToken (username, password));
	}
	
	/**
	 * Convenience helper for creating users.
	 * 
	 * @param username		The username of the user to create.
	 * @param superuser		Set to true if this user must have the superuser flag.
	 */
	private Gebruiker createUser (final String username, final String password, final boolean superuser) {
		final Gebruiker gebruiker = new Gebruiker ();
		
		gebruiker.setGebruikersnaam (username);
		gebruiker.setEmail ("mail@mail.local");
		gebruiker.setSuperuser (superuser);
		gebruiker.setWachtwoord (password);
		
		managerDao.create (gebruiker);
		
		entityManager.flush ();
		
		return gebruiker;
	}
}
