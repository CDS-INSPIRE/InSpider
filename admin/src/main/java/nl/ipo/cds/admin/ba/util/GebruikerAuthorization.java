package nl.ipo.cds.admin.ba.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.Gebruiker;
import nl.ipo.cds.domain.GebruikerThemaAutorisatie;
import nl.ipo.cds.domain.Thema;
import nl.ipo.cds.domain.TypeGebruik;

/**
 * Utility class to fetch various properties of the given user: authorized bronhouders, authorized
 * themes, etc. 
 */
public class GebruikerAuthorization {

	private final Gebruiker gebruiker;
	private final TypeGebruik typeGebruik;
	private final ManagerDao managerDao;
	
	public GebruikerAuthorization (final Gebruiker gebruiker, final TypeGebruik typeGebruik, final ManagerDao managerDao) {
		if (gebruiker == null) {
			throw new NullPointerException ("gebruiker cannot be null");
		}
		if (typeGebruik == null) {
			throw new NullPointerException ("typeGebruik cannot be null");
		}
		if (managerDao == null) {
			throw new NullPointerException ("managerDao cannot be null");
		}
		
		this.gebruiker = gebruiker;
		this.typeGebruik = typeGebruik;
		this.managerDao = managerDao;
	}

	/**
	 * Returns the user in this GebruikerAuthorization.
	 * 
	 * @return The user.
	 */
	public Gebruiker getGebruiker () {
		return gebruiker;
	}
	
	/**
	 * Returns all bronhouders for which this user has the given permissions.
	 * 
	 * @return An ordered collection of all bronhouders matching the given permission. Ordered by name.
	 */
	public Collection<Bronhouder> getAuthorizedBronhouders () {
		final SortedSet<Bronhouder> authorizedBronhouders = new TreeSet<Bronhouder> (new Comparator<Bronhouder> () {
			@Override
			public int compare (final Bronhouder o1, final Bronhouder o2) {
				return o1.getNaam ().compareTo (o2.getNaam ());
			}
		});

		// Superusers always have access to all bronhouders:
		if (gebruiker.isSuperuser ()) {
			authorizedBronhouders.addAll (managerDao.getAllBronhouders ());
			return Collections.unmodifiableCollection (authorizedBronhouders);
		}
		
		// Lookup authorized bronhouders:
		for (final GebruikerThemaAutorisatie gta: managerDao.getGebruikerThemaAutorisatie (gebruiker)) {
			if (!gta.getTypeGebruik ().isAllowed (typeGebruik)) {
				continue;
			}
			
			authorizedBronhouders.add (gta.getBronhouderThema ().getBronhouder ());
		}

		return Collections.unmodifiableCollection (authorizedBronhouders);
	}

	/**
	 * Returns all themes for which this user has the given permission.
	 * 
	 * @return				An ordered collection of all bronhouders matching the given permission. Ordered by name.
	 */
	public Collection<Thema> getAuthorizedThemas () {
		final SortedSet<Thema> authorizedThemes = new TreeSet<Thema> (new Comparator<Thema> () {
			@Override
			public int compare (final Thema o1, final Thema o2) {
				return o1.getNaam ().compareTo (o2.getNaam ());
			}
		});

		// Superusers have access to all themes:
		if (gebruiker.isSuperuser ()) {
			authorizedThemes.addAll (managerDao.getAllThemas ());
			return Collections.unmodifiableCollection (authorizedThemes);
		}
		
		// Locate authorized themes:
		for (final GebruikerThemaAutorisatie gta: managerDao.getGebruikerThemaAutorisatie (gebruiker)) {
			if (!gta.getTypeGebruik ().isAllowed (typeGebruik)) {
				continue;
			}
			
			authorizedThemes.add (gta.getBronhouderThema ().getThema ());
		}
		
		return Collections.unmodifiableCollection (authorizedThemes);
	}

	/**
	 * Looks up the bronhouder identified by the given ID and returns it if it
	 * exists and if the user has permissions to use it. If the user has no permissions,
	 * a default bronhouder is returned. If that doesn't exist, null is returned.
	 * 
	 * @param bronhouderId	The ID of the bronhouder, or null.
	 * @return				The bronhouder, or null if it doesn't exist or if the user has no permissions.
	 */
	public Bronhouder getAuthorizedBronhouder (final Long bronhouderId) {
		final Collection<Bronhouder> authorizedBronhouders = getAuthorizedBronhouders ();
		final Bronhouder defaultBronhouder = authorizedBronhouders.isEmpty () ? null : authorizedBronhouders.iterator ().next ();
		
		if (bronhouderId == null) {
			return defaultBronhouder;
		}
		
		final Bronhouder bronhouder = managerDao.getBronhouder (bronhouderId);
		if (bronhouder == null) {
			return defaultBronhouder;
		}
		
		// Test whether the user is authorized for this bronhouder:
		if (gebruiker.isSuperuser ()) {
			return bronhouder;
		}
		
		return authorizedBronhouders.contains (bronhouder) ? bronhouder : defaultBronhouder;
	}

	/**
	 * Looks up the theme identified by the given ID and returns it if it exists
	 * and if the user has permissions to use it.
	 * 
	 * @param themaId		The ID of the theme, or null.
	 * @return				The theme, or null if it doesn't exist or if the user has no permissions.
	 */
	public Thema getAuthorizedThema (final Long themaId) {
		final Collection<Thema> authorizedThemas = getAuthorizedThemas ();
		final Thema defaultThema = authorizedThemas.isEmpty () ? null : authorizedThemas.iterator ().next ();
		
		if (themaId == null) {
			return defaultThema;
		}
		
		final Thema thema = managerDao.getThema (themaId);
		if (thema == null) {
			return defaultThema;
		}
		
		// Test whether the user is authorized for this theme:
		if (gebruiker.isSuperuser ()) {
			return thema;
		}
		
		return getAuthorizedThemas ().contains (thema) ? thema : null;
	}

	/**
	 * Returns a list of all authorized themes for the current user that are associated with the given
	 * bronhouder.
	 * 
	 * @param bronhouder The bronhouder instance to use as a filter.
	 * @return A collection of authorized themes, associated with the given bronhouder.
	 */
	public Collection<Thema> getAuthorizedThemas (final Bronhouder bronhouder) {
		// Superusers have access to all themes linked to the given bronhouder:
		if (gebruiker.isSuperuser ()) {
			return Collections.unmodifiableCollection (managerDao.getAllThemas (bronhouder));
		}
		
		final List<Thema> result = new ArrayList<Thema> ();
		
		for (final GebruikerThemaAutorisatie gta: managerDao.getGebruikerThemaAutorisatie (bronhouder)) {
			if (gta.getGebruiker ().getGebruikersnaam ().equals (getGebruiker ().getGebruikersnaam ()) && gta.getTypeGebruik ().isAllowed (typeGebruik)) {
				result.add (gta.getBronhouderThema ().getThema ());
			}
		}
		
		return Collections.unmodifiableCollection (result);
	}

	/**
	 * Returns a specific theme identified by themeName and associated with bronhouder, for which the current
	 * user has permissions.
	 * 
	 * @param themaName		The theme name to use as a filter, or null.
	 * @param bronhouder	The bronhouder to use as a filter.
	 * @return				The theme identified by name, or the default theme, or null if there are no themes.
	 */
	public Thema getAuthorizedThemaByName (final String themaName, final Bronhouder bronhouder) {
		final Collection<Thema> authorizedThemas = getAuthorizedThemas (bronhouder);
		final Thema defaultTheme = authorizedThemas.isEmpty () ? null : authorizedThemas.iterator ().next ();
		
		if (themaName == null) {
			return defaultTheme;
		}
		
		for (final Thema thema: authorizedThemas) {
			if (thema.getNaam ().equals (themaName)) {
				return thema;
			}
		}
		
		return defaultTheme;
	}
}
