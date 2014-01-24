package nl.ipo.cds.domain.metadata;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.validation.Valid;

import nl.ipo.cds.validator.constraints.ValidCollection;

import org.hibernate.validator.constraints.NotBlank;

/**
 * @author eshuism
 * 13 jan 2012
 */
@Entity
public class Service extends BaseDomainObject{

	public Service(String name, String description) {
		super();
		this.name = name;
		this.description = description;
	}

	/**
	 * Default constructor for JPA
	 */
	Service(){
		super();
	}

	@OneToOne(cascade=CascadeType.ALL)
	@Valid
	private ServiceIdentification serviceIdentification;

	@OneToOne(cascade=CascadeType.ALL)
	@Valid
	private ServiceProvider serviceProvider;
	
	@OneToOne(cascade=CascadeType.ALL)
	@Valid
	private ExtendedCapabilities extendedCapabilities;

	@Column(unique=true, nullable=false, columnDefinition="text")
	@NotBlank(message="Verplicht")
	private String name;

	@Column(nullable=false, columnDefinition="text")
	@NotBlank(message="Verplicht")
	private String description;

	@ElementCollection(fetch=FetchType.EAGER)
	@CollectionTable(name="service_datasetmetadata")
	@OrderColumn(name="index")
	@Valid
	private List<DatasetMetadata> datasetMetadatas;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ServiceIdentification getServiceIdentification() {
		return serviceIdentification;
	}

	public void setServiceIdentification(ServiceIdentification serviceIdentification) {
		this.serviceIdentification = serviceIdentification;
	}

	public List<DatasetMetadata> getDatasetMetadatas() {
		return datasetMetadatas;
	}

	public void setDatasetMetadatas(List<DatasetMetadata> datasetMetadatas) {
		this.datasetMetadatas = datasetMetadatas;
	}	

	public ServiceProvider getServiceProvider() {
		return serviceProvider;
	}

	public void setServiceProvider(ServiceProvider serviceProvider) {
		this.serviceProvider = serviceProvider;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ExtendedCapabilities getExtendedCapabilities() {
		return extendedCapabilities;
	}

	public void setExtendedCapabilities(ExtendedCapabilities extendedCapabilities) {
		this.extendedCapabilities = extendedCapabilities;
	}
}
