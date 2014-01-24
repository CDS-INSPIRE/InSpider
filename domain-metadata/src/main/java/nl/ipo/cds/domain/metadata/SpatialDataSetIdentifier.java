package nl.ipo.cds.domain.metadata;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class SpatialDataSetIdentifier {

	@Column(columnDefinition="text")
	private String code;
	
	@Column(columnDefinition="text")
	private String namespace;

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
