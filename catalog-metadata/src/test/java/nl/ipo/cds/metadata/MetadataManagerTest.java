package nl.ipo.cds.metadata;

import java.io.File;
import java.io.InputStream;

import java.nio.file.Files;

import nl.ipo.cds.metadata.MetadataManager;
import nl.ipo.cds.metadata.XMLRewriter;

import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class MetadataManagerTest {

	File tempDir;	
	MetadataManager manager;

	@Before
	public void setUp() throws Exception {
		tempDir = Files.createTempDirectory("MetadataManagerTest").toFile();
		tempDir.deleteOnExit();

		manager = new MetadataManager(tempDir);
	}

	private InputStream getInputStream(String documentName) throws Exception {
		return getClass().getClassLoader().getResourceAsStream("nl/ipo/cds/metadata/" + documentName);
	}
	
	private XMLRewriter getRewriter(String documentName) throws Exception {
		InputStream inputStream = getInputStream(documentName);
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

	@Test
	public void testStore() throws Exception {
		String documentName = "protectedSites.xml";

		File f = new File(tempDir, documentName);
		assertFalse(f.exists());

		manager.storeDocument(documentName, getInputStream(documentName));
		assertTrue(f.exists());
	}
}
