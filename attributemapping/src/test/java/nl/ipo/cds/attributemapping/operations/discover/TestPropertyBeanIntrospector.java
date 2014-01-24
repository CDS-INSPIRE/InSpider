package nl.ipo.cds.attributemapping.operations.discover;

import static org.junit.Assert.*;

import java.lang.reflect.Type;

import nl.ipo.cds.attributemapping.operations.PropertyBeanDescription;
import nl.ipo.cds.attributemapping.operations.PropertyBeanFieldDescription;

import org.junit.Before;
import org.junit.Test;

public class TestPropertyBeanIntrospector {

	private PropertyBeanIntrospector introspector;
	
	@Before
	public void before () {
		introspector = new PropertyBeanIntrospector ();
	}
	
	@Test
	public void testIntrospect () {
		final PropertyBeanDescription d = introspector.getDescriptorForBeanClass (BeanClass.class);
		
		assertEquals (BeanClass.class, d.getBeanClass ());
		assertEquals (2, d.getFieldDescriptions ().size ());
		
		assertHasField (d, "value", String.class);
		assertHasField (d, "value2", Boolean.TYPE);
	}
	
	private static void assertHasField (final PropertyBeanDescription bd, final String name, final Type type) {
		for (final PropertyBeanFieldDescription fd: bd.getFieldDescriptions ()) {
			if (name.equals (fd.getName ()) && type.equals (fd.getType ())) {
				return;
			}
		}
		
		fail (String.format ("Field %s not found (type %s)", name, type));
	}
	
	public static class BeanClass {
		public String getValue () {
			return null;
		}
		
		public boolean getValue2 () {
			return false;
		}
		
		public void setValue (final String value) {
		}
		
		public void setValue2 (final boolean value) {
		}
		
		public String getReadOnlyProperty () {
			return null;
		}
	}

}
