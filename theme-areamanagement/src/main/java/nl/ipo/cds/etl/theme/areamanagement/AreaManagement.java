package nl.ipo.cds.etl.theme.areamanagement;

import java.sql.Date;
import java.sql.Timestamp;

import nl.ipo.cds.etl.PersistableFeature;
import nl.ipo.cds.etl.db.annotation.CodeSpaceColumn;
import nl.ipo.cds.etl.db.annotation.Column;
import nl.ipo.cds.etl.db.annotation.Table;
import nl.ipo.cds.etl.theme.annotation.CodeSpace;
import nl.ipo.cds.etl.theme.annotation.MappableAttribute;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.geometry.Geometry;

@Table(name = "area_management", schema = "bron")
public class AreaManagement extends PersistableFeature {

	@Column(name = "inspire_id_dataset_code")
	private CodeType inspireIdDatasetCode;

	@Column(name = "inspire_id_local_id")
	private String inspireIdlocalId;

	@Column(name = "geometry")
	private Geometry geometry;

	@Column(name = "zonetype_code")
	private CodeType zoneTypeCode;

	@Column(name = "environmental_domain_code")
	private CodeType environmentalDomainCode;

	@Column(name = "thematic_id_identifier")
	private String thematicIdIdentifier;

	@Column(name = "thematic_id_identifier_scheme")
	private String thematicIdIdentifierScheme;

	@Column(name = "name_spelling")
	private String nameSpelling;

	@Column(name = "competent_authority_organisation_name")
	private String competentAuthorityOrganisationName;

	@Column(name = "legal_basis_name")
	private String legalBasisName;

	@Column(name = "legal_basis_link")
	private String legalBasisLink;

	@Column(name = "legal_basis_date")
	private Date legalBasisDate;

	@Column(name = "specialised_zone_type_code")
	@CodeSpaceColumn(name = "specialised_zone_type_codespace")
	private CodeType specialisedZoneTypeCode;

	@Column(name = "designation_period_begin_designation")
	private Timestamp designationPeriodBeginDesignation;

	@Column(name = "designation_period_end_designation")
	private Timestamp designationPeriodEndDesignation;

	@Column(name = "vergunde_kuubs")
	private Double vergundeKuubs;

	@Column(name = "vergunde_diepte")
	private Double vergundeDiepte;
	
	@Column(name = "noise_low_value")
	private Double noiseLowValue;
	
	@Column(name = "noise_high_value")
	private Double noiseHighValue;	

	@MappableAttribute
	@CodeSpace ("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/AreaManagement")
	public CodeType getInspireIdDatasetCode() {
		return inspireIdDatasetCode;
	}

	@MappableAttribute
	@CodeSpace ("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/AreaManagement")	
	public void setInspireIdDatasetCode(CodeType inspireIdDatasetCode) {
		this.inspireIdDatasetCode = inspireIdDatasetCode;
	}

	@MappableAttribute
	public String getInspireIdLocalId() {
		return inspireIdlocalId;
	}

	@MappableAttribute
	public void setInspireIdLocalId(String inspireIdlocalId) {
		this.inspireIdlocalId = inspireIdlocalId;
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
	@CodeSpace ("http://inspire.ec.europa.eu/codeList/ZoneTypeCode")	
	public CodeType getZoneTypeCode() {
		return zoneTypeCode;
	}

	@MappableAttribute
	@CodeSpace ("http://inspire.ec.europa.eu/codeList/ZoneTypeCode")
	public void setZoneTypeCode(CodeType zonetypeCode) {
		this.zoneTypeCode = zonetypeCode;
	}

	@MappableAttribute
	@CodeSpace ("http://inspire.ec.europa.eu/codeList/EnvironmentalDomain")	
	public CodeType getEnvironmentalDomainCode() {
		return environmentalDomainCode;
	}

	@MappableAttribute
	@CodeSpace ("http://inspire.ec.europa.eu/codeList/EnvironmentalDomain")	
	public void setEnvironmentalDomainCode(CodeType environmentalDomainCode) {
		this.environmentalDomainCode = environmentalDomainCode;
	}

	@MappableAttribute
	public String getThematicIdIdentifier() {
		return thematicIdIdentifier;
	}

	@MappableAttribute
	public void setThematicIdIdentifier(String thematicIdIdentifier) {
		this.thematicIdIdentifier = thematicIdIdentifier;
	}

	@MappableAttribute
	public String getThematicIdIdentifierScheme() {
		return thematicIdIdentifierScheme;
	}

	@MappableAttribute
	public void setThematicIdIdentifierScheme(String thematicIdIdentifierScheme) {
		this.thematicIdIdentifierScheme = thematicIdIdentifierScheme;
	}

	@MappableAttribute
	public String getNameSpelling() {
		return nameSpelling;
	}

	@MappableAttribute
	public void setNameSpelling(String nameSpelling) {
		this.nameSpelling = nameSpelling;
	}

	@MappableAttribute
	public String getCompetentAuthorityOrganisationName() {
		return competentAuthorityOrganisationName;
	}

	@MappableAttribute
	public void setCompetentAuthorityOrganisationName(String competentAuthorityOrganisationName) {
		this.competentAuthorityOrganisationName = competentAuthorityOrganisationName;
	}

	@MappableAttribute
	public String getLegalBasisName() {
		return legalBasisName;
	}

	@MappableAttribute
	public void setLegalBasisName(String legalBasisName) {
		this.legalBasisName = legalBasisName;
	}

	@MappableAttribute
	public String getLegalBasisLink() {
		return legalBasisLink;
	}

	@MappableAttribute
	public void setLegalBasisLink(String legalBasisLink) {
		this.legalBasisLink = legalBasisLink;
	}

	@MappableAttribute
	public Date getLegalBasisDate() {
		return legalBasisDate;
	}

	@MappableAttribute
	public void setLegalBasisDate(Date legalBasisDate) {
		this.legalBasisDate = legalBasisDate;
	}

	@MappableAttribute
	public CodeType getSpecialisedZoneTypeCode() {
		return specialisedZoneTypeCode;
	}

	@MappableAttribute
	public void setSpecialisedZoneTypeCode(CodeType specialisedZoneTypeCode) {
		this.specialisedZoneTypeCode = specialisedZoneTypeCode;
	}

	@MappableAttribute
	public Timestamp getDesignationPeriodBeginDesignation() {
		return designationPeriodBeginDesignation;
	}

	@MappableAttribute
	public void setDesignationPeriodBeginDesignation(Timestamp designationPeriodBeginDesignation) {
		this.designationPeriodBeginDesignation = designationPeriodBeginDesignation;
	}

	@MappableAttribute
	public Timestamp getDesignationPeriodEndDesignation() {
		return designationPeriodEndDesignation;
	}

	@MappableAttribute
	public void setDesignationPeriodEndDesignation(Timestamp designationPeriodEndDesignation) {
		this.designationPeriodEndDesignation = designationPeriodEndDesignation;
	}

	@MappableAttribute
	public Double getVergundeKuubs() {
		return vergundeKuubs;
	}

	@MappableAttribute
	public void setVergundeKuubs(Double vergundeKuubs) {
		this.vergundeKuubs = vergundeKuubs;
	}

	@MappableAttribute
	public Double getVergundeDiepte() {
		return vergundeDiepte;
	}

	@MappableAttribute
	public void setVergundeDiepte(Double vergundeDiepte) {
		this.vergundeDiepte = vergundeDiepte;
	}

	@MappableAttribute	
	public Double getNoiseLowValue() {
		return noiseLowValue;
	}

	@MappableAttribute	
	public void setNoiseLowValue(Double noiseLowValue) {
		this.noiseLowValue = noiseLowValue;
	}

	@MappableAttribute	
	public Double getNoiseHighValue() {
		return noiseHighValue;
	}

	@MappableAttribute	
	public void setNoiseHighValue(Double noiseHighValue) {
		this.noiseHighValue = noiseHighValue;
	}
}
