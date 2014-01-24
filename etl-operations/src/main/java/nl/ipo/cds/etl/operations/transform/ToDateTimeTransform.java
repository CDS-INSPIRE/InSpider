package nl.ipo.cds.etl.operations.transform;

import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.ipo.cds.attributemapping.operations.annotation.Execute;
import nl.ipo.cds.attributemapping.operations.annotation.Input;
import nl.ipo.cds.attributemapping.operations.annotation.MappingOperation;

@MappingOperation
public class ToDateTimeTransform {

	private final Pattern datePattern;
	
	public ToDateTimeTransform () {
		datePattern = Pattern.compile ("^([0-9]{4}-[0-9]{2}-[0-9]{2})[T ]([0-9]{2}\\:[0-9]{2}\\:[0-9]{2}(\\.[0-9]+)?)$");
	}
	
	@Execute
	public Timestamp execute (final @Input ("value") String value) {
		if (value == null || value.trim ().length () == 0) {
			return null;
		}
		
		final Matcher matcher;
		
		if ((matcher = datePattern.matcher (value)).matches ()) {
			final String date = matcher.group (1);
			final String time = matcher.group (2);
			return Timestamp.valueOf (date + " " + time);
		}
		
		return null;
	}
}
