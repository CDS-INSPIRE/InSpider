package nl.ipo.cds.etl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.domain.ValidateJob;

import org.junit.Test;

public class JobMailTest {

	@Test
	public void testCreateMsg() {
		Bronhouder bronhouder = new Bronhouder();
		bronhouder.setId(300l);
		
		EtlJob job = new ValidateJob ();
		job.setId(100l);
		job.setBronhouder(bronhouder);
		
		EtlJobMail jobMail = new EtlJobMail();
		jobMail.setHost("www.inspire-provincies.nl");
		String msg = jobMail.createMsg(job);
		
		assertNotNull(msg);
		assertTrue(msg.length() > 0);
		assertEquals(-1, msg.indexOf("$"));
	}
}
