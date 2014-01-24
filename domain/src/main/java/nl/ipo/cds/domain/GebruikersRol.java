package nl.ipo.cds.domain;

/**
 * The 'GebruikersRol' represents a relation between a user, a role and an optional 'bronhouder'. Roles are immutable and
 * the implementation is provided by the DAO. Altering a role is done through the DAO.
 *
 */
public interface GebruikersRol {
	
	/**
	 * @return the gebruiker
	 */
	public Gebruiker getGebruiker ();
	
	/**
	 * @return the rol
	 */
	public Rol getRol ();
	
	/**
	 * 
	 * @return the bronhouder
	 */
	public Bronhouder getBronhouder ();
}