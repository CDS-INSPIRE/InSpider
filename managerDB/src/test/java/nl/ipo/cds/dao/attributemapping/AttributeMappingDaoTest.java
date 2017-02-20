package nl.ipo.cds.dao.attributemapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import nl.ipo.cds.attributemapping.MapperContext;
import nl.ipo.cds.attributemapping.executer.OperationExecuter;
import nl.ipo.cds.attributemapping.operations.OperationInputType;
import nl.ipo.cds.attributemapping.operations.OperationType;
import nl.ipo.cds.attributemapping.operations.OutputOperationType;
import nl.ipo.cds.categories.IntegrationTests;
import nl.ipo.cds.dao.BaseManagerDaoTest;
import nl.ipo.cds.domain.AttributeMapping;
import nl.ipo.cds.domain.AttributeType;
import nl.ipo.cds.domain.Dataset;
import nl.ipo.cds.domain.FeatureTypeAttribute;
import nl.ipo.cds.domain.MappingOperation.MappingOperationType;
import nl.ipo.cds.domain.QName;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(defaultRollback = true, transactionManager = "transactionManager")
@Category(IntegrationTests.class)
public class AttributeMappingDaoTest extends BaseManagerDaoTest {

	private AttributeMappingDao dao;
	
    @PersistenceContext
    private EntityManager entityManager;
    
    private Set<FeatureTypeAttribute> inputAttributes;
    
    private Set<OperationType> operationTypes; 
    
	@Before
	public void createAttributeMappingDao () throws Exception {
		buildDB ();
		
		inputAttributes = new HashSet<FeatureTypeAttribute> ();
		
		inputAttributes.add (createFeatureTypeAttribute ("a", AttributeType.STRING));
		inputAttributes.add (createFeatureTypeAttribute ("b", AttributeType.BOOLEAN));
		
		operationTypes = new HashSet<OperationType> ();

		operationTypes.add (createOperationType ());
		
		dao = new AttributeMappingDao (managerDao, inputAttributes, operationTypes);
		
		assertNotNull (dao);
	}
	
	@Test
	public void testPutAttributeMapping () throws Exception {
		final Dataset dataset = managerDao.getAllDatasets ().get (0);
		final OperationDTO operation = createTransformOperation (null);
		
		// Write the attribute mapping:
		dao.putAttributeMapping (dataset, createOutputOperationType ("testAttribute"), operation, false);
		
		// Read the resulting mapping:
		final AttributeMapping mapping = managerDao.getAttributeMapping (dataset, "testAttribute");
		
		assertNotNull (mapping);
		assertEquals ("testAttribute", mapping.getAttributeName ());
		assertNotNull (mapping.getRootOperation ());
		assertNull (mapping.getRootOperation ().getInputAttributeType());
		assertEquals (0, mapping.getRootOperation ().getInputs ().size ());
		assertEquals ("testTransformOperation", mapping.getRootOperation ().getOperationName ());
		assertNull (mapping.getRootOperation ().getParent ());
		assertNull (mapping.getRootOperation ().getProperties ());
	}

	@Test
	public void testPutAttributeMappingProperties () throws Exception {
		final Dataset dataset = managerDao.getAllDatasets ().get (0);
		final OperationDTO operation = createTransformOperation (new PropertyClass ("a", 42, true));
		
		dao.putAttributeMapping (dataset, createOutputOperationType ("testAttribute"), operation, false);
		
		final AttributeMapping mapping = managerDao.getAttributeMapping (dataset, "testAttribute");
		
		assertNotNull (mapping.getRootOperation ().getProperties ());
		assertTrue (mapping.getRootOperation ().getProperties ().length () > 0);
		assertTrue (mapping.getRootOperation ().getProperties ().startsWith ("{"));
		assertTrue (mapping.getRootOperation ().getProperties ().endsWith ("}"));
	}
	
	@Test
	public void testPutAttributeMappingInputs () throws Exception {
		final Dataset dataset = managerDao.getAllDatasets ().get (0);
		final OperationDTO operation = createTransformOperation (null, createTransformOperation (null), createTransformOperation (null));

		dao.putAttributeMapping (dataset, createOutputOperationType ("testAttribute"), operation, false);
		final AttributeMapping mapping = managerDao.getAttributeMapping (dataset, "testAttribute");
		
		assertEquals (2, mapping.getRootOperation ().getInputs ().size ());
		assertEquals ("testTransformOperation", mapping.getRootOperation ().getInputs ().get (0).getOperationName ());
		assertEquals ("testTransformOperation", mapping.getRootOperation ().getInputs ().get (1).getOperationName ());
		assertSame (mapping.getRootOperation (), mapping.getRootOperation ().getInputs ().get (0).getParent ());
		assertSame (mapping.getRootOperation (), mapping.getRootOperation ().getInputs ().get (1).getParent ());
	}
	
	@Test
	public void testPutAttributeMappingInputAttributes () throws Exception {
		final Dataset dataset = managerDao.getAllDatasets ().get (0);
		final OperationDTO operation = createTransformOperation (null, createInputOperation ("a", AttributeType.STRING), createInputOperation ("b", AttributeType.BOOLEAN));
		
		dao.putAttributeMapping (dataset, createOutputOperationType ("testAttribute"), operation, false);
		final AttributeMapping mapping = managerDao.getAttributeMapping (dataset, "testAttribute");
		
		assertEquals (2, mapping.getRootOperation ().getInputs ().size ());
		
		assertEquals (MappingOperationType.INPUT_OPERATION, mapping.getRootOperation ().getInputs ().get (0).getOperationType ());
		assertEquals ("a", mapping.getRootOperation ().getInputs ().get (0).getOperationName ());
		assertEquals (0, mapping.getRootOperation ().getInputs ().get (0).getInputs().size ());
		assertEquals (AttributeType.STRING, mapping.getRootOperation ().getInputs ().get (0).getInputAttributeType ());
		assertNull (mapping.getRootOperation ().getInputs ().get (0).getProperties ());
		
		assertEquals (MappingOperationType.INPUT_OPERATION, mapping.getRootOperation ().getInputs ().get (1).getOperationType ());
		assertEquals ("b", mapping.getRootOperation ().getInputs ().get (1).getOperationName ());
		assertEquals (0, mapping.getRootOperation ().getInputs ().get (1).getInputs().size ());
		assertEquals (AttributeType.BOOLEAN, mapping.getRootOperation ().getInputs ().get (1).getInputAttributeType ());
		assertNull (mapping.getRootOperation ().getInputs ().get (1).getProperties ());
	}
	
	@Test
	public void testPutAttributeMappingNullInputs () throws Exception {
		final Dataset dataset = managerDao.getAllDatasets ().get (0);
		final OperationDTO operation = createTransformOperation (null, null, createInputOperation ("a", AttributeType.STRING), null, createInputOperation ("b", AttributeType.BOOLEAN), null);
		
		dao.putAttributeMapping (dataset, createOutputOperationType ("testAttribute"), operation, false);
		final AttributeMapping mapping = managerDao.getAttributeMapping (dataset, "testAttribute");

		assertEquals (4, mapping.getRootOperation ().getInputs ().size ());
		
		assertNull (mapping.getRootOperation ().getInputs ().get (0));
		assertNotNull (mapping.getRootOperation ().getInputs ().get (1));
		assertNull (mapping.getRootOperation ().getInputs ().get (2));
		assertNotNull (mapping.getRootOperation ().getInputs ().get (3));
	}
	
	@Test
	public void testUpdateAttributeMapping () throws Exception {
		final Dataset dataset = managerDao.getAllDatasets ().get (0);
		final OperationDTO operation = createTransformOperation (null);
		final OutputOperationType operationType = createOutputOperationType ("testAttribute");
		
		// Write the attribute mapping:
		dao.putAttributeMapping (dataset, operationType, operation, false);
		
		final OperationDTO operation2 = createTransformOperation (null);
		
		dao.putAttributeMapping (dataset, operationType, operation2, false);
		
		// Read the resulting mapping:
		final AttributeMapping mapping = managerDao.getAttributeMapping (dataset, "testAttribute");
		
		assertNotNull (mapping);
		assertEquals ("testAttribute", mapping.getAttributeName ());
		assertNotNull (mapping.getRootOperation ());
		assertNull (mapping.getRootOperation ().getInputAttributeType());
		assertEquals (0, mapping.getRootOperation ().getInputs ().size ());
		assertEquals ("testTransformOperation", mapping.getRootOperation ().getOperationName ());
		assertNull (mapping.getRootOperation ().getParent ());
		assertNull (mapping.getRootOperation ().getProperties ());
	}
	
	@Test
	public void testUpdateAttributeMappingInputType () throws Exception {
		final Dataset dataset = managerDao.getAllDatasets ().get (0);
		final OperationDTO operation = createTransformOperation (null, createTransformOperation (null), createTransformOperation (null));
		final OutputOperationType operationType = createOutputOperationType ("testAttribute");

		dao.putAttributeMapping (dataset, operationType, operation, false);
		
		final OperationDTO operation2 = createTransformOperation (null, createInputOperation ("a", AttributeType.STRING), createTransformOperation (null));
		
		dao.putAttributeMapping (dataset, operationType, operation2, false);
		
		final AttributeMapping mapping = managerDao.getAttributeMapping (dataset, "testAttribute");
		
		assertEquals (2, mapping.getRootOperation ().getInputs ().size ());
		
		assertEquals (MappingOperationType.INPUT_OPERATION, mapping.getRootOperation ().getInputs ().get (0).getOperationType ());
		assertEquals ("a", mapping.getRootOperation ().getInputs ().get (0).getOperationName ());
		assertEquals (0, mapping.getRootOperation ().getInputs ().get (0).getInputs().size ());
		assertEquals (AttributeType.STRING, mapping.getRootOperation ().getInputs ().get (0).getInputAttributeType ());
		assertNull (mapping.getRootOperation ().getInputs ().get (0).getProperties ());
		
		assertEquals ("testTransformOperation", mapping.getRootOperation ().getInputs ().get (1).getOperationName ());
		assertSame (mapping.getRootOperation (), mapping.getRootOperation ().getInputs ().get (1).getParent ());
	}
	
	@Test
	public void testUpdateAttributeMappingInputCount () throws Exception {
		final Dataset dataset = managerDao.getAllDatasets ().get (0);
		final OutputOperationType operationType = createOutputOperationType ("testAttribute");

		dao.putAttributeMapping (dataset, operationType, createTransformOperation (null, createTransformOperation (null), createTransformOperation (null)), false);
		dao.putAttributeMapping (dataset, operationType, createTransformOperation (null, createTransformOperation (null), createTransformOperation (null), createTransformOperation (null)), false);
		dao.putAttributeMapping (dataset, operationType, createTransformOperation (null, createTransformOperation (null)), false);
		dao.putAttributeMapping (dataset, operationType, createTransformOperation (null), false);
		dao.putAttributeMapping (dataset, operationType, createTransformOperation (null, createTransformOperation (null)), false);
		
		final AttributeMapping mapping = managerDao.getAttributeMapping (dataset, "testAttribute");
		
		assertEquals (1, mapping.getRootOperation ().getInputs ().size ());
		assertEquals ("testTransformOperation", mapping.getRootOperation ().getInputs ().get (0).getOperationName ());
		assertSame (mapping.getRootOperation (), mapping.getRootOperation ().getInputs ().get (0).getParent ());
	}
	
	@Test
	public void testUpdateAttributeMappingProperties () throws Exception {
		final Dataset dataset = managerDao.getAllDatasets ().get (0);
		final OutputOperationType operationType = createOutputOperationType ("testAttribute");
		
		dao.putAttributeMapping (dataset, operationType, createTransformOperation (new PropertyClass ("a", 42, true)), false);
		dao.putAttributeMapping (dataset, operationType, createTransformOperation (new PropertyClass ("b", 43, false)), false);
		
		final AttributeMapping mapping = managerDao.getAttributeMapping (dataset, "testAttribute");
		
		assertNotNull (mapping.getRootOperation ().getProperties ());
		assertTrue (mapping.getRootOperation ().getProperties ().length () > 0);
		assertTrue (mapping.getRootOperation ().getProperties ().startsWith ("{"));
		assertTrue (mapping.getRootOperation ().getProperties ().endsWith ("}"));

		final OperationDTO op = dao.getAttributeMapping (dataset, operationType);
		
		assertTrue (op instanceof TransformOperationDTO);
		assertTrue (((TransformOperationDTO)op).getOperationProperties () instanceof PropertyClass);
		assertEquals (new PropertyClass ("b", 43, false), ((PropertyClass)((TransformOperationDTO)op).getOperationProperties ()));
	}
	
	@Test
	public void testUpdateAttributeMappingNullInputs () throws Exception {
		// Turn previously existing inputs into null values:
		final Dataset dataset = managerDao.getAllDatasets ().get (0);
		final OutputOperationType operationType = createOutputOperationType ("testAttribute");

		dao.putAttributeMapping (dataset, operationType, createTransformOperation (null, createTransformOperation (null), createTransformOperation (null), createTransformOperation (null), createTransformOperation (null)), false);
		dao.putAttributeMapping (dataset, operationType, createTransformOperation (null, null, null, null, createTransformOperation (null)), false);
		
		final AttributeMapping mapping = managerDao.getAttributeMapping (dataset, "testAttribute");

		assertEquals (4, mapping.getRootOperation ().getInputs ().size ());
		assertNull (mapping.getRootOperation ().getInputs ().get (0));
		assertNull (mapping.getRootOperation ().getInputs ().get (1));
		assertNull (mapping.getRootOperation ().getInputs ().get (2));
		assertNotNull (mapping.getRootOperation ().getInputs ().get (3));
	}
	
	/**
	 * Tests a 'cascading' delete on an attribute mapping: a complex tree of mapping operations is
	 * removed in a single call. Mapping operations should be removed in the correct order.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUpdateAttributeMappingCascade () throws Exception {
		final Dataset dataset = managerDao.getAllDatasets ().get (0);
		final OutputOperationType operationType = createOutputOperationType ("testAttribute");
		
		dao.putAttributeMapping (dataset, operationType, createTransformOperation (null, createTransformOperation (null, createTransformOperation (null, createTransformOperation (null)), createTransformOperation (null, createTransformOperation (null)))), false);
		entityManager.flush ();
		dao.putAttributeMapping (dataset, operationType, createTransformOperation (null), false);
		entityManager.flush ();
	}
	
	@Test
	public void testGetAttributeMapping () throws Exception {
		final Dataset dataset = managerDao.getAllDatasets ().get (0);
		final OperationDTO operation = createTransformOperation (null);
		final OutputOperationType operationType = createOutputOperationType ("testAttribute"); 
		
		dao.putAttributeMapping (dataset, operationType, operation, false);
		final OperationDTO op = dao.getAttributeMapping (dataset, operationType);
		
		assertNotNull (op);
		assertEquals (operation.getOperationType (), op.getOperationType ());
		assertEquals (operation.getInputs ().size (), op.getInputs ().size ());
		assertEquals (operation.getOperationType (), op.getOperationType ());
		assertEquals (operation.getOperationProperties (), op.getOperationProperties ());
	}
	
	@Test
	public void testGetAttributeMappingProperties () throws Exception {
		final Dataset dataset = managerDao.getAllDatasets ().get (0);
		final OperationDTO operation = createTransformOperation (new PropertyClass ("a", 42, true));
		final OutputOperationType operationType = createOutputOperationType ("testAttribute"); 
		
		dao.putAttributeMapping (dataset, operationType, operation, false);
		final OperationDTO op = dao.getAttributeMapping (dataset, operationType);
		
		assertNotNull (op.getOperationProperties ());
		assertEquals (operation.getOperationProperties (), op.getOperationProperties ());
	}
	
	@Test
	public void testGetAttributeMappingInputs () throws Exception {
		final Dataset dataset = managerDao.getAllDatasets ().get (0);
		final OperationDTO operation = createTransformOperation (null, createTransformOperation (null), createTransformOperation (null));
		final OutputOperationType operationType = createOutputOperationType ("testAttribute"); 

		dao.putAttributeMapping (dataset, operationType, operation, false);
		final OperationDTO op = dao.getAttributeMapping (dataset, operationType);
		
		assertEquals (2, op.getInputs ().size ());
		assertEquals (operation.getInputs ().get (0).getOperation ().getOperationType (), op.getInputs ().get (0).getOperation ().getOperationType ());
		assertEquals (operation.getInputs ().get (0).getOperation ().getOperationProperties (), op.getInputs ().get (0).getOperation ().getOperationProperties ());
	}
	
	@Test
	public void testGetAttributeMappingInputAttributes () throws Exception {
		final Dataset dataset = managerDao.getAllDatasets ().get (0);
		final OperationDTO operation = createTransformOperation (null, createInputOperation ("a", AttributeType.STRING), createInputOperation ("b", AttributeType.BOOLEAN));
		final OutputOperationType operationType = createOutputOperationType ("testAttribute"); 
		
		dao.putAttributeMapping (dataset, operationType, operation, false);
		final OperationDTO op = dao.getAttributeMapping (dataset, operationType);
		
		assertEquals (2, op.getInputs ().size ());
		
		assertTrue (op.getInputs ().get (0).getOperation () instanceof InputOperationDTO);
		assertEquals ("a", ((InputOperationDTO)op.getInputs ().get (0).getOperation ()).getAttributeName ());
		assertEquals (AttributeType.STRING, ((InputOperationDTO)op.getInputs ().get (0).getOperation ()).getAttributeType ());
		assertNotNull (((InputOperationDTO)op.getInputs ().get (0).getOperation ()).getAttribute ());
		
		assertTrue (op.getInputs ().get (1).getOperation () instanceof InputOperationDTO);
		assertEquals ("b", ((InputOperationDTO)op.getInputs ().get (1).getOperation ()).getAttributeName ());
		assertEquals (AttributeType.BOOLEAN, ((InputOperationDTO)op.getInputs ().get (1).getOperation ()).getAttributeType ());
		assertNotNull (((InputOperationDTO)op.getInputs ().get (1).getOperation ()).getAttribute ());
	}
	
	@Test
	public void testGetAttributeMappingNullInputs () throws Exception {
		final Dataset dataset = managerDao.getAllDatasets ().get (0);
		final OperationDTO operation = createTransformOperation (null, null, createInputOperation ("a", AttributeType.STRING), null, createInputOperation ("b", AttributeType.BOOLEAN), null);
		final OutputOperationType operationType = createOutputOperationType ("testAttribute");
		
		dao.putAttributeMapping (dataset, operationType, operation, false);
		final OperationDTO op = dao.getAttributeMapping (dataset, operationType);

		assertEquals (4, op.getInputs ().size ());
		assertNull (op.getInputs ().get (0).getOperation ());
		assertNotNull (op.getInputs ().get (1).getOperation ());
		assertNull (op.getInputs ().get (2).getOperation ());
		assertNotNull (op.getInputs ().get (3).getOperation ());
	}
	
	public static class PropertyClass {
		private String a;
		private int b;
		private boolean c;

		public PropertyClass () {
		}
		
		public PropertyClass (final String a, final int b, final boolean c) {
			this.a = a;
			this.b = b;
			this.c = c;
		}

		public String getA() {
			return a;
		}

		public void setA(String a) {
			this.a = a;
		}

		public int getB() {
			return b;
		}

		public void setB(int b) {
			this.b = b;
		}

		public boolean isC() {
			return c;
		}

		public void setC(boolean c) {
			this.c = c;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((a == null) ? 0 : a.hashCode());
			result = prime * result + b;
			result = prime * result + (c ? 1231 : 1237);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PropertyClass other = (PropertyClass) obj;
			if (a == null) {
				if (other.a != null)
					return false;
			} else if (!a.equals(other.a))
				return false;
			if (b != other.b)
				return false;
			if (c != other.c)
				return false;
			return true;
		}
	}
	
	private static OutputOperationType createOutputOperationType (final String name) {
		return new OutputOperationType() {
			@Override
			public Type getReturnType() {
				return Object.class;
			}
			
			@Override
			public Class<?> getPropertyBeanClass() {
				return null;
			}
			
			@Override
			public String getName() {
				return name;
			}
			
			@Override
			public String getLabel(Locale locale) {
				return getName ();
			}
			
			@Override
			public String getFormatLabel (final Locale locale) {
				return getName ();
			}
			
			@Override
			public List<OperationInputType> getInputs() {
				return new ArrayList<OperationInputType> ();
			}
			
			@Override
			public String getDescription(Locale locale) {
				return getName ();
			}
			
			@Override
			public OperationExecuter createExecuter(Object operationProperties,
					MapperContext context) {
				return null;
			}
		};
	}
	
	private static InputOperationDTO createInputOperation (final String attributeName, final AttributeType attributeType) {
		final InputOperationDTO op = new InputOperationDTO (null, attributeName, attributeType);
		return op;
	}
	
	private static TransformOperationDTO createTransformOperation (final PropertyClass properties, final OperationDTO ... inputs) {
		final List<OperationInputDTO> opInputs = new ArrayList<OperationInputDTO> ();
		for (final OperationDTO input: inputs) {
			opInputs.add (new OperationInputDTO (input));
		}
		
		return new TransformOperationDTO (
				createOperationType (), 
				opInputs, 
				properties
			);
	}
	
	private static OperationType createOperationType () {
		return new OperationType() {
			
			@Override
			public boolean equals (final Object o) {
				if (o == null || !(o instanceof OperationType)) {
					return false;
				}
				return getName ().equals (((OperationType)o).getName ());
			}
			
			@Override
			public String toString () {
				return String.format ("OperationType(%s)", getName ());
			}
			
			@Override
			public Type getReturnType() {
				return String.class;
			}
			
			@Override
			public Class<?> getPropertyBeanClass () {
				return PropertyClass.class;
			}
			
			@Override
			public String getName () {
				return "testTransformOperation";
			}
			
			@Override
			public String getLabel (final Locale locale) {
				return getName ();
			}
			
			@Override
			public String getFormatLabel (final Locale locale) {
				return getName ();
			}
			
			@Override
			public List<OperationInputType> getInputs() {
				final List<OperationInputType> inputs = new ArrayList<OperationInputType> ();
				inputs.add (new OperationInputType () {
					@Override
					public String getName () {
						return "input";
					}

					@Override
					public Type getInputType () {
						return Object.class;
					}

					@Override
					public String getDescription (final Locale locale) {
						return getName ();
					}

					@Override
					public boolean isVariableInputCount () {
						return true;
					}
				});
				return inputs;
			}
			
			@Override
			public String getDescription (final Locale locale) {
				return getName ();
			}
			
			@Override
			public OperationExecuter createExecuter (final Object operationProperties,
					final MapperContext context) {
				return null;
			}
		};
	}
	
	private static FeatureTypeAttribute createFeatureTypeAttribute (final String name, final AttributeType type) {
		return new FeatureTypeAttribute () {
			@Override
			public boolean equals (final Object o) {
				if (o == null || !(o instanceof FeatureTypeAttribute)) {
					return false;
				}
				
				return getName ().equals (((FeatureTypeAttribute)o).getName ());
			}
			
			@Override
			public String toString () {
				return String.format ("FeatureTypeAttribute(%s: %s)", name, type);
			}
			
			@Override
			public int compareTo (FeatureTypeAttribute o) {
				return getName ().compareTo (o.getName ());
			}
			
			@Override
			public AttributeType getType() {
				return type;
			}
			
			@Override
			public QName getName() {
				return new QName () {
					@Override
					public int compareTo(QName o) {
						return getLocalPart ().compareTo (o.getLocalPart ());
					}

					@Override
					public String getNamespace() {
						return "http://www.idgis.nl";
					}

					@Override
					public String getLocalPart() {
						return name;
					}
				};
			}
		};
	}
}
