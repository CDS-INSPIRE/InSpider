package nl.ipo.cds.validation;

import nl.ipo.cds.validation.gml.codelists.CodeListFactory;

import org.deegree.geometry.Geometry;
import org.deegree.geometry.primitive.Point;

public interface ValidatorContext<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>> {

	void setLastLocation(Point location);

	Point getLastLocation();

	GeometryValidationResult validateGeometry(Geometry geometry);
	
	ValidationReporter<K, C> getReporter ();
	
	CodeListFactory getCodeListFactory ();
}