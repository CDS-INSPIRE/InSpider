package nl.ipo.cds.etl.generalization.beans;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Filter {

	@XmlElement
	private String table;
	
	@XmlElement
	private String key;
	
	@XmlElement(name = "column")
	private Column[] columns;

	public String getTable() {
		return table;
	}

	public Column[] getColumns() {
		return columns;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
