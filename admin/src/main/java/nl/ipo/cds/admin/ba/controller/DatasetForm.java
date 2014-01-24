package nl.ipo.cds.admin.ba.controller;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

public class DatasetForm {

	@NotNull
	private Long datasettypeId;
	
	@NotBlank
	private String uuid;
	
	@NotBlank
	private String naam;
	
	@NotNull
	private String thema;

	public Long getDatasettypeId() {
		return datasettypeId;
	}

	public void setDatasettypeId(Long datasettypeId) {
		this.datasettypeId = datasettypeId;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getNaam() {
		return naam;
	}

	public void setNaam(String naam) {
		this.naam = naam;
	}

	public String getThema() {
		return thema;
	}

	public void setThema(String thema) {
		this.thema = thema;
	}
}
