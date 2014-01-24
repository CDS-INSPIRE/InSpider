package nl.ipo.cds.validation.gml.codelists;

import java.util.HashMap;
import java.util.Map;

public abstract class CachingCodeListFactory implements CodeListFactory {

	private final Map<String, CodeList> codeLists = new HashMap<> ();

	@Override
	public CodeList getCodeList (final String codeSpace) throws CodeListException {
		if (codeLists.containsKey (codeSpace)) {
			final CodeList codeList = codeLists.get (codeSpace);
			if (codeList == null) {
				throw new CodeListException (codeSpace, codeSpace, String.format ("Code list %s not found", codeSpace));
			}
			return codeList;
		}
		
		try {
			final CodeList codeList = doGetCodeList (codeSpace);
			
			codeLists.put (codeSpace, codeList);
			
			return codeList;
		} catch (CodeListException e) {
			codeLists.put (codeSpace, null);
			throw e;
		}
	}
	
	protected abstract CodeList doGetCodeList (String codeSpace) throws CodeListException;
}
