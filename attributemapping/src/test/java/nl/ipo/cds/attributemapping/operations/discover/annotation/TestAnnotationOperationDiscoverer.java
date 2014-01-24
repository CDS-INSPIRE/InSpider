package nl.ipo.cds.attributemapping.operations.discover.annotation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import nl.ipo.cds.attributemapping.operations.OperationInputType;
import nl.ipo.cds.attributemapping.operations.OperationType;
import nl.ipo.cds.attributemapping.operations.annotation.Execute;
import nl.ipo.cds.attributemapping.operations.annotation.Input;
import nl.ipo.cds.attributemapping.operations.discover.OperationDiscovererException;
import nl.ipo.cds.attributemapping.operations.discover.annotation.AnnotationInputOperationType;
import nl.ipo.cds.attributemapping.operations.discover.annotation.AnnotationOperationDiscoverer;
import nl.ipo.cds.attributemapping.operations.discover.annotation.AnnotationOperationType;
import nl.ipo.cds.attributemapping.operations.discover.annotation.AnnotationOutputOperationType;
import nl.ipo.cds.attributemapping.operations.discover.annotation.AnnotationTransformOperationType;
import nl.ipo.cds.attributemapping.operations.discover.annotation.TestAnnotationOperationDiscoverer.TestAnnotationOperationDiscovererConfig;
import nl.ipo.cds.attributemapping.operations.discover.annotation.operations.Package;
import nl.ipo.cds.attributemapping.operations.discover.annotation.operations.TestOperation;
import nl.ipo.cds.attributemapping.operations.discover.annotation.operations.TestOperationBeforeAfter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestAnnotationOperationDiscovererConfig.class)
public class TestAnnotationOperationDiscoverer {

	@Inject
	private AnnotationOperationDiscoverer operationDiscoverer;

	@Inject
	private ApplicationContext applicationContext;
	
	@Configuration
	@ComponentScan(basePackageClasses = Package.class)
	public static class TestAnnotationOperationDiscovererConfig {
		@Bean
		public AnnotationOperationDiscoverer annotationOperationDiscoverer () {
			return new AnnotationOperationDiscoverer ();
		}
	}
	
	@Test
	public void testDiscover () {
		final Collection<OperationType> types = operationDiscoverer.getOperationTypes ();
		
		assertNotNull (types);
		assertHasOperation (types, "testOperation", AnnotationTransformOperationType.class, String.class, TestOperation.Settings.class);
		assertHasOperation (types, "testOperationWithMultipleMethods", AnnotationTransformOperationType.class, String.class, null);
		assertHasOperation (types, "testInput", AnnotationInputOperationType.class, String.class, null);
		assertHasOperation (types, "testOutput", AnnotationOutputOperationType.class, Void.TYPE, null);
		assertHasOperation (types, "testOperationVarargs", AnnotationTransformOperationType.class, String.class, null);
		assertHasOperation (types, "testOperationBeforeAfter", AnnotationTransformOperationType.class, String.class, TestOperationBeforeAfter.Settings.class);
		
		assertHasInputs (types, "testOperation",
				new In ("a", String.class, false),
				new In ("b", String.class, false)
			);
		assertHasInputs (types, "testOperationWithMultipleMethods",
				new In ("a", String.class, false),
				new In ("b", String.class, false)
			);
		assertHasInputs (types, "testInput");
		assertHasInputs (types, "testOutput",
				new In ("in", String.class, false)
			);
		assertHasInputs (types, "testOperationVarargs",
				new In ("a", Integer.TYPE, false),
				new In ("b", String.class, true)
			);
	}
	
	static void assertHasOperation (final Collection<OperationType> operations, final String name, final Class<?> iface, final Class<?> returnType, final Class<?> beanClass) {
		for (final OperationType ot: operations) {
			if (
					name.equals (ot.getName ()) 
					&& iface.isAssignableFrom (ot.getClass ()) 
					&& returnType.equals (ot.getReturnType ())
					&& ((beanClass == null && ot.getPropertyBeanClass () == null) || (beanClass != null && beanClass.equals (ot.getPropertyBeanClass())))) {
				return;
			}
		}
		
		fail (String.format ("Missing operation: %s", name));
	}
	
	static void assertHasInputs (final Collection<OperationType> operations, final String name, final In ... inputs) {
		OperationType operationType = null;
		for (final OperationType ot: operations) {
			if (name.equals (ot.getName ())) {
				operationType = ot;
				break;
			}
		}
		if (operationType == null) {
			fail ("Operation type not found");
		}
		
		final List<OperationInputType> operationInputs = operationType.getInputs ();
		if (operationInputs.size () != inputs.length) {
			fail (String.format ("Operation %s, expected %d inputs, found %d", name, operationInputs.size (), inputs.length));
		}
		
		for (int i = 0; i < inputs.length; ++ i) {
			final OperationInputType it = operationInputs.get (i);
			
			if (!it.getName ().equals (inputs[i].name)) {
				fail (String.format ("Operation %s, expected input %s, found %s at %d", name, inputs[i].name, it.getName (), i));
			}
			if (!it.getInputType ().equals (inputs[i].type)) {
				fail (String.format ("Input %s.%s, expected type %s, found %s", name, inputs[i].name, inputs[i].type, it.getInputType ()));
			}
			if (it.isVariableInputCount () != inputs[i].variableInputs) {
				fail (String.format ("Input %s.%s has wrong varinputs setting", name, inputs[i].name));
			}
		}
	}
	
	@Test(expected = OperationDiscovererException.class)
	public void testDiscoverNoExecuteMethod () {
		AnnotationOperationType.getOperationMethod (new Object () {
		});
	}
	
	@Test(expected = OperationDiscovererException.class)
	public void testDiscoverMultipleExecuteMethods () {
		AnnotationOperationType.getOperationMethod (new Object () {
			@SuppressWarnings("unused")
			public int a () {
				return 0;
			}
			
			@SuppressWarnings("unused")
			public void b (int a) {
			}
		});
	}

	@Test(expected = OperationDiscovererException.class)
	public void testDiscoverInvalidExecuteMethod () {
		AnnotationOperationType.getOperationMethod (new Object () {
			@SuppressWarnings("unused")
			public void a () {
			}
		});
	}
	
	@Test(expected = OperationDiscovererException.class)
	public void testDiscoverMultiplAnnotatedMethods () {
		AnnotationOperationType.getOperationMethod (new Object () {
			@Execute
			public void a (int a) {
			}
			
			@Execute
			public void b (int b) {
			}
		});
	}
	
	@Test(expected = OperationDiscovererException.class)
	public void testDiscoverNoInputAnnotation () {
		AnnotationOperationType.getOperationMethod (new Object () {
			@Execute
			public void a (int a) {
			}
		});
	}
	
	@Test
	public void testDiscoverAnnotatedInput () {
		AnnotationOperationType.getOperationMethod (new Object () {
			@Execute
			public void a (@Input int a) {
			}
		});
	}
	
	@Test(expected = OperationDiscovererException.class)
	public void testDiscoverDuplicateInput () {
		final Object obj = new Object () {
			@Execute
			public int a (@Input("a") int a, @Input("a") int b) {
				return 0;
			}
		};
		
		new AnnotationTransformOperationType (obj, "obj", applicationContext);
	}
	
	private static class In {
		public final String name;
		public final Class<?> type;
		public final boolean variableInputs;
		
		public In (final String name, final Class<?> type, final boolean variableInputs) {
			this.name = name;
			this.type = type;
			this.variableInputs = variableInputs;
		}
	}
}
