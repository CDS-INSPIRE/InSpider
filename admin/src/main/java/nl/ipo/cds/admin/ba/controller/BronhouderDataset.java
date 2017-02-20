package nl.ipo.cds.admin.ba.controller;

import nl.ipo.cds.domain.Dataset;
import nl.ipo.cds.domain.RefreshPolicy;

public class BronhouderDataset {

	private long id;
	private String type;
	private String naam = "";
	private boolean actief = false;
	private String uuid;
	private RefreshPolicy refreshPolicy;
	
	private final int validAttributeMappings;
	private final int totalAttributeMappings;
	
	public BronhouderDataset() {
		validAttributeMappings = 0;
		totalAttributeMappings = 0;
	}

	public BronhouderDataset(Dataset dataset, int validAttributeMappings, int totalAttributeMappings){
		
		this.id = dataset.getId();
		this.type = dataset.getDatasetType().getNaam();
		this.naam = dataset.getNaam();
		this.uuid = dataset.getUuid();
		this.actief = dataset.getActief();
		this.refreshPolicy = dataset.getRefreshPolicy();		//W1502 019 
		this.validAttributeMappings = validAttributeMappings;
		this.totalAttributeMappings = totalAttributeMappings;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getNaam() {
		return naam;
	}

	public void setNaam(String naam) {
		this.naam = naam;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public int getValidAttributeMappings() {
		return validAttributeMappings;
	}

	public int getTotalAttributeMappings() {
		return totalAttributeMappings;
	}

	public boolean isActief() {
		return actief;
	}

	public void setActief(boolean actief) {
		this.actief = actief;
	}	
	
	//W1502 019
	
	public void setRefreshPolicy(RefreshPolicy refreshPolicy){
		this.refreshPolicy = refreshPolicy;
	}
	
	public RefreshPolicy getRefreshPolicy(){
		return refreshPolicy;
	}
}
