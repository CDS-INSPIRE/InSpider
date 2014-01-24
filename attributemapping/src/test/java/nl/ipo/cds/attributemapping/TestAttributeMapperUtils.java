package nl.ipo.cds.attributemapping;

import static nl.ipo.cds.attributemapping.AttributeMapperUtils.areTypesAssignable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestAttributeMapperUtils {

	@Test
	public void testAssignablePrimitiveTypes () {
		assertTrue (areTypesAssignable (Byte.TYPE, Byte.TYPE));
		assertTrue (areTypesAssignable (Character.TYPE, Character.TYPE));
		assertTrue (areTypesAssignable (Short.TYPE, Short.TYPE));
		assertTrue (areTypesAssignable (Integer.TYPE, Integer.TYPE));
		assertTrue (areTypesAssignable (Long.TYPE, Long.TYPE));
		assertTrue (areTypesAssignable (Float.TYPE, Float.TYPE));
		assertTrue (areTypesAssignable (Double.TYPE, Double.TYPE));
		assertTrue (areTypesAssignable (Boolean.TYPE, Boolean.TYPE));
	}
	
	@Test
	public void testAssignableBoxing () throws Exception {
		final int value = 1;
		final Method method = getClass ().getMethod ("testBoxing", Integer.class);
		method.invoke (this, value);
		
		assertTrue (areTypesAssignable (Byte.TYPE, Byte.class));
		assertTrue (areTypesAssignable (Character.TYPE, Character.class));
		assertTrue (areTypesAssignable (Short.TYPE, Short.class));
		assertTrue (areTypesAssignable (Integer.TYPE, Integer.class));
		assertTrue (areTypesAssignable (Long.TYPE, Long.class));
		assertTrue (areTypesAssignable (Float.TYPE, Float.class));
		assertTrue (areTypesAssignable (Double.TYPE, Double.class));
		assertTrue (areTypesAssignable (Boolean.TYPE, Boolean.class));
	}
	
	@Test
	public void testAssignableUnboxing () throws Exception {
		final Integer value = 1;
		final Method method = getClass ().getMethod ("testUnboxing", Integer.TYPE);
		method.invoke (this, value);
		
		assertTrue (areTypesAssignable (Byte.class, Byte.TYPE));
		assertTrue (areTypesAssignable (Character.class, Character.TYPE));
		assertTrue (areTypesAssignable (Short.class, Short.TYPE));
		assertTrue (areTypesAssignable (Integer.class, Integer.TYPE));
		assertTrue (areTypesAssignable (Long.class, Long.TYPE));
		assertTrue (areTypesAssignable (Float.class, Float.TYPE));
		assertTrue (areTypesAssignable (Double.class, Double.TYPE));
		assertTrue (areTypesAssignable (Boolean.class, Boolean.TYPE));
	}
	
	@Test
	public void testAssignableUpcasting () throws Exception {
		final int value = 1;
		final Method method = getClass ().getMethod ("testUpcasting", Long.TYPE);
		method.invoke (this, value);

		assertTrue (areTypesAssignable (Byte.TYPE, Short.TYPE));
		assertTrue (areTypesAssignable (Byte.TYPE, Character.TYPE));
		assertTrue (areTypesAssignable (Byte.TYPE, Integer.TYPE));
		assertTrue (areTypesAssignable (Byte.TYPE, Long.TYPE));
		assertFalse (areTypesAssignable (Byte.TYPE, Float.TYPE));
		assertFalse (areTypesAssignable (Byte.TYPE, Double.TYPE));
		
		assertFalse (areTypesAssignable (Character.TYPE, Byte.TYPE));
		assertTrue (areTypesAssignable (Character.TYPE, Short.TYPE));
		assertTrue (areTypesAssignable (Character.TYPE, Integer.TYPE));
		assertTrue (areTypesAssignable (Character.TYPE, Long.TYPE));
		assertFalse (areTypesAssignable (Character.TYPE, Float.TYPE));
		assertFalse (areTypesAssignable (Character.TYPE, Double.TYPE));
		
		assertFalse (areTypesAssignable (Short.TYPE, Byte.TYPE));
		assertTrue (areTypesAssignable (Short.TYPE, Character.TYPE));
		assertTrue (areTypesAssignable (Short.TYPE, Integer.TYPE));
		assertTrue (areTypesAssignable (Short.TYPE, Long.TYPE));
		assertFalse (areTypesAssignable (Short.TYPE, Float.TYPE));
		assertFalse (areTypesAssignable (Short.TYPE, Double.TYPE));
		
		assertFalse (areTypesAssignable (Integer.TYPE, Byte.TYPE));
		assertFalse (areTypesAssignable (Integer.TYPE, Character.TYPE));
		assertFalse (areTypesAssignable (Integer.TYPE, Short.TYPE));
		assertTrue (areTypesAssignable (Integer.TYPE, Long.TYPE));
		assertFalse (areTypesAssignable (Integer.TYPE, Float.TYPE));
		assertFalse (areTypesAssignable (Integer.TYPE, Double.TYPE));
		
		assertFalse (areTypesAssignable (Long.TYPE, Byte.TYPE));
		assertFalse (areTypesAssignable (Long.TYPE, Character.TYPE));
		assertFalse (areTypesAssignable (Long.TYPE, Short.TYPE));
		assertFalse (areTypesAssignable (Long.TYPE, Integer.TYPE));
		assertFalse (areTypesAssignable (Long.TYPE, Float.TYPE));
		assertFalse (areTypesAssignable (Long.TYPE, Double.TYPE));
		
		assertFalse (areTypesAssignable (Float.TYPE, Byte.TYPE));
		assertFalse (areTypesAssignable (Float.TYPE, Character.TYPE));
		assertFalse (areTypesAssignable (Float.TYPE, Short.TYPE));
		assertFalse (areTypesAssignable (Float.TYPE, Integer.TYPE));
		assertFalse (areTypesAssignable (Float.TYPE, Long.TYPE));
		assertTrue (areTypesAssignable (Float.TYPE, Double.TYPE));

		assertFalse (areTypesAssignable (Double.TYPE, Byte.TYPE));
		assertFalse (areTypesAssignable (Double.TYPE, Character.TYPE));
		assertFalse (areTypesAssignable (Double.TYPE, Short.TYPE));
		assertFalse (areTypesAssignable (Double.TYPE, Integer.TYPE));
		assertFalse (areTypesAssignable (Double.TYPE, Long.TYPE));
		assertFalse (areTypesAssignable (Double.TYPE, Float.TYPE));
		
		// Test whether boxed types are converted during upcast-check:
		assertTrue (areTypesAssignable (Byte.class, Long.TYPE));
		assertTrue (areTypesAssignable (Short.class, Long.TYPE));
		assertTrue (areTypesAssignable (Character.class, Long.TYPE));
		assertTrue (areTypesAssignable (Integer.class, Long.TYPE));
		assertTrue (areTypesAssignable (Float.class, Double.TYPE));
	}
	
	@Test
	public void testAssignableSubclasses () {
		assertTrue (areTypesAssignable (Horse.class, Animal.class));
		assertFalse (areTypesAssignable (Animal.class, Horse.class));
	}
	
	@Test
	public void testAssignableArrays () {
		assertTrue (areTypesAssignable (String[].class, String[].class));
		assertFalse (areTypesAssignable (String.class, String[].class));
		assertFalse (areTypesAssignable (String[].class, String.class));
		assertTrue (areTypesAssignable (String[].class, Object.class));
		assertFalse (areTypesAssignable (Object.class, String[].class));
		assertFalse (areTypesAssignable (Object[].class, String[].class));
		assertTrue (areTypesAssignable (String[].class, Object[].class));
		assertFalse (areTypesAssignable (String[][].class, String[].class));
		assertFalse (areTypesAssignable (String[].class, String[][].class));
	}
	
	@Test
	public void testAssignableGenericParameters () throws Exception {
		
		// Generic types without parameters are never assignable because this may cause class-cast exceptions
		// at runtime:
		assertFalse (areTypesAssignable (List.class, List.class));
		assertFalse (areTypesAssignable (ArrayList.class, List.class));
		assertFalse (areTypesAssignable (StringList.class, ArrayList.class));
		assertFalse (areTypesAssignable (IntegerList.class, ArrayList.class));
		assertFalse (areTypesAssignable (IntegerList.class, StringList.class));
		assertFalse (areTypesAssignable (List[].class, List[].class));
		assertFalse (areTypesAssignable (List[][].class, List[][].class));
		
		// Generic types can be assigned to compatibel superclasses that are not generic:
		assertTrue (areTypesAssignable (List.class, Object.class));
		assertTrue (areTypesAssignable (ParameterizedStringList.class, StringList.class));
		
		final Method method = getClass ().getMethod (
				"testGenericTypes", 
				List.class, // List<String> 
				List.class, // List<String>
				ArrayList.class, // ArrayList<String>
				ArrayList.class, // ArrayList<String>
				List.class, // List<Integer>
				ArrayList.class, // ArrayList<Integer>
				List[].class, // List<String>[]
				List[].class, // List<String>[]
				List[].class, // List<Integer>[]
				ArrayList[].class, // ArrayList<String>[]
				String.class); 
		final Type[] types = method.getGenericParameterTypes ();
		
		assertTrue (types[0] instanceof ParameterizedType);
		assertTrue (types[10] instanceof Class<?>);
		
		assertTrue (areTypesAssignable (types[0], types[1])); // List<String> -> List<String>
		assertTrue (areTypesAssignable (types[2], types[3])); // ArrayList<String> -> ArrayList<String>
		assertTrue (areTypesAssignable (types[2], types[0])); // ArrayList<String> -> List<String>
		assertFalse (areTypesAssignable (types[0], types[2])); // List<String> -> ArrayList<String>
		assertFalse (areTypesAssignable (types[0], types[4])); // List<String> -> List<Integer>
		assertFalse (areTypesAssignable (StringList.class, types[4])); // StringList -> List<Integer>
		assertFalse (areTypesAssignable (types[4], types[0])); // List<Integer> -> List<String>
		assertFalse (areTypesAssignable (types[5], types[0])); // ArrayList<Integer> -> List<String>
		
		assertTrue (areTypesAssignable (types[6], types[7])); // List<String>[] -> List<String>[]
		assertTrue (areTypesAssignable (types[9], types[6])); // ArrayList<String>[] -> List<String>[]
		assertFalse (areTypesAssignable (types[8], types[6])); // List<Integer>[] -> List<String>[]
		assertFalse (areTypesAssignable (StringList[].class, types[8])); // StringList[] -> List<Integer>[]
	}
	
	@Test
	public void testAssignablePrimitivesToObject () throws Exception {
		assertTrue (areTypesAssignable (Byte.TYPE, Object.class));
		assertTrue (areTypesAssignable (Character.TYPE, Object.class));
		assertTrue (areTypesAssignable (Short.TYPE, Object.class));
		assertTrue (areTypesAssignable (Integer.TYPE, Object.class));
		assertTrue (areTypesAssignable (Long.TYPE, Object.class));
		assertTrue (areTypesAssignable (Float.TYPE, Object.class));
		assertTrue (areTypesAssignable (Double.TYPE, Object.class));
		assertTrue (areTypesAssignable (Boolean.TYPE, Object.class));
	}
	
	@Test
	public void testAssignableInterfaceToObject () throws Exception {
		assertTrue (Object.class.isAssignableFrom (TestInterface.class));
		
		assertTrue (areTypesAssignable (TestInterface.class, Object.class));
	}
	
	@Test
	public void testAssignableNullToObject () throws Exception {
		assertTrue (areTypesAssignable (NullReference.class, Animal.class));
	}
	
	public void testBoxing (Integer i) {
		assertNotNull (i);
	}
	
	public void testUnboxing (int i) {
		assertNotNull (i);
	}
	
	public void testUpcasting (long l) {
		assertEquals (1, l);
	}
	
	public void testUpcastingBoxed (Long l) {
		assertEquals (1, l.longValue ());
	}
	
	public void testGenericTypes (
			final List<String> a, 
			final List<String> b, 
			final ArrayList<String> c, 
			final ArrayList<String> d, 
			final List<Integer> e, 
			final ArrayList<Integer> f,
			final List<String>[] g,
			final List<String>[] h,
			final List<Integer>[] i,
			final ArrayList<String>[] j,
			final String k) {
	}
	
	public static class TestInterface {
	}
	
	public static class Animal {
	}
	
	public static class Horse extends Animal {
	}
	
	public static class StringList extends ArrayList<String> {
		private static final long serialVersionUID = 4715459014586031508L;
	}

	public static class ParameterizedStringList<T> extends StringList {
		private static final long serialVersionUID = 1734710538263249942L;
	}
	
	public static class IntegerList extends ArrayList<Integer> {
		private static final long serialVersionUID = -7203056260491531646L;
	}
}
