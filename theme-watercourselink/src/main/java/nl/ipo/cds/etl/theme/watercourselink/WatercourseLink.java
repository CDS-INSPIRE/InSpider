package nl.ipo.cds.etl.theme.watercourselink;

import nl.ipo.cds.etl.PersistableFeature;
import nl.ipo.cds.etl.db.annotation.Column;
import nl.ipo.cds.etl.db.annotation.Table;
import nl.ipo.cds.etl.theme.annotation.CodeSpace;
import nl.ipo.cds.etl.theme.annotation.MappableAttribute;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.geometry.Geometry;

@Table(name = "watercourse_link", schema = "bron")
public class WatercourseLink extends PersistableFeature {

	@Column(name = "inspire_id_dataset_code")
	private CodeType inspireIdDatasetCode;

	@Column(name = "inspire_id_local_id")
	private String inspireIdLocalId;

	@Column(name = "production_facility_id")
	private String productionFacilityId;

	@Column(name = "thematic_identifier")
	private String thematicIdentifier;

	@Column(name = "thematic_identifier_scheme")
	private String thematicIdentifierScheme;

	@Column(name = "geometry")
	private Geometry geometry;

	@Column(name = "function_activity")
	private CodeType functionActivity;

	@Column(name = "function_input")
	private CodeType functionInput;

	@Column(name = "function_output")
	private CodeType functionOutput;

	@Column(name = "function_description")
	private String functionDescription;

	@Column(name = "name")
	private String name;

	@Column(name = "surface_geometry")
	private Geometry surfaceGeometry;

	@Column(name = "status_type")
	private CodeType statusType;

	@Column(name = "status_description")
	private String statusDescription;


	@MappableAttribute
	@CodeSpace("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/ProductionFacility")
	public CodeType getInspireIdDatasetCode() {
		return inspireIdDatasetCode;
	}

	@MappableAttribute
	@CodeSpace("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/ProductionFacility")
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
	public Geometry getGeometry() {
		return geometry;
	}

	@MappableAttribute
	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
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
	@CodeSpace("http://inspire.ec.europa.eu/codeList/EconomicActivityNACEValue")
	public CodeType getFunctionActivity() {
		return functionActivity;
	}

	@MappableAttribute
	@CodeSpace("http://inspire.ec.europa.eu/codeList/EconomicActivityNACEValue")
	public void setFunctionActivity(CodeType functionActivity) {
		this.functionActivity = functionActivity;
	}

	@MappableAttribute
	@CodeSpace("http://inspire.ec.europa.eu/codeList/ProductCPAValue")
	public CodeType getFunctionInput() {
		return functionInput;
	}

	@MappableAttribute
	@CodeSpace("http://inspire.ec.europa.eu/codeList/ProductCPAValue")
	public void setFunctionInput(CodeType functionInput) {
		this.functionInput = functionInput;
	}

	@MappableAttribute
	@CodeSpace("http://inspire.ec.europa.eu/codeList/ProductCPAValue")
	public CodeType getFunctionOutput() {
		return functionOutput;
	}

	@MappableAttribute
	@CodeSpace("http://inspire.ec.europa.eu/codeList/ProductCPAValue")
	public void setFunctionOutput(CodeType functionOutput) {
		this.functionOutput = functionOutput;
	}

	@MappableAttribute
	public String getFunctionDescription() {
		return functionDescription;
	}

	@MappableAttribute
	public void setFunctionDescription(String functionDescription) {
		this.functionDescription = functionDescription;
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
	public Geometry getSurfaceGeometry() {
		return surfaceGeometry;
	}

	@MappableAttribute
	public void setSurfaceGeometry(Geometry surfaceGeometry) {
		this.surfaceGeometry = surfaceGeometry;
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

}
