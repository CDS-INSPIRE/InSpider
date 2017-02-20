package nl.ipo.cds.etl;

import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.DatasetType;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.domain.ValidateJob;
import org.junit.Test;

import static org.junit.Assert.*;

public class JobMailTest {

	@Test
	public void testCreateMsg() {
		Bronhouder bronhouder = new Bronhouder();
		bronhouder.setId(300l);
		
		EtlJob job = new ValidateJob ();
		job.setId(100l);
		job.setBronhouder(bronhouder);
		DatasetType dt = new DatasetType();
		dt.setId((long) 2002);
		job.setDatasetType(dt);
		
		EtlJobMail jobMail = new EtlJobMail();
		jobMail.setHost("www.inspire-provincies.nl");
		jobMail.setHostProto("http");
		String msg = jobMail.createMsg(job);
		
		assertNotNull(msg);
		assertTrue(msg.length() > 0);
		assertEquals(-1, msg.indexOf("$"));
	}
}
