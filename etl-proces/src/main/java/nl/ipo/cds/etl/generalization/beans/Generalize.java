package nl.ipo.cds.etl.generalization.beans;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Generalize {

	@XmlElement
	private String destination;
	
	@XmlElement
	private Source source;

	public String getDestination() {
		return destination;
	}

	public Source getSource() {
		return source;
	}
}
