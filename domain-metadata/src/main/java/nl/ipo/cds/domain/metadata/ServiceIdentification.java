/**
 * 
 */
package nl.ipo.cds.domain.metadata;

import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.validation.Valid;

import org.hibernate.validator.constraints.NotBlank;

/**
 * @author eshuism
 * 16 jan 2012
 * XML-Schema: ServiceIdentification
 */
@Entity
public class ServiceIdentification extends BaseDomainObject{

	//XML-Schema: DescriptionType
	@Column(nullable=true, columnDefinition="text")
	private String title;
	
	@Column(nullable=false, name="abstract", columnDefinition="text")
	private String _abstract;

	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(name="si_keyword")
	@OrderColumn(name="index")
	@Valid
	private List<Keyword> keywords; // Optional

	//XML-Schema: ServiceIdentification
	/**
	 * service type = WMS, WFS
	 */
	@Column(nullable=false, columnDefinition="text")
	@NotBlank(message="Verplicht")
	private String serviceType;
	
	/**
	 * servicePath is the part between host and query of the service url<br>
	 * e.g. 'ProtectedSites/services'
	 */
	@Column(nullable=false, columnDefinition="text")
	@NotBlank(message="Verplicht")
	private String servicePath;
	
	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(name="si_version")
	@Column(name="version", columnDefinition="text")
	@OrderColumn(name="index")
	private List<String> versions; // Must be at least one
	
	@Column(nullable=true, columnDefinition="text")
	private String fees;
	
	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(name="si_accessconstraint")
	@Column(name="accessconstraint", columnDefinition="text")
	@OrderColumn(name="index")
	private List<String> accessContraints; // Optional
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@NotBlank(message="Verplicht")
	public String getAbstract() {
		return _abstract;
	}

	public void setAbstract(String _abstract) {
		this._abstract = _abstract;
	}

	public List<Keyword> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<Keyword> keywords) {
		this.keywords = keywords;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getServicePath() {
		return servicePath;
	}

	public void setServicePath(String servicePath) {
		this.servicePath = servicePath;
	}

	public List<String> getVersions() {
		return versions;
	}

	public void setVersions(List<String> versions) {
		this.versions = versions;
	}

	public String getFees() {
		return fees;
	}

	public void setFees(String fees) {
		this.fees = fees;
	}

	public List<String> getAccessContraints() {
		return accessContraints;
	}

	public void setAccessContraints(List<String> accessContraints) {
		this.accessContraints = accessContraints;
	}
	
}
