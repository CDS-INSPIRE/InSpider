package nl.ipo.cds.webservices;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.JaxenException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class FeatureCollectionComparator {

	private static final Log logger = LogFactory.getLog(FeatureCollectionComparator.class);

	private final String FEATURECOLLECTION_SORTFILE = "/nl/ipo/cds/webservices/featureCollectionSortfile.xslt";
	
	private Resource featureCollectionTransformerFile = new ClassPathResource(FEATURECOLLECTION_SORTFILE);
	
	public FeatureCollectionComparisonResult compareFeatureCollections(Resource resourceA, Resource resourceB) throws IOException, TransformerException, XMLStreamException, FactoryConfigurationError, JaxenException{
		
		List<String> featureMemberIdsA = createFeatureIdStringCollectionFlat(resourceA);
		List<String> featureMemberIdsB = createFeatureIdStringCollectionFlat(resourceB);
		return this.compareFeatureCollections(featureMemberIdsA, featureMemberIdsB);
		
	}

	public FeatureCollectionComparisonResult compareFeatureCollectionsByResources(List<Resource> getFeatureResources, List<String> featureMemberIdsB) throws IOException, TransformerException, XMLStreamException, FactoryConfigurationError, JaxenException{

		List<String> featureMemberIdsA = new ArrayList<String>();		
		for (Iterator<Resource> iterator = getFeatureResources.iterator(); iterator
				.hasNext();) {
			Resource resource = (Resource) iterator.next();
			logger.debug("Adding cachedResource" + "\"" + resource.getDescription() + "\" to the feature-id-list.");
			featureMemberIdsA.addAll(this.createFeatureIdStringCollectionFlat(resource));
		}
		
		return this.compareFeatureCollections(featureMemberIdsA, featureMemberIdsB);
	}
	
	public FeatureCollectionComparisonResult compareFeatureCollections(List<String> featureMemberIdsA, List<String> featureMemberIdsB) throws IOException, TransformerException, XMLStreamException, FactoryConfigurationError, JaxenException{
		FeatureCollectionComparisonResult featureCollectionComparisonResult = new FeatureCollectionComparisonResult();

		logger.debug("Start comparing collections");
		boolean success = true;

		// Collection same size?
		if(featureMemberIdsA.size() != featureMemberIdsB.size()){
			success = false;
			featureCollectionComparisonResult.addMessage("Collection A has \"" + featureMemberIdsA.size() + "\" features, but Collection B has \"" + featureMemberIdsB.size() + "\" features");
		} else {
			logger.debug("Start comparing collections on equality");
			success = CollectionUtils.isEqualCollection(featureMemberIdsA, featureMemberIdsB);
			logger.debug("Done comparing collections on equality");
		}		

		if(!success){
			logger.debug("There are differences. Start making report of differences");

			Collection<String> subtraction = CollectionUtils.subtract(featureMemberIdsA, featureMemberIdsB);
			for (Iterator<String> iterator = subtraction.iterator(); iterator.hasNext();) {
				String inspireId = (String) iterator.next();
				featureCollectionComparisonResult.addMessage("inspireID \"" + inspireId + "\" resides not in collection B");				
			}
			subtraction = CollectionUtils.subtract(featureMemberIdsB, featureMemberIdsA);
			for (Iterator<String> iterator = subtraction.iterator(); iterator.hasNext();) {
				String inspireId = (String) iterator.next();
				featureCollectionComparisonResult.addMessage("inspireID \"" + inspireId + "\" resides not in collection A");				
			}
		}
		featureCollectionComparisonResult.setFeatureCount(featureMemberIdsA.size());
		featureCollectionComparisonResult.setSuccess(success);

		logger.debug("Done comparing collections");

		return featureCollectionComparisonResult;
	
	}

	public static List<String> createFeatureIdStringCollectionFlat(Resource resource)
			throws JaxenException, XMLStreamException,
			FactoryConfigurationError, IOException {
		
	    Source xmlSource = new StreamSource(resource.getInputStream());

		final String FEATURE_ID_STRING = "//*[ends-with(lower-case(local-name(.)),'member')]//*[ends-with(lower-case(local-name(.)),'inspireid')]";
		AXIOMXPath queryXPath;

		queryXPath = new AXIOMXPath(FEATURE_ID_STRING);
//			queryXPath.addNamespace("wfs", "http://www.opengis.net/wfs");
		queryXPath.addNamespace("gml", "http://www.opengis.net/gml");
		XMLStreamReader parserA = XMLInputFactory.newInstance().createXMLStreamReader(xmlSource);
		StAXOMBuilder builderA = new StAXOMBuilder(parserA);
		OMElement featureCollectionA = builderA.getDocumentElement();

		List<OMElement> featureMemberIdElements = queryXPath.selectNodes(featureCollectionA);
		List<String> featureMemberStringIds = new ArrayList<String>();
		for (Iterator<OMElement> iterator = featureMemberIdElements.iterator(); iterator.hasNext();) {
			String id = null;
			OMElement inspireId = (OMElement) iterator.next();
			OMElement identifier = inspireId.getFirstElement();
			if(identifier != null){
				id = identifier.getFirstChildWithName(new QName("urn:x-inspire:specification:gmlas:BaseTypes:3.2","localId")).getText();
				id = identifier.getFirstChildWithName(new QName("urn:x-inspire:specification:gmlas:BaseTypes:3.2","namespace")).getText() + "." + id;
			} else {
				id = inspireId.getText();
			}
			featureMemberStringIds.add(id);
		}
		return featureMemberStringIds;
	}

	private FeatureCollectionComparisonResult compareViaSort(final Source xmlSourceA, final Source xmlSourceB)
			throws IOException, TransformerFactoryConfigurationError,
			TransformerConfigurationException, TransformerException,
			XMLStreamException, FactoryConfigurationError {

		// NOTE: Cannot work. Only allowed to use Pipes in different threads
		
		String message = null;
		boolean success = true;

		PipedOutputStream pipedOutputStreamA = new PipedOutputStream();
	    PipedInputStream pipedInputStreamA = new PipedInputStream(pipedOutputStreamA);
	    StreamResult sortedCollectionA = new StreamResult(pipedOutputStreamA);
	    sortCollection(xmlSourceA, sortedCollectionA);

	    PipedOutputStream pipedOutputStreamB = new PipedOutputStream();
	    PipedInputStream pipedInputStreamB = new PipedInputStream(pipedOutputStreamB);
	    StreamResult sortedCollectionB = new StreamResult(pipedOutputStreamB);
	    sortCollection(xmlSourceB, sortedCollectionB);
	
		XMLStreamReader parserA = XMLInputFactory.newInstance().createXMLStreamReader(pipedInputStreamA);
		StAXOMBuilder builderA = new StAXOMBuilder(parserA);
		OMElement featureCollectionA = builderA.getDocumentElement();

		XMLStreamReader parserB = XMLInputFactory.newInstance().createXMLStreamReader(pipedInputStreamB);
		StAXOMBuilder builderB = new StAXOMBuilder(parserB);
		OMElement featureCollectionB = builderB.getDocumentElement();

		Iterator<OMElement> featureCollectionIteratorA = featureCollectionA.getChildrenWithLocalName("featureMember");
		Iterator<OMElement> featureCollectionIteratorB = featureCollectionB.getChildrenWithLocalName("featureMember");
		long i = 0;
		for (Iterator<OMElement> iterator = featureCollectionIteratorA; iterator.hasNext();) {
			i++;
			OMElement featureMemberA = (OMElement) iterator.next();
			OMElement featureMemberB = featureCollectionIteratorB.next();
			boolean equal = StringUtils.equalsIgnoreCase(featureMemberA.getText(), featureMemberB.getText());
			System.out.println(i + "; id=" + featureMemberA.getText());
			if(!equal){
				message = "FeatureId \"" + featureMemberA.getText() + "\" not equal FeatureId \"" + featureMemberB.getText() + "\"";
				success = false;
				break;
			}
		}
		
		FeatureCollectionComparisonResult featureCollectionComparisonResult = new FeatureCollectionComparisonResult();
		return featureCollectionComparisonResult;
	}

	private StreamResult sortCollection(Source xmlSource, StreamResult sortedCollection)
			throws IOException, TransformerFactoryConfigurationError,
			TransformerConfigurationException, TransformerException {
		Source xsltSource = new StreamSource(featureCollectionTransformerFile.getInputStream());
	    // create an instance of TransformerFactory
	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
	 
	    Transformer transformer = transformerFactory.newTransformer(xsltSource);
	 
	    transformer.transform(xmlSource, sortedCollection);
	    
	    return sortedCollection;
	}
}
