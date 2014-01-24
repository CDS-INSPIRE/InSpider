package nl.ipo.cds.etl.generalization.beans;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Generalization {

	@XmlElement
	private Grid grid;
	
	@XmlElement
	private Generalize[] generalize;

	public Grid getGrid() {
		return grid;
	}

	public Generalize[] getGeneralize() {
		return generalize;
	}
}
