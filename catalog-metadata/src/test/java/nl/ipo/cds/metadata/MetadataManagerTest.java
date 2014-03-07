package nl.ipo.cds.metadata;

import java.io.InputStream;

import nl.ipo.cds.metadata.MetadataManager;
import nl.ipo.cds.metadata.XMLRewriter;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class MetadataManagerTest {
	
	MetadataManager manager = new MetadataManager();
	
	private XMLRewriter getRewriter(String documentName) throws Exception {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("nl/ipo/cds/metadata/" + documentName);
		assertNotNull(inputStream);
		
		XMLRewriter rewriter = manager.createRewriter(inputStream);
		assertNotNull(rewriter);
		
		return rewriter;
	}

	@Test
	public void testUpdate() throws Exception {		
		manager.updateDatasetMetadata(getRewriter("protectedSites.xml"), "");
		manager.updateServiceMetadata(getRewriter("protectedSitesView.xml"), "");
	}
}
