package nl.ipo.cds.etl.featuretype;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import nl.ipo.cds.domain.AttributeType;
import nl.ipo.cds.domain.FeatureType;

import org.apache.commons.io.IOUtils;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.property.CodePropertyType;
import org.deegree.feature.types.property.CustomPropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.schema.GMLAppSchemaReader;
import org.w3c.dom.ls.LSInput;

public class GMLFeatureTypeParser {

	private final GMLVersion gmlVersion;
	
	private final static String XML_SCHEMA_LOCAL_NAME = "schema";
	private final static String XML_SCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
	
	public GMLFeatureTypeParser () {
		this (null);
	}
	
	public GMLFeatureTypeParser (final GMLVersion gmlVersion) {
		this.gmlVersion = gmlVersion;
	}
	
	public FeatureType parseSchema (final AppSchema appSchema, final String featureTypeName) throws FeatureTypeNotFoundException {
		// Locate the feature type:
		final org.deegree.feature.types.FeatureType featureType = findFeatureType (appSchema, featureTypeName);
		if (featureType == null) {
			throw new FeatureTypeNotFoundException (featureTypeName, appSchema);
		}
		
		final Set<DefaultFeatureTypeAttribute> attributes = new HashSet<DefaultFeatureTypeAttribute> ();
		for (final PropertyType propertyType: featureType.getPropertyDeclarations ()) {
			final AttributeType attributeType = getAttributeType (propertyType);
			
			if (attributeType == null) {
				continue;
			}

			final DefaultFeatureTypeAttribute attribute = new DefaultFeatureTypeAttribute (new DefaultQName (propertyType.getName ()), attributeType);

			if (!attributes.contains (attribute)) {
				attributes.add (attribute);
			}
		}
		
		return new DefaultFeatureType (new DefaultQName (featureType.getName ()), attributes);
	}
	
	public FeatureType parseSchema (final InputStream inputStream, final String featureTypeName, final String encoding) throws FeatureTypeNotFoundException, ParseSchemaException {
		final AppSchema appSchema = parseApplicationSchema (inputStream, encoding);
		
		return parseSchema (appSchema, featureTypeName);
	}
	
	public AppSchema parseApplicationSchema (final InputStream inputStream, final String encoding) throws ParseSchemaException {
		final ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream ();
		final byte[] byteArray;

		// Load the XML schema into a byte array:
		try {
			IOUtils.copy (inputStream, byteOutputStream);
			byteArray = byteOutputStream.toByteArray ();
		} catch (IOException e) {
			throw new ParseSchemaException (e);
		}
		
		// Probe whether this inputStream contains an XML schema: 
		try {
			final XMLStreamReader xmlStreamReader;
			if (encoding == null) {
				xmlStreamReader = XMLInputFactory.newInstance ().createXMLStreamReader (new ByteArrayInputStream (byteArray));
			} else {
				xmlStreamReader = XMLInputFactory.newInstance ().createXMLStreamReader (new ByteArrayInputStream (byteArray), encoding);
			}
			
			while (!xmlStreamReader.isStartElement () && xmlStreamReader.hasNext ()) {
				xmlStreamReader.next ();
			}
			
			if (!xmlStreamReader.isStartElement () 
					|| !xmlStreamReader.getLocalName ().equals (XML_SCHEMA_LOCAL_NAME) 
					|| !xmlStreamReader.getNamespaceURI ().equals (XML_SCHEMA_NAMESPACE)) {
				throw new ParseSchemaException ("Not an XML schema");
			}
		} catch (XMLStreamException e) {
			throw new ParseSchemaException (e);
		}

		// Parse the XML schema:
		try {
			final GMLAppSchemaReader reader = new GMLAppSchemaReader (gmlVersion, null, createInput (new ByteArrayInputStream (byteArray), encoding));
			return reader.extractAppSchema ();
		} catch (ClassCastException e) {
			throw new ParseSchemaException (e);
		} catch (ClassNotFoundException e) {
			throw new ParseSchemaException (e);
		} catch (InstantiationException e) {
			throw new ParseSchemaException (e);
		} catch (IllegalAccessException e) {
			throw new ParseSchemaException (e);
		}
	}
	
	private AttributeType getAttributeType (final PropertyType propertyType) {
		if (propertyType instanceof GeometryPropertyType) {
			return AttributeType.GEOMETRY;
		} else if (propertyType instanceof CodePropertyType) {
			return AttributeType.CODE;
		} else if (propertyType instanceof CustomPropertyType) {
			final CustomPropertyType pt = (CustomPropertyType)propertyType;
			
			final XSComplexTypeDefinition typeDef = ((CustomPropertyType)propertyType).getXSDValueType ();
			
			if (typeDef == null || !typeDef.getNamespace ().startsWith ("http://www.opengis.net/gml")) {
				return null;
			}
			
			final String name = typeDef.getName ();
			if ("CodeType".equals (name)) {
				return AttributeType.CODE;
			} else {
				return null;
			}
		} else if (!(propertyType instanceof SimplePropertyType)) {
			return null;
		}
		
		final SimplePropertyType pt = (SimplePropertyType)propertyType;
		
		switch (pt.getPrimitiveType ().getBaseType ()) {
		case BOOLEAN:
			return AttributeType.BOOLEAN;
		case DATE:
			return AttributeType.DATE;
		case DATE_TIME:
			return AttributeType.DATE_TIME;
		case DECIMAL:
			// deegree 3 returns decimal for float and double, correct this:
			if ("double".equals (pt.getElementDecl().getTypeDefinition().getName())) {
				return AttributeType.DOUBLE;
			} else if ("float".equals (pt.getElementDecl().getTypeDefinition().getName())) {
				return AttributeType.FLOAT;
			} else {
				return AttributeType.DECIMAL;
			}
		case DOUBLE:
			return AttributeType.DOUBLE;
		case INTEGER:
			return AttributeType.INTEGER;
		case STRING:
			return AttributeType.STRING;
		case TIME:
			return AttributeType.TIME;
		default:
			return null;
		}
	}
	
	private org.deegree.feature.types.FeatureType findFeatureType (final AppSchema appSchema, final String featureTypeName) {
		final Set<String> namespaces = appSchema.getAppNamespaces ();

		for (final String namespace: namespaces) {
			final org.deegree.feature.types.FeatureType ft = appSchema.getFeatureType (new javax.xml.namespace.QName (namespace, featureTypeName));
			
			if (ft != null) {
				return ft;
			}
		}
		
		return null;
	}

	private LSInput createInput (final InputStream inputStream, final String encoding) {
		final Input input = new Input ();
		
		input.setByteStream (inputStream);
		input.setEncoding (encoding);
		
		return input;
	}
	
	private static class Input implements LSInput {

		private Reader characterStream;
		private InputStream byteStream;
		private String stringData;
		private String systemId;
		private String publicId;
		private String baseURI;
		private String encoding;
		private boolean certifiedText;
		
		@Override
		public Reader getCharacterStream() {
			return characterStream;
		}

		@Override
		public void setCharacterStream(Reader characterStream) {
			this.characterStream = characterStream;
		}

		@Override
		public InputStream getByteStream() {
			return byteStream;
		}

		@Override
		public void setByteStream(InputStream byteStream) {
			this.byteStream = byteStream;
		}

		@Override
		public String getStringData() {
			return stringData;
		}

		@Override
		public void setStringData(String stringData) {
			this.stringData = stringData;
		}

		@Override
		public String getSystemId() {
			return systemId;
		}

		@Override
		public void setSystemId(String systemId) {
			this.systemId = systemId;
		}

		@Override
		public String getPublicId() {
			return publicId;
		}

		@Override
		public void setPublicId(String publicId) {
			this.publicId = publicId;
		}

		@Override
		public String getBaseURI() {
			return baseURI;
		}

		@Override
		public void setBaseURI(String baseURI) {
			this.baseURI = baseURI;
		}

		@Override
		public String getEncoding() {
			return encoding;
		}

		@Override
		public void setEncoding(String encoding) {
			this.encoding = encoding;
		}

		@Override
		public boolean getCertifiedText() {
			return certifiedText;
		}

		@Override
		public void setCertifiedText(boolean certifiedText) {
			this.certifiedText = certifiedText;
		}
	}
}
