package nl.ipo.cds.metadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import java.nio.file.Files;
import java.util.Set;

import nl.ipo.cds.domain.MetadataDocumentType;
import nl.ipo.cds.metadata.MetadataManager;
import nl.ipo.cds.metadata.XMLRewriter;

import org.apache.axiom.attachments.utils.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class MetadataManagerTest {

	static File tempDir;	
	static MetadataManager manager;

	@BeforeClass
	public static void setUp() throws Exception {
		tempDir = Files.createTempDirectory("MetadataManagerTest").toFile();
		manager = new MetadataManager(tempDir);
	}
	
	@AfterClass
	public static void deleteTempDir() {
		tempDir.delete();
	}
	
	@After
	public void cleanUpTempDir() {
		for(File f : tempDir.listFiles()) {
			if(!f.delete()) {
				fail("cleanup failed");
			}
		}
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

		manager.storeDocument(documentName, IOUtils.getStreamAsByteArray(getInputStream(documentName)));
		assertTrue(f.exists());
	}
	
	@Test
	public void testList() throws Exception {
		String documentName = "protectedSites.xml";
		
		Set<String> documentNames = manager.listDocuments();
		assertNotNull(documentNames);
		assertTrue(documentNames.isEmpty());
		
		File f = new File(tempDir, documentName);		
		assertTrue(f.createNewFile());		
		
		documentNames = manager.listDocuments();
		assertNotNull(documentNames);
		assertEquals(1, documentNames.size());
		assertTrue(documentNames.contains(documentName));
	}
	
	@Test
	public void testDelete() throws Exception {
		String documentName = "protectedSites.xml";
		
		File f = new File(tempDir, documentName);
		assertTrue(f.createNewFile());
		
		Set<String> documentNames = manager.listDocuments();
		assertNotNull(documentNames);
		assertEquals(1, documentNames.size());
		assertTrue(documentNames.contains(documentName));
		
		manager.deleteDocument(documentName);
		
		documentNames = manager.listDocuments();
		assertNotNull(documentNames);
		assertTrue(documentNames.isEmpty());
	}
	
	@Test
	public void testRetrieve() throws Exception {
		String documentName = "protectedSites.xml";
		
		File f = new File(tempDir, documentName);
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(42);
		fos.close();
		
		byte[] content = manager.retrieveDocument(documentName);
		assertNotNull(content);
		assertEquals(1, content.length);
		assertEquals(42, content[0]);
	}
	
	@Test
	public void testValidateNotWellFormed() throws UnsupportedEncodingException {
		ValidationResult result = manager.validateDocument("<xml>/xml>".getBytes("utf-8"), MetadataDocumentType.DATASET);
		
		assertNotNull(result);
		assertEquals(ValidationResult.NOT_WELL_FORMED, result);
	}
	
	@Test
	public void testValidateSchemaViolation() throws Exception {
		InputStream inputStream = getInputStream("protectedSitesSchemaViolation.xml");
		assertNotNull(inputStream);
		
		ValidationResult result = manager.validateDocument(IOUtils.getStreamAsByteArray(inputStream), MetadataDocumentType.DATASET);
		
		assertNotNull(result);
		assertEquals(ValidationResult.SCHEMA_VIOLATION, result);
	}
	
	@Test
	public void testValidateGmd() throws Exception {
		InputStream inputStream = getInputStream("protectedSites.xml");
		assertNotNull(inputStream);
		
		ValidationResult result = manager.validateDocument(IOUtils.getStreamAsByteArray(inputStream), MetadataDocumentType.DATASET);
		
		assertNotNull(result);
		assertEquals(ValidationResult.VALID, result);
	}
	
	@Test
	public void testValidateSrv() throws Exception {
		InputStream inputStream = getInputStream("protectedSitesView.xml");
		assertNotNull(inputStream);
		
		ValidationResult result = manager.validateDocument(IOUtils.getStreamAsByteArray(inputStream), MetadataDocumentType.SERVICE);
		
		assertNotNull(result);
		assertEquals(ValidationResult.VALID, result);
	}
	
	@Test
	public void testValidateDatePathMissing() throws Exception {
		InputStream inputStream = getInputStream("protectedSitesDateMissing.xml");
		assertNotNull(inputStream);
		
		ValidationResult result = manager.validateDocument(IOUtils.getStreamAsByteArray(inputStream), MetadataDocumentType.DATASET);
		
		assertNotNull(result);
		assertEquals(ValidationResult.DATE_PATH_MISSING, result);
	}
}
