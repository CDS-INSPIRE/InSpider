package nl.ipo.cds.domain;

/**
 * Describes the type of authorization when authorizing a {@link DbGebruiker} with a {@link BronhouderThema}.
 */
public enum TypeGebruik {
	/**
	 * The user can manage the data in datasets belonging to a theme.
	 */
	DATABEHEERDER,
	
	/**
	 * The user can access data in datasets belonging to a theme through the services.
	 */
	RAADPLEGER,
	
	/**
	 * The user can publish a dataset.
	 */
	VASTSTELLER
}
