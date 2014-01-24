package nl.ipo.cds.metadata;

import nl.ipo.cds.metadata.MetadataManager;
import nl.ipo.cds.metadata.XMLRewriter;

import org.junit.Test;

public class MetadataManagerTest {

	@Test
	public void testUpdate() throws Exception {
		MetadataManager manager = new MetadataManager();
		XMLRewriter rewriter = manager.createRewriter(getClass().getClassLoader().getResourceAsStream("nl/ipo/cds/metadata/protectedSites.xml"));
		manager.updateDatasetMetadata(rewriter, "");
	}
}
