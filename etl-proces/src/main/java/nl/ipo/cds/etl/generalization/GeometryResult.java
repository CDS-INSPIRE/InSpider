package nl.ipo.cds.etl.generalization;

import com.vividsolutions.jts.geom.Geometry;

public class GeometryResult implements Event {

	private final Geometry geometry;
	private final String[] columnValues;
	
	public GeometryResult(Geometry geometry, String[] columnValues) {
		this.geometry = geometry;
		this.columnValues = columnValues;
	}
	
	public String[] getColumnValues() {
		return columnValues;
	}
	
	public Geometry getGeometry() {
		return geometry;
	}
}
