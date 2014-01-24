package nl.ipo.cds.etl.featurecollection;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import nl.ipo.cds.domain.AttributeType;
import nl.ipo.cds.domain.FeatureType;
import nl.ipo.cds.domain.FeatureTypeAttribute;
import nl.ipo.cds.etl.GenericFeature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.tom.datetime.DateTime;
import org.deegree.commons.tom.genericxml.GenericXMLElement;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.uom.Measure;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.types.AppSchema;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;

public class FeatureCollectionReader {
	
	private static final Log logger = LogFactory.getLog(FeatureCollectionReader.class);
	
	private final GMLVersion gmlVersion;
	private final AppSchema applicationSchema;
	
	private final String GML_NS = "http://www.opengis.net/gml";
	
	private abstract class AbstractFeatureCollection implements FeatureCollection {
		final Envelope boundedBy;
		final FeatureType featureType;
		
		public AbstractFeatureCollection(Envelope boundedBy, final FeatureType featureType) {			
			if (featureType == null) {
				throw new NullPointerException ("featureType cannot be null");
			}
			this.boundedBy = boundedBy;
			this.featureType = featureType;
		}
		
		@Override
		public Envelope getBoundedBy() {
			return boundedBy;
		}
		
		@Override
		public FeatureType getFeatureType () {
			return featureType;
		}
	}
	
	private class DefaultFeatureCollection extends AbstractFeatureCollection {		
		boolean parsing = false, endOfStream = false;
				
		final XMLStreamReader streamReader;
		final boolean featureMembers;

		public DefaultFeatureCollection(XMLStreamReader streamReader, Envelope boundedBy, boolean featureMembers, final FeatureType featureType) {
			super(boundedBy, featureType);
			
			this.streamReader = streamReader;
			this.featureMembers = featureMembers;
		}
		
		@Override
		public Iterator<GenericFeature> iterator() {
			if(parsing) {
				throw new IllegalStateException("Parsing already started");
			}
			
			parsing = true;
			
			final ICRS defaultCRS;
			if(boundedBy != null) {
				defaultCRS = boundedBy.getCoordinateSystem();				
			} else {
				defaultCRS = null;
			}
			
			try {
				return new Iterator<GenericFeature>() {
					
					{							
						logger.debug("interator constructed");
						if(featureMembers) {
							nextTag();
						} else {
							nextFeatureMember();
						}
					}
					
					private void nextTag() throws XMLStreamException {
						streamReader.next();
						while(streamReader.hasNext() && !streamReader.isStartElement()) {
							streamReader.next();
						}
					}
					
					private void nextFeatureMember() throws XMLStreamException {
						logger.debug("fetching next feature");
						
						if(featureMembers) {
							
							while(streamReader.hasNext()) {
								if(streamReader.isStartElement()) {
									logger.debug("next feature within featureMembers fetched");
									break;
								}
								
								if(streamReader.isEndElement() 
									&& WFSResponseReader.GML_NAMESPACES.contains (streamReader.getNamespaceURI())
									&& streamReader.getLocalName().equals("featureMembers")) {
									
									logger.debug("end of stream");										
									endOfStream = true;
									break;
								}
								
								streamReader.next();
							}
						} else {							
							while(streamReader.hasNext()) {
								if(streamReader.isStartElement()) {
									QName currentName = streamReader.getName();
									if(WFSResponseReader.GML_NAMESPACES.contains (currentName.getNamespaceURI())
										&& currentName.getLocalPart().equals("featureMember")) {										
										nextTag();											
										
										logger.debug("next feature fetched");
										
										return;										
									}
								}
								
								streamReader.next();
							}
							
							logger.debug("end of stream");
							endOfStream = true;
						}
					}

					@Override
					public boolean hasNext() {
						return !endOfStream;
					}

					@Override
					public GenericFeature next() {
						try {
							final GMLStreamReader gmlStreamReader = GMLInputFactory.createGMLStreamReader (gmlVersion, streamReader);
							
							gmlStreamReader.setApplicationSchema (applicationSchema);
							if (defaultCRS != null) {
								gmlStreamReader.setDefaultCRS (defaultCRS);
							}
							
							logger.debug("unmarshal feature");
							final Feature feature = gmlStreamReader.readFeature ();
							
							nextFeatureMember ();
							
							return createGenericFeature (feature, featureType);
						} catch(Exception e) {
							logger.debug("error: " + e.getMessage());
							throw new RuntimeException("Couldn't read feature member", e);
						}
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}					
				};
			} catch(Exception e) {
				logger.debug("error: " + e.getMessage());
				throw new RuntimeException("Couldn't parse feature collection", e);
			}
		}
	}
	
	private class EmptyFeatureCollection extends AbstractFeatureCollection {

		public EmptyFeatureCollection(Envelope boundedBy, final FeatureType featureType) {
			super(boundedBy, featureType);
		}

		@Override
		public Iterator<GenericFeature> iterator() {
			return Collections.<GenericFeature>emptyList().iterator();
		}
	}
	
	public FeatureCollectionReader(final AppSchema applicationSchema) {
		if (applicationSchema == null) {
			throw new NullPointerException ("applicationSchema is null");
		}
		if (applicationSchema.getGMLSchema () == null || applicationSchema.getGMLSchema ().getVersion () == null) {
			throw new IllegalArgumentException ("Provided applicationSchema has no GML version");
		}
		
		this.gmlVersion = applicationSchema.getGMLSchema ().getVersion ();
		this.applicationSchema = applicationSchema;
	}

	FeatureCollection parseCollection(final XMLStreamReader streamReader, final FeatureType featureType) throws XMLStreamException, XMLParsingException, UnknownCRSException {
		Envelope boundedBy = null;
		
		if(streamReader.isStartElement()) {
			QName rootName = streamReader.getName();
			if((rootName.getNamespaceURI().equals(WFSResponseReader.WFS_NS) || WFSResponseReader.GML_NAMESPACES.contains (rootName.getNamespaceURI ()))
				&& rootName.getLocalPart().equals("FeatureCollection")) {									
				while(streamReader.hasNext()) {
					if(streamReader.isStartElement()) {
						if(WFSResponseReader.GML_NAMESPACES.contains (streamReader.getNamespaceURI ())) {
							String localName = streamReader.getLocalName();
							if(localName.equals("featureMember")) {
								return new DefaultFeatureCollection(streamReader, boundedBy, false, featureType);
							} else if(localName.equals("featureMembers")) {
								return new DefaultFeatureCollection(streamReader, boundedBy, true, featureType);
							} else if(localName.equals("boundedBy")) {
								streamReader.nextTag();			
								GMLStreamReader gmlStreamReader = GMLInputFactory.createGMLStreamReader(gmlVersion, streamReader);								
								boundedBy = (Envelope)gmlStreamReader.readGeometryOrEnvelope();
							}
						}
					} else if(streamReader.isEndElement() && streamReader.getName().equals(rootName)) {
						return new EmptyFeatureCollection(boundedBy, featureType);
					}
					
					streamReader.next();
				}									
			}
			
			throw new RuntimeException("XML stream is not a feature collection");			
		} else {
			throw new RuntimeException("Empty XML stream");
		}
	}
	
	public static GenericFeature createGenericFeature (final Feature feature, final FeatureType featureType) {
		if (feature == null) {
			throw new NullPointerException ("feature is null");
		}
		
		final Map<String, Object> values = new HashMap<String, Object> ();
		
		for (final Property property: feature.getProperties ()) {
			final TypedObjectNode node = property.getValue ();
			final String name = property.getName ().getLocalPart ();

			// Lookup the attribute in the feature type:
			final FeatureTypeAttribute attribute = getFeatureTypeAttribute (featureType, name);
			
			if (node instanceof Geometry) {
				values.put (property.getName ().getLocalPart (), (Geometry)node);
			} else if (node instanceof PrimitiveValue) {
				final PrimitiveValue value = (PrimitiveValue)node;

				values.put (property.getName ().getLocalPart (), convertPrimitiveValue (value, attribute));
			} else if (node instanceof CodeType || node instanceof Measure) {
				values.put (property.getName ().getLocalPart (), node);
			} else if (node instanceof GenericXMLElement) {
				final GenericXMLElement elem = (GenericXMLElement)node;
				
				// Parse code types and measures:
				final String value = elem.getValue ().getAsText ();
				final String codeSpace = getAttributeValue (elem, "codeSpace");
				values.put (property.getName ().getLocalPart (), new CodeType (value, codeSpace));
			}
		}
		
		return new GenericFeature (feature.getId (), values);
	}
	
	private static FeatureTypeAttribute getFeatureTypeAttribute (final FeatureType featureType, final String propertyName) {
		for (final FeatureTypeAttribute attr: featureType.getAttributes ()) {
			if (attr.getName ().getLocalPart ().equals (propertyName)) {
				return attr;
			}
		}
		
		return null;
	}
	
	private static String getAttributeValue (final GenericXMLElement element, final String localName) {
		final Map<QName, PrimitiveValue> attributes = element.getAttributes ();
		
		for (final Map.Entry<QName, PrimitiveValue> entry: attributes.entrySet ()) {
			if (entry.getKey ().getLocalPart ().equals (localName)) {
				return entry.getValue ().getAsText ();
			}
		}
		
		return null;
	}
	
	protected static Object convertPrimitiveValue (final PrimitiveValue value, final FeatureTypeAttribute attribute) {
		final Object val = value.getValue ();
		
		if (val instanceof DateTime) {
			return new Timestamp (((DateTime)val).getTimeInMilliseconds ());
		} else if (val instanceof Date) {
			return new java.sql.Date (((Date)val).getTimeInMilliseconds ());
		}
		
		// Convert BigDecimal into either Double, Float or BigDecimal depending on the type of the feature type attribute.
		// deegree returns each of these as BigDecimal.
		if (val instanceof BigDecimal && attribute != null) {
			if (AttributeType.DOUBLE.equals (attribute.getType())) {
				return ((BigDecimal)val).doubleValue ();
			} else if (AttributeType.FLOAT.equals (attribute.getType ())) {
				return ((BigDecimal)val).floatValue ();
			} else {
				return (BigDecimal)val;
			}
		}
		
		return val;
	}
}
