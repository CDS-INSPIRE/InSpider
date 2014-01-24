package nl.ipo.cds.etl.operations.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import nl.ipo.cds.attributemapping.operations.annotation.Execute;
import nl.ipo.cds.attributemapping.operations.annotation.Input;
import nl.ipo.cds.attributemapping.operations.annotation.MappingOperation;
import nl.ipo.cds.etl.operations.transform.SplitStringTransform.Settings;

@MappingOperation (propertiesClass = Settings.class)
public class SplitStringTransform {

	@Execute
	public String[] execute (final @Input("string") String input, final Settings settings) {
		final String boundary = settings.getBoundary ();
		final boolean trimWhitespace = settings.isTrimWhitespace ();
		final boolean ignoreEmptyItems = settings.isIgnoreEmptyItems ();
		final String[] parts;
		
		// Split the string:
		try {
			parts = input.split (Pattern.quote (boundary));
		} catch (PatternSyntaxException e) {
			throw new IllegalStateException (String.format ("Invalid split pattern: `%s`", boundary));
		}
		
		// Process the parts (trim and ignore empty elements):
		final List<String> result = new ArrayList<String> (parts.length);
		for (String part: parts) {
			if (trimWhitespace) {
				part = part.trim ();
			}
			if (ignoreEmptyItems && part.isEmpty ()) {
				continue;
			}
			
			result.add (part);
		}

		return result.toArray (new String[result.size ()]);
	}
	
	public final static class Settings {
		private String boundary = ",";
		private boolean trimWhitespace = true;
		private boolean ignoreEmptyItems = false;
		
		public String getBoundary () {
			return boundary;
		}
		
		public void setBoundary (final String boundary) {
			if (boundary == null) {
				throw new NullPointerException ();
			}
			
			this.boundary = boundary;
		}
		
		public boolean isTrimWhitespace () {
			return trimWhitespace;
		}
		
		public void setTrimWhitespace (final boolean trimWhitespace) {
			this.trimWhitespace = trimWhitespace;
		}
		
		public boolean isIgnoreEmptyItems () {
			return ignoreEmptyItems;
		}
		
		public void setIgnoreEmptyItems (final boolean ignoreEmptyItems) {
			this.ignoreEmptyItems = ignoreEmptyItems;
		}
	}
}
