package nl.ipo.cds.etl.operations.transform;

import java.sql.Date;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.ipo.cds.attributemapping.operations.annotation.Execute;
import nl.ipo.cds.attributemapping.operations.annotation.Input;
import nl.ipo.cds.attributemapping.operations.annotation.MappingOperation;

@MappingOperation
public class ToDateTransform {

	private final Pattern datePattern;
	private final Pattern datePatternNL;
	
	public ToDateTransform () {
		datePattern = Pattern.compile ("^([0-9]{4})[-\\/]([0-9]{2})[-\\/]([0-9]{2})$");
		datePatternNL = Pattern.compile ("^([0-9]{2})[-\\/]([0-9]{2})[-\\/]([0-9]{4})$");
	}
	
	@Execute
	public Date execute (final @Input ("value") String value) {
		if (value == null || value.trim ().length () == 0) {
			return null;
		}

		Matcher matcher = datePattern.matcher (value);
		final int year;
		final int month;
		final int day;
		
		if ((matcher = datePattern.matcher (value)).matches ()) {
			year = Integer.valueOf (matcher.group (1));
			month = Integer.valueOf (matcher.group (2));
			day = Integer.valueOf (matcher.group (3));
		} else if ((matcher = datePatternNL.matcher (value)).matches ()) {
			year = Integer.valueOf (matcher.group (3));
			month = Integer.valueOf (matcher.group (2));
			day = Integer.valueOf (matcher.group (1));
		} else {
			return null;
		}
		
		final Calendar calendar = Calendar.getInstance ();
		calendar.set (year, month - 1, day);
		
		return new Date (calendar.getTime ().getTime ());
	}

}
