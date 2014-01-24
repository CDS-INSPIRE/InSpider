package nl.ipo.cds.etl.process;

import static org.junit.Assert.assertEquals;
import nl.ipo.cds.domain.ImportJob;

import org.junit.Before;
import org.junit.Test;

public class HarvesterTest {
	
	private Harvester harvester;

	@Before
	public void setUp() {
		HarvesterFactory harvesterFactory = new HarvesterFactory();
		harvester = harvesterFactory.createHarvester(new ImportJob());
	}
	
	@Test
	public void testWfsGetCapabilitiesUrl() {
		final String expectedUrl = "http://host.test/service?service=WFS&request=GetCapabilities";
		assertEquals(expectedUrl, harvester.getMetadataHarvester ().createWfsGetCapabilitiesUrl("http://host.test/service"));
		assertEquals(expectedUrl, harvester.getMetadataHarvester ().createWfsGetCapabilitiesUrl("http://host.test/service?"));
		assertEquals(expectedUrl, harvester.getMetadataHarvester ().createWfsGetCapabilitiesUrl("http://host.test/service?service=WFS"));
		assertEquals(expectedUrl, harvester.getMetadataHarvester ().createWfsGetCapabilitiesUrl("http://host.test/service?service=WFS&request=GetCapabilities"));
	}
	
	@Test
	public void testWfsGetFeatureUrl() {
		String expected = "http://host.test/service?request=GetFeature&typename=EHS&service=WFS&version=1.1.0";
		assertEquals(expected, harvester.createWfsGetFeatureUrl("http://host.test/service", "EHS"));
		assertEquals(expected, harvester.createWfsGetFeatureUrl("http://host.test/service?", "EHS"));
		
		expected = "http://host.test/service?vendorParam=value&request=GetFeature&typename=EHS&service=WFS&version=1.1.0";
		assertEquals(expected, harvester.createWfsGetFeatureUrl("http://host.test/service?vendorParam=value", "EHS"));
		assertEquals(expected, harvester.createWfsGetFeatureUrl("http://host.test/service?vendorParam=value&", "EHS"));
	}
}
