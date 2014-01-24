package nl.ipo.cds.etl.theme.productioninstallationpart;

import nl.ipo.cds.etl.PersistableFeature;
import nl.ipo.cds.etl.db.annotation.CodeSpaceColumn;
import nl.ipo.cds.etl.db.annotation.Column;
import nl.ipo.cds.etl.db.annotation.Table;
import nl.ipo.cds.etl.theme.annotation.CodeSpace;
import nl.ipo.cds.etl.theme.annotation.MappableAttribute;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.geometry.Geometry;

@Table(name = "production_installation_part", schema = "bron")
public class ProductionInstallationPart extends PersistableFeature {

	@Column(name = "inspire_id_dataset_code")
	private CodeType inspireIdDatasetCode;

	@Column(name = "inspire_id_local_id")
	private String inspireIdLocalId;

	@Column(name = "production_installation_id")
	private String productionInstallationId;

	@Column(name = "point_geometry")
	private Geometry pointGeometry;

	@Column(name = "name")
	private String name;

	@Column(name = "status_type")
	private CodeType statusType;

	@Column(name = "status_description")
	private String statusDescription;

	@Column(name = "type")
	@CodeSpaceColumn(name = "type_codespace")
	private CodeType type;

	@Column(name = "technique")
	private CodeType technique;

	@MappableAttribute
	@CodeSpace("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/ProductionInstallationPart")
	public CodeType getInspireIdDatasetCode() {
		return inspireIdDatasetCode;
	}

	@MappableAttribute
	@CodeSpace("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/ProductionInstallationPart")
	public void setInspireIdDatasetCode(CodeType inspireIdDatasetCode) {
		this.inspireIdDatasetCode = inspireIdDatasetCode;
	}

	@MappableAttribute
	public String getInspireIdLocalId() {
		return inspireIdLocalId;
	}

	@MappableAttribute
	public void setInspireIdLocalId(String inspireIdLocalId) {
		this.inspireIdLocalId = inspireIdLocalId;
	}

	@MappableAttribute
	public String getProductionInstallationId() {
		return productionInstallationId;
	}

	@MappableAttribute
	public void setProductionInstallationId(String productionInstallationId) {
		this.productionInstallationId = productionInstallationId;
	}

	@MappableAttribute
	public Geometry getPointGeometry() {
		return pointGeometry;
	}

	@MappableAttribute
	public void setPointGeometry(Geometry pointGeometry) {
		this.pointGeometry = pointGeometry;
	}

	@MappableAttribute
	public String getName() {
		return name;
	}

	@MappableAttribute
	public void setName(String name) {
		this.name = name;
	}

	@MappableAttribute
	@CodeSpace("http://inspire.ec.europa.eu/codeList/ConditionOfFacilityValue")
	public CodeType getStatusType() {
		return statusType;
	}

	@MappableAttribute
	@CodeSpace("http://inspire.ec.europa.eu/codeList/ConditionOfFacilityValue")
	public void setStatusType(CodeType statusType) {
		this.statusType = statusType;
	}

	@MappableAttribute
	public String getStatusDescription() {
		return statusDescription;
	}

	@MappableAttribute
	public void setStatusDescription(String statusDescription) {
		this.statusDescription = statusDescription;
	}

	@MappableAttribute
	public CodeType getType() {
		return type;
	}

	@MappableAttribute
	public void setType(CodeType type) {
		this.type = type;
	}

	@MappableAttribute
	@CodeSpace("http://inspire.ec.europa.eu/codeList/PollutionAbatementTechniqueValue")
	public CodeType getTechnique() {
		return technique;
	}

	@MappableAttribute
	@CodeSpace("http://inspire.ec.europa.eu/codeList/PollutionAbatementTechniqueValue")
	public void setTechnique(CodeType technique) {
		this.technique = technique;
	}

}
