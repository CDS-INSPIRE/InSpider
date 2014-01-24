package nl.ipo.cds.etl.operations.transform;

import static org.junit.Assert.*;

import java.sql.Date;
import java.sql.Timestamp;

import org.junit.Before;
import org.junit.Test;

public class TestDateToTimestampTransform {

	private DateToTimestampTransform transform;
	
	@Before
	public void createTransform () {
		transform = new DateToTimestampTransform ();
	}
	
	@Test
	public void testExecute () {
		assertEquals (ts ("2013-01-01 00:00:00.0"), transform.execute (d ("2013-01-01")));
		assertNull (transform.execute (null));
	}
	
	private Timestamp ts (final String ts) {
		return Timestamp.valueOf (ts);
	}
	
	private Date d (final String d) {
		return Date.valueOf (d);
	}
}
