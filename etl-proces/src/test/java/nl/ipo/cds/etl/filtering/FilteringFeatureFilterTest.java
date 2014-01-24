package nl.ipo.cds.etl.filtering;

import static nl.ipo.cds.etl.filtering.FilterExpressionFactory.attribute;
import static nl.ipo.cds.etl.filtering.FilterExpressionFactory.equal;
import static nl.ipo.cds.etl.filtering.FilterExpressionFactory.stringValue;
import static nl.ipo.cds.etl.filtering.FilterExpressionFactory.notNull;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import nl.ipo.cds.domain.AttributeType;
import nl.ipo.cds.etl.CountingFeatureOutputStream;
import nl.ipo.cds.etl.Feature;
import nl.ipo.cds.etl.GenericFeature;

import org.deegree.filter.Filter;
import org.junit.Test;

public class FilteringFeatureFilterTest {

	@Test
	public void testPass () {
		final CountingFeatureOutputStream<GenericFeature> output = new CountingFeatureOutputStream<GenericFeature> ();
		final CountingFeatureOutputStream<Feature> errorOutput = new CountingFeatureOutputStream<Feature> ();
		final GenericFeature feature = createFeature ("Hello, world!");
		final DatasetFilterer filter = new DatasetFilterer (createFilter ());
		
		filter.processFeature (feature, output, errorOutput);
		
		assertEquals (1, output.getFeatureCount ());
		assertEquals (0, errorOutput.getFeatureCount ());
	}
	
	@Test
	public void testNotPass () {
		final CountingFeatureOutputStream<GenericFeature> output = new CountingFeatureOutputStream<GenericFeature> ();
		final CountingFeatureOutputStream<Feature> errorOutput = new CountingFeatureOutputStream<Feature> ();
		final GenericFeature feature = createFeature ("not Hello, world!");
		final DatasetFilterer filter = new DatasetFilterer (createFilter ());
		
		filter.processFeature (feature, output, errorOutput);
		
		assertEquals (0, output.getFeatureCount ());
		assertEquals (0, errorOutput.getFeatureCount ());
	}
	
	@Test
	public void testPassNotNull () {
		final CountingFeatureOutputStream<GenericFeature> output = new CountingFeatureOutputStream<GenericFeature> ();
		final CountingFeatureOutputStream<Feature> errorOutput = new CountingFeatureOutputStream<Feature> ();
		final GenericFeature feature = createFeature ("Hello, world!");
		final DatasetFilterer filter = new DatasetFilterer (createFilterNotNull ());
		
		filter.processFeature (feature, output, errorOutput);
		
		assertEquals (1, output.getFeatureCount ());
		assertEquals (0, errorOutput.getFeatureCount ());
	}
	
	@Test
	public void testNotPassNull () {
		final CountingFeatureOutputStream<GenericFeature> output = new CountingFeatureOutputStream<GenericFeature> ();
		final CountingFeatureOutputStream<Feature> errorOutput = new CountingFeatureOutputStream<Feature> ();
		final GenericFeature feature = createFeature ("Hello, world!");
		final DatasetFilterer filter = new DatasetFilterer (createFilterNull ());
		
		filter.processFeature (feature, output, errorOutput);
		
		assertEquals (0, output.getFeatureCount ());
		assertEquals (0, errorOutput.getFeatureCount ());
	}
	
	private static GenericFeature createFeature (final String value) {
		return new GenericFeature ("test-feature-id", new HashMap<String, Object> () {
			private static final long serialVersionUID = -2323010554811941246L;
			{
				put ("a", value);
				put ("b", null);
			}
		});
	}

	private static Filter createFilter () {
		final FilterFactory factory = new FilterFactory (FilterFactoryTest.createFeatureType ());
		return factory.createFilter (equal (attribute ("a", AttributeType.STRING), stringValue ("Hello, world!")));
	}
	
	private static Filter createFilterNotNull () {
		final FilterFactory factory = new FilterFactory (FilterFactoryTest.createFeatureType ());
		return factory.createFilter (notNull (attribute ("a", AttributeType.STRING)));
	}
	
	private static Filter createFilterNull () {
		final FilterFactory factory = new FilterFactory (FilterFactoryTest.createFeatureType ());
		return factory.createFilter (notNull (attribute ("b", AttributeType.STRING)));
	}
}
