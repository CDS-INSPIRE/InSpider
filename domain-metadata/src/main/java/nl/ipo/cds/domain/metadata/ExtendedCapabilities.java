package nl.ipo.cds.domain.metadata;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.validator.constraints.NotBlank;

@Entity
public class ExtendedCapabilities extends BaseDomainObject {

	@NotBlank(message="Verplicht")
	@Column(nullable=false, columnDefinition="text")
	private String metadataUrl;
	
	private SpatialDataSetIdentifier spatialDataSetIdentifier;

	public String getMetadataUrl() {
		return metadataUrl;
	}

	public void setMetadataUrl(String metadataUrl) {
		this.metadataUrl = metadataUrl;
	}

	public SpatialDataSetIdentifier getSpatialDataSetIdentifier() {
		return spatialDataSetIdentifier;
	}

	public void setSpatialDataSetIdentifier(SpatialDataSetIdentifier spatialDataSetIdentifier) {
		this.spatialDataSetIdentifier = spatialDataSetIdentifier;
	}
}
