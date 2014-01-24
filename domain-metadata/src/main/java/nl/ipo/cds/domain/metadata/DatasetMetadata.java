/**
 * 
 */
package nl.ipo.cds.domain.metadata;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.validator.constraints.NotBlank;

/**
 * @author eshuism
 * 17 jan 2012
 */
@Embeddable
public class DatasetMetadata {

	@Column(nullable=false, columnDefinition="text")
	@NotBlank(message="Verplicht")
    private String name;

	@Column(columnDefinition="text")
    private String namespace;

	@Column(nullable=false, columnDefinition="text")
	@NotBlank(message="Verplicht")
    private String url;

	
	public DatasetMetadata() {
		super();
	}

	public DatasetMetadata(String name, String namespace, String url) {
		super();
		this.name = name;
		this.namespace = namespace;
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

}
