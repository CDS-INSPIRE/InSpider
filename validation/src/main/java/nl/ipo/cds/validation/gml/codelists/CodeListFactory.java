package nl.ipo.cds.validation.gml.codelists;

public interface CodeListFactory {

	CodeList getCodeList (String codeSpace) throws CodeListException;
}
