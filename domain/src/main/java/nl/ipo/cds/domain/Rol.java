package nl.ipo.cds.domain;

/**
 * Possible roles of Inspire users.
 */
public enum Rol {
	
	/**
	 * The user is a 'bronhouder'. A GebruikersRol must exist for the user that has a non-null value for 'bronhouder'.
	 */
	BRONHOUDER,
	
	/**
	 * The user is an administrator. A GebruikersRol must exist for the user that has a null value for 'bronhouder'.
	 */
	BEHEERDER
}
