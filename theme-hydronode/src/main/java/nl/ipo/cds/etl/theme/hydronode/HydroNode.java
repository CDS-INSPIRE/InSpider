package nl.ipo.cds.etl.theme.hydronode;

import nl.ipo.cds.etl.PersistableFeature;
import nl.ipo.cds.etl.db.annotation.Column;
import nl.ipo.cds.etl.db.annotation.Table;
import nl.ipo.cds.etl.theme.annotation.CodeSpace;
import nl.ipo.cds.etl.theme.annotation.MappableAttribute;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.geometry.Geometry;

@Table(name = "hydro_node", schema = "bron")
public class HydroNode extends PersistableFeature {

	@Column(name = "inspire_id_dataset_code")
	private CodeType inspireIdDatasetCode;

	@Column(name = "inspire_id_local_id")
	private String inspireIdLocalId;

	@Column(name = "geometry")
	private Geometry geometry;

	@Column(name = "name")
	private String name;

	@Column(name = "category")
	private CodeType category;

	@MappableAttribute
	@CodeSpace("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/HydroNode")
	public CodeType getInspireIdDatasetCode() {
		return inspireIdDatasetCode;
	}

	@MappableAttribute
	@CodeSpace("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/HydroNode")
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
	public String getName() {
		return name;
	}

	@MappableAttribute
	public void setName(String name) {
		this.name = name;
	}
	
	@MappableAttribute
	@CodeSpace("http://inspire.ec.europa.eu/codeList/HydroNodeCategoryValue")
	public CodeType getCategory() {
		return category;
	}

	@MappableAttribute
	@CodeSpace("http://inspire.ec.europa.eu/codeList/HydroNodeCategoryValue")
	public void setCategory(CodeType category) {
		this.category = category;
	}

}
