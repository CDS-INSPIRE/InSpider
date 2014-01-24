package nl.ipo.cds.etl.attributemapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobLogger;
import nl.ipo.cds.dao.attributemapping.InputOperationDTO;
import nl.ipo.cds.dao.attributemapping.OperationDTO;
import nl.ipo.cds.dao.attributemapping.OperationInputDTO;
import nl.ipo.cds.dao.attributemapping.TransformOperationDTO;
import nl.ipo.cds.domain.AttributeType;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.domain.FeatureType;
import nl.ipo.cds.domain.FeatureTypeAttribute;
import nl.ipo.cds.domain.ImportJob;
import nl.ipo.cds.domain.QName;
import nl.ipo.cds.etl.FeatureOutputStream;
import nl.ipo.cds.etl.GenericFeature;
import nl.ipo.cds.etl.featurecollection.FeatureCollection;
import nl.ipo.cds.etl.operations.transform.ConditionalTransform;
import nl.ipo.cds.etl.theme.AttributeDescriptor;
import nl.ipo.cds.etl.theme.ThemeConfig;
import nl.ipo.cds.etl.theme.protectedSite.ProtectedSite;
import nl.ipo.cds.etl.theme.protectedSite.ProtectedSiteThemeConfig;

import org.deegree.geometry.Envelope;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AttributeMapperTest.Config.class)
public class AttributeMapperTest extends AbstractAttributeMapperTest {

	private static final String themeName = "Protected sites";
	
	@Named ("attributeMappingValidatorMessages")
	@Inject
	private Properties validatorMessages;
	
	private EtlJob job;
	private ThemeConfig<ProtectedSite> themeConfig;
	private FeatureType featureType;
	private Map<AttributeDescriptor<ProtectedSite>, OperationDTO> attributeMappings;
	private AttributeMapper<ProtectedSite> attributeMapper;
	private List<String> logLines;
	
	@Configuration
	@ComponentScan (basePackageClasses = { 
		nl.ipo.cds.etl.theme.protectedSite.config.Package.class, 
		nl.ipo.cds.etl.theme.Package.class
	})
	public static class Config {
	}
	
	@Before
	public void setUp () {
		// Create a job:
		job = new ImportJob ();

		// Locate the theme configuration:
		themeConfig = themeDiscoverer.getThemeConfiguration (themeName, ProtectedSiteThemeConfig.class);
		assertNotNull (themeConfig);
		
		// Create a mocked feature type:
		featureType = getFeatureType ();
		
		// Create mocked attribute mappings:
		attributeMappings = getAttributeMappings (themeConfig, featureType);
		
		logLines = new ArrayList<String> ();
		attributeMapper = new AttributeMapper<ProtectedSite> (
				job,
				themeConfig, 
				featureType, 
				Collections.<AttributeDescriptor<?>, OperationDTO>unmodifiableMap (attributeMappings), 
				new JobLogger () {

					@Override
					public void logString(Job job, String key, LogLevel logLevel, String message) {
						logLines.add (message);
					}

					@Override
					public void logString(Job job, String key,
							LogLevel logLevel, String message,
							Map<String, Object> context) {
						logString (job, key, logLevel, message);
					}
				}, 
				validatorMessages
			);
		
		assertTrue (attributeMapper.isValid ());
	}
	
	@Test
	public void testAttributeMapping () {
		final FeatureCollection featureCollection = featureCollection (Arrays.asList (new GenericFeature[] { 
			feature ("feature.0", "feature.0.inspireID", "feature.0.siteName"),	
			feature ("feature.1", "feature.1.inspireID", "feature.1.siteName"),	
		}), featureType);
		final List<ProtectedSite> result = new ArrayList<ProtectedSite> ();
		
		attributeMapper.processFeatures (featureCollection, new FeatureOutputStream<ProtectedSite> () {
			@Override
			public void writeFeature (final ProtectedSite feature) {
				result.add (feature);
			}
		});

		assertEquals (2, result.size ());
		assertEquals ("feature.0", result.get (0).getId ());
		assertEquals ("feature.1", result.get (1).getId ());
		assertEquals ("feature.0.inspireID", result.get (0).getInspireID ());
		assertEquals ("feature.1.inspireID", result.get (1).getInspireID ());
		assertEquals ("feature.0.siteName", result.get (0).getSiteName ());
		assertEquals ("feature.1.siteName", result.get (1).getSiteName ());
	}

	private GenericFeature feature (final String id, final String inspireID, final String siteName) {
		final Map<String, Object> values = new HashMap<String, Object> ();
		
		values.put ("inspireID", inspireID);
		values.put ("siteName", siteName);
		
		return new GenericFeature (id, values);
	}
	
	private FeatureCollection featureCollection (final List<GenericFeature> features, final FeatureType featureType) {
		return new FeatureCollection() {
			@Override
			public Iterator<GenericFeature> iterator () {
				return features.iterator ();
			}
			
			@Override
			public Envelope getBoundedBy () {
				return null;
			}
			
			@Override
			public FeatureType getFeatureType () {
				return featureType;
			}
		};
	}
	
	private Map<AttributeDescriptor<ProtectedSite>, OperationDTO> getAttributeMappings (final ThemeConfig<ProtectedSite> themeConfig, final FeatureType featureType) {
		final Map<AttributeDescriptor<ProtectedSite>, OperationDTO> mappings = new HashMap<AttributeDescriptor<ProtectedSite>, OperationDTO> ();
		
		mappings.put (getAttributeDescriptor (themeConfig, "inspireID"), mapping (getFeatureTypeAttribute (featureType, "inspireID")));
		mappings.put (getAttributeDescriptor (themeConfig, "siteName"), mapping (getFeatureTypeAttribute (featureType, "siteName")));
		
		return mappings;
	}
	
	private FeatureTypeAttribute getFeatureTypeAttribute (final FeatureType featureType, final String name) {
		for (final FeatureTypeAttribute attr: featureType.getAttributes ()) {
			if (attr.getName ().getLocalPart ().equals (name)) {
				return attr;
			}
		}
		
		fail (String.format ("Attribute not found: %s", name));
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private AttributeDescriptor<ProtectedSite> getAttributeDescriptor (final ThemeConfig<ProtectedSite> themeConfig, final String name) {
		for (final AttributeDescriptor<?> ad: themeConfig.getAttributeDescriptors ()) {
			if (ad.getName ().equals (name)) {
				return (AttributeDescriptor<ProtectedSite>)ad;
			}
		}
		
		fail (String.format ("Attribute descriptor not found: %s", name));
		return null;
	}
	
	private OperationDTO mapping (final FeatureTypeAttribute attribute) {
		final InputOperationDTO inputOperation = new InputOperationDTO (attribute, attribute.getName ().getLocalPart (), AttributeType.STRING);
		final List<OperationInputDTO> operationInputs = Arrays.asList (new OperationInputDTO[] { new OperationInputDTO (inputOperation) });
		
		return new TransformOperationDTO (getOperationType (ConditionalTransform.class), operationInputs, new ConditionalTransform.Settings ());
	}
	
	private FeatureType getFeatureType () {
		return new FeatureType () {
			@Override
			public QName getName () {
				return qname ("TestFeatureType");
			}
			
			@Override
			public Set<FeatureTypeAttribute> getAttributes () {
				final Set<FeatureTypeAttribute> attributes = new HashSet<FeatureTypeAttribute> ();
				
				attributes.add (attribute ("inspireID", AttributeType.STRING));
				attributes.add (attribute ("siteName", AttributeType.STRING));
				
				return attributes;
			}
		};
	}
	
	private FeatureTypeAttribute attribute (final String name, final AttributeType attributeType) {
		return new FeatureTypeAttribute () {
			@Override
			public int compareTo (final FeatureTypeAttribute o) {
				return getName ().compareTo (o.getName ());
			}
			
			@Override
			public AttributeType getType () {
				return attributeType;
			}
			
			@Override
			public QName getName () {
				return qname (name);
			}
		};
	}
	
	private QName qname (final String localPart) {
		assertNotNull (localPart);
		return new QName() {
			@Override
			public int compareTo (final QName o) {
				// Ignore the namespace when comparing:
				return getLocalPart ().compareTo (o.getLocalPart ());
			}
			
			@Override
			public String getNamespace () {
				return "http://www.idgis.nl";
			}
			
			@Override
			public String getLocalPart () {
				return localPart;
			}
		};
	}
}
