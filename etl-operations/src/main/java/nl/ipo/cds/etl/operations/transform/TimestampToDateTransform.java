package nl.ipo.cds.etl.operations.transform;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;

import nl.ipo.cds.attributemapping.operations.annotation.Execute;
import nl.ipo.cds.attributemapping.operations.annotation.Input;
import nl.ipo.cds.attributemapping.operations.annotation.MappingOperation;

@MappingOperation
public class TimestampToDateTransform {

	@Execute
	public Date convertTimestampToDate (final @Input("input") Timestamp timestamp) {
		final Calendar calendar = Calendar.getInstance ();
		final Calendar result = Calendar.getInstance ();
		
		calendar.setTimeInMillis (timestamp.getTime ());
		
		result.set (Calendar.YEAR, calendar.get (Calendar.YEAR));
		result.set (Calendar.MONTH, calendar.get (Calendar.MONTH));
		result.set (Calendar.DAY_OF_MONTH, calendar.get (Calendar.DAY_OF_MONTH));
		
		return new Date (result.getTimeInMillis ());
	}
}
