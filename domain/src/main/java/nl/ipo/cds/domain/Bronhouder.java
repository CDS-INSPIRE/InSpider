/**
 * 
 */
package nl.ipo.cds.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Bronhouder bevat gegevens over bronhouders, die via de beheer applicatie
 * worden ingevoerd.<br>
 * <em>Stamtabel<em>.
 * 
 * @author Rob
 * 
 */
@Entity
//@Table(name="bronhouder", schema="manager")
public class Bronhouder implements Identity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	/**
	 vorm: 99xx <br>
		xx = CBS Provincie code<br>
		20 Groningen<br>
		21 Friesland<br>
		22 Drenthe<br>
		23 Overijssel<br>
		24 Flevoland<br>
		25 Gelderland<br>
		26 Utrecht<br>
		27 Noord-Holland<br>
		28 Zuid-Holland<br>
		29 Zeeland<br>
		30 Noord-Brabant<br>
		31 Limburg<br>
	 */
	@Column(columnDefinition = "varchar(64)", unique=true, nullable=false)
	private String code;

	@Column(unique=true, nullable=false)
	private String naam;

	@NotNull
	@Column (nullable = false, name = "contact_naam")
	private String contactNaam;

	@Column (name = "contact_adres")
	private String contactAdres;

	@Column (name = "contact_plaats")
	private String contactPlaats;

	@Column (name = "contact_postcode")
	private String contactPostcode;

	@Column (name = "contact_telefoonnummer")
	private String contactTelefoonnummer;

	@NotNull
	@Column (name = "contact_emailadres", nullable = false)
	private String contactEmailadres;
	
	@Column (name = "common_name", unique = true, nullable = false)
	private String commonName;

	@Column (name = "contact_extraemailadres")
	private String contactExtraEmailadres;
	
	/**
	 * @return the id
	 */
	@Override
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return the provincie
	 */
	public String getNaam() {
		return naam;
	}

	/**
	 * @param provincie
	 *            the provincie to set
	 */
	public void setNaam(String naam) {
		this.naam = naam;
	}

	/**
	 * @return the naam
	 */
	public String getContactNaam() {
		return contactNaam;
	}

	/**
	 * @param naam
	 *            the naam to set
	 */
	public void setContactNaam(String contactNaam) {
		this.contactNaam = contactNaam;
	}

	/**
	 * @return the adres
	 */
	public String getContactAdres() {
		return contactAdres;
	}

	/**
	 * @param adres
	 *            the adres to set
	 */
	public void setContactAdres(String contactAdres) {
		this.contactAdres = contactAdres;
	}

	/**
	 * @return the plaats
	 */
	public String getContactPlaats() {
		return contactPlaats;
	}

	/**
	 * @param plaats
	 *            the plaats to set
	 */
	public void setContactPlaats(String contactPlaats) {
		this.contactPlaats = contactPlaats;
	}

	/**
	 * @return the postcode
	 */
	public String getContactPostcode() {
		return contactPostcode;
	}

	/**
	 * @param postcode
	 *            the postcode to set
	 */
	public void setContactPostcode(String contactPostcode) {
		this.contactPostcode = contactPostcode;
	}

	/**
	 * @return the telefoonnummer
	 */
	public String getContactTelefoonnummer() {
		return contactTelefoonnummer;
	}

	/**
	 * @param telefoonnummer
	 *            the telefoonnummer to set
	 */
	public void setContactTelefoonnummer(String contactTelefoonnummer) {
		this.contactTelefoonnummer = contactTelefoonnummer;
	}

	/**
	 * @return the emailadres
	 */
	public String getContactEmailadres() {
		return contactEmailadres;
	}

	/**
	 * @param emailadres
	 *            the emailadres to set
	 */
	public void setContactEmailadres(String contactEmailadres) {
		this.contactEmailadres = contactEmailadres;
	}

	/**
	 * Returns the "common name" of this bronhouder. The common name is used to identify
	 * this entity in the LDAP server.
	 * 
	 * @return This bronhouder's common name.
	 */
	public String getCommonName () {
		return commonName;
	}

	/**
	 * Sets the "common name" of this bronhouder. The common name is used to identify
	 * this entity in the LDAP server.
	 * 
	 * @param commonName The new common name of this bronhouder.
	 */
	public void setCommonName (final String commonName) {
		this.commonName = commonName;
	}

	public String toString(){
		return "## Bronhouder (id: " + id + ", contactNaam: " + contactNaam + ", contactAdres: " + contactAdres + ", " + contactPostcode + ", " + contactPlaats + ", email: " + contactEmailadres + ", extraemail: " + contactExtraEmailadres + ")";		
	}

	public String getContactExtraEmailadres() {
		return contactExtraEmailadres;
	}

	public void setContactExtraEmailadres(String contactExtraEmailadres) {
		this.contactExtraEmailadres = contactExtraEmailadres;
	}

	@Override  
	public boolean equals(Object obj)  
	{  
		if (obj instanceof Bronhouder == false)  
		{  
			return false;  
		}  
		if (this == obj)  
		{  
			return true;  
		}  
		final Bronhouder otherObject = (Bronhouder) obj;  

		return new EqualsBuilder()  
		.append(this.id, otherObject.id)  
		.isEquals();  
	}

	@Override  
	public int hashCode()  
	{  
		return new HashCodeBuilder()  
		.append(this.id)  
		.toHashCode();  
	}  
}
