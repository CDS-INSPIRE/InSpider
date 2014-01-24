package nl.ipo.cds.admin.ba.controller;

import nl.ipo.cds.domain.Bronhouder;


public class BronhouderNAW {

	private Bronhouder bronhouder;

	public BronhouderNAW(Bronhouder bronhouder){
		this.bronhouder = bronhouder;
	}

	public Long getId() {
		return this.bronhouder.getId();
	}

	public void setId(Long id) {
		this.bronhouder.setId(id);
	}

	public String getCode() {
		return this.bronhouder.getCode();
	}

	public void setCode(String code) {
		this.bronhouder.setCode(code);
	}

	public String getBronhouderNaam() {
		return this.bronhouder.getNaam();
	}

	public void setBronhouderNaam(String naam) {
		this.bronhouder.setNaam(naam);
	}

	public String getNaam() {
		return this.bronhouder.getContactNaam();
	}

	public void setNaam(String naam) {
		this.bronhouder.setContactNaam(naam);
	}

	public String getAdres() {
		return this.bronhouder.getContactAdres();
	}

	public void setAdres(String adres) {
		this.bronhouder.setContactAdres(adres);
	}

	public String getPlaats() {
		return this.bronhouder.getContactPlaats();
	}

	public void setPlaats(String plaats) {
		this.bronhouder.setContactPlaats(plaats);
	}

	public String getPostcode() {
		return this.bronhouder.getContactPostcode();
	}

	public void setPostcode(String postcode) {
		this.bronhouder.setContactPostcode(postcode);
	}

	public String getTelefoonnummer() {
		return this.bronhouder.getContactTelefoonnummer();
	}

	public void setTelefoonnummer(String telefoonnummer) {
		this.bronhouder.setContactTelefoonnummer(telefoonnummer);
	}

	public String getEmailadres() {
		return this.bronhouder.getContactEmailadres();
	}

	public void setEmailadres(String emailadres) {
		this.bronhouder.setContactEmailadres(emailadres);
	}

	public Bronhouder getBronhouder() {
		return bronhouder;
	}

	public void setBronhouder(Bronhouder bronhouder) {
		this.bronhouder = bronhouder;
	}

}
