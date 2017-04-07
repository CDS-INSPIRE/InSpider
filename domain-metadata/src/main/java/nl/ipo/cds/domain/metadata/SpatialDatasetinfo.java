package nl.ipo.cds.domain.metadata;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Table;
import javax.persistence.Entity;


@Embeddable
public class SpatialDatasetinfo {

	@Column(columnDefinition="text")
	private String type;
	
	@Column(columnDefinition="text")
	private String name;

	@Column(columnDefinition="text")
	private String code;
	
	@Column(columnDefinition="text")
	private String namespace;

	public SpatialDatasetinfo() {
		super();
	}
	

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName () {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
       
       public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
}
