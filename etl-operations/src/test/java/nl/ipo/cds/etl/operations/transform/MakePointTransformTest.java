package nl.ipo.cds.etl.operations.transform;

import static org.junit.Assert.*;

import org.deegree.geometry.primitive.Point;
import org.junit.Test;

public class MakePointTransformTest {

	@Test
	public void testCreatePoint () throws Exception {
		final MakePointTransform.Settings settings = new MakePointTransform.Settings ();
		
		settings.setCrs ("EPSG:28992");
		
		final MakePointTransform transform = new MakePointTransform ();

		transform.createFactory (settings);
		final Point point = (Point)transform.execute (Double.valueOf (1), Double.valueOf (2));

		assertEquals (1, point.get0 (), 0.001);
		assertEquals (2, point.get1 (), 0.001);
		assertNotNull (point.getCoordinateSystem ());
		assertTrue (point.getCoordinateSystem ().getCode ().getOriginal ().contains ("28992"));
	}
}
