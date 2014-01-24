package nl.ipo.cds.validation;

import org.deegree.geometry.primitive.Point;

public class GeometryValidationStatus {
	private final Point location;
	
	public GeometryValidationStatus () {
		this.location = null;
	}
	
	public GeometryValidationStatus (final Point location) {
		this.location = location;
	}
	
	public boolean status () {
		return location != null;
	}
	
	public Point location () {
		return location;
	}
}