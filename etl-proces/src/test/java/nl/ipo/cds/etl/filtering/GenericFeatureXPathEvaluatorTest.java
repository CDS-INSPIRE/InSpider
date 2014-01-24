package nl.ipo.cds.etl.filtering;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import nl.ipo.cds.etl.GenericFeature;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.filter.expression.ValueReference;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.WKTReader;
import org.jaxen.SimpleNamespaceContext;
import org.junit.Test;

import com.vividsolutions.jts.io.ParseException;

public class GenericFeatureXPathEvaluatorTest {

	@Test
	public void testXPathEvaluator () throws Exception {
		final GenericFeature feature = createFeature ();
		final GenericFeatureXPathEvaluator evaluator = new GenericFeatureXPathEvaluator ();

		// Existing properties of different types:
		assertPrimitive (evaluator.eval (feature, ref ("stringProperty")), BaseType.STRING, "Hello, world!");
		assertPrimitive (evaluator.eval (feature, ref ("booleanProperty")), BaseType.BOOLEAN, true);
		assertPrimitive (evaluator.eval (feature, ref ("decimalProperty")), BaseType.DECIMAL, new BigDecimal (42));
		assertPrimitive (evaluator.eval (feature, ref ("doubleProperty")), BaseType.DOUBLE, 43.43);
		assertPrimitive (evaluator.eval (feature, ref ("floatProperty")), BaseType.DOUBLE, 44.44);
		assertPrimitive (evaluator.eval (feature, ref ("integerProperty")), BaseType.INTEGER, 45);
		assertPrimitive (evaluator.eval (feature, ref ("dateProperty")), BaseType.DATE, new org.deegree.commons.tom.datetime.Date (new java.util.Date (0), null));
		assertPrimitive (evaluator.eval (feature, ref ("dateTimeProperty")), BaseType.DATE_TIME, new org.deegree.commons.tom.datetime.DateTime (new java.util.Date (1), null));
		assertPrimitive (evaluator.eval (feature, ref ("timeProperty")), BaseType.TIME, new org.deegree.commons.tom.datetime.Time (new java.util.Date (2), null));
		
		// References to the geometry property should be returned as-is:
		final TypedObjectNode[] geometryResult = evaluator.eval (feature, ref ("geometryProperty"));
		assertEquals (1, geometryResult.length);
		assertTrue (geometryResult[0] instanceof Geometry);
		
		// References to the area of a geometry should return a double:
		assertPrimitive (evaluator.eval (feature, ref ("geometryProperty/area")), BaseType.DOUBLE, null);

		// Properties that have an explicit null value should be returned as such:
		final TypedObjectNode[] nullResult = evaluator.eval (feature, ref ("nullValueProperty"));
		assertEquals (1, nullResult.length);
		assertNull (nullResult[0]);
		
		// Non-existing properties:
		assertEquals (0, evaluator.eval (feature, ref ("nonExistingProperty")).length);
	}
	
	@Test (expected = IllegalStateException.class)
	public void testXPathEvaluatorInvalidType () throws Exception {
		final GenericFeature feature = createFeature ();
		final GenericFeatureXPathEvaluator evaluator = new GenericFeatureXPathEvaluator ();
		
		// Existing property, but of an unknown type:
		assertEquals (0, evaluator.eval (feature, ref ("invalidTypeProperty")).length);
	}
	
	private static void assertTom (final TypedObjectNode[] nodes, final Class<? extends TypedObjectNode> expectedClass) {
		assertNotNull (nodes);
		assertEquals (1, nodes.length);
		assertNotNull (nodes[0]);
		assertTrue (expectedClass.isAssignableFrom (nodes[0].getClass ()));
	}
	
	private static void assertPrimitive (final TypedObjectNode[] nodes, final BaseType baseType, final Object expectedValue) {

		assertTom (nodes, PrimitiveValue.class);
		
		final PrimitiveValue value = (PrimitiveValue)nodes[0];
		
		assertNotNull (value.getType ());
		assertNotNull (value.getType ().getBaseType ());
		assertEquals (value.getType ().getBaseType (), baseType);
		
		if (expectedValue != null) {
			assertEquals (expectedValue, value.getValue ());
		}
	}
	
	private static ValueReference ref (final String name) {
		final SimpleNamespaceContext nsContext = new SimpleNamespaceContext ();
		nsContext.addNamespace ("app", "http://www.idgis.nl/test");
		return new ValueReference (String.format ("app:%s", name), nsContext);
	}
	
	private static GenericFeature createFeature () {
		final Map<String, Object> properties = new HashMap<String, Object> () {
			private static final long serialVersionUID = -1709912833020416177L;
			{
				put ("stringProperty", "Hello, world!");
				put ("booleanProperty", true);
				put ("decimalProperty", new BigDecimal (42));
				put ("doubleProperty", 43.43);
				put ("floatProperty", 44.44);
				put ("integerProperty", 45);
				put ("dateProperty", new Date (0));
				put ("dateTimeProperty", new Timestamp (1));
				put ("timeProperty", new Time (2));
				put ("geometryProperty", createGeometry ());
				put ("invalidTypeProperty", GenericFeatureXPathEvaluator.class);
				put ("nullValueProperty", null);
			}
		};
		
		return new GenericFeature ("test-feature-id", properties);
	}
	
	private static Geometry createGeometry () {
		final WKTReader reader = new WKTReader (null);
		
		try {
			return reader.read ("POLYGON((208939.795029 490351.884381,238330.434512 488526.378823,231758.614504 473922.334360,206931.738915 480402.879090,208939.795029 490351.884381))");
		} catch (ParseException e) {
			return null;
		}
	}
}
