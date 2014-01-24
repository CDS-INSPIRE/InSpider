package nl.ipo.cds.validation.gml.codelists;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class StaticCodeListFactory extends CachingCodeListFactory {

	private final Map<String, CodeList> codeLists;
	
	public StaticCodeListFactory (final Map<String, CodeList> codeLists) {
		this.codeLists = new HashMap<String, CodeList> (codeLists);
	}

	@Override
	protected CodeList doGetCodeList (final String codeSpace) throws CodeListException {
		if (codeLists.containsKey (codeSpace)) {
			return codeLists.get (codeSpace);
		}
		
		throw new CodeListException (codeSpace, codeSpace, String.format ("Code list not found: %s", codeSpace));
	}

	public static class StaticCodeList implements CodeList {

		public final String codeSpace;
		public final Set<String> codes;
		
		public StaticCodeList (final String codeSpace, final Set<String> codes) {
			assert codeSpace != null;
			assert codes != null;
			
			this.codeSpace = codeSpace;
			this.codes = new HashSet<> (codes);
		}
		
		@Override
		public String getCodeSpace () {
			return codeSpace;
		}

		@Override
		public Set<String> getCodes () {
			return Collections.unmodifiableSet (codes);
		}

		@Override
		public boolean hasCode (final String code) {
			return codes.contains (code);
		}
	}
}
