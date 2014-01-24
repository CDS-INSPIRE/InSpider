package nl.ipo.cds.etl.generalization.beans;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Source {

	@XmlElement
	private String table;
	
	@XmlElement
	private String key;
	
	@XmlElement
	private String column;
	
	@XmlElement
	private Join join;
	
	@XmlElement
	private Filter filter;

	public String getTable() {
		return table;
	}

	public String getColumn() {
		return column;
	}

	public Filter getFilter() {
		return filter;
	}

	public String getKey() {
		return key;
	}

	public Join getJoin() {
		return join;
	}
}
