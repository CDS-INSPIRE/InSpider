package nl.ipo.cds.validation;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import nl.ipo.cds.validation.gml.codelists.CodeListFactory;

import org.deegree.geometry.Geometry;
import org.deegree.geometry.primitive.Point;

public class DefaultValidatorContext<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>> implements ValidatorContext<K, C> {
	public final CodeListFactory codeListFactory;
	public final ValidationReporter<K, C> reporter;
	
	private GeometryValidationResult currentGeometryValidationResult = null;

	private final LinkedHashMap<Geometry,GeometryValidationResult> geometryToValidationResult = new LinkedHashMap<Geometry,GeometryValidationResult> (100, 0.75f, true) {

		private static final long serialVersionUID = 8716160952616001432L;
		
        protected boolean removeEldestEntry(Entry<Geometry, GeometryValidationResult> eldest) {
        	return size() == 100;
        }
		
	};
	
	private Point lastLocation = null;
	
	public DefaultValidatorContext (final CodeListFactory codeListFactory, final ValidationReporter<K, C> reporter) {
		this.codeListFactory = codeListFactory;
		this.reporter = reporter;
	}
	
	/* (non-Javadoc)
	 * @see nl.ipo.cds.validation.ValidatorContextInterface#setLastLocation(org.deegree.geometry.primitive.Point)
	 */
	@Override
	public void setLastLocation (final Point location) {
		this.lastLocation = location;
	}
	
	/* (non-Javadoc)
	 * @see nl.ipo.cds.validation.ValidatorContextInterface#getLastLocation()
	 */
	@Override
	public Point getLastLocation () {
		return lastLocation;
	}
	
	/* (non-Javadoc)
	 * @see nl.ipo.cds.validation.ValidatorContextInterface#validateGeometry(org.deegree.geometry.Geometry)
	 */
	@Override
	public GeometryValidationResult validateGeometry (final Geometry geometry) {
		if (currentGeometryValidationResult == null || currentGeometryValidationResult.geometry != geometry) {
			currentGeometryValidationResult = doValidateGeometry (geometry);
		}
		
		return currentGeometryValidationResult;
	}
	
	private GeometryValidationResult doValidateGeometry (final Geometry geometry) {		
		GeometryValidationResult result = geometryToValidationResult.get(geometry);
		if (result == null) {
			result = new GeometryValidationResult (geometry);
			geometryToValidationResult.put(geometry, result);
		}
		return result;
	}
	
	@Override
	public ValidationReporter<K, C> getReporter () {
		return reporter;
	}

	@Override
	public CodeListFactory getCodeListFactory () {
		return codeListFactory;
	}
}
