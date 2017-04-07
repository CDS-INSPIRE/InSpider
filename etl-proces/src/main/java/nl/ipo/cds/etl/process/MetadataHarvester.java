package nl.ipo.cds.etl.process;

import static nl.ipo.cds.etl.process.MetadataHarvester.FeatureCollectionType.GML;
import static nl.ipo.cds.etl.process.MetadataHarvester.FeatureCollectionType.WFS;
import static nl.ipo.cds.etl.process.MetadataHarvester.FeatureCollectionType.XML;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;

import nl.ipo.cds.etl.process.helpers.HttpGetUtil;
import nl.ipo.cds.utils.AxiomUtils;
import nl.ipo.cds.utils.DateTimeUtils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deegree.commons.utils.Pair;
import org.jaxen.JaxenException;

public class MetadataHarvester {

	public static enum FeatureCollectionType {
		WFS,
		GML,
		XML
	}
	
	public static class FeatureCollectionReference {
		public final FeatureCollectionType type;
		public final String url;
		public final String featureTypeName;
		//TODO uitbreiden met xsd (W3C:XSD) URL uit metadata doc 
		public final String xsdUrl;
		
		public FeatureCollectionReference (final FeatureCollectionType type, final String url, final String featureTypeName, final String xsdUrl) {
			assert type != null;
			assert url != null;
			
			this.type = type;
			this.url = url;
			this.featureTypeName = featureTypeName;
			this.xsdUrl = xsdUrl;
		}
	}
	
	private static final Log technicalLog = LogFactory.getLog (MetadataHarvester.class); // developer log
	
	private final static String xpathCreationDate = "//*[local-name()='MD_Metadata']/*[local-name()='identificationInfo']/*[local-name()='MD_DataIdentification']/*[local-name()='citation']/*[local-name()='CI_Citation']/*[local-name()='date']/*[local-name()='CI_Date'][*[local-name()='dateType']/*[local-name()='CI_DateTypeCode']/@codeListValue='creation']/*[local-name()='date']/*[local-name()='Date']";
	private final static String xpathRevisionDate = "//*[local-name()='MD_Metadata']/*[local-name()='identificationInfo']/*[local-name()='MD_DataIdentification']/*[local-name()='citation']/*[local-name()='CI_Citation']/*[local-name()='date']/*[local-name()='CI_Date'][*[local-name()='dateType']/*[local-name()='CI_DateTypeCode']/@codeListValue='revision']/*[local-name()='date']/*[local-name()='Date']";
	private final static String xpathCreationDateOrRevisionDate = "//*[local-name()='MD_Metadata']/*[local-name()='identificationInfo']/*[local-name()='MD_DataIdentification']/*[local-name()='citation']/*[local-name()='CI_Citation']/*[local-name()='date']/*[local-name()='CI_Date'][(*[local-name()='dateType']/*[local-name()='CI_DateTypeCode']/@codeListValue='creation') or (*[local-name()='dateType']/*[local-name()='CI_DateTypeCode']/@codeListValue='revision')]/*[local-name()='date']/*[local-name()='Date']";
	private final static String xpathCreationDateTime = "//*[local-name()='MD_Metadata']/*[local-name()='identificationInfo']/*[local-name()='MD_DataIdentification']/*[local-name()='citation']/*[local-name()='CI_Citation']/*[local-name()='date']/*[local-name()='CI_Date'][*[local-name()='dateType']/*[local-name()='CI_DateTypeCode']/@codeListValue='creation']/*[local-name()='date']/*[local-name()='DateTime']";
	private final static String xpathRevisionDateTime = "//*[local-name()='MD_Metadata']/*[local-name()='identificationInfo']/*[local-name()='MD_DataIdentification']/*[local-name()='citation']/*[local-name()='CI_Citation']/*[local-name()='date']/*[local-name()='CI_Date'][*[local-name()='dateType']/*[local-name()='CI_DateTypeCode']/@codeListValue='revision']/*[local-name()='date']/*[local-name()='DateTime']";
	private final static String xpathCreationDateTimeOrRevisionDateTime = "//*[local-name()='MD_Metadata']/*[local-name()='identificationInfo']/*[local-name()='MD_DataIdentification']/*[local-name()='citation']/*[local-name()='CI_Citation']/*[local-name()='date']/*[local-name()='CI_Date'][(*[local-name()='dateType']/*[local-name()='CI_DateTypeCode']/@codeListValue='creation') or (*[local-name()='dateType']/*[local-name()='CI_DateTypeCode']/@codeListValue='revision')]/*[local-name()='date']/*[local-name()='DateTime']";
	private final static String xpathWfs                  = "//*[local-name()='MD_Metadata']/*[local-name()='distributionInfo']/*[local-name()='MD_Distribution']/*[local-name()='transferOptions']/*[local-name()='MD_DigitalTransferOptions']/*[local-name()='onLine']/*[local-name()='CI_OnlineResource'][*[local-name()='protocol']/*[local-name()='CharacterString']='OGC:WFS']/*[local-name()='linkage']/*[local-name()='URL']";
	private final static String xpathGmlFeatureCollection = "//*[local-name()='MD_Metadata']/*[local-name()='distributionInfo']/*[local-name()='MD_Distribution']/*[local-name()='transferOptions']/*[local-name()='MD_DigitalTransferOptions']/*[local-name()='onLine']/*[local-name()='CI_OnlineResource'][*[local-name()='protocol']/*[local-name()='CharacterString']='OGC:GML']/*[local-name()='linkage']/*[local-name()='URL']";
	private final static String xpathXmlDataset           = "//*[local-name()='MD_Metadata']/*[local-name()='distributionInfo']/*[local-name()='MD_Distribution']/*[local-name()='transferOptions']/*[local-name()='MD_DigitalTransferOptions']/*[local-name()='onLine']/*[local-name()='CI_OnlineResource'][*[local-name()='protocol']/*[local-name()='CharacterString']='W3C:XML']/*[local-name()='linkage']/*[local-name()='URL']";
	private final static String xpathXsdUrl               = "//*[local-name()='MD_Metadata']/*[local-name()='distributionInfo']/*[local-name()='MD_Distribution']/*[local-name()='transferOptions']/*[local-name()='MD_DigitalTransferOptions']/*[local-name()='onLine']/*[local-name()='CI_OnlineResource'][*[local-name()='protocol']/*[local-name()='CharacterString']='W3C:XSD']/*[local-name()='linkage']/*[local-name()='URL']";
	private final static String xpathFeatureType    = "//*[local-name()='MD_Metadata']/*[local-name()='distributionInfo']/*[local-name()='MD_Distribution']/*[local-name()='transferOptions']/*[local-name()='MD_DigitalTransferOptions']/*[local-name()='onLine']/*[local-name()='CI_OnlineResource'][*[local-name()='protocol']/*[local-name()='CharacterString']='OGC:WFS']/*[local-name()='name']/*[local-name()='CharacterString']";
	private final static String xpathGmlFeatureType = "//*[local-name()='MD_Metadata']/*[local-name()='distributionInfo']/*[local-name()='MD_Distribution']/*[local-name()='transferOptions']/*[local-name()='MD_DigitalTransferOptions']/*[local-name()='onLine']/*[local-name()='CI_OnlineResource'][*[local-name()='protocol']/*[local-name()='CharacterString']='OGC:GML']/*[local-name()='name']/*[local-name()='CharacterString']";
	private final static String xpathXmlFeatureType = "//*[local-name()='MD_Metadata']/*[local-name()='distributionInfo']/*[local-name()='MD_Distribution']/*[local-name()='transferOptions']/*[local-name()='MD_DigitalTransferOptions']/*[local-name()='onLine']/*[local-name()='CI_OnlineResource'][*[local-name()='protocol']/*[local-name()='CharacterString']='W3C:XML']/*[local-name()='name']/*[local-name()='CharacterString']";	
	private final static String xpathGetFeature = "//*[local-name()='OperationsMetadata']/*[local-name()='Operation'][@name='GetFeature']/*[local-name()='DCP']/*[local-name()='HTTP']/*[local-name()='Get']";
	private static final String xpathGetRecordByIdResponse = "//*[local-name()='GetRecordByIdResponse']/*[local-name()='MD_Metadata']";
	private static final String xpathValidMetadataDocument = "//*[local-name()='MD_Metadata']";



	private final static Map<FeatureCollectionType, Pair<String, String>> featureCollectionReferenceMap = new HashMap<FeatureCollectionType, Pair<String,String>> ();
	static {
		featureCollectionReferenceMap.put (WFS, new Pair<String, String> (xpathWfs, xpathFeatureType));
		featureCollectionReferenceMap.put (GML, new Pair<String, String> (xpathGmlFeatureCollection, xpathGmlFeatureType));
		featureCollectionReferenceMap.put (XML, new Pair<String, String> (xpathXmlDataset, xpathXmlFeatureType));
	}
	
	private final static String metaDataDateTimeFormat 	= "yyyy-MM-dd'T'HH:mm:ss.SSS";
	private final static String metaDataDateFormat 		= "yyyy-MM-dd";
	
	private final static Properties namespaces = new Properties ();
	static {
		namespaces.setProperty("csw", "http://www.opengis.net/cat/csw/2.0.2");
		namespaces.setProperty("gmd", "http://www.isotc211.org/2005/gmd");
		namespaces.setProperty("gco", "http://www.isotc211.org/2005/gco");
		namespaces.setProperty("ows", "http://www.opengis.net/ows");
	}

	private final String pgrBaseUrl;
	
	public MetadataHarvester (final String pgrBaseUrl) {
		this.pgrBaseUrl = pgrBaseUrl;
	}
	
	public DatasetMetadata parseMetadata (final String uuid) throws HarvesterException {
		if (uuid != null && (uuid.startsWith ("http://")||uuid.startsWith ("https://"))) {
			if (uuid.contains (";")) {
				return parseStaticGmlMetadata (uuid);
			} else {
				final HttpGetUtil metadataGetUtil = new HttpGetUtil (uuid);
				return parseMetadataFromUrl (uuid, metadataGetUtil);
			} 
		} else {
			final String url = getMetadataUrl (uuid);
			technicalLog.debug("metadata URL to PGR: " + url);
			
			final HttpGetUtil pgrHttpGetUtil = new HttpGetUtil (url);
			
			return parseMetadataFromPgr (uuid, url, pgrHttpGetUtil);
		}
	}
	
	protected GmlMetadata parseStaticGmlMetadata (final String uuid) throws HarvesterException {
		final String[] parts = uuid.split (";");
		
		if (parts.length != 3) {
			throw new HarvesterException (HarvesterMessageKey.METADATA_INVALID_IDENTIFIER, uuid);
		}
		
		return new GmlMetadata (parts[2], parts[0], parts[1]);
	}
	
	protected PgrMetadata parseMetadataFromUrl (final String url, final HttpGetUtil httpGetUtil) throws HarvesterException {
		try {
			final OMElement rootElement;

			if (!httpGetUtil.isValidResponse ()) {
				throw new HarvesterException (HarvesterMessageKey.PGR_HTTP_ERROR, url, String.valueOf (httpGetUtil.getStatusCode ()));
			}
			
			if ((rootElement = httpGetUtil.getEntityOMElement ()) == null) {
				throw new RuntimeException ("Metadata root not found");
			}
			
			technicalLog.debug (" - metadata root: " + url);
			testValidMetadataDocument (url, rootElement);
			
			final Timestamp metadataDate = getMetadataDate (rootElement, url);
			final FeatureCollectionReference featureCollectionReference = getMetadataFeatureCollectionReference (rootElement, url);
			
			if(metadataDate == null || featureCollectionReference == null) {
				return null;
			}

			return new PgrMetadata (url, metadataDate, featureCollectionReference);
		} catch (HarvesterException e) {
			throw e;
		} catch (Exception e) {
			throw new HarvesterException (e, HarvesterMessageKey.PGR_EXCEPTION, url, getExceptionMessage (e));
		} finally {
			httpGetUtil.close ();
		}
	}
	
	protected PgrMetadata parseMetadataFromPgr (final String uuid, final String url, final HttpGetUtil pgrHttpGetUtil) throws HarvesterException {
		try {
			final OMElement rootPGR;
			
			
			try {
				if(!pgrHttpGetUtil.isValidResponse()) {
					throw new HarvesterException (HarvesterMessageKey.PGR_HTTP_ERROR, url, String.valueOf (pgrHttpGetUtil.getStatusCode ()));
				}
				
				rootPGR = pgrHttpGetUtil.getEntityOMElement();
				if (rootPGR == null){
					// We should not get here
					throw new RuntimeException ("Root of PGR not found");
				} else{
					technicalLog.debug(" - root PGR: " + url);
					// Check if correct GetRecordByIdResponse
					testSuccessfulGetRecordByIdResponse (url, rootPGR);
				}
			} catch (HarvesterException e) {
				throw e;
			} catch (Exception e) {
				throw new HarvesterException (e, HarvesterMessageKey.PGR_EXCEPTION, url, getExceptionMessage (e)); 
			}
			
			final Timestamp metadataDate = getMetadataDate(rootPGR, url);
			final FeatureCollectionReference featureCollectionReference = getMetadataFeatureCollectionReference (rootPGR, url);
			
			if(metadataDate == null || featureCollectionReference == null){
				return null;
			}
			
			return new PgrMetadata (url, metadataDate, featureCollectionReference);

		} finally {
			pgrHttpGetUtil.close();
		}
	}
	
	private String getExceptionMessage (final Exception exception) {
		if (exception.getLocalizedMessage () != null) {
			return exception.getLocalizedMessage ();
		} else {
			return exception.toString ();
		}
	}
	

	
	public String getMetadataUrl (final String uuid) {
		if (uuid.startsWith ("http://")) {
			if (uuid.contains (";")) {
				return null;
			} else {
				return uuid;
			}
		}
		
		// get metadata from PGR 
		try {
			return pgrBaseUrl + java.net.URLEncoder.encode(uuid, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			technicalLog.warn ("Unable to URLencode uuid: " + uuid);
			return pgrBaseUrl + uuid;
		}
	}
	
	private void testValidMetadataDocument (final String url, final OMElement rootElement) throws HarvesterException {
		testXPath (url, rootElement, xpathValidMetadataDocument);
	}
	
	private void testSuccessfulGetRecordByIdResponse (final String url, final OMElement rootPGR) throws HarvesterException {
		testXPath (url, rootPGR, xpathGetRecordByIdResponse);
	}
	
	private void testXPath (final String url, final OMElement rootElement, final String xpath) throws HarvesterException {

		final String queryXPathString = xpath;
		AXIOMXPath queryXPath;
		OMElement queryElement = null;
		try {
			queryXPath = new AXIOMXPath(queryXPathString);
			queryXPath.addNamespace("csw", "http://www.opengis.net/cat/csw/2.0.2");
			queryXPath.addNamespace("gmd", "http://www.isotc211.org/2005/gmd");
			queryXPath.addNamespace("gco", "http://www.isotc211.org/2005/gco");
			queryXPath.addNamespace("ows", "http://www.opengis.net/ows");
				
			queryElement = (OMElement)queryXPath.selectSingleNode(rootElement);
			if(queryElement == null){
				throw new HarvesterException (HarvesterMessageKey.METADATA_NOT_FOUND, url);
			}
		} catch (JaxenException e) {
			throw new HarvesterException (e, HarvesterMessageKey.METADATA_NOT_FOUND_ERROR, url, getExceptionMessage (e));
		}
	}
	
	/**
	 * Get the date from metadata.<br>
	 * It handles cases:<br>
	 * <code>&lt;gco:DateTime&gt;2012-05-15T00:00:00.000&lt;/gco:DateTime&gt;</code><br>
	 * and<br>
	 * <code>&lt;gco:Date&gt;2009-03-06&lt;/gco:Date&gt;</code>
	 * @param rootPGR the metadata
	 * @param url used to retrieve the date from
	 * @return Timestamp containing the date
	 */
	private Timestamp getMetadataDate (final OMElement rootPGR, final String url) throws HarvesterException {
		// get metadata date
		String xpathRevision = "";
		String xpathCreation = "";
		
		OMElement dateElement = null;

		try {
			xpathRevision = xpathRevisionDateTime;
			dateElement = AxiomUtils.getOMElementWithXPath(rootPGR, xpathRevision, namespaces);
			if (dateElement == null) {
				xpathRevision = xpathRevisionDate;
				dateElement = AxiomUtils.getOMElementWithXPath(rootPGR, xpathRevision, namespaces);
				if (dateElement == null) {
					xpathCreation = xpathCreationDateTime;
					dateElement = AxiomUtils.getOMElementWithXPath(rootPGR, xpathCreation, namespaces);
					if (dateElement == null) {
						xpathCreation = xpathCreationDate;
						dateElement = AxiomUtils.getOMElementWithXPath(rootPGR, xpathCreation, namespaces);
						if (dateElement == null) {
							throw new HarvesterException(
									HarvesterMessageKey.METADATA_DATE, url, xpathCreationDateOrRevisionDate, "");
						} else {
							technicalLog.debug(" - creation date: "
									+ dateElement.getText());
						}
					}
				} else {
					technicalLog.debug(" - revision date: "
							+ dateElement.getText());
				}
			}
		} catch (Exception e) {
			throw new HarvesterException(e, HarvesterMessageKey.METADATA_DATE,
					url, xpathCreationDateTimeOrRevisionDateTime,
					getExceptionMessage(e));
		}

		return parseMetadataDate(dateElement, xpathCreation.isEmpty() ? xpathRevision : xpathCreation, url);
	}
	
	/**
	 * Check the date in dateElement against a certain pattern
	 * @param dateElement element containing the date string
	 * @param xpathRevisionDateTime 
	 * @param url the date is retrieved from
	 * @return Timestamp containing the date.
	 * @throws HarvesterException 
	 */
	private Timestamp parseMetadataDate (final OMElement dateElement, final String xpathDate, final String url) throws HarvesterException {
		// parse date string into timestamp object 
		Timestamp metadataUpdateDatum = null;
		try {
			metadataUpdateDatum = new Timestamp(DateTimeUtils.parseDate(dateElement.getText(), metaDataDateTimeFormat));
		} catch (Exception e1) {
			try {
				metadataUpdateDatum = new Timestamp(DateTimeUtils.parseDate(dateElement.getText(), metaDataDateFormat));
			} catch (Exception e2) {
				throw new HarvesterException (e2, HarvesterMessageKey.METADATA_DATEFORMAT, url, xpathDate, getExceptionMessage (e2), metaDataDateFormat);
			}
		}
		return metadataUpdateDatum;
	}	
	
	/**
	 * Get a Wfs url form metadata.
	 * @param rootPGR the metadata
	 * @param url used to retrieve WFS url from
	 * @return String containing a Wfs Url
	 * @throws HarvesterException 
	 */
	private FeatureCollectionReference getMetadataFeatureCollectionReference (final OMElement rootElement, final String url) throws HarvesterException {
		for (final Map.Entry<FeatureCollectionType, Pair<String, String>> entry: featureCollectionReferenceMap.entrySet ()) {
			final String xpathFeatureCollectionUrl = entry.getValue ().first;
			final String xpathFeatureTypeName = entry.getValue ().second;
			
			final OMElement featureCollectionUrlElement;
			final OMElement featureTypeNameElement;
			
			try {
				 featureCollectionUrlElement= AxiomUtils.getOMElementWithXPath (rootElement, xpathFeatureCollectionUrl, namespaces);
			} catch (Exception e) {
				throw new HarvesterException (e, HarvesterMessageKey.METADATA_WFSURL, url, xpathFeatureCollectionUrl, getExceptionMessage (e));
			}
			
			try {
				featureTypeNameElement = AxiomUtils.getOMElementWithXPath (rootElement, xpathFeatureTypeName, namespaces);
			} catch (Exception e) {
				throw new HarvesterException (e, HarvesterMessageKey.METADATA_FEATURETYPE, url, xpathFeatureTypeName, getExceptionMessage (e));
			}
			
				
			if (featureCollectionUrlElement == null) {
				continue;
			}else{
				String fcUrl = featureCollectionUrlElement.getText().trim();
				if (fcUrl.indexOf("?")>0){
					fcUrl = fcUrl.substring(0, fcUrl.indexOf("?"));
				}
				if (!fcUrl.endsWith("?") && fcUrl.indexOf("?")>0){
					throw new HarvesterException (HarvesterMessageKey.METADATA_WFSURL, url, xpathFeatureCollectionUrl, "Url not correct: [" + fcUrl+"]");
				}
			}
				
			if (featureTypeNameElement == null){
				throw new HarvesterException (HarvesterMessageKey.METADATA_FEATURETYPE, url, xpathFeatureTypeName, "featureTypeNameElement not found");
			}
			
			technicalLog.debug (" - feature collection url: [" + featureCollectionUrlElement.getText ()+"]");
			technicalLog.debug (" - feature type name     : [" + featureTypeNameElement.getText ()+"]");

			//expect an W3C:XSD url if xpathFeatureCollectionUrl is OGC:GML or W3C:XML 		
			String xsdUrl = null;
			if (xpathFeatureCollectionUrl.equals(xpathGmlFeatureCollection) || xpathFeatureCollectionUrl.equals(xpathXmlDataset)) {
				final OMElement xsdLocationElement;
				try {		
					xsdLocationElement= AxiomUtils.getOMElementWithXPath (rootElement, xpathXsdUrl, namespaces);
					if (xsdLocationElement != null){
						xsdUrl = xsdLocationElement.getText();
					}
				} catch (Exception e) {
					System.err.println("XSD error: " + getExceptionMessage (e));
					throw new HarvesterException (e, HarvesterMessageKey.METADATA_XSDURL, url, xpathXsdUrl, getExceptionMessage (e));
				}
			}		
			
			technicalLog.debug (" - feature xsd url       : [" + xsdUrl+"]");
				
			return new FeatureCollectionReference (entry.getKey (), featureCollectionUrlElement.getText ().trim(), featureTypeNameElement == null ? null : featureTypeNameElement.getText (), xsdUrl);
		}
		
		return null;
	}
	
	/**
	 * Request capabilities from a wfs.
	 * @param wfsUrl String containing wfs url
	 * @return OMElement containing the getcapabilities document
	 */
	public String getFeatureCollectionUrl (final DatasetMetadata metadata) throws HarvesterException {

		if (!(metadata instanceof PgrMetadata)) {
			return metadata.getFeatureCollectionUrl ();
		} 
		
		final PgrMetadata pgrMetadata = (PgrMetadata)metadata;
		
		FeatureCollectionType dataType = pgrMetadata.getFeatureCollectionReference ().type;
		if (dataType == GML || dataType == XML) {
			return pgrMetadata.getFeatureCollectionReference ().url;
		}
		
		OMElement urlElement = null;
		
		String capabilitiesUrl = createWfsGetCapabilitiesUrl(metadata.getFeatureCollectionUrl ());
		
		technicalLog.debug("GetCapabilitiesUrl: " + capabilitiesUrl);
		HttpGetUtil capabilitiesHttpGetUtil = null;
		try {
			OMElement rootWFS = null;
			try {
				capabilitiesHttpGetUtil = new HttpGetUtil(capabilitiesUrl);
				if (!capabilitiesHttpGetUtil.isValidResponse()) {
					throw new HarvesterException (HarvesterMessageKey.METADATA_CAPABILITIES_HTTP_ERROR, capabilitiesUrl, ""+capabilitiesHttpGetUtil.getStatusCode());
				}
				rootWFS = capabilitiesHttpGetUtil.getEntityOMElement();
				if(rootWFS == null){
					// We should not get here
					throw new RuntimeException("Not a valid GetCapabilitiesResponse");
				}
			} catch (Exception e) {
				throw new HarvesterException (e, HarvesterMessageKey.METADATA_CAPABILITIES_EXCEPTION, capabilitiesUrl, getExceptionMessage (e));
			}
			try {
				urlElement = AxiomUtils.getOMElementWithXPath(rootWFS, xpathGetFeature, namespaces);
				if (urlElement==null){
					throw new RuntimeException("Url to GetFeature operation not found in capabilities-response document");
				} else {
					technicalLog.debug(" - wfs url href: " + urlElement.getAttributeValue(new QName("http://www.w3.org/1999/xlink","href")));
				}
			} catch (Exception e) {
				throw new HarvesterException (e, HarvesterMessageKey.METADATA_CAPABILITIES_WFSURL, capabilitiesUrl, xpathGetFeature, getExceptionMessage (e));
			}
		} finally{
			if(capabilitiesHttpGetUtil != null) {
				capabilitiesHttpGetUtil.close();
			}
		}
		return urlElement == null ? null : urlElement.getAttributeValue(new QName("http://www.w3.org/1999/xlink","href"));
	}

	protected String createWfsGetCapabilitiesUrl(final String wfsUrl) {
		// some url's end with '?service=wfs', others do not
		String separator;
		String capabilitiesUrl = wfsUrl;
		int qmarkIndex = wfsUrl.indexOf("?");
		boolean hasService = false, hasRequest = false;
		if(qmarkIndex != -1) {
			String query = wfsUrl.substring(qmarkIndex + 1).trim();
			if(!query.isEmpty()) {
				String[] parameters = query.split("&");
				for(String parameter : parameters) {
					String[] parameterSplit = parameter.split("=");
					if(parameterSplit.length == 2) {					
						try {
							String key = URLDecoder.decode(parameterSplit[0], "utf-8").toLowerCase();
							String value = URLDecoder.decode(parameterSplit[1], "utf-8");
							
							hasService |= 
								"service".equals(key) 
								&& value != null 
								&& value.toLowerCase().equals("wfs");
							
							hasRequest |= 
								"request".equals(key)
								&& "GetCapabilities".equals(value);
							
						} catch (UnsupportedEncodingException e) {}
					}
				}
				
				separator = "&";
			} else {
				separator = "";
			}
		} else {
			separator = "?";
		}
		
		if(!hasService) {
			capabilitiesUrl += separator + "service=WFS";
			separator = "&";
		}
		
		if(!hasRequest) {
			capabilitiesUrl += separator + "request=GetCapabilities";
		}
		return capabilitiesUrl;
	}
}
