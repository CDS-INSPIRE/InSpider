package nl.ipo.cds.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Describes the type of authorization when authorizing a {@link DbGebruiker} with a {@link BronhouderThema}.
 */
public enum TypeGebruik {
	/**
	 * The user can access data in datasets belonging to a theme through the services.
	 */
	RAADPLEGER,
	
	/**
	 * The user can manage the data in datasets belonging to a theme.
	 */
	DATABEHEERDER (TypeGebruik.RAADPLEGER),
	
	/**
	 * The user can publish a dataset.
	 */
	VASTSTELLER (TypeGebruik.RAADPLEGER);

	private final Set<TypeGebruik> permissions;
	
	TypeGebruik () {
		this (new TypeGebruik[0]);
	}
	
	TypeGebruik (final TypeGebruik ... otherPermissions) {
		final Set<TypeGebruik> permissions = new HashSet<> ();
		
		permissions.add (this);
		for (final TypeGebruik other: otherPermissions) {
			permissions.add (other);
		}
		
		this.permissions = Collections.unmodifiableSet (permissions);
	}

	/**
	 * Returns true if the given typeGebruik is either equal to or implied by this type gebruik.
	 * 
	 * @param typeGebruik The type gebruik to test.
	 * @return True if "this" equals typeGebruik, or if typeGebruik is implied by this.
	 */
	public boolean isAllowed (final TypeGebruik typeGebruik) {
		return permissions.contains (typeGebruik);
	}
	
	public Set<TypeGebruik> getPermissions () {
		return Collections.unmodifiableSet (permissions);
	}
}
