package nl.ipo.cds.etl.filtering;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import nl.ipo.cds.etl.GenericFeature;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.BaseType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.XPathEvaluator;
import org.deegree.filter.expression.ValueReference;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.multi.MultiPolygon;
import org.deegree.geometry.multi.MultiSurface;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Surface;

public class GenericFeatureXPathEvaluator implements XPathEvaluator<GenericFeature> {

	@Override
	public TypedObjectNode[] eval (final GenericFeature context, final ValueReference valueRef) throws FilterEvaluationException {
		final String[] parts = parseExpression (valueRef.getAsText ()); 
		
		if (parts.length == 0 || parts.length > 2) {
			return new TypedObjectNode[0];
		}
		
		final String name = parts[0];
		final String propertyName = parts.length > 1 ? parts[1] : null;
		
		if (!context.hasProperty (name)) {
			return new TypedObjectNode[0];
		}
		
		final Object value = context.get (name);
		
		if (value instanceof String) {
			return wrapTom (value ((String)value, BaseType.STRING));
		} else if (value instanceof Boolean) {
			return wrapTom (value ((Boolean)value, BaseType.BOOLEAN));
		} else if (value instanceof BigDecimal) {
			return wrapTom (value ((BigDecimal)value, BaseType.DECIMAL));
		} else if (value instanceof BigInteger) {
			return wrapTom (value ((BigInteger)value, BaseType.INTEGER));
		} else if (value instanceof Double) {
			return wrapTom (value ((Double)value, BaseType.DOUBLE));
		} else if (value instanceof Float) {
			final double val = (Float)value;
			return wrapTom (value (val, BaseType.DOUBLE));
		} else if (value instanceof Integer) {
			return wrapTom (value ((Integer)value, BaseType.INTEGER));
		} else if (value instanceof Date) {
			final org.deegree.commons.tom.datetime.Date d = new org.deegree.commons.tom.datetime.Date (new java.util.Date (((Date)value).getTime ()), null);
			return wrapTom (value (d, BaseType.DATE));
		} else if (value instanceof Timestamp) {
			final org.deegree.commons.tom.datetime.DateTime dt = new org.deegree.commons.tom.datetime.DateTime (new java.util.Date (((Timestamp)value).getTime ()), null);
			return wrapTom (value (dt, BaseType.DATE_TIME));
		} else if (value instanceof Time) {
			final org.deegree.commons.tom.datetime.Time t = new org.deegree.commons.tom.datetime.Time (new java.util.Date (((Time)value).getTime ()), null);
			return wrapTom (value (t, BaseType.TIME));
		} else if ((value instanceof Geometry) && "area".equals (propertyName)) {
			return wrapTom (value (getArea ((Geometry)value), BaseType.DOUBLE));
		} else if (value instanceof Geometry) {
			return wrapTom ( (Geometry)value );
		} else if (value == null) {
			return new TypedObjectNode[] { null };
		} else {
			throw new IllegalStateException (String.format ("Unknown feature attribute type `%s`", value == null ? "null" : value.getClass ().getCanonicalName ()));
		}
	}

	private double getArea (final Geometry geometry) {
		if (geometry instanceof Polygon) {
			return ((Polygon)geometry).getArea (null).getValueAsDouble ();
		} else if (geometry instanceof MultiPolygon) {
			final MultiPolygon multi = (MultiPolygon)geometry;
			double area = 0;
			
			for (final Polygon poly: multi) {
				area += poly.getArea (null).getValueAsDouble ();
			}
			
			return area;
		} else if (geometry instanceof Surface) {
			return ((Surface)geometry).getArea (null).getValueAsDouble ();
		} else if (geometry instanceof MultiSurface) {
			@SuppressWarnings("unchecked")
			final MultiSurface<Surface> geom = (MultiSurface<Surface>)geometry;
			double area = 0;
			
			for (final Surface surface: geom) {
				area += surface.getArea (null).getValueAsDouble ();
			}
			
			return area;
		} else {
			return 0;
		}
	}
	
	@Override
	public String getId (final GenericFeature context) {
		return context.getId ();
	}

	private static PrimitiveValue value (final Object value, final BaseType baseType) {
		return new PrimitiveValue (value, new PrimitiveType (baseType));
	}
	
	private static TypedObjectNode[] wrapTom (final TypedObjectNode ... value) {
		return value;
	}
	
	private static String[] parseExpression (final String xpath) {
		if (xpath == null || xpath.isEmpty ()) {
			return new String[0];
		}
		
		final String[] parts = xpath.split ("\\/");
		final String[] result = new String[parts.length];

		for (int i = 0; i < parts.length; ++ i) {
			final String part = parts[i];
			final int offset = part.indexOf (':');
			
			if (offset >= 0) {
				result[i] = part.substring (offset + 1);
			} else {
				result[i] = part;
			}
		}
		
		return result;
	}
}
