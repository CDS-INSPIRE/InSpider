package nl.ipo.cds.etl.theme.hazardarea;

import nl.ipo.cds.etl.PersistableFeature;
import nl.ipo.cds.etl.db.annotation.Column;
import nl.ipo.cds.etl.db.annotation.Table;
import nl.ipo.cds.etl.theme.annotation.CodeSpace;
import nl.ipo.cds.etl.theme.annotation.MappableAttribute;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.geometry.Geometry;

@Table(name = "hazard_area", schema = "bron")
public class HazardArea extends PersistableFeature {

	@Column(name = "inspire_id_dataset_code")
	private CodeType inspireIdDatasetCode;

	@Column(name = "inspire_id_local_id")
	private String inspireIdLocalId;

	@Column(name = "hazard_area_id")
	private String hazardAreaId;

	@Column(name = "determination_method")
	private String determinationMethod;

	@Column(name = "type_of_hazard_hazard_category")
	private CodeType typeOfHazardHazardCategory;

	@Column(name = "geometry")
	private Geometry geometry;

	@Column(name = "likelihood_of_occurrence_assement_method_name")
	private String likelihoodOfOccurrenceAssessmentMethodName;

	@Column(name = "likelihood_of_occurrence_assement_method_link")
	private String likelihoodOfOccurrenceAssessmentMethodLink;

	@Column(name = "likelihood_of_occurrence_qualitative_likelihood")
	private String likelihoodOfOccurrenceQualitativeLikelihood;

	@Column(name = "likelihood_of_occurrence_quantitative_likelihood_probability")
	private Double likelihoodOfOccurrenceQuantitativeLikelihoodProbabilityOfOccurrence;

	@Column(name = "likelihood_of_occurrence_quantitative_likelihood_return_period")
	private Double likelihoodOfOccurrenceQuantitativeLikelihoodReturnPeriod;

	@MappableAttribute
	@CodeSpace("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/HazardArea")
	public CodeType getInspireIdDatasetCode() {
		return inspireIdDatasetCode;
	}

	@MappableAttribute
	@CodeSpace("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/HazardArea")
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
	public String getHazardAreaId() {
		return hazardAreaId;
	}

	@MappableAttribute
	public void setHazardAreaId(String hazardAreaId) {
		this.hazardAreaId = hazardAreaId;
	}

	@MappableAttribute
	public String getDeterminationMethod() {
		return determinationMethod;
	}

	@MappableAttribute
	public void setDeterminationMethod(String determinationMethod) {
		this.determinationMethod = determinationMethod;
	}

	@MappableAttribute
	@CodeSpace("http://inspire.ec.europa.eu/codeList/RiskOrHazardCategoryValue")
	public CodeType getTypeOfHazardHazardCategory() {
		return typeOfHazardHazardCategory;
	}

	@MappableAttribute
	@CodeSpace("http://inspire.ec.europa.eu/codeList/RiskOrHazardCategoryValue")
	public void setTypeOfHazardHazardCategory(CodeType typeOfHazardHazardCategory) {
		this.typeOfHazardHazardCategory = typeOfHazardHazardCategory;
	}

	@MappableAttribute
	public String getLikelihoodOfOccurrenceAssessmentMethodName() {
		return likelihoodOfOccurrenceAssessmentMethodName;
	}

	@MappableAttribute
	public void setLikelihoodOfOccurrenceAssessmentMethodName(
			String likelihoodOfOccurrenceAssessmentMethodName) {
		this.likelihoodOfOccurrenceAssessmentMethodName = likelihoodOfOccurrenceAssessmentMethodName;
	}

	@MappableAttribute
	public String getLikelihoodOfOccurrenceAssessmentMethodLink() {
		return likelihoodOfOccurrenceAssessmentMethodLink;
	}

	@MappableAttribute
	public void setLikelihoodOfOccurrenceAssessmentMethodLink(
			String likelihoodOfOccurrenceAssessmentMethodLink) {
		this.likelihoodOfOccurrenceAssessmentMethodLink = likelihoodOfOccurrenceAssessmentMethodLink;
	}

	@MappableAttribute
	public String getLikelihoodOfOccurrenceQualitativeLikelihood() {
		return likelihoodOfOccurrenceQualitativeLikelihood;
	}

	@MappableAttribute
	public void setLikelihoodOfOccurrenceQualitativeLikelihood(
			String likelihoodOfOccurrenceQualitativeLikelihood) {
		this.likelihoodOfOccurrenceQualitativeLikelihood = likelihoodOfOccurrenceQualitativeLikelihood;
	}

	@MappableAttribute
	public Double getLikelihoodOfOccurrenceQuantitativeLikelihoodProbabilityOfOccurrence() {
		return likelihoodOfOccurrenceQuantitativeLikelihoodProbabilityOfOccurrence;
	}

	@MappableAttribute
	public void setLikelihoodOfOccurrenceQuantitativeLikelihoodProbabilityOfOccurrence(
			Double likelihoodOfOccurrenceQuantitativeLikelihoodProbabilityOfOccurrence) {
		this.likelihoodOfOccurrenceQuantitativeLikelihoodProbabilityOfOccurrence = likelihoodOfOccurrenceQuantitativeLikelihoodProbabilityOfOccurrence;
	}

	@MappableAttribute
	public Double getLikelihoodOfOccurrenceQuantitativeLikelihoodReturnPeriod() {
		return likelihoodOfOccurrenceQuantitativeLikelihoodReturnPeriod;
	}

	@MappableAttribute
	public void setLikelihoodOfOccurrenceQuantitativeLikelihoodReturnPeriod(
			Double likelihoodOfOccurrenceQuantitativeLikelihoodReturnPeriod) {
		this.likelihoodOfOccurrenceQuantitativeLikelihoodReturnPeriod = likelihoodOfOccurrenceQuantitativeLikelihoodReturnPeriod;
	}

}
