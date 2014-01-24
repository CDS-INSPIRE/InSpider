package nl.ipo.cds.etl;

import static org.junit.Assert.assertFalse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.deegree.geometry.GeometryFactory;

import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;

import org.deegree.geometry.validation.GeometryValidationEventHandler;
import org.deegree.geometry.validation.GeometryValidator;

import org.junit.Before;
import org.junit.Test;

public class GeometryValidatorTest {
	
	private GeometryValidator geometryValidator;
	private GeometryFactory geometryFactory;	
	private List<String> validationResult; 
	
	@Before
	public void setUp() {
		GeometryValidationEventHandler handler = (GeometryValidationEventHandler)Proxy.newProxyInstance(
			getClass().getClassLoader(), 
			new Class<?>[]{GeometryValidationEventHandler.class}, 
			new InvocationHandler() {
				
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				StringBuilder builder = new StringBuilder(method.getName());
				
				for(Object arg : args) {
					builder.append(" ");
					builder.append(arg);
				}
				
				validationResult.add(builder.toString());
				
				return false;
			}
		});
		
		geometryValidator = new GeometryValidator(handler);
		geometryFactory = new GeometryFactory();
		validationResult = new ArrayList<String>();
	}
	
	@Test
	public void testSingleRing() {
		Polygon polygon = createPolygon(
			new double[] {
				0, 0, 
				1000, 0, 
				1000, 1000, 
				750, 500, 
				500, 500, 
				1000, 1000, 
				0, 1000, 
				0, 0});
		
		geometryValidator.validateGeometry(polygon);
		assertFalse(validationResult.isEmpty());
	}
	
	@Test
	public void testInteriorIntersectsExteriorOnce0() {
		Polygon polygon = createPolygon(
			new double[] {
				0, 0,
				1000, 0,
				1000, 1000,
				0, 1000,
				0, 0},
			
			new double[] {
				1000, 1000,
				750, 500,
				500, 500,	
				1000, 1000
				});
		
		geometryValidator.validateGeometry(polygon);
		assertFalse(validationResult.isEmpty());
	}
	
	@Test
	public void testInteriorIntersectsExteriorOnce1() {
		Polygon polygon = createPolygon(
			new double[] {
				0, 0,
				1000, 0,
				1000, 1000,
				0, 1000,
				0, 0},
			
			new double[] {
				900, 900,
				750, 500,
				0, 0,
				900, 900
				});
		
		geometryValidator.validateGeometry(polygon);
		assertFalse(validationResult.isEmpty());
	}
	
	@Test
	public void testInteriorIntersectsExteriorTwice() {
		Polygon polygon = createPolygon(
			new double[] {
				0, 0,
				1000, 0,
				1000, 1000,
				0, 1000,
				0, 0},
			
			new double[] {
				1000, 1000,
				750, 500,
				0, 0, 
				1000, 1000
				});
		
		geometryValidator.validateGeometry(polygon);
		assertFalse(validationResult.isEmpty());
	}
	
	private Polygon createPolygon(double[] exterior) {
		return geometryFactory.createPolygon(null, null, 
				createRing(exterior), Collections.<Ring>emptyList());
	}
	
	private Polygon createPolygon(double[] exterior, double[] interior) {
		return geometryFactory.createPolygon(null, null, 
				createRing(exterior), Arrays.asList(createRing(interior)));
	}
	
	private Ring createRing(double[] points) {
		ArrayList<Point> pointList = new ArrayList<Point>();
		int i = 0;
		while(i < points.length) {
			pointList.add(geometryFactory.createPoint(null, new double[]{points[i++], points[i++]}, null));
		}
		return geometryFactory.createLinearRing(null, null, geometryFactory.createPoints(pointList));
	}
}
