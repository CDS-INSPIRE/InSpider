package nl.ipo.cds.etl.operations.transform;

import static org.junit.Assert.*;

import java.sql.Timestamp;

import org.junit.Before;
import org.junit.Test;

public class TestToDateTimeTransform {

	private ToDateTimeTransform transform;
	
	@Before
	public void createTransform () {
		transform = new ToDateTimeTransform ();
	}
	
	@Test
	public void testDate () {
		
		assertEquals (ts ("2000-01-02 03:04:05"), transform.execute ("2000-01-02 03:04:05"));
		assertEquals (ts ("2000-01-02 03:04:05"), transform.execute ("2000-01-02T03:04:05"));
		
		assertEquals (ts ("2000-01-02 03:04:05.6"), transform.execute ("2000-01-02 03:04:05.6"));
		assertEquals (ts ("2000-01-02 03:04:05.6"), transform.execute ("2000-01-02T03:04:05.6"));

		assertNull (transform.execute ("2000/01/02 03:04:05"));
		assertNull (transform.execute ("2000/01/02T03:04:05"));
	}
	
	public Timestamp ts (final String s) {
		return Timestamp.valueOf (s);
	}

}
