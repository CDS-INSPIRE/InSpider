package nl.ipo.cds.attributemapping.operations.discover.annotation;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Locale;

import nl.ipo.cds.attributemapping.MapperContext;
import nl.ipo.cds.attributemapping.MappingDestination;
import nl.ipo.cds.attributemapping.MappingSource;
import nl.ipo.cds.attributemapping.executer.OperationExecuter;
import nl.ipo.cds.attributemapping.executer.OperationExecutionException;
import nl.ipo.cds.attributemapping.operations.discover.annotation.operations.TestInput;
import nl.ipo.cds.attributemapping.operations.discover.annotation.operations.TestOperation;
import nl.ipo.cds.attributemapping.operations.discover.annotation.operations.TestOperationVarargs;
import nl.ipo.cds.attributemapping.operations.discover.annotation.operations.TestOutput;

import org.junit.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

public class TestAnnotationOperationType {

	@Test
	public void testCreateTransformation () {
		final TestOperation bean = new TestOperation ();
		new AnnotationTransformOperationType (bean, "testOperation", messageSource);
	}
	
	@Test
	public void testCreateInput () {
		final TestInput bean = new TestInput ();
		new AnnotationTransformOperationType (bean, "testInput", messageSource);
	}
	
	@Test
	public void testCreateOutput () {
		final TestOutput bean = new TestOutput ();
		new AnnotationTransformOperationType (bean, "testOutput", messageSource);
	}
	
	@Test
	public void testExecuteTransform () throws Exception {
		final TestOperation bean = new TestOperation ();
		final AnnotationTransformOperationType ot = new AnnotationTransformOperationType (bean, "testOperation", messageSource);
		final OperationExecuter executer = ot.createExecuter (null, new MapperContext ());
		
		final Object result = executer.execute (
				mappingSource, 
				mappingDestination, 
				Arrays.asList (new Object[] { "a", "b" })
			);
		
		assertEquals ("a:b", result);
	}
	
	@Test
	public void testExecuteTransformVarargs () throws Exception {
		final TestOperationVarargs bean = new TestOperationVarargs ();
		final AnnotationTransformOperationType ot = new AnnotationTransformOperationType (bean, "testOperationVarargs", messageSource);
		final Object result = ot
				.createExecuter (null, new MapperContext ())
				.execute (mappingSource, mappingDestination, Arrays.asList (new Object[] { 42, "a", "b", "c" }));
		
		assertEquals ("42abc", result);
	}
	
	@Test (expected = OperationExecutionException.class)
	public void testExecuteInvalidType () throws Exception {
		final TestOperation bean = new TestOperation ();
		final AnnotationTransformOperationType ot = new AnnotationTransformOperationType (bean, "testOperation", messageSource);
		final OperationExecuter executer = ot.createExecuter (null, new MapperContext ());
		
		executer.execute (
				mappingSource, 
				mappingDestination, 
				Arrays.asList (new Object[] { 1, false })
			);
		
	}

	private final static MessageSource messageSource = new MessageSource() {
		@Override
		public String getMessage(String code, Object[] args, String defaultMessage,
				Locale locale) {
			return "a";
		}
		
		@Override
		public String getMessage(String code, Object[] args, Locale locale)
				throws NoSuchMessageException {
			return "a";
		}
		
		@Override
		public String getMessage(MessageSourceResolvable resolvable, Locale locale)
				throws NoSuchMessageException {
			return "a";
		}
	};
	
	private final static MappingSource mappingSource = new MappingSource() {
		
		@Override
		public boolean hasAttribute(String name) {
			return false;
		}
		
		@Override
		public Object getAttributeValue(String name) {
			return null;
		}
	};
	
	private final static MappingDestination mappingDestination = new MappingDestination() {
		
		@Override
		public void setValue(Object value) {
		}
	};
}
