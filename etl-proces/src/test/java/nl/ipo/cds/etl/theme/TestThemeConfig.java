package nl.ipo.cds.etl.theme;

import static org.junit.Assert.fail;

import java.lang.reflect.Type;
import java.sql.Date;
import java.util.Set;

import javax.inject.Inject;

import nl.ipo.cds.attributemapping.operations.discover.annotation.AnnotationOperationDiscoverer;
import nl.ipo.cds.etl.theme.annotation.SkipConfiguration;
import nl.ipo.cds.etl.theme.protectedSite.ProtectedSite;
import nl.ipo.cds.etl.theme.protectedSite.ProtectedSiteThemeConfig;

import org.deegree.geometry.Geometry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestThemeConfig.Config.class)
public class TestThemeConfig {

	@Configuration
	@SkipConfiguration
	public static class Config {
		@Bean
		public ThemeConfig<ProtectedSite> protectedSiteConfig () {
			return new ProtectedSiteThemeConfig (null, new AnnotationOperationDiscoverer ());
		}
	}
	
	@Inject
	private ThemeConfig<ProtectedSite> config;
	
	@Test
	public void testIntrospect () {
		assertHasObjectClass (ProtectedSite.class);
		
		assertHasAttribute (ProtectedSite.class, "geometry", Geometry.class);
		assertHasAttribute (ProtectedSite.class, "legalFoundationDate", Date.class);
		assertHasAttribute (ProtectedSite.class, "legalFoundationDocument", String.class);
		assertHasAttribute (ProtectedSite.class, "inspireID", String.class);
		assertHasAttribute (ProtectedSite.class, "siteName", String.class);
		assertHasAttribute (ProtectedSite.class, "siteDesignation", String[].class);
		assertHasAttribute (ProtectedSite.class, "siteProtectionClassification", String[].class);
	}

	private void assertHasObjectClass (final Class<?> cls) {
		final Set<ObjectDescriptor<?>> ds = config.getObjectDescriptors ();
		
		for (final ObjectDescriptor<?> d: ds) {
			if (cls.equals (d.getObjectClass ())) {
				return;
			}
		}
		
		fail (String.format ("Class %s not found in object descriptors", cls));
	}
	
	private void assertHasAttribute (final Class<?> cls, final String name, final Type type) {
		final Set<AttributeDescriptor<?>> as = config.getAttributeDescriptors ();
				
		for (final AttributeDescriptor<?> a: as) {
			if (
					cls.equals (a.getObjectDescriptor ().getObjectClass ()) 
					&& name.equals (a.getName ())
					&& type.equals (a.getAttributeType ())) {
				return;
			}
		}
		
		fail (String.format ("Property %s of class %s not found in attribute descriptors", name, cls));
	}
}
