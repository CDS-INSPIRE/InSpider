package nl.ipo.cds.etl.operations.transform;

import static org.junit.Assert.*;

import java.sql.Date;
import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;

public class TestToDateTransform {

	private ToDateTransform toDate;
	
	@Before
	public void createToDateTransform () {
		toDate = new ToDateTransform ();
	}
	
	@Test
	public void testNull () {
		assertNull (toDate.execute (null));
	}
	
	@Test
	public void testEmptyString () {
		assertNull (toDate.execute (""));
		assertNull (toDate.execute (" "));
	}
	
	@Test
	public void testInvalidFormat () {
		assertNull (toDate.execute ("Hello, world!"));
	}
	
	@Test
	public void testConvertDate () {
		assertDate (2013, 1, 2, parseDate ("2013-01-02"));
		assertDate (2013, 1, 2, parseDate ("2013/01/02"));
		
		assertDate (2014, 6, 18, parseDate ("18-06-2014"));
		assertDate (2015, 7, 19, parseDate ("19-07-2015"));
	}
	
	@Test
	public void testConvertDateInvalid () {
		assertDate (2013, 2, 1, parseDate ("2013-01-32"));
	}

	private Calendar parseDate (final String dateString) {
		final Date date = toDate.execute (dateString);
		if (date == null) {
			return null;
		}
		
		final Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis (date.getTime ());
		
		return calendar;
	}
	
	private static void assertDate (final int year, final int month, final int day, final Calendar calendar) {
		assertEquals (year, calendar.get (Calendar.YEAR));
		assertEquals (month - 1, calendar.get (Calendar.MONTH));
		assertEquals (day, calendar.get (Calendar.DAY_OF_MONTH));
	}
}
