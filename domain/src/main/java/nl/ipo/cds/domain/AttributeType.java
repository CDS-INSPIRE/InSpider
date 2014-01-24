package nl.ipo.cds.domain;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.geometry.Geometry;

public enum AttributeType {

	STRING(String.class),
	BOOLEAN(Boolean.class),
	DECIMAL(BigDecimal.class),
	DOUBLE(Double.TYPE),
	FLOAT(Float.TYPE),
	INTEGER(BigInteger.class),
	DATE(Date.class),
	DATE_TIME(Timestamp.class),
	TIME(Time.class),
	GEOMETRY(Geometry.class),
	CODE(CodeType.class);

	private final Type javaType;
	
	AttributeType (final Type javaType) {
		this.javaType = javaType;
	}
	
	/**
	 * Returns the java type corresponding with this attribute type.
	 * 
	 * @return The java type corresponding with this attribute type.
	 */
	public Type getJavaType () {
		return javaType;
	}
	
	/**
	 * Convert from the toString representation of a Java type to an attribute type.
	 * 
	 * @param attributeTypeString
	 * @return An AttributeType instance corresponding to the string, or null if
	 * 	one doesn't exist.
	 */
	public static AttributeType fromString (final String attributeTypeString) {
		for (final AttributeType at: AttributeType.values ()) {
			final String typeString = at.getJavaType ().toString ();
			if (typeString.equals (attributeTypeString)) {
				return at;
			}
		}
		
		return null;
	}
}