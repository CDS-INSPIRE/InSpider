package nl.ipo.cds.etl.featurecollection;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import nl.ipo.cds.domain.FeatureType;
import nl.ipo.cds.etl.featuretype.FeatureTypeNotFoundException;
import nl.ipo.cds.etl.featuretype.GMLFeatureTypeParser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.types.AppSchema;

public class WFSResponseReader {

	private static final Log technicalLog = LogFactory.getLog(WFSResponseReader.class); // developer log

	final static String WFS_NS = "http://www.opengis.net/wfs";
	final static String OWS_NS = "http://www.opengis.net/ows";

	final static Set<String> GML_NAMESPACES = new HashSet<String> ();
	static {
		GML_NAMESPACES.add ("http://www.opengis.net/gml");
		GML_NAMESPACES.add ("http://www.opengis.net/gml/3.2");
	}
	
	public WFSResponse parseWFSResponse(final XMLStreamReader streamReader, final AppSchema applicationSchema, final String featureTypeName) throws XMLStreamException, FeatureTypeNotFoundException {
		if (streamReader == null) {
			throw new NullPointerException ("streamReader is null");
		}
		if (applicationSchema == null) {
			throw new NullPointerException ("applicationSchema is null");
		}
		if (featureTypeName == null) {
			throw new NullPointerException ("featureTypeName is null");
		}

		
		while(streamReader.hasNext() && !streamReader.isStartElement()) {
			streamReader.next();
		}
		
		final GMLFeatureTypeParser featureTypeParser = new GMLFeatureTypeParser ();
		final FeatureType featureType = featureTypeParser.parseSchema (applicationSchema, featureTypeName.substring (featureTypeName.indexOf (':') + 1));
		
		return new WFSResponse () {
			
			ExceptionReport exceptionReport;
			FeatureCollection featureCollection;

			@Override
			public boolean isExceptionReport() {
				
				technicalLog.debug("Startelement: " + streamReader.isStartElement() + "; namespaceURI: " + streamReader.getNamespaceURI() + "; localName: " + streamReader.getLocalName());
				return exceptionReport != null || (
					streamReader.isStartElement()
					&& streamReader.getNamespaceURI().equals(OWS_NS)
					&& streamReader.getLocalName().equals("ExceptionReport")
				);
			}

			@Override
			public ExceptionReport getExceptionReport() throws XMLStreamException {
				if(!isExceptionReport()) {
					throw new XMLStreamException("Response is not a exception report");
				}
				
				if(exceptionReport == null) {
					
					exceptionReport = new ExceptionReport() {
						
						String exceptionCode = null, 
						locator = null, 
						exceptionText = null;
						
						{	
							streamReader.nextTag();
							
							QName name = streamReader.getName();							
							if(name.getNamespaceURI().equals(OWS_NS)
								&& name.getLocalPart().equals("Exception")) {
								
								exceptionCode = streamReader.getAttributeValue(null, "exceptionCode");
								locator = streamReader.getAttributeValue(null, "locator");
								
								streamReader.nextTag();
								
								name = streamReader.getName();
								if(name.getNamespaceURI().equals(OWS_NS)
									&& name.getLocalPart().equals("ExceptionText")) {
									
									streamReader.next();
									StringBuilder exceptionTextBuilder = new StringBuilder();
									while(streamReader.isCharacters()) {
										exceptionTextBuilder.append(streamReader.getText());
										streamReader.next();
									}
									
									exceptionText = exceptionTextBuilder.toString();
								}
							}
						}
						
						@Override
						public String getExceptionCode() {
							return exceptionCode;
						}
						
						@Override
						public String getLocator() {
							return locator;
						}
						
						@Override
						public String getExceptionText() {
							return exceptionText;
						}

						@Override
						public boolean hasExceptionCode() {							
							return exceptionCode != null;
						}

						@Override
						public boolean hasLocator() {
							return locator != null;
						}

						@Override
						public boolean hasExceptionText() {
							return exceptionText != null;
						}
					};
				}
				
				return exceptionReport;
			}

			@Override
			public boolean isFeatureCollection() {

				technicalLog.debug("Startelement: " + streamReader.isStartElement() + "; namespaceURI: " + streamReader.getNamespaceURI() + "; localName: " + streamReader.getLocalName());

				return featureCollection != null || (
					streamReader.isStartElement()
					&& (streamReader.getNamespaceURI().equals(WFS_NS) || GML_NAMESPACES.contains (streamReader.getNamespaceURI ()))
					&& streamReader.getLocalName().equals("FeatureCollection")
				);
			}

			@Override
			public  FeatureCollection getFeatureCollection() throws XMLStreamException, XMLParsingException, UnknownCRSException {
				if(!isFeatureCollection()) {
					throw new XMLStreamException("Response is not a feature collection");
				}
				
				if(featureCollection == null) {
					FeatureCollectionReader featureCollectionReader = 
						new FeatureCollectionReader(applicationSchema);
					featureCollection = featureCollectionReader.parseCollection(streamReader, featureType);
				}
				
				return featureCollection;
			}
		};
	}
}
