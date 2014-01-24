package nl.ipo.cds.webservices;

import java.io.IOException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;

import junit.framework.Assert;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class FeatureCountTest{

	private static final Log logger = LogFactory.getLog(FeatureCountTest.class);
	
	@Test
	public void compareFeatureCollectionsEqualSize() throws IOException, TransformerException, XMLStreamException, FactoryConfigurationError, JaxenException{
		final String FEATURECOLLECTION_A = "/nl/ipo/cds/webservices/featureCollectionFlat.xml";
		final String FEATURECOLLECTION_B = "/nl/ipo/cds/webservices/featureCollectionInspireProtectedSite.xml";

		FeatureCollectionComparator comparator = new FeatureCollectionComparator();
		
		Resource featureCollectionA = new ClassPathResource(FEATURECOLLECTION_A);
		Resource featureCollectionB = new ClassPathResource(FEATURECOLLECTION_B);

		FeatureCollectionComparisonResult result = comparator.compareFeatureCollections(featureCollectionA, featureCollectionB);
		
		logger.debug(ReflectionToStringBuilder.toString(result.getMessages().toArray(), ToStringStyle.MULTI_LINE_STYLE));
		Assert.assertTrue("Collection's should be equal", result.isSuccess());
	}
	
	@Test
	public void compareFeatureCollectionsNotEqualSize() throws IOException, TransformerException, XMLStreamException, FactoryConfigurationError, JaxenException{
		final String FEATURECOLLECTION_A = "/nl/ipo/cds/webservices/featureCollectionFlat.xml";
		final String FEATURECOLLECTION_B = "/nl/ipo/cds/webservices/featureCollectionInspireProtectedSiteNotEqualSize.xml";

		FeatureCollectionComparator comparator = new FeatureCollectionComparator();
		
		Resource featureCollectionA = new ClassPathResource(FEATURECOLLECTION_A);
		Resource featureCollectionB = new ClassPathResource(FEATURECOLLECTION_B);

		FeatureCollectionComparisonResult result = comparator.compareFeatureCollections(featureCollectionA, featureCollectionB);
		
		logger.debug(ReflectionToStringBuilder.toString(result.getMessages().toArray(), ToStringStyle.MULTI_LINE_STYLE));
		Assert.assertFalse("Collection's should have a different size", result.isSuccess());
	}

	@Test
	public void compareFeatureCollectionsOneDifferentId() throws IOException, TransformerException, XMLStreamException, FactoryConfigurationError, JaxenException{
		final String FEATURECOLLECTION_A = "/nl/ipo/cds/webservices/featureCollectionFlat.xml";
		final String FEATURECOLLECTION_B = "/nl/ipo/cds/webservices/featureCollectionInspireProtectedSiteOneDifferentId.xml";

		FeatureCollectionComparator comparator = new FeatureCollectionComparator();
		
		Resource featureCollectionA = new ClassPathResource(FEATURECOLLECTION_A);
		Resource featureCollectionB = new ClassPathResource(FEATURECOLLECTION_B);

		FeatureCollectionComparisonResult result = comparator.compareFeatureCollections(featureCollectionA, featureCollectionB);
		
		logger.debug(ReflectionToStringBuilder.toString(result.getMessages().toArray(), ToStringStyle.MULTI_LINE_STYLE));
		Assert.assertFalse("One inspireID should be different", result.isSuccess());
	}
}
