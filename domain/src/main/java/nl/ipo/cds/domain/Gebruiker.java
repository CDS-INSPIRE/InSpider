package nl.ipo.cds.domain;

import javax.validation.Valid;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Describes a user by composing a {@link LdapGebruiker} and a {@link DbGebruiker}. All
 * getters and setters are delegated to one of the backings.
 * 
 * Using the LDAP backing, common properties of the user are stored as an inetOrgPerson.
 * Additional InSpider specific properties are stored in the optional database backing.
 */
public final class Gebruiker {
	
	@Valid
	private final LdapGebruiker ldapGebruiker;
	
	@Valid
	private final DbGebruiker dbGebruiker;
	
	/**
	 * Creates a new user with an empty LDAP user and no DB backing.
	 */
	public Gebruiker () {
		this (new LdapGebruiker (), null);
	}
	
	/**
	 * Creates a new user from the given LDAP and DB backings.
	 * 
	 * @param ldapGebruiker The LDAP backing of this user. Required attribute.
	 * @param dbGebruiker The database backing of this user. Optional, can be null.
	 */
	public Gebruiker (final LdapGebruiker ldapGebruiker, final DbGebruiker dbGebruiker) {
		if (ldapGebruiker == null) {
			throw new NullPointerException ("ldapGebruiker cannot be null");
		}
		
		this.ldapGebruiker = ldapGebruiker;
		this.dbGebruiker = dbGebruiker == null ? new DbGebruiker (ldapGebruiker.getGebruikersnaam ()) : dbGebruiker;
	}

	/**
	 * Returns the LDAP backing of this user.
	 * 
	 * @see LdapGebruiker
	 * @return The LDAP backing of this user.
	 */
	public LdapGebruiker getLdapGebruiker () {
		return ldapGebruiker;
	}

	/**
	 * Returns the DB backing of this user.
	 * 
	 * @see DbGebruiker
	 * @return The database backing of this user.
	 */
	public DbGebruiker getDbGebruiker () {
		return dbGebruiker;
	}
	
	/**
	 * Returns the username of this user. The username also corresponds with the 'uid' attribute in LDAP.
	 * 
	 * @return The username.
	 */
	public String getGebruikersnaam () {
		return ldapGebruiker.getGebruikersnaam ();
	}
	
	/**
	 * Sets the username of this user. The username corresponds with the 'uid' attribute in LDAP.
	 * 
	 * @param gebruikersnaam
	 */
	public void setGebruikersnaam (final String gebruikersnaam) {
		ldapGebruiker.setGebruikersnaam (gebruikersnaam);
		dbGebruiker.setGebruikersnaam (gebruikersnaam);
	}
	
	/**
	 * Returns the e-mail address of this user.
	 *  
	 * @return This user's e-mail address
	 */
	public String getEmail () {
		return ldapGebruiker.getEmail ();
	}
	
	/**
	 * Sets the e-mail address of this user.
	 * 
	 * @param email The user's new e-mail address.
	 */
	public void setEmail (final String email) {
		ldapGebruiker.setEmail (email);
	}
	
	/**
	 * Returns the optional mobile phone number of this user, or null if no number is set. Phone numbers must be specified
	 * in ITU format.
	 * 
	 * @return This users mobile phone number in ITU format, or null.
	 */
	public String getMobile () {
		return ldapGebruiker.getMobile ();
	}
	
	/**
	 * Sets the optional mobile phone number of this user, or null if no number is set. Phone numbers must be specified
	 * in ITU format.
	 * 
	 * @param mobile This user's mobile phone number in ITU format, or null.
	 */
	public void setMobile (final String mobile) {
		ldapGebruiker.setMobile (mobile);
	}
	
	/**
	 * Returns the SHA hashed password for this user.
	 * 
	 * @return The password hash as a base64 encoded string.
	 */
	public String getWachtwoordHash () {
		return ldapGebruiker.getWachtwoordHash ();
	}
	
	/**
	 * Sets the SHA hashed password for this user.
	 * 
	 * @param wachtwoordHash as a base64 encoded string.
	 */
	public void setWachtwoordHash (final String wachtwoordHash) {
		ldapGebruiker.setWachtwoordHash (wachtwoordHash);
	}
	
	/**
	 * Sets the password for this user. The password is hashed, base64 encoded and stored
	 * using setWachtwoordHash
	 * 
	 * @param wachtwoord
	 */
	public void setWachtwoord (final String wachtwoord) {
		ldapGebruiker.setWachtwoord (wachtwoord);
	}
	
	/**
	 * Returns true if the user is a superuser. The superuser flag is stored in the database
	 * backing of the user ({@link DbGebruiker}).
	 * 
	 * @see DbGebruiker
	 * @return true if the user is a superuser, false otherwise.
	 */
	public boolean isSuperuser () {
		return dbGebruiker.isSuperuser ();
	}
	
	/**
	 * Sets the superuser flag for this user. The superuser flag is stored in the datbase
	 * backing of the user ({@link DbGebruiker}).
	 * 
	 * @see DbGebruiker
	 * @param superuser
	 */
	public void setSuperuser (final boolean superuser) {
		dbGebruiker.setSuperuser (superuser);
	}
	
	@Override
	public int hashCode () {
		return new HashCodeBuilder ().
			append (getGebruikersnaam ()).
			toHashCode ();
	}

	@Override
	public boolean equals (final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (! (obj instanceof Gebruiker)) {
			return false;
		}
		
		final Gebruiker other = (Gebruiker) obj;

		return new EqualsBuilder ().
			append (getGebruikersnaam (), other.getGebruikersnaam ()).
			isEquals ();

	}	
}
