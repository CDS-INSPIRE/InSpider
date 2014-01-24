package nl.ipo.cds.etl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import nl.ipo.cds.etl.theme.protectedSite.ProtectedSite;

import org.junit.Before;
import org.junit.Test;

public class FeaturePipelineTest {

	private ProtectedSite a;
	private ProtectedSite b;
	private ProtectedSite c;
	private ProtectedSite d;
	
	@Before
	public void createFeatures () {
		a = new ProtectedSite ();
		b = new ProtectedSite ();
		c = new ProtectedSite ();
		d = new ProtectedSite ();
	}
	
	/**
	 * Test a pipeline without any filters: it should pass all features
	 * directly to output stream without modification or change in ordering.
	 */
	public @Test void testEmptyPipeline () {
		List<ProtectedSite> result = process (pipeline ());

		assertEquals (0, result.size ());
		
		result = process (pipeline (), a);
		
		assertEquals (1, result.size ());
		assertSame (a, result.get (0));
		
		result = process (pipeline (), a, b, c);
		
		assertEquals (3, result.size ());
		assertSame (a, result.get (0));
		assertSame (b, result.get (1));
		assertSame (c, result.get (2));
	}

	/**
	 * Tests a pipeline that discards all or some of the input features.
	 */
	public @Test void testDiscardingPipeline () {
		List<ProtectedSite> result = process (pipeline (discardFilter ()), a, b, c);
		
		assertEquals (0, result.size ());
		
		result = process (pipeline (discardModuloFilter (2)), a, b, c, d);
		
		assertEquals (2, result.size ());
		assertSame (a, result.get (0));
		assertSame (c, result.get (1));
	}
	
	/**
	 * Test using pipelines that increase the number of features.
	 */
	public @Test void testEmitMultiple () {
		List<ProtectedSite> result = process (pipeline (duplicateFilter ()), a, b, c);
		
		assertEquals (6, result.size ());
		assertSame (a, result.get (0));
		assertSame (b, result.get (2));
		assertSame (c, result.get (4));
	}
	
	/**
	 * Tests using pipelines consisting of multiple filters.
	 */
	public @Test void testLongPipeline () {
		List<ProtectedSite> result = process (pipeline (noOpFilter (), noOpFilter (), noOpFilter (), noOpFilter ()), a, b, c);
		
		assertEquals (3, result.size ());
		assertSame (a, result.get (0));
		assertSame (b, result.get (1));
		assertSame (c, result.get (2));
		
		result = process (pipeline (noOpFilter (), noOpFilter (), discardFilter (), noOpFilter ()), a, b, c);
		
		assertEquals (0, result.size ());
		
		result = process (pipeline (noOpFilter (), discardModuloFilter (2), noOpFilter (), noOpFilter ()), a, b, c);
		
		assertEquals (2, result.size ());
		assertSame (a, result.get (0));
		assertSame (c, result.get (1));
		
		result = process (pipeline (noOpFilter (), discardModuloFilter (3), noOpFilter (), discardModuloFilter (2), noOpFilter ()), a, b, c, d);
		
		assertEquals (2, result.size ());
		assertSame (a, result.get (0));
		assertSame (d, result.get (1));
	}
	
	private static List<ProtectedSite> process (final FeaturePipeline<ProtectedSite, ProtectedSite> pipeline, final ProtectedSite ... features) {
		final List<ProtectedSite> result = new ArrayList<ProtectedSite> ();
		final FeatureOutputStream<ProtectedSite> outputStream = new FeatureOutputStream<ProtectedSite> () {
			@Override
			public void writeFeature (final ProtectedSite feature) {
				assertNotNull (feature);
				result.add (feature);
			}
		};
		
		final FeatureOutputStream<Feature> errorStream = new FeatureOutputStream<Feature> () {
			@Override
			public void writeFeature (final Feature feature) {
				assertNotNull (feature);
			}
		};
		
		for (final ProtectedSite feature: features) {
			pipeline.processFeature (feature, outputStream, errorStream);
		}
		
		return result;
	}
	
	private static FeaturePipeline<ProtectedSite, ProtectedSite> pipeline (final Filter ... featureFilters) {
		return new FeaturePipeline<ProtectedSite, ProtectedSite> (featureFilters);
	}
	
	private static Filter noOpFilter () {
		return new Filter () {
			@Override
			public void processFeature (final ProtectedSite feature, final FeatureOutputStream<ProtectedSite> outputStream, final FeatureOutputStream<Feature> errorStream) {
				outputStream.writeFeature (feature);
			}

			@Override
			public void finish () {
			}
		};
	}
	
	private static Filter discardFilter () {
		return new Filter () {
			@Override
			public void processFeature (final ProtectedSite feature, final FeatureOutputStream<ProtectedSite> outputStream, final FeatureOutputStream<Feature> errorStream) {
				// Write nothing to the output stream.
			}
			
			@Override
			public void finish () {
			}
		};
	}
	
	private static Filter discardModuloFilter (final int n) {
		return new Filter () {
			int count = 1;
			
			@Override
			public void processFeature (final ProtectedSite feature, final FeatureOutputStream<ProtectedSite> outputStream, final FeatureOutputStream<Feature> errorStream) {
				if ((count % n) != 0) {
					outputStream.writeFeature (feature);
				}
				++ count;
			}

			@Override
			public void finish () {
			}
		};
	}
	
	private static Filter duplicateFilter () {
		return new Filter () {
			@Override
			public void processFeature (final ProtectedSite feature, final FeatureOutputStream<ProtectedSite> outputStream, final FeatureOutputStream<Feature> errorStream) {
				outputStream.writeFeature (feature);
				outputStream.writeFeature (new ProtectedSite ());
			}
			
			@Override
			public void finish () {
			}
		};
	}
	
	private static interface Filter extends FeatureFilter<ProtectedSite, ProtectedSite> {
	}
}
