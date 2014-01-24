package nl.ipo.cds.etl.filtering;

import static org.junit.Assert.*;
import static nl.ipo.cds.etl.filtering.FilterExpressionFactory.*;

import java.util.HashSet;
import java.util.Set;

import nl.ipo.cds.domain.AttributeType;
import nl.ipo.cds.domain.FeatureType;
import nl.ipo.cds.domain.FeatureTypeAttribute;
import nl.ipo.cds.domain.QName;

import org.deegree.filter.Filter;
import org.junit.Before;
import org.junit.Test;

public class FilterFactoryTest {

	private FeatureType featureType;
	private FilterFactory factory;
	
	@Before
	public void createFilterFactory () {
		featureType = createFeatureType ();
		factory = new FilterFactory (featureType);
	}
	
	@Test
	public void testEqual () {
		final Filter filter = factory.createFilter (equal (attribute ("a", AttributeType.STRING), stringValue ("test")));
		
		assertNotNull (filter);
		System.out.println (filter);
	}
	
	@Test
	public void testAnd () {
		final Filter filter = factory.createFilter (
				and (
					equal (attribute ("a", AttributeType.STRING), stringValue ("test")),
					notEqual (attribute ("a", AttributeType.STRING), stringValue ("test2"))
				)
			);
		
		assertNotNull (filter);
		System.out.println (filter);
	}
	
	@Test
	public void testAndOr () {
		final Filter filter = factory.createFilter (
				or (
					and (
						equal (attribute ("a", AttributeType.STRING), stringValue ("test")),
						notEqual (attribute ("a", AttributeType.STRING), stringValue ("test2"))
					),
					and (
						equal (attribute ("a", AttributeType.STRING), stringValue ("test3")),
						notEqual (attribute ("a", AttributeType.STRING), stringValue ("test4"))
					)
				)
			);
		
		assertNotNull (filter);
		System.out.println (filter);
	}
	
	@Test
	public void testLike () {
		final Filter filter = factory.createFilter (
				like (attribute ("a", AttributeType.STRING), "*test*")
			);
			
		assertNotNull (filter);
		System.out.println (filter);
	}
	
	@Test
	public void testIn () {
		final Filter filter = factory.createFilter (
				in (attribute ("a", AttributeType.STRING), "test,test1,   test2,  test3   ,    test4   ")
			);
		
		assertNotNull (filter);
		System.out.println (filter);
	}
	
	@Test
	public void testNotNull () {
		final Filter filter = factory.createFilter (
				notNull (attribute ("a", AttributeType.STRING))
			);
		
		assertNotNull (filter);
		System.out.println (filter);
	}
	
	protected static FeatureType createFeatureType () {
		return new FeatureType() {
			
			@Override
			public QName getName () {
				return new QName () {
					@Override
					public int compareTo (final QName o) {
						return getLocalPart ().compareTo (o.getLocalPart ()); 
					}
					
					@Override
					public String getNamespace () {
						return "http://www.idgis.nl/test";
					}
					
					@Override
					public String getLocalPart () {
						return "TestFeatureType";
					}
				};
			}
			
			@Override
			public Set<FeatureTypeAttribute> getAttributes () {
				return new HashSet<FeatureTypeAttribute>() {
					private static final long serialVersionUID = 1L;
					{
						add (new FeatureTypeAttribute () {
							
							@Override
							public int compareTo (final FeatureTypeAttribute o) {
								return getName ().compareTo (o.getName ());
							}
							
							@Override
							public AttributeType getType () {
								return AttributeType.STRING;
							}
							
							@Override
							public QName getName () {
								return new QName () {
									
									@Override
									public int compareTo (final QName o) {
										return getLocalPart ().compareTo (o.getLocalPart ());
									}
									
									@Override
									public String getNamespace () {
										return "http://www.idgis.nl/test";
									}
									
									@Override
									public String getLocalPart () {
										return "a";
									}
								};
							}
						});
					}
					
				};
			}
		};
	}
}
