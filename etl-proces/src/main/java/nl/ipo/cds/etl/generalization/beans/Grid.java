package nl.ipo.cds.etl.generalization.beans;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Grid {

	@XmlElement
	private int width;
	
	@XmlElement
	private int height;

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
