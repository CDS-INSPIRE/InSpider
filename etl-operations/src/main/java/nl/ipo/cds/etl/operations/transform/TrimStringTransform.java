package nl.ipo.cds.etl.operations.transform;

import nl.ipo.cds.attributemapping.operations.annotation.Input;
import nl.ipo.cds.attributemapping.operations.annotation.MappingOperation;

@MappingOperation (propertiesClass = TrimStringTransform.Settings.class)
public class TrimStringTransform {

	public String execute (final @Input ("value") String value, final Settings settings) {
		if (value == null || value.length () == 0) {
			return value;
		}

		final String additionalCharacters = settings.getAdditionalCharacters ();
		
		if (additionalCharacters == null || additionalCharacters.length () == 0) {
			return value.trim ();
		}
		
		final StringBuffer buffer = new StringBuffer (value);
		final char[] chars = additionalCharacters.toCharArray ();
		
		// Trim leading characters:
		leadingLoop: while (buffer.length () > 0) {
			final char c = buffer.charAt (0);
			
			// Strip whitespace:
			if (Character.isWhitespace (c)) {
				buffer.deleteCharAt (0);
				continue;
			}
			
			for (int i = 0; i < chars.length; ++ i) {
				if (c == chars[i]) {
					buffer.deleteCharAt (0);
					continue leadingLoop;
				}
			}
			
			break;
		}
		
		// Trim trailing characters:
		trailingLoop: while (buffer.length () > 0) {
			final int lastIndex = buffer.length () - 1;
			final char c = buffer.charAt (lastIndex);
			
			if (Character.isWhitespace (c)) {
				buffer.deleteCharAt (lastIndex);
				continue;
			}
			
			for (int i = 0; i < chars.length; ++ i) {
				if (c == chars[i]) {
					buffer.deleteCharAt (lastIndex);
					continue trailingLoop;
				}
			}
			
			break;
		}
		
		return buffer.toString ();
	}
	
	public final static class Settings {
		private String additionalCharacters = "";

		public String getAdditionalCharacters () {
			return additionalCharacters;
		}

		public void setAdditionalCharacters (final String additionalCharacters) {
			this.additionalCharacters = additionalCharacters;
		}
	}
}
