/**
 * 
 */
package nl.ipo.cds.domain;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @author Rob
 *
 */
public class LdapGebruiker {

	@NotBlank(message="Verplicht")
	private String gebruikersnaam;
	
	@NotBlank(message="Verplicht")
	@Email(message="Geen geldig emailadres")
	private String email;
	
	private String mobile;
	
	@NotBlank(message="Verplicht")
	private String wachtwoordHash;
	
	/**
	 * Returns the username of this user. The username also corresponds with the 'uid' attribute in LDAP.
	 * 
	 * @return The username.
	 */
	public String getGebruikersnaam() {
		return gebruikersnaam;
	}
	
	/**
	 * Sets the username of this user. The username corresponds with the 'uid' attribute in LDAP.
	 * 
	 * @param gebruikersnaam
	 */
	public void setGebruikersnaam(String gebruikersnaam) {
		this.gebruikersnaam = gebruikersnaam;
	}
	
	/**
	 * Returns the e-mail address of this user.
	 *  
	 * @return This user's e-mail address
	 */
	public String getEmail() {
		return email;
	}
	
	/**
	 * Sets the e-mail address of this user.
	 * 
	 * @param email The user's new e-mail address.
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	
	/**
	 * Returns the optional mobile phone number of this user, or null if no number is set. Phone numbers must be specified
	 * in ITU format.
	 * 
	 * @return This users mobile phone number in ITU format, or null.
	 */
	public String getMobile() {
		return mobile;
	}
	
	/**
	 * Sets the optional mobile phone number of this user, or null if no number is set. Phone numbers must be specified
	 * in ITU format.
	 * 
	 * @param mobile This user's mobile phone number in ITU format, or null.
	 */
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
	/**
	 * Returns the SHA hashed password for this user.
	 * 
	 * @return The password hash as a base64 encoded string.
	 */
	public String getWachtwoordHash() {
		return wachtwoordHash;
	}
	
	/**
	 * Sets the SHA hashed password for this user.
	 * 
	 * @param wachtwoordHash as a base64 encoded string.
	 */
	public void setWachtwoordHash(String wachtwoordHash) {
		this.wachtwoordHash = wachtwoordHash;
	}
	
	/**
	 * Sets the password for this user. The password is hashed, base64 encoded and stored
	 * using setWachtwoordHash
	 * 
	 * @param wachtwoord
	 */
	public void setWachtwoord (String wachtwoord) {
		this.setWachtwoordHash (hashWachtwoord (wachtwoord));
	}
	
	/**
	 * Hashes a plaintext password and encodes it as base64.
	 * 
	 * @param plaintext The plaintext password.
	 * @return The base64 encoded hash of the plaintext password.
	 */
    private String hashWachtwoord(final String plaintext) {
		final MessageDigest md;
		
		try {
			md = MessageDigest.getInstance("SHA");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e.getMessage());
		}
		
		try {
			md.update(plaintext.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException (e.getMessage());
		}
		
		return Base64.encodeBase64String(md.digest ());
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().
			append(this.getGebruikersnaam()).
			toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (! (obj instanceof LdapGebruiker))
			return false;
		
		final LdapGebruiker other = (LdapGebruiker) obj;

		return new EqualsBuilder().
			append(this.getGebruikersnaam(), other.getGebruikersnaam()).
			isEquals();

	}
    
    
}
