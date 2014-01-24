package nl.ipo.cds.etl.operations.transform;

import nl.ipo.cds.attributemapping.operations.annotation.Before;
import nl.ipo.cds.attributemapping.operations.annotation.Execute;
import nl.ipo.cds.attributemapping.operations.annotation.Input;
import nl.ipo.cds.attributemapping.operations.annotation.MappingOperation;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;

@MappingOperation (propertiesClass = MakePointTransform.Settings.class)
public class MakePointTransform {

	private GeometryFactory factory;
	private ICRS crs;
	
	@Before
	public void createFactory (final Settings settings) {
		factory = new GeometryFactory ();
		
		// Get the CRS:
		final String crsString = settings == null ? null : settings.getCrs ();
		if (crsString != null) {
			crs = CRSManager.getCRSRef (crsString);
		} else {
			crs = null;
		}
	}
	
	@Execute
	public Geometry execute (final @Input("x") Double x, final @Input("y") Double y) {
		if (x == null || y == null) {
			return null;
		}
		
		return factory.createPoint (null, x, y, crs);
	}

	public static class Settings {
		private String crs = "EPSG:28992";

		public String getCrs () {
			return crs;
		}

		public void setCrs (final String crs) {
			this.crs = crs;
		}
	}
}
