package nl.ipo.cds.etl.featurecollection;

import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.cs.exceptions.UnknownCRSException;

public interface WFSResponse {
	boolean isExceptionReport() throws XMLStreamException;
	ExceptionReport getExceptionReport() throws XMLStreamException;;
	
	boolean isFeatureCollection() throws XMLStreamException;
	FeatureCollection getFeatureCollection() throws XMLStreamException, XMLParsingException, UnknownCRSException;
}
