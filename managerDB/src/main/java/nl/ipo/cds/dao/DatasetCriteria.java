package nl.ipo.cds.dao;

import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.DatasetType;
import nl.ipo.cds.domain.Thema;

public final class DatasetCriteria extends BaseSearchCriteria {

	private Bronhouder bronhouder;
	
	private Long id;
	
	private DatasetType datasetType;
	
	private Thema thema;
	
	public DatasetCriteria (Bronhouder bronhouder) {
		this.bronhouder = bronhouder;
	}
	
	public Bronhouder getBronhouder() {
		return bronhouder;
	}

	public void setBronhouder(Bronhouder bronhouder) {
		this.bronhouder = bronhouder;
	}
	
	public boolean hasBronhouder () {
		return bronhouder != null;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public DatasetType getDatasetType() {
		return datasetType;
	}

	public void setDatasetType(DatasetType datasetType) {
		this.datasetType = datasetType;
	}
	
	public void setThema (final Thema thema) {
		this.thema = thema;
	}
	
	public Thema getThema () {
		return thema;
	}
	
	public boolean hasThema () {
		return thema != null;
	}
}
