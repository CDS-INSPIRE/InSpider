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

	@Column(name = "geometry")
	private Geometry geometry;

	@Column(name = "name")
	private String name;

	@Column(name = "end_node_local_id")
	private String endNodeLocalId;	

	@Column(name = "start_node_local_id")
	private String startNodeLocalId;	

	@MappableAttribute
	@CodeSpace("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/WatercourseLink")
	public CodeType getInspireIdDatasetCode() {
		return inspireIdDatasetCode;
	}

	@MappableAttribute
	@CodeSpace("http://www.inspire-provincies.nl/codeList/DatasetTypeCode/WatercourseLink")
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
	public String getEndNodeLocalId() {
		return endNodeLocalId;
	}

	@MappableAttribute	
	public void setEndNodeLocalId(String endNodeLocalId) {
		this.endNodeLocalId = endNodeLocalId;
	}

	@MappableAttribute
	public String getStartNodeLocalId() {
		return startNodeLocalId;
	}

	@MappableAttribute
	public void setStartNodeLocalId(String startNodeLocalId) {
		this.startNodeLocalId = startNodeLocalId;
	}

}
