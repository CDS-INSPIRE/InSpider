package nl.ipo.cds.dao;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import nl.ipo.cds.dao.impl.ManagerDaoImpl;
import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.Gebruiker;
import nl.ipo.cds.domain.GebruikersRol;
import nl.ipo.cds.domain.Rol;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.test.LdapTestUtils;

public class GebruikerDaoTest extends BaseManagerDaoTest {

	private static final DistinguishedName BASE_NAME = new DistinguishedName ("dc=inspire,dc=idgis,dc=eu");
	private static final String PRINCIPAL = "uid=admin,ou=system";
	private static final String CREDENTIALS = "secret";
	private static final int PORT = 10389;
	private static final String[] gebruikers = {
		"brabant",
		"drenthe",
		"flevoland",
		"fryslan",
		"gelderland",
		"groningen",
		"limburg",
		"noord-holland",
		"overijssel",
		"utrecht",
		"zeeland",
		"zuid-holland"
	};
	
	private LdapTemplate ldapTemplate;
	
	@BeforeClass
	public static void setUpClass () throws Exception {
		LdapTestUtils.startApacheDirectoryServer (PORT, BASE_NAME.toString (), "odm-test", PRINCIPAL, CREDENTIALS);
	}
	
	@AfterClass
	public static void tearDownClass () throws Exception {
		LdapTestUtils.destroyApacheDirectoryServer (PRINCIPAL, CREDENTIALS);
	}
	
    @Before @Override
    public void buildDB() throws Exception {
    	super.buildDB ();

    	
		// Bind to the LDAP directory:
		final LdapContextSource contextSource = new LdapContextSource ();
		contextSource.setUrl ("ldap://127.0.0.1:" + PORT + "/dc=inspire,dc=idgis,dc=eu");
		contextSource.setUserDn (PRINCIPAL);
		contextSource.setPassword (CREDENTIALS);
		contextSource.setPooled (false);
		contextSource.afterPropertiesSet ();
		
		// Create an LDAP template:
		ldapTemplate = new LdapTemplate (contextSource);
		
		LdapTestUtils.cleanAndSetup (ldapTemplate.getContextSource (), new DistinguishedName (), new ClassPathResource ("nl/ipo/cds/dao/testdata.ldif"));
		
		((ManagerDaoImpl)managerDao).setLdapTemplate (ldapTemplate);
    }

    // =========================================================================
    // Gebruiker CRUD:
    // =========================================================================
	@Test
	public void testGetGebruiker () {
		final Gebruiker gebruiker = managerDao.getGebruiker ("overijssel");
		
		assertNotNull (gebruiker);
		assertEquals ("overijssel", gebruiker.getGebruikersnaam ());
		assertEquals ("test@idgis.nl", gebruiker.getEmail ());
		assertNull (gebruiker.getMobile ());
		assertNotNull (gebruiker.getWachtwoordHash ());
	}
	
	@Test
	public void testGetGebruikerInvalid () {
		final Gebruiker gebruiker = managerDao.getGebruiker ("overijssl");
		
		assertNull (gebruiker);
	}
	
	@Test
	public void testGetAllGebruikers () {
		final List<Gebruiker> gebruikers = managerDao.getAllGebruikers ();
		
		assertEquals (GebruikerDaoTest.gebruikers.length, gebruikers.size ());
		
		final String[] names = new String[gebruikers.size ()];
		for (int i = 0; i < gebruikers.size (); ++ i) {
			names[i] = gebruikers.get (i).getGebruikersnaam ();
		}
		
		assertArrayEquals(GebruikerDaoTest.gebruikers, names);
	}
	
	@Test
	public void testCreateGebruiker () {
		final Gebruiker gebruiker = new Gebruiker ();
		
		gebruiker.setGebruikersnaam ("idgis");
		gebruiker.setEmail("test@idgis.nl");
		gebruiker.setMobile (null);
		gebruiker.setWachtwoord ("12test34");
		
		managerDao.create (gebruiker);
		
		final Gebruiker result = managerDao.getGebruiker ("idgis");
		
		assertNotNull (result);
		assertEquals ("idgis", result.getGebruikersnaam ());
		assertEquals ("test@idgis.nl", result.getEmail ());
		assertNull (result.getMobile ());
		assertNotNull (result.getWachtwoordHash ());
	}
	
	@Test(expected = RuntimeException.class)
	public void testCreateGebruikerInvalid () {
		final Gebruiker gebruiker = new Gebruiker ();
		gebruiker.setGebruikersnaam ("idgis");
		
		managerDao.create (gebruiker);
	}
	
	@Test(expected = NameAlreadyBoundException.class)
	public void testCreateGebruikerDuplicate () {
		final Gebruiker gebruiker = new Gebruiker ();
		
		gebruiker.setGebruikersnaam ("overijssel");
		gebruiker.setEmail("test@idgis.nl");
		gebruiker.setMobile (null);
		gebruiker.setWachtwoord ("12test34");
		
		managerDao.create (gebruiker);
	}
	
	@Test
	public void testDeleteGebruiker () {
		final Gebruiker gebruiker = managerDao.getGebruiker ("overijssel");
		
		assertNotNull (gebruiker);
		
		managerDao.delete (gebruiker);
		
		final Gebruiker deletedGebruiker = managerDao.getGebruiker ("overijssel");
		
		assertNull (deletedGebruiker);
	}
	
	@Test(expected = RuntimeException.class)
	public void testDeleteGebruikerInvalid () {
		final Gebruiker gebruiker = new Gebruiker ();
		
		gebruiker.setGebruikersnaam ("idgis");
		gebruiker.setEmail ("test@idgis.nl");
		gebruiker.setWachtwoord ("12test34");
		
		managerDao.delete (gebruiker);
	}
	
	@Test 
	public void testUpdateGebruiker () {
		final Gebruiker gebruiker = managerDao.getGebruiker ("overijssel");
		
		assertNotNull (gebruiker);
		
		gebruiker.setEmail ("test2@idgis.nl");
		
		managerDao.update (gebruiker);
		
		final Gebruiker updatedGebruiker = managerDao.getGebruiker ("overijssel");
		
		assertNotNull (updatedGebruiker);
		assertEquals ("test2@idgis.nl", updatedGebruiker.getEmail ());
	}
	
	@Test(expected = RuntimeException.class) 
	public void testUpdateGebruikerInvalid () {
		final Gebruiker gebruiker = new Gebruiker ();
		
		gebruiker.setGebruikersnaam ("idgis");
		gebruiker.setEmail("test@idgis.nl");
		gebruiker.setWachtwoord ("12test34");
		
		managerDao.update (gebruiker);
	}
	
	@Test(expected = RuntimeException.class) 
	public void testUpdateGebruikerConstraints () {
		final Gebruiker gebruiker = managerDao.getGebruiker ("overijssel");
		
		assertNotNull (gebruiker);
		
		gebruiker.setEmail (null);
		
		managerDao.update (gebruiker);
		
		final Gebruiker updatedGebruiker = managerDao.getGebruiker ("overijssel");
		
		assertNotNull (updatedGebruiker);
		assertEquals ("test2@idgis.nl", updatedGebruiker.getEmail ());
	}
	
    // =========================================================================
    // GebruikersRol:
    // =========================================================================
	@Test
	public void testAuthenticate () {
		final Gebruiker gebruiker = managerDao.getGebruiker ("overijssel");
		
		assertFalse (managerDao.authenticate(gebruiker.getGebruikersnaam(), "12test34"));
		
		gebruiker.setWachtwoord ("12test34");
		managerDao.update (gebruiker);
		
		assertTrue (managerDao.authenticate(gebruiker.getGebruikersnaam(), "12test34"));
	}
	
    // =========================================================================
    // GebruikersRol:
    // =========================================================================
	private GebruikersRol getRol (final Gebruiker gebruiker, final Rol rol, final Bronhouder bronhouder) {
		final List<GebruikersRol> rollen = managerDao.getGebruikersRollenByGebruiker (gebruiker);
		
		for (final GebruikersRol gr: rollen) {
			if (gr.getRol () != rol) {
				continue;
			}
			if ((gr.getBronhouder() == null && bronhouder != null) || (gr.getBronhouder () != null && bronhouder == null)) {
				continue;
			}
			if (bronhouder != null && gr.getBronhouder () != null && !bronhouder.getCommonName().equals(gr.getBronhouder ().getCommonName())) {
				continue;
			}
			
			return gr;
		}
		
		return null;
	}
	
	private boolean hasRol (final Gebruiker gebruiker, final Rol rol, final Bronhouder bronhouder) {
		return getRol (gebruiker, rol, bronhouder) != null;
	}
	
	public void assertHasRol (final Gebruiker gebruiker, final Rol rol, final Bronhouder bronhouder) {
		if (!hasRol (gebruiker, rol, bronhouder)) {
			fail (String.format ("Gebruiker does not have role `%s` (%s)", rol.toString (), bronhouder));
		}
	}
	
	public void assertHasNotRol (final Gebruiker gebruiker, final Rol rol, final Bronhouder bronhouder) {
		if (hasRol (gebruiker, rol, bronhouder)) {
			fail (String.format ("Gebruiker has role `%s` (%s)", rol.toString (), bronhouder));
		}
	}
	
	@Test
	public void testAddRolBeheerder () {
		final Gebruiker gebruiker = managerDao.getGebruiker ("overijssel");
		
		assertHasNotRol (gebruiker, Rol.BEHEERDER, null);
		managerDao.createGebruikersRol (gebruiker, Rol.BEHEERDER, null);
		assertHasRol (gebruiker, Rol.BEHEERDER, null);
	}
	
	@Test
	public void testRemoveRolBeheerder () {
		final Gebruiker gebruiker = managerDao.getGebruiker ("brabant");
		final GebruikersRol rol = getRol (gebruiker, Rol.BEHEERDER, null);
		
		assertNotNull (rol);
		
		managerDao.delete (rol);
		
		assertHasNotRol (gebruiker, Rol.BEHEERDER, null);
	}
	
	@Test(expected = RuntimeException.class)
	public void testRemoveRolBeheerderInvalid () {
		final Gebruiker gebruiker = managerDao.getGebruiker ("brabant");
		final GebruikersRol rol = getRol (gebruiker, Rol.BEHEERDER, null);
		
		managerDao.delete (rol);
		managerDao.delete (rol);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAddRolIllegalArgumentBeheerder () {
		managerDao.createGebruikersRol (managerDao.getGebruiker("overijssel"), Rol.BEHEERDER, managerDao.getBronhouderByCommonName ("overijssel"));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAddRolIllegalArgumentBronhouder () {
		managerDao.createGebruikersRol (managerDao.getGebruiker("overijssel"), Rol.BRONHOUDER, null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAddRolInvalidGebruiker () {
		final Gebruiker gebruiker = new Gebruiker ();
		
		gebruiker.setGebruikersnaam ("idgis");
		gebruiker.setEmail ("test@idgis.nl");
		gebruiker.setWachtwoord ("12test34");
		
		managerDao.createGebruikersRol (gebruiker, Rol.BEHEERDER, null);
	}

	/*
	@Test
	public void testAddRolBronhouder () {
		final Gebruiker gebruiker = managerDao.getGebruiker ("overijssel");
		final Bronhouder bronhouder = managerDao.getBronhouderByCode ("9923");
		
		assertHasNotRol (gebruiker, Rol.BRONHOUDER, bronhouder);
		
		managerDao.createGebruikersRol (gebruiker, Rol.BRONHOUDER, bronhouder);
		
		assertHasRol (gebruiker, Rol.BRONHOUDER, bronhouder);
	}
	*/
}
