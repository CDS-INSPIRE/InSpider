package nl.ipo.cds.etl.operations.input;

import nl.ipo.cds.attributemapping.MappingSource;
import nl.ipo.cds.attributemapping.operations.annotation.Execute;
import nl.ipo.cds.attributemapping.operations.annotation.MappingOperation;

@MappingOperation (propertiesClass = StringInput.Settings.class, internal = true)
public class StringInput {

	@Execute
	public String execute (final Settings settings, final MappingSource source) {
		return source.getAttributeValue (settings.getAttributeName ()).toString ();
	}
	
	public final static class Settings {
		private String attributeName;

		public String getAttributeName () {
			return attributeName;
		}

		public void setAttributeName (final String attributeName) {
			if (attributeName == null) {
				throw new NullPointerException ();
			}
			this.attributeName = attributeName;
		}
	}
}
