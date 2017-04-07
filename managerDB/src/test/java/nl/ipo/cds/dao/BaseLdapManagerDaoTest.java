package nl.ipo.cds.dao;

import nl.ipo.cds.dao.impl.ManagerDaoImpl;
import nl.ipo.cds.domain.BronhouderThema;
import nl.ipo.cds.domain.Gebruiker;
import nl.ipo.cds.domain.GebruikerThemaAutorisatie;
import nl.ipo.cds.domain.TypeGebruik;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.test.LdapTestUtils;

public abstract class BaseLdapManagerDaoTest extends BaseManagerDaoTest {

	protected static final DistinguishedName BASE_NAME = new DistinguishedName ("dc=inspire,dc=idgis,dc=eu");
	protected static final String PRINCIPAL = "uid=admin,ou=system";
	protected static final String CREDENTIALS = "secret";
	protected static final int PORT = 10389;
	protected static final String[] gebruikers = {
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
	
	protected LdapTemplate ldapTemplate;
	
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
		
        entityManager.flush ();
    }
    
    protected void createGebruikerThemaAutorisatie () {
		// Create GebruikerThemaAutorisatie instances for testing:
        createGebruikerThemaAutorisatie ("Thema 2", "Overijssel", "overijssel", TypeGebruik.RAADPLEGER);
        createGebruikerThemaAutorisatie ("Protected sites", "Limburg", "limburg", TypeGebruik.RAADPLEGER);
        createGebruikerThemaAutorisatie ("Thema 2", "Noord-Holland", "noord-holland", TypeGebruik.RAADPLEGER);
        createGebruikerThemaAutorisatie ("Protected sites", "Drenthe", "drenthe", TypeGebruik.RAADPLEGER);
        
    	entityManager.flush ();
    }

	protected GebruikerThemaAutorisatie createGebruikerThemaAutorisatie (final String themeName, final String bronhouderName, final String gebruikerName, final TypeGebruik typeGebruik) {
		final Gebruiker gebruiker = managerDao.getGebruiker (gebruikerName);
		
		// Create database backing for the user:
		managerDao.update (gebruiker);
		
		final BronhouderThema bronhouderThema = entityManager
			.createQuery ("from BronhouderThema bt where bt.bronhouder.naam = ?1 and bt.thema.naam = ?2", BronhouderThema.class)
			.setParameter (1, bronhouderName)
			.setParameter (2, themeName)
			.getSingleResult ();
		
		final GebruikerThemaAutorisatie gta = new GebruikerThemaAutorisatie (gebruiker.getDbGebruiker (), bronhouderThema, typeGebruik);
		
		entityManager.persist (gta);
		
		return gta;
	}
}
