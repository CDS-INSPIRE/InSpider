package nl.ipo.cds.etl.operations.transform;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;

import nl.ipo.cds.attributemapping.operations.annotation.Execute;
import nl.ipo.cds.attributemapping.operations.annotation.Input;
import nl.ipo.cds.attributemapping.operations.annotation.MappingOperation;

@MappingOperation
public class DateToTimestampTransform {
	@Execute
	public Timestamp execute (final @Input("date") Date date) {
		if (date == null) {
			return null;
		}
		
		final Calendar calendar = new GregorianCalendar ();
		
		calendar.setTime (date);
		calendar.set (Calendar.HOUR_OF_DAY, 0);
		calendar.set (Calendar.MINUTE, 0);
		calendar.set (Calendar.SECOND, 0);
		calendar.set (Calendar.MILLISECOND, 0);
		
		return new Timestamp (calendar.getTimeInMillis ());
	}
}
