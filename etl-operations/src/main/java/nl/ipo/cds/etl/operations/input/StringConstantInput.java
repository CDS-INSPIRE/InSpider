package nl.ipo.cds.etl.operations.input;

import nl.ipo.cds.attributemapping.operations.annotation.Execute;
import nl.ipo.cds.attributemapping.operations.annotation.MappingOperation;

import nl.ipo.cds.etl.operations.input.StringConstantInput.Settings;

@MappingOperation (propertiesClass = Settings.class)
public class StringConstantInput {

	@Execute
	public String execute (final Settings settings) {
		return settings.getValue ();
	}
	
	public static class Settings {
		private String value = "";

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			if (value == null) {
				throw new NullPointerException ();
			}
			this.value = value;
		}
	}
}
