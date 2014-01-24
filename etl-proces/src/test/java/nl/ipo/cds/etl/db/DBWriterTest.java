package nl.ipo.cds.etl.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Date;

import nl.idgis.commons.utils.DateTimeUtils;
import nl.ipo.cds.etl.test.TestData;
import nl.ipo.cds.etl.theme.protectedSite.ProtectedSite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;

public class DBWriterTest {
	
	private static final String TEST_DATASET_ID = "0";	
	private static final String TEST_ID = "TEST.ID.0";
	
	private static final Log logger = LogFactory.getLog(DBWriterTest.class);
	
	private DBWriterFactory<ProtectedSite> dbWriterFactory;
	
	private void assertOccurrence(int expected, String line, String str) {
		int count = 0, index = line.indexOf(str);
		while(index != -1) {
			count++;
			index = line.indexOf(str, index + str.length());
		}
		
		assertEquals(expected, count);
	}
	
	@Before
	public void setUp() {
		dbWriterFactory = new DBWriterFactory<ProtectedSite>(ProtectedSite.class, "dataset_id", TEST_DATASET_ID);
	}
	
	@Test
	public void testQuery() throws IOException {		
		String query = dbWriterFactory.getQuery();
		
		assertTrue(query.startsWith("copy "));
		assertFalse(query.indexOf("bron.protected_site") == -1);
		
		int open = query.indexOf("(");
		int close = query.indexOf(")");		
		assertFalse(open == -1);
		assertFalse(close == -1);
		
		String columnsString = query.substring(open + 1, close);
		assertOccurrence(8, columnsString, ", ");
		
		String[] columns = columnsString.split(", ");
		assertEquals(9, columns.length);
		
		assertFalse(query.indexOf("csv") == -1);
	}
	
	@Test
	public void testSingleRecord() throws IOException {
		StringWriter stringWriter = new StringWriter();
		DBWriter<ProtectedSite> dbWriter = dbWriterFactory.getDBWriter(stringWriter);		

		final Date now = new Date (DateTimeUtils.now ().getTime ());
		
		ProtectedSite ps = new ProtectedSite();
		ps.setInspireID("");
		ps.setId(TEST_ID);
		ps.setSiteProtectionClassification (new String[] { "cultural", "ecological" });
		ps.setLegalFoundationDate (now);
		
		dbWriter.writeObject(ps);
		
		BufferedReader bufferedReader = new BufferedReader(new StringReader(stringWriter.getBuffer().toString()));
		String line = bufferedReader.readLine();
		assertNotNull(line);		
		assertOccurrence(8, line, ","); // 9 columns -> 8 separators
		
		String[] columns = line.split(",");
		assertEquals(9, columns.length);
		
		int nulls = 0, empties = 0, ids = 0, datasetIds = 0, lists = 0, dates = 0;
		for(String column : columns) {
			if(column.isEmpty()) {
				column = null;
			} else {
				assertEquals('"', column.charAt(0));
				assertEquals('"', column.charAt(column.length() - 1));
				column = column.substring(1, column.length() - 1);
			}
			
			if(column == null) {
				nulls++;
			} else if(column.equals("")) {
				empties++;
			} else if(column.equals(TEST_ID)) {
				ids++;
			} else if(column.equals(TEST_DATASET_ID)) {
				datasetIds++;
			} else if (column.contains ("|")) {
				++ lists;
			} else if (column.equals (now.toString ())) {
				++ dates;
			}
		}
		
		assertEquals(1, empties);
		assertEquals(4, nulls);
		assertEquals(1, ids);		
		assertEquals(1, datasetIds);
		assertEquals (1, lists);
		assertEquals (1, dates);
		
		line = bufferedReader.readLine();
		assertNull(line);		
	}
	
	@Test
	public void testFeatureCollection() throws Exception {
		File tempFile = File.createTempFile("test", ".sql");
		logger.info(tempFile);
		
		FileWriter writer = new FileWriter(tempFile);		
		PrintWriter printWriter = new PrintWriter(writer);
		
		DBWriter<ProtectedSite> dbWriter = dbWriterFactory.getDBWriter(printWriter);
		printWriter.println(dbWriterFactory.getQuery() + ";");
		
		TestData testData = new TestData();
		Iterable<ProtectedSite> stilteGebieden = testData.getProtectedSites();
		int count = 0;
		for(ProtectedSite stilteGebied : stilteGebieden) {
			assertNotNull(stilteGebied);
			dbWriter.writeObject(stilteGebied);
			count++;
		}
		
		assertEquals(33, count);
		
		printWriter.close();
	}
}
