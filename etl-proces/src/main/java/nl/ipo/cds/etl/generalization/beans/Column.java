package nl.ipo.cds.etl.generalization.beans;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Column {

	@XmlElement
	private String name;
	
	@XmlElement
	private String value;

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}
}
