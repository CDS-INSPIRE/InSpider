package nl.ipo.cds.etl.operations.transform;

import nl.ipo.cds.attributemapping.operations.annotation.Execute;
import nl.ipo.cds.attributemapping.operations.annotation.Input;
import nl.ipo.cds.attributemapping.operations.annotation.MappingOperation;

@MappingOperation (propertiesClass = SubstringTransform.Settings.class)
public class SubstringTransform {

	@Execute
	public String execute (final @Input("string") String input, final Settings settings) {
		if (input == null || input.isEmpty ()) {
			return "";
		}
		
		final int start = settings.getStartIndex () - 1;
		final int end = Math.min (start + settings.getLength (), input.length ());
		
		if (start >= end || start < 0) {
			return "";
		}
		
		return input.substring (start, end);
	}
	
	public final static class Settings {
		private int startIndex = 1;
		private int length = 1;
		
		public int getStartIndex() {
			return startIndex;
		}
		
		public void setStartIndex(int startIndex) {
			this.startIndex = startIndex;
		}
		
		public int getLength() {
			return length;
		}

		public void setLength(int length) {
			this.length = length;
		}
	}
}
