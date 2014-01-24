package nl.ipo.cds.etl.theme.productioninstallation;

import nl.ipo.cds.etl.PersistableFeature;
import nl.ipo.cds.etl.db.annotation.CodeSpaceColumn;
import nl.ipo.cds.etl.db.annotation.Column;
import nl.ipo.cds.etl.db.annotation.Table;
import nl.ipo.cds.etl.theme.annotation.CodeSpace;
import nl.ipo.cds.etl.theme.annotation.MappableAttribute;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.geometry.Geometry;

@Table(name = "production_installation", schema = "bron")
public class ProductionInstallation extends PersistableFeature {

	@Column(name = "inspire_id_dataset_code")
	private CodeType inspireIdDatasetCode;

	@Column(name = "inspire_id_local_id")
	private String inspireIdLocalId;

	@Column(name = "production_facility_id")
	private String productionFacilityId;

	@Column(name = "production_installation_id")
	private String productionInstallationId;

	@Column(name = "thematic_identifier")
	private String thematicIdentifier;

	@Column(name = "thematic_identifier_scheme")
	private String thematicIdentifierScheme;

	@Column(name = "point_geometry")
	private Geometry pointGeometry;

	@Column(name = "surface_geometry")
	private Geometry surfaceGeometry;

	@Column(name = "line_geometry")
	private Geometry lineGeometry;

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "status_type")
	private CodeType statusType;

	@Column(name = "status_description")
	private String statusDescription;

	@Column(name = "type")
	@CodeSpaceColumn(name = "type_codespace")
	private CodeType type;

	@MappableAttribute
	@CodeSpace("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/ProductionInstallation")
	public CodeType getInspireIdDatasetCode() {
		return inspireIdDatasetCode;
	}

	@MappableAttribute
	@CodeSpace("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/ProductionInstallation")
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
	public String getProductionFacilityId() {
		return productionFacilityId;
	}

	@MappableAttribute
	public void setProductionFacilityId(String productionFacilityId) {
		this.productionFacilityId = productionFacilityId;
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
	public String getThematicIdentifier() {
		return thematicIdentifier;
	}

	@MappableAttribute
	public void setThematicIdentifier(String thematicIdentifier) {
		this.thematicIdentifier = thematicIdentifier;
	}

	@MappableAttribute
	public String getThematicIdentifierScheme() {
		return thematicIdentifierScheme;
	}

	@MappableAttribute
	public void setThematicIdentifierScheme(String thematicIdentifierScheme) {
		this.thematicIdentifierScheme = thematicIdentifierScheme;
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
	public Geometry getSurfaceGeometry() {
		return surfaceGeometry;
	}

	@MappableAttribute
	public void setSurfaceGeometry(Geometry surfaceGeometry) {
		this.surfaceGeometry = surfaceGeometry;
	}

	@MappableAttribute
	public Geometry getLineGeometry() {
		return lineGeometry;
	}

	@MappableAttribute
	public void setLineGeometry(Geometry lineGeometry) {
		this.lineGeometry = lineGeometry;
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
	public String getDescription() {
		return description;
	}

	@MappableAttribute
	public void setDescription(String description) {
		this.description = description;
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

}
