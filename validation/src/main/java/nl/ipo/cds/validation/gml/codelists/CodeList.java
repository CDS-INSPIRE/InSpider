package nl.ipo.cds.validation.gml.codelists;

import java.util.Set;

public interface CodeList {

	String getCodeSpace ();
	Set<String> getCodes ();
	boolean hasCode (String code);
}
