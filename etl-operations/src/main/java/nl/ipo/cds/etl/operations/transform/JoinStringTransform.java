package nl.ipo.cds.etl.operations.transform;

import nl.ipo.cds.attributemapping.operations.annotation.Execute;
import nl.ipo.cds.attributemapping.operations.annotation.Input;
import nl.ipo.cds.attributemapping.operations.annotation.MappingOperation;

@MappingOperation (propertiesClass = JoinStringTransform.Settings.class)
public class JoinStringTransform {

	@Execute
	public String execute (final @Input("list") String[] input, final Settings settings) {
		final StringBuilder builder = new StringBuilder (settings.getPrefix ());
		final String separator = settings.getSeparator ();
		String s = "";
		
		if (input != null) {
			for (final String value: input) {
				builder.append (s);
				builder.append (value);
				s = separator;
			}
		}
		
		builder.append (settings.getPostfix ());
		
		return builder.toString ();
	}
	
	public static class Settings {
		private String prefix = "";
		private String postfix = "";
		private String separator = ",";

		public String getSeparator() {
			return separator;
		}

		public void setSeparator(String separator) {
			assert (separator != null);
			this.separator = separator;
		}

		public String getPrefix() {
			return prefix;
		}

		public void setPrefix(String prefix) {
			assert (prefix != null);
			this.prefix = prefix;
		}

		public String getPostfix() {
			return postfix;
		}

		public void setPostfix(String postfix) {
			assert (postfix != null);
			this.postfix = postfix;
		}
	}
}
