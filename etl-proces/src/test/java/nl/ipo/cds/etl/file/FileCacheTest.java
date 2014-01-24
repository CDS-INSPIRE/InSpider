/**
 * 
 */
package nl.ipo.cds.etl.file;


import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.Dataset;
import nl.ipo.cds.domain.DatasetType;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.domain.ImportJob;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.Assert;

/**
 * @author Rob
 *
 */
//@RunWith(SpringJUnit4ClassRunner.class)
public class FileCacheTest {
	private static FileCacheImpl fileCache;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUp() throws Exception {
		fileCache = new FileCacheImpl();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFileCache() throws Exception {
		String testText = "Hello World!\n";
		
		EtlJob job = constructJob();
		File fileW = fileCache.makeFile(job);
	    FileWriter fw = new FileWriter(fileW);
	    // Write string to the file
	    fw.write(testText);
	    // Close file writer
	    fw.close();
	    // read it back with filereader
		File fileR = fileCache.getFile(job);
	    FileReader fr = new FileReader(fileR);
	    StringBuffer sb = new StringBuffer("");
	    int c;
	    while ((c = fr.read()) != -1) {
	    	sb.append((char) c);
	    }
	    Assert.isTrue(sb.toString().equals(testText), "written: ["+testText+"] and read: ["+sb.toString()+"] text does not match!");
	    fr.close ();
	}
	
	private EtlJob constructJob() {
		Bronhouder bronhouder = new Bronhouder();
		bronhouder.setCode("9900");
		
		Dataset dataset = new Dataset();
		dataset.setBronhouder(bronhouder);
		dataset.setUuid("NotAValidUUID");
		DatasetType datasetType =new DatasetType(); 
		datasetType.setNaam("datasetType");
		dataset.setDatasetType(datasetType);

		EtlJob job = new ImportJob();
		job.setId(1000l);
		// copy properties from dataset to job
		job.setBronhouder(dataset.getBronhouder());
		job.setDatasetType(dataset.getDatasetType());
		job.setUuid(dataset.getUuid());
		return job;		
	}

	
}
