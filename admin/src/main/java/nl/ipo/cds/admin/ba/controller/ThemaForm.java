package nl.ipo.cds.admin.ba.controller;

import nl.ipo.cds.domain.Thema;

public class ThemaForm {
	
	private Thema thema;

	public ThemaForm(Thema thema){
		this.thema = thema;
	}
	
	public Long getId() {
		return this.thema.getId();
	}

	public void setId(Long id) {
		this.thema.setId(id);
	}

	public String getNaam() {
		return this.thema.getNaam();
	}

	public void setNaam(String naam) {
		this.thema.setNaam(naam);
	}

	public String getEmailteksten() {
		return this.thema.getEmailteksten();
	}

	public void setEmailteksten(String emailteksten) {
		this.thema.setEmailteksten(emailteksten);
	}
}
