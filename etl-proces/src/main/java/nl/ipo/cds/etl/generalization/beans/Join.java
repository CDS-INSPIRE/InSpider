package nl.ipo.cds.etl.generalization.beans;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Join {

	@XmlElement
	private String table;
	
	@XmlElement
	private String key;
	
	@XmlElement
	private String column;

	public String getTable() {
		return table;
	}

	public String getKey() {
		return key;
	}

	public String getColumn() {
		return column;
	}
}
