package nl.ipo.cds.etl.theme.habitat;

import nl.ipo.cds.etl.PersistableFeature;
import nl.ipo.cds.etl.db.annotation.CodeSpaceColumn;
import nl.ipo.cds.etl.db.annotation.Column;
import nl.ipo.cds.etl.db.annotation.Table;
import nl.ipo.cds.etl.theme.annotation.CodeSpace;
import nl.ipo.cds.etl.theme.annotation.MappableAttribute;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.geometry.Geometry;

@Table(name = "habitat", schema = "bron")
public class Habitat extends PersistableFeature {

	@Column(name = "inspire_id_dataset_code")
	private CodeType inspireIdDatasetCode;

	@Column(name = "inspire_id_local_id")
	private String inspireIdLocalId;

	@Column(name = "geometry")
	private Geometry geometry;

	@Column(name = "habitat_reference_habitat_type_id_code")
	@CodeSpaceColumn(name = "habitat_reference_habitat_type_id_codespace")
	private CodeType habitatReferenceHabitatTypeIdCode;

	@Column(name = "habitat_reference_habitat_type_scheme_code")
	private CodeType habitatReferenceHabitatTypeSchemeCode;

	@Column(name = "habitat_reference_habitat_type_name")
	private String habitatReferenceHabitatTypeName;

	@Column(name = "local_habitat_name_local_scheme")
	private String localHabitatNameLocalScheme;

	@Column(name = "local_habitat_name_local_name_code")
	@CodeSpaceColumn(name = "local_habitat_name_local_name_codespace")
	private CodeType localHabitatNameLocalNameCode;

	@Column(name = "local_habitat_name_local_name")
	private String localHabitatNameLocalName;

	@Column(name = "local_habitat_name_qualifier_local_name")
	private CodeType localHabitatNameQualifierLocalName;

	@Column(name = "habitat_area_covered")
	private Double habitatAreaCovered;

	@MappableAttribute
	@CodeSpace("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/Habitat")
	public CodeType getInspireIdDatasetCode() {
		return inspireIdDatasetCode;
	}

	@MappableAttribute
	@CodeSpace("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/Habitat")
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
	public CodeType getHabitatReferenceHabitatTypeIdCode() {
		return habitatReferenceHabitatTypeIdCode;
	}

	@MappableAttribute
	public void setHabitatReferenceHabitatTypeIdCode(CodeType habitatReferenceHabitatTypeIdCode) {
		this.habitatReferenceHabitatTypeIdCode = habitatReferenceHabitatTypeIdCode;
	}

	@MappableAttribute
	@CodeSpace("http://inspire.ec.europa.eu/codeList/ReferenceHabitatTypeSchemeValue")
	public CodeType getHabitatReferenceHabitatTypeSchemeCode() {
		return habitatReferenceHabitatTypeSchemeCode;
	}

	@MappableAttribute
	@CodeSpace("http://inspire.ec.europa.eu/codeList/ReferenceHabitatTypeSchemeValue")
	public void setHabitatReferenceHabitatTypeSchemeCode(CodeType habitatReferenceHabitatTypeSchemeCode) {
		this.habitatReferenceHabitatTypeSchemeCode = habitatReferenceHabitatTypeSchemeCode;
	}

	@MappableAttribute
	public String getHabitatReferenceHabitatTypeName() {
		return habitatReferenceHabitatTypeName;
	}

	@MappableAttribute
	public void setHabitatReferenceHabitatTypeName(String habitatReferenceHabitatTypeName) {
		this.habitatReferenceHabitatTypeName = habitatReferenceHabitatTypeName;
	}

	@MappableAttribute
	public String getLocalHabitatNameLocalScheme() {
		return localHabitatNameLocalScheme;
	}

	@MappableAttribute
	public void setLocalHabitatNameLocalScheme(String localHabitatNameLocalScheme) {
		this.localHabitatNameLocalScheme = localHabitatNameLocalScheme;
	}

	@MappableAttribute
	public CodeType getLocalHabitatNameLocalNameCode() {
		return localHabitatNameLocalNameCode;
	}

	@MappableAttribute
	public void setLocalHabitatNameLocalNameCode(CodeType localHabitatNameLocalNameCode) {
		this.localHabitatNameLocalNameCode = localHabitatNameLocalNameCode;
	}

	@MappableAttribute
	public String getLocalHabitatNameLocalName() {
		return localHabitatNameLocalName;
	}

	@MappableAttribute
	public void setLocalHabitatNameLocalName(String localHabitatNameLocalName) {
		this.localHabitatNameLocalName = localHabitatNameLocalName;
	}

	@MappableAttribute
	@CodeSpace("http://inspire.ec.europa.eu/codeList/QualifierLocalNameValue")
	public CodeType getLocalHabitatNameQualifierLocalName() {
		return localHabitatNameQualifierLocalName;
	}

	@MappableAttribute
	@CodeSpace("http://inspire.ec.europa.eu/codeList/QualifierLocalNameValue")
	public void setLocalHabitatNameQualifierLocalName(CodeType localHabitatNameQualifierLocalName) {
		this.localHabitatNameQualifierLocalName = localHabitatNameQualifierLocalName;
	}

	@MappableAttribute
	public Double getHabitatAreaCovered() {
		return habitatAreaCovered;
	}

	@MappableAttribute
	public void setHabitatAreaCovered(Double habitatAreaCovered) {
		this.habitatAreaCovered = habitatAreaCovered;
	}

}
