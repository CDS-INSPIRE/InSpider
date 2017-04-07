package nl.ipo.cds.admin.ba.controller.gebruikersbeheer.beans;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;

import nl.ipo.cds.domain.Bronhouder;

/**
 * Form bean class for editing "bronhouders". Contains the editable properties of the {@link Bronhouder}
 * class and adds validation constraints.
 */
public class BronhouderForm {

	@NotNull(message = "Er moet een code worden ingevuld voor deze bronhouder")
	@Size (min = 1, message = "De code dient uit tenminste 1 karakter te bestaan")
	@Pattern (regexp = "[a-z0-9\\-_]+", flags = Pattern.Flag.CASE_INSENSITIVE, message = "De code dient te bestaan uit een combinatie van letters, cijfers, - en _")
	private String code = "";
	
	@NotNull(message = "Er dient een bronhoudernaam ingevuld te worden")
	@Size (min = 2, message = "De bronhoudernaam dient uit tenminste 2 karakters te bestaan")
	private String naam = "";
	
	private String contactNaam;
	
	private String contactAdres;

	private String contactPlaats;

	private String contactPostcode;

	private String contactTelefoonnummer;

	@Email
	private String contactEmailadres;
	
	@Email
	private String contactExtraEmailadres;
	
	@NotNull(message = "Er dient een unieke identificatie voor de bronhouder te worden ingevuld")
	@Size (min = 2, message = "De identificatie dient uit tenminste 2 karakters te bestaan")
	@Pattern (regexp = "[a-z0-9\\-_]+", flags = Pattern.Flag.CASE_INSENSITIVE, message = "De identificatie dient te bestaan uit een combinatie van letters, cijfers, - en _")
	private String commonName = "";

	/**
	 * Creates a new, empty, bronhouder form.
	 */
	public BronhouderForm () {
	}
	
	/**
	 * Creates a new bronhouder form by copying the properties of the given existing bronhouder.
	 * 
	 * @param bronhouder The bronhouder whose properties are to be copied.
	 */
	public BronhouderForm (final Bronhouder bronhouder) {
		if (bronhouder == null) {
			throw new NullPointerException ("bronhouder cannot be null");
		}
		
		setCode (bronhouder.getCode ());
		setNaam (bronhouder.getNaam ());
		setCommonName (bronhouder.getCommonName ());
		setContactNaam (bronhouder.getContactNaam ());
		setContactAdres (bronhouder.getContactAdres ());
		setContactPlaats (bronhouder.getContactPlaats ());
		setContactPostcode (bronhouder.getContactPostcode ());
		setContactTelefoonnummer (bronhouder.getContactTelefoonnummer ());
		setContactEmailadres (bronhouder.getContactEmailadres ());
		setContactExtraEmailadres(bronhouder.getContactExtraEmailadres ());
	}

	public String getCode () {
		return code;
	}

	public void setCode (final String code) {
		this.code = code;
	}

	public String getNaam () {
		return naam;
	}

	public void setNaam (final String naam) {
		this.naam = naam;
	}

	public String getContactNaam () {
		return contactNaam;
	}

	public void setContactNaam (final String contactNaam) {
		this.contactNaam = contactNaam;
	}

	public String getContactAdres () {
		return contactAdres;
	}

	public void setContactAdres (final String contactAdres) {
		this.contactAdres = contactAdres;
	}

	public String getContactPlaats () {
		return contactPlaats;
	}

	public void setContactPlaats (final String contactPlaats) {
		this.contactPlaats = contactPlaats;
	}

	public String getContactPostcode () {
		return contactPostcode;
	}

	public void setContactPostcode (final String contactPostcode) {
		this.contactPostcode = contactPostcode;
	}

	public String getContactTelefoonnummer () {
		return contactTelefoonnummer;
	}

	public void setContactTelefoonnummer (final String contactTelefoonnummer) {
		this.contactTelefoonnummer = contactTelefoonnummer;
	}

	public String getContactEmailadres () {
		return contactEmailadres;
	}

	public void setContactEmailadres (final String contactEmailadres) {
		this.contactEmailadres = contactEmailadres;
	}

	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}
	
	public String getContactExtraEmailadres() {
		return contactExtraEmailadres;
	}

	public void setContactExtraEmailadres(String contactExtraEmailadres) {
		this.contactExtraEmailadres = contactExtraEmailadres;
	}
}
