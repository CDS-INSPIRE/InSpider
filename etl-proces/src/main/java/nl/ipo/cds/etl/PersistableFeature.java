package nl.ipo.cds.etl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import nl.ipo.cds.etl.db.annotation.Column;

@XmlAccessorType(XmlAccessType.NONE)
public abstract class PersistableFeature implements Feature {

	@XmlAttribute(name="id", namespace="http://www.opengis.net/gml")
	@Column(name="gfid")
	String id;

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
