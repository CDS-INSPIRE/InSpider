package nl.ipo.cds.etl.test;

import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import nl.ipo.cds.etl.GenericFeature;
import nl.ipo.cds.etl.featurecollection.FeatureCollection;
import nl.ipo.cds.etl.featurecollection.WFSResponse;
import nl.ipo.cds.etl.featurecollection.WFSResponseReader;
import nl.ipo.cds.etl.featuretype.FeatureTypeNotFoundException;
import nl.ipo.cds.etl.theme.protectedSite.ProtectedSite;
import nl.ipo.cds.etl.util.LSInputUtils;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.types.AppSchema;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.schema.GMLAppSchemaReader;
import org.junit.Test;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public class TestData {
	
	private XMLInputFactory inputFactory;
	
	public TestData() {
		inputFactory = XMLInputFactory.newInstance();
	}
	
	public InputStream getInputStream() {
		return getClass().getResourceAsStream("featureCollection.xml");
	}
	
	@Test
	public void dummy(){
		
	}
	public XMLStreamReader getXMLStreamReader() throws XMLStreamException {
		return inputFactory.createXMLStreamReader(getInputStream());
	}
	
	public String getFeatureTypeName () {
		return "StilteGebieden";
	}
	
	public AppSchema getAppSchema () throws ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		final InputStream stream = getClass ().getClassLoader ().getResourceAsStream ("nl/ipo/cds/etl/test/appschema.xsd");
		
		final GMLAppSchemaReader appSchemaReader = new GMLAppSchemaReader (GMLVersion.GML_31, null, LSInputUtils.createInput (stream, "UTF-8"));
		
		return appSchemaReader.extractAppSchema ();
	}
	
	public FeatureCollection getFeatureCollection() throws XMLStreamException, JAXBException, XMLParsingException, UnknownCRSException, ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException, FeatureTypeNotFoundException {
		final WFSResponseReader wfsResponseReader = new WFSResponseReader();
		final WFSResponse wfsResponse = wfsResponseReader.parseWFSResponse(getXMLStreamReader (), getAppSchema (), getFeatureTypeName ());
		
		return wfsResponse.getFeatureCollection();
	}
	
	public List<ProtectedSite> getProtectedSites () throws XMLParsingException, ClassCastException, XMLStreamException, JAXBException, UnknownCRSException, ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, FeatureTypeNotFoundException {
		final List<ProtectedSite> protectedSites = new ArrayList<ProtectedSite> ();
		
		for (final GenericFeature feature: getFeatureCollection ()) {
			final ProtectedSite protectedSite = new ProtectedSite ();
			final BeanWrapper wrapper = new BeanWrapperImpl (protectedSite);
			
			protectedSite.setId (feature.getId ());
			
			for (final PropertyDescriptor pd: wrapper.getPropertyDescriptors ()) {
				if (pd.getName ().equals ("id")) {
					continue;
				}
				
				if (pd.getWriteMethod () == null) {
					continue;
				}
				
				final Object value; 
				if (pd.getPropertyType ().equals (String[].class)) {
					value = feature.get (pd.getName ()).toString ().split ("\\|");
				} else if (pd.getPropertyType ().equals (Date.class)) {
					value = new Date (DatatypeConverter.parseDate (feature.get (pd.getName ()).toString ()).getTime ().getTime ());
				} else {
					value = feature.get (pd.getName ());
				}
				
				pd.getWriteMethod ().invoke (protectedSite, value);
				
			}
			
			protectedSites.add (protectedSite);
		}
		
		return protectedSites;
	}
}
