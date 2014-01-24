package nl.ipo.cds.etl.operations.transform;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.ipo.cds.attributemapping.MappingSource;
import nl.ipo.cds.attributemapping.operations.annotation.Execute;
import nl.ipo.cds.attributemapping.operations.annotation.Input;
import nl.ipo.cds.attributemapping.operations.annotation.MappingOperation;
import nl.ipo.cds.etl.operations.transform.ConditionalTransform.Settings;

import org.deegree.geometry.Geometry;
import org.deegree.geometry.multi.MultiCurve;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.multi.MultiLineString;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.multi.MultiPolygon;
import org.deegree.geometry.multi.MultiSurface;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Surface;

@MappingOperation (propertiesClass = Settings.class, internal = true) 
public class ConditionalTransform {
	
	public final static Map<String, Class<? extends Geometry>> geometryTypeMap;
	static {
		final HashMap<String, Class<? extends Geometry>> dict = new HashMap<> ();
		
		dict.put ("curve", Curve.class);
		dict.put ("point", Point.class);
		dict.put ("surface", Surface.class);
		dict.put ("polygon", Polygon.class);
		dict.put ("multi", MultiGeometry.class);
		dict.put ("multiCurve", MultiCurve.class);
		dict.put ("multiLineString", MultiLineString.class);
		dict.put ("multiPoint", MultiPoint.class);
		dict.put ("multiSurface", MultiSurface.class);
		dict.put ("multiPolygon", MultiPolygon.class);
		
		geometryTypeMap = Collections.unmodifiableMap (dict);
	}

	@Execute
	public Object execute (final Settings settings, final MappingSource source, final @Input("values") Object ... values) {
		final Condition[] conditions = settings.conditions;
		final int conditionCount = conditions.length;
		
		if (values.length != conditionCount + 1) {
			throw new IllegalArgumentException (String.format ("Invalid number of arguments to conditional transform, expected %d", conditionCount + 1));
		}
		
		loop: for (int i = 0; i < conditionCount; ++ i) {
			final Condition condition = conditions[i];
			final Object attributeValue = source.getAttributeValue (condition.attribute);
			final Operation operation = condition.operation;

			switch (operation) {
			case IN:
			{
				if (attributeValue == null) {
					continue;
				}
				
				final String stringAttributeValue = attributeValue.toString ();
						
				for (final String s: condition.values) {
					if (stringAttributeValue.equals (s)) {
						return values[i];
					}
				}
				break;
			}
			case NOT_IN:
			{
				if (attributeValue == null) {
					return values[i];
				}
				
				final String stringAttributeValue = attributeValue.toString ();
						
				for (final String s: condition.values) {
					if (stringAttributeValue.equals (s)) {
						continue loop;
					}
				}
				
				return values[i];
			}
			case IS_EMPTY:
				if (attributeValue == null || (attributeValue instanceof String && ((String)attributeValue).trim ().isEmpty ())) {
					return values[i];
				}
				break;
			case IS_NOT_EMPTY:
				if (attributeValue != null && !(attributeValue instanceof String && ((String)attributeValue).trim ().isEmpty ())) {
					return values[i];
				}
				break;
			case IS_NULL:
				if (attributeValue == null) {
					return values[i];
				}
				break;
			case IS_NOT_NULL:
				if (attributeValue != null) {
					return values[i];
				}
				break;
			case IS_GEOMETRY_TYPE:
				if (attributeValue == null || !(attributeValue instanceof Geometry)) {
					continue;
				}
				
				final Geometry geom = (Geometry)attributeValue;
				final Class<? extends Geometry> geomClass = geom.getClass (); 
				
				for (final String typeString: condition.values) {
					final Class<? extends Geometry> cls = geometryTypeMap.get (typeString);
					if (cls == null) {
						continue;
					}
					
					if (cls.isAssignableFrom (geomClass)) {
						return values[i];
					}
				}
				break;
			}
		}
		
		return values[conditionCount];
	}
	
	public final static class Settings {
		Condition[] conditions = new Condition[0];

		public List<Condition> getConditions() {
			return conditions == null ? Collections.<Condition>emptyList () : Arrays.asList (conditions);
		}

		public void setConditions (final List<Condition> conditions) {
			if (conditions == null) {
				throw new NullPointerException ();
			}
			
			this.conditions = conditions.toArray (new Condition[conditions.size ()]);
		}
	}
	
	public static enum Operation {
		IS_EMPTY,
		IS_NULL,
		IS_NOT_NULL,
		IS_NOT_EMPTY,
		IN,
		NOT_IN,
		
		IS_GEOMETRY_TYPE
	}
	
	public final static class Condition {
		String attribute;
		Operation operation = Operation.IN;
		String[] values;
		
		public String getAttribute () {
			return attribute;
		}
		
		public void setAttribute (final String attribute) {
			this.attribute = attribute;
		}

		public Operation getOperation () {
			return operation;
		}
		
		public void setOperation (final Operation operation) {
			this.operation = operation;
		}
		
		public String[] getValues () {
			return values;
		}
		
		public void setValues (final String[] values) {
			if (values == null) {
				throw new NullPointerException ();
			}
			for (final String s: values) {
				if (s == null) {
					throw new NullPointerException ();
				}
			}
			
			this.values = Arrays.copyOf (values, values.length);
		}
	}
}
