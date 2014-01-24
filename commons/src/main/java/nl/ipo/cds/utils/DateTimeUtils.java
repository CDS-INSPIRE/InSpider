/**
 * 
 */
package nl.ipo.cds.utils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Utility class voor allerlei utility methods.<br>
 * Kan tzt gesplitst worden.
 * 
 * @author Rob
 * 
 */
public class DateTimeUtils {
	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static Format dateFormatter = new SimpleDateFormat(DATE_FORMAT);
	private static Calendar cal = Calendar.getInstance();

	/*
	 * Date and Time functions
	 */
	/**
	 * current date and time.
	 * @return timestamp representing the current time.
	 */
	public static Timestamp now() {
		Calendar cal = Calendar.getInstance();
		return new Timestamp(cal.getTime().getTime());
	}

	/**
	 * current date and time.
	 * @return String representing the current time.
	 */
	/**
	 * @return
	 */
	public static String nowToDateString() {
		Calendar cal = Calendar.getInstance();
		return dateFormatter.format(cal.getTime());
	}
	
	/**
	 * Stopwatch .
	 * @return initial time in nanoseconds
	 */
	public static long startTiming(){
		return System.nanoTime();
	}

	/**
	 * Time elapsed since a start time.
	 * @param timestart in nanoseconds
	 * @return nanoseconds since timestart
	 */
	public static long timeSince(long timestart){
		return (System.nanoTime() - timestart);		
	}
	
	/**
	 * Time elapsed since a start time.
	 * @param timestart in nanoseconds
	 * @return milliseconds since timestart
	 */
	public static long timeSinceMillis(long timestart){
		return (timeSince(timestart) / 1000000);		
	}
	
	/**
	 * Parses a nr of milliseconds into a date/time string for the current locale.<br> 
	 * @param milliseconds the number of milliseconds since January 1, 1970, 00:00:00 GMT 
	 * @return String representing the date and time in the current locale
	 */
	public static String timeToDateString(long milliseconds){
		Date date = cal.getTime();
		date.setTime(milliseconds);
		return dateFormatter.format(date);
	}

	/**
	 * Parse a date time String according to a supplied pattern.
	 * @param dateString e.g. 2009-06-17T00:00:00.000
	 * @param pattern e.g. yyyy-mm-dd'T'HH:mm:ss.SSS
	 * @return Returns the number of milliseconds since January 1, 1970, 00:00:00 GMT 
	 * @throws Exception ParseException, but also IllegalArgumentException
	 */
	public static long parseDate(String dateString, String pattern) throws Exception{
		return new SimpleDateFormat(pattern).parse(dateString).getTime(); 
	}
}
