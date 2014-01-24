package nl.ipo.cds.etl.featurecollection;

public interface ExceptionReport {

	boolean hasExceptionCode();	
	String getExceptionCode();
	
	boolean hasLocator();
	String getLocator();
	
	boolean hasExceptionText();
	String getExceptionText();
}
