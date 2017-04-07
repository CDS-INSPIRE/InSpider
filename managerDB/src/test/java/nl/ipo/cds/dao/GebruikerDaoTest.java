package nl.ipo.cds.dao;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import nl.ipo.cds.categories.IntegrationTests;
import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.BronhouderThema;
import nl.ipo.cds.domain.DbGebruiker;
import nl.ipo.cds.domain.Gebruiker;
import nl.ipo.cds.domain.GebruikerThemaAutorisatie;
import nl.ipo.cds.domain.Thema;
import nl.ipo.cds.domain.TypeGebruik;

import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTests.class)
public class GebruikerDaoTest extends BaseLdapManagerDaoTest {

    // =========================================================================
    // Gebruiker CRUD:
    // =========================================================================
	@Test
	public void testGetGebruiker () {
		final Gebruiker gebruiker = managerDao.getGebruiker ("overijssel");
		
		assertNotNull (gebruiker);
		assertEquals ("uid=overijssel,ou=OtherPeople", gebruiker.getLdapGebruiker ().getDistinguishedName ());
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
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateGebruikerDuplicate () {
		final Gebruiker gebruiker = new Gebruiker ();
		
		gebruiker.setGebruikersnaam ("overijssel");
		gebruiker.setEmail("test@idgis.nl");
		gebruiker.setMobile (null);
		gebruiker.setWachtwoord ("12test34");
		
		managerDao.create (gebruiker);
	}

	/**
	 * Test delete a user without database backing. Such a user only exists in LDAP and the additional properties
	 * stored in the database have default values.
	 */
	@Test
	public void testDeleteGebruiker () {
		final Gebruiker gebruiker = managerDao.getGebruiker ("overijssel");
		
		assertNotNull (gebruiker);
		
		managerDao.delete (gebruiker);
		
		final Gebruiker deletedGebruiker = managerDao.getGebruiker ("overijssel");
		
		assertNull (deletedGebruiker);
	}
	
	/**
	 * Test whether a user with database backing can be deleted. A user has database backing when the
	 * superuser flag is persisted.
	 */
	@Test
	public void testDeleteGebruikerWithDatabaseBacking () {
		final Gebruiker gebruiker = new Gebruiker ();
		
		gebruiker.setGebruikersnaam ("test-with-db-backing");
		gebruiker.setEmail ("mail@mail.local");
		gebruiker.setSuperuser (true);
		gebruiker.setWachtwoord ("abcde");
		
		managerDao.create (gebruiker);
		
		entityManager.flush ();
		
		final Gebruiker gebruikerToDelete = managerDao.getGebruiker ("test-with-db-backing");
		
		assertNotNull (gebruikerToDelete);
		assertTrue (gebruikerToDelete.isSuperuser ());
		
		managerDao.delete (gebruiker);
		
		entityManager.flush ();
		
		final Gebruiker deletedGebruiker = managerDao.getGebruiker ("test-with-db-backing");
		
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
	
	/**
	 * Verifies that a user initially has no database backing and that
	 * a corresponding record is inserted into the database when
	 * persisting.
	 */
	public @Test void testGebruikerCreateDatabaseBacking () throws Throwable {
		final Gebruiker gebruiker = managerDao.getGebruiker ("flevoland");
		
		entityManager.flush ();
		
		assertNull (entityManager.find (DbGebruiker.class, gebruiker.getGebruikersnaam ()));
		
		gebruiker.setSuperuser (true);
		
		managerDao.update (gebruiker);
		
		entityManager.flush ();
		
		final DbGebruiker dbGebruiker = entityManager.find (DbGebruiker.class, gebruiker.getGebruikersnaam ());
		
		assertNotNull (dbGebruiker);
		assertEquals ("flevoland", dbGebruiker.getGebruikersnaam ());
		assertTrue (dbGebruiker.isSuperuser ());
	}
	
	public @Test void testGetAllThemasForBronhouder () throws Throwable {
    	createGebruikerThemaAutorisatie ();
		
		final Thema thema = managerDao.getThemaByName ("Protected sites");
		final Bronhouder bronhouder = managerDao.getBronhouderByCommonName ("overijssel");
		final BronhouderThema bronhouderThema = new BronhouderThema (thema, bronhouder);
		
		entityManager.persist (bronhouderThema);
		
		entityManager.flush ();
		
		final List<Thema> themas = managerDao.getAllThemas (bronhouder);
		
		assertNotNull (themas);
		assertEquals (2, themas.size ());
		assertEquals ("Protected sites", themas.get (0).getNaam ());
		assertEquals ("Thema 2", themas.get (1).getNaam ());
	}
	
    @Test
    public void testCreateGebruikerThemaAutorisatie () throws Throwable {
    	createGebruikerThemaAutorisatie ();

    	assertEquals (4, entityManager.createQuery ("from GebruikerThemaAutorisatie", GebruikerThemaAutorisatie.class).getResultList ().size ());
    	
    	final Gebruiker gebruiker = managerDao.getGebruiker ("utrecht");
    	final BronhouderThema bronhouderThema = managerDao.getBronhouderThemas ().get (0);
    	
    	managerDao.createGebruikerThemaAutorisatie (gebruiker, bronhouderThema, TypeGebruik.DATABEHEERDER);
    	
    	entityManager.flush ();
    	
    	assertEquals (5, entityManager.createQuery ("from GebruikerThemaAutorisatie", GebruikerThemaAutorisatie.class).getResultList ().size ());
    	
    	final List<GebruikerThemaAutorisatie> result = entityManager
    		.createQuery ("from GebruikerThemaAutorisatie gta where gta.gebruiker.gebruikersnaam = ?1 and gta.bronhouderThema.bronhouder.naam = ?2 and gta.bronhouderThema.thema.naam = ?3", GebruikerThemaAutorisatie.class)
    		.setParameter (1, "utrecht")
    		.setParameter (2, bronhouderThema.getBronhouder ().getNaam ())
    		.setParameter (3, bronhouderThema.getThema ().getNaam ())
    		.getResultList ();
    	
    	assertEquals (1, result.size ());
    	assertEquals (TypeGebruik.DATABEHEERDER, result.get (0).getTypeGebruik ());
    }
    
    @Test
    public void testDeleteGebruikerThemaAutorisatie () {
    	createGebruikerThemaAutorisatie ();
    	
    	final GebruikerThemaAutorisatie gta = entityManager
    			.createQuery ("from GebruikerThemaAutorisatie", GebruikerThemaAutorisatie.class)
    			.getResultList ()
    			.get (0);
    	
    	assertEquals (4, entityManager.createQuery ("from GebruikerThemaAutorisatie", GebruikerThemaAutorisatie.class).getResultList ().size ());

    	managerDao.delete (gta);
    	
    	entityManager.flush ();
    	
    	assertEquals (3, entityManager.createQuery ("from GebruikerThemaAutorisatie", GebruikerThemaAutorisatie.class).getResultList ().size ());
    }
    
    @Test
    public void testGetGebruikerThemaAutorisatie () {
    	createGebruikerThemaAutorisatie ();
    	
    	final List<GebruikerThemaAutorisatie> gtas = managerDao.getGebruikerThemaAutorisatie ();
    	
    	assertEquals (4, gtas.size ());
    	
    	assertEquals ("drenthe", gtas.get (0).getGebruiker ().getGebruikersnaam ());
    	assertEquals ("limburg", gtas.get (1).getGebruiker ().getGebruikersnaam ());
    	assertEquals ("noord-holland", gtas.get (2).getGebruiker ().getGebruikersnaam ());
    	assertEquals ("overijssel", gtas.get (3).getGebruiker ().getGebruikersnaam ());
    	
    	assertEquals ("Drenthe", gtas.get (0).getBronhouderThema ().getBronhouder ().getNaam ());
    	assertEquals ("Limburg", gtas.get (1).getBronhouderThema ().getBronhouder ().getNaam ());
    	assertEquals ("Noord-Holland", gtas.get (2).getBronhouderThema ().getBronhouder ().getNaam ());
    	assertEquals ("Overijssel", gtas.get (3).getBronhouderThema ().getBronhouder ().getNaam ());
    	
    	assertEquals ("Protected sites", gtas.get (0).getBronhouderThema ().getThema ().getNaam ());
    	assertEquals ("Protected sites", gtas.get (1).getBronhouderThema ().getThema ().getNaam ());
    	assertEquals ("Thema 2", gtas.get (2).getBronhouderThema ().getThema ().getNaam ());
    	assertEquals ("Thema 2", gtas.get (3).getBronhouderThema ().getThema ().getNaam ());
    	
    	assertEquals (TypeGebruik.RAADPLEGER, gtas.get (0).getTypeGebruik ());
    	assertEquals (TypeGebruik.RAADPLEGER, gtas.get (1).getTypeGebruik ());
    	assertEquals (TypeGebruik.RAADPLEGER, gtas.get (2).getTypeGebruik ());
    	assertEquals (TypeGebruik.RAADPLEGER, gtas.get (3).getTypeGebruik ());
    }
    
    @Test
    public void testGetGebruikerThemaAutorisatieByGebruiker () {
    	createGebruikerThemaAutorisatie ();
    	
    	final List<GebruikerThemaAutorisatie> gtas = managerDao.getGebruikerThemaAutorisatie (managerDao.getGebruiker ("drenthe"));
    	
    	assertEquals (1, gtas.size ());
    	assertEquals ("drenthe", gtas.get (0).getGebruiker ().getGebruikersnaam ());
    	assertEquals ("Drenthe", gtas.get (0).getBronhouderThema ().getBronhouder ().getNaam ());
    	assertEquals ("Protected sites", gtas.get (0).getBronhouderThema ().getThema ().getNaam ());
    	assertEquals (TypeGebruik.RAADPLEGER, gtas.get (0).getTypeGebruik ());
    }
    
    @Test
    public void testGetGebruikerThemaAutorisatieByBronhouder () {
    	createGebruikerThemaAutorisatie ();
    	
    	final List<GebruikerThemaAutorisatie> gtas = managerDao.getGebruikerThemaAutorisatie (managerDao.getBronhouderByNaam ("Drenthe"));
    	
    	assertEquals (1, gtas.size ());
    	assertEquals ("drenthe", gtas.get (0).getGebruiker ().getGebruikersnaam ());
    	assertEquals ("Drenthe", gtas.get (0).getBronhouderThema ().getBronhouder ().getNaam ());
    	assertEquals ("Protected sites", gtas.get (0).getBronhouderThema ().getThema ().getNaam ());
    	assertEquals (TypeGebruik.RAADPLEGER, gtas.get (0).getTypeGebruik ());
    }
    
    @Test
    public void testGetGebruikerThemaAutorisatieByThema () {
    	createGebruikerThemaAutorisatie ();
    	
    	final List<GebruikerThemaAutorisatie> gtas = managerDao.getGebruikerThemaAutorisatie (managerDao.getThemaByName ("Protected sites"));
    	
    	assertEquals (2, gtas.size ());
    	assertEquals ("drenthe", gtas.get (0).getGebruiker ().getGebruikersnaam ());
    	assertEquals ("Drenthe", gtas.get (0).getBronhouderThema ().getBronhouder ().getNaam ());
    	assertEquals ("Protected sites", gtas.get (0).getBronhouderThema ().getThema ().getNaam ());
    	assertEquals (TypeGebruik.RAADPLEGER, gtas.get (0).getTypeGebruik ());
    	
    	assertEquals ("limburg", gtas.get (1).getGebruiker ().getGebruikersnaam ());
    	assertEquals ("Limburg", gtas.get (1).getBronhouderThema ().getBronhouder ().getNaam ());
    	assertEquals ("Protected sites", gtas.get (1).getBronhouderThema ().getThema ().getNaam ());
    	assertEquals (TypeGebruik.RAADPLEGER, gtas.get (1).getTypeGebruik ());
    }
}
