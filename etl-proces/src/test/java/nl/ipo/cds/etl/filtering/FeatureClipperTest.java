package nl.ipo.cds.etl.filtering;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.ipo.cds.etl.Feature;
import nl.ipo.cds.etl.FeatureOutputStream;
import nl.ipo.cds.etl.PersistableFeature;
import nl.ipo.cds.etl.theme.protectedSite.ProtectedSite;

import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.WKTReader;
import org.deegree.geometry.io.WKTWriter;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.io.ParseException;

public class FeatureClipperTest {

	private WKTReader reader;
	private Geometry clipGeometry;
	private OutputStream outputStream;
	private ErrorOutputStream errorStream;
	private FeatureClipper clipper;
	
	@Before
	public void createClipper () throws ParseException {
		reader = new WKTReader (null);
		clipGeometry = reader.read ("POLYGON((217884.772263 508606.939961,224821.693383 509793.518573,230206.934779 507602.911904,235774.726731 490260.609103,217976.047541 486427.047432,210035.098364 498566.659392,217884.772263 508606.939961))");
		outputStream = new OutputStream ();
		errorStream = new ErrorOutputStream ();
		clipper = new FeatureClipper (clipGeometry, ProtectedSite.class);
	}

	@Test
	public void testClipFeatures () throws ParseException {
		final ProtectedSite feature = createFeature ("POLYGON((208939.795029 490351.884381,238330.434512 488526.378823,231758.614504 473922.334360,206931.738915 480402.879090,208939.795029 490351.884381))");
		
		clipper.processFeature (feature, outputStream, errorStream);
		
		assertEquals (1, outputStream.getFeatures ().size ());
		assertNotNull (outputStream.getFeatures ().get (0));
		assertTrue (((ProtectedSite)outputStream.getFeatures ().get (0)).getGeometry () instanceof Polygon);
		
		final Polygon geom = (Polygon)((ProtectedSite)outputStream.getFeatures ().get (0)).getGeometry ();
		System.out.println (WKTWriter.write (geom));
		
		assertEquals (4, geom.getExteriorRingCoordinates ().size ());
	}
	
	@Test
	public void testContainedFeatures () throws ParseException {
		final ProtectedSite feature = createFeature ("POLYGON((216606.918373 502400.221064,224274.041716 503221.698565,224821.693383 495919.676333,216880.744206 495737.125777,216606.918373 502400.221064))");
		final Geometry originalGeometry = feature.getGeometry ();
		
		clipper.processFeature (feature, outputStream, errorStream);
		
		assertEquals (1, outputStream.getFeatures ().size ());
		assertNotNull (outputStream.getFeatures ().get (0));
		assertTrue (((ProtectedSite)outputStream.getFeatures ().get (0)).getGeometry () instanceof Polygon);
		
		final Polygon geom = (Polygon)((ProtectedSite)outputStream.getFeatures ().get (0)).getGeometry ();
		System.out.println (WKTWriter.write (geom));
		
		assertEquals (5, geom.getExteriorRingCoordinates ().size ());
		
		final Points originalPoints = ((Polygon)originalGeometry).getExteriorRingCoordinates ();
		final Points resultPoints = geom.getExteriorRingCoordinates ();
				
		assertEquals (originalPoints.size (), resultPoints.size ());
		assertTrue (originalPoints.size () > 0);
		
		for (int i = 0; i < originalPoints.size (); ++i) {
			final Point a = originalPoints.get (i);
			final Point b = resultPoints.get (i);
			
			assertEquals (a.get1 (), b.get1 (), .00001);
			assertEquals (a.get2 (), b.get2 (), .00001);
		}
	}
	
	@Test
	public void testFilterFeatures () throws ParseException {
		final ProtectedSite feature = createFeature ("POLYGON((207114.289471 510706.271352,216972.019484 508241.838849,210947.851143 500392.164950,203828.379467 506781.434403,207114.289471 510706.271352))");

		clipper.processFeature (feature, outputStream, errorStream);
		
		assertEquals (0, outputStream.getFeatures ().size ());
	}
	

	private ProtectedSite createFeature (final String wktGeometry) throws ParseException {
		final Geometry geometry = reader.read (wktGeometry);
		final ProtectedSite feature = new ProtectedSite ();
		
		feature.setGeometry (geometry);
		
		return feature;
	}
	
	private static class OutputStream implements FeatureOutputStream<PersistableFeature> {
		private List<PersistableFeature> features = new ArrayList<PersistableFeature> ();
		
		@Override
		public void writeFeature (final PersistableFeature feature) {
			features.add (feature);
		}
		
		public List<PersistableFeature> getFeatures () {
			return Collections.unmodifiableList (features);
		}
	}
	
	private static class ErrorOutputStream implements FeatureOutputStream<Feature> {
		private List<Feature> features = new ArrayList<Feature> ();
		
		@Override
		public void writeFeature (final Feature feature) {
			features.add (feature);
		}
		
		@SuppressWarnings("unused")
		public List<Feature> getFeatures () {
			return Collections.unmodifiableList (features);
		}
	}
}
