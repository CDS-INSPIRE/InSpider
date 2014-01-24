package nl.ipo.cds.attributemapping.operations.discover.annotation.operations;

import nl.ipo.cds.attributemapping.operations.annotation.Input;
import nl.ipo.cds.attributemapping.operations.annotation.MappingOperation;

@MappingOperation (propertiesClass = TestOperation.Settings.class)
public class TestOperation {
	public String execute (final @Input("a") String a, final @Input("b") String b) {
		return String.format ("%s:%s", a, b);
	}
	
	public static class Settings {
		public String getSeparator () {
			return null;
		}
		
		public void setSeparator (final String separator) {
		}
	}
}
