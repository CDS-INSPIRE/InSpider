package nl.ipo.cds.admin.ba.controller.gebruikersbeheer.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.ipo.cds.domain.Gebruiker;
import nl.ipo.cds.domain.GebruikerThemaAutorisatie;

/**
 * Container bean for {@link Gebruiker} and an associated list of {@link GebruikerThemaAutorisatie} for that user. Represents
 * the themes that the given user is authorized to use, and the permissions that apply.
 * Used to pass data between controller and view.
 */
public final class GebruikerThemas {

	private final Gebruiker gebruiker;
	private final List<GebruikerThemaAutorisatie> gebruikerThemas;

	/**
	 * Constructs a new GebruikerThemas by providing a user and associated authorization of that user on themes.
	 * 
	 * @param gebruiker
	 * @param bronhouderThemas
	 */
	public GebruikerThemas (final Gebruiker gebruiker, final List<GebruikerThemaAutorisatie> gebruikerThemas) {
		if (gebruiker == null) {
			throw new NullPointerException ("gebruiker cannot be null");
		}
		
		this.gebruiker = gebruiker;
		this.gebruikerThemas = gebruikerThemas == null || gebruikerThemas.isEmpty () ? Collections.<GebruikerThemaAutorisatie>emptyList () : new ArrayList<GebruikerThemaAutorisatie> (gebruikerThemas);
	}

	/**
	 * Returns the user.
	 * 
	 * @return The user for which this object containts the authorization.
	 */
	public Gebruiker getGebruiker () {
		return gebruiker;
	}

	/**
	 * Returns the list of {@link GebruikerThemaAutorisatie} describing the authorization of
	 * this user.
	 * 
	 * @return The authorization for this user.
	 */
	public List<GebruikerThemaAutorisatie> getGebruikerThemas () {
		return Collections.unmodifiableList (gebruikerThemas);
	}
}
