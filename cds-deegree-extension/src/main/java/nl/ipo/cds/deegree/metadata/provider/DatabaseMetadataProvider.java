package nl.ipo.cds.deegree.metadata.provider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import nl.ipo.cds.dao.metadata.MetadataDao;
import nl.ipo.cds.domain.metadata.ExtendedCapabilities;
import nl.ipo.cds.domain.metadata.Keyword;
import nl.ipo.cds.domain.metadata.SpatialDataSetIdentifier;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deegree.commons.ows.metadata.DatasetMetadata;
import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.ServiceProvider;
import org.deegree.commons.ows.metadata.party.Address;
import org.deegree.commons.ows.metadata.party.ContactInfo;
import org.deegree.commons.ows.metadata.party.ResponsibleParty;
import org.deegree.commons.ows.metadata.party.Telephone;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.StringPair;
import org.deegree.services.metadata.OWSMetadataProvider;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceMetadata;
import org.jaxen.JaxenException;

class DatabaseMetadataProvider implements OWSMetadataProvider {

	protected static final String NS_INSPIRE_COMMON = "http://inspire.ec.europa.eu/schemas/common/1.0";
	protected static final String NS_INSPIRE_DLS = "http://inspire.ec.europa.eu/schemas/inspire_dls/1.0";

	private static final Log technicalLog = LogFactory.getLog(DatabaseMetadataProvider.class);

	private final MetadataDao metadataDao;

	private final Map<String, List<OMElement>> protocolVersionToExtendedCapabilitiesTemplates;

	private final DatabaseMetadataProviderMetadata resourceMetadata;

	private final String serviceId;

	private final Map<String, String> externalMetadataAuthorities;

	DatabaseMetadataProvider(final MetadataDao metadataDao, final Map<String, List<OMElement>> protocolVersionToExtendedCapabilitiesTemplates, final DatabaseMetadataProviderMetadata resourceMetadata) {
		this.metadataDao = metadataDao;
		this.resourceMetadata = resourceMetadata;
		this.protocolVersionToExtendedCapabilitiesTemplates = protocolVersionToExtendedCapabilitiesTemplates;
		Map<String, String> externalMetadataAuthorities = new HashMap<String, String>();
		externalMetadataAuthorities.put("NL.IPO", "http://www.ipo.nl");
		this.externalMetadataAuthorities = Collections.unmodifiableMap(externalMetadataAuthorities);
		String mdId = resourceMetadata.getIdentifier().getId();
		this.serviceId = mdId.substring(mdId.lastIndexOf("/") +1, mdId.lastIndexOf("_"));
		technicalLog.info("DatabaseMetadataProvider: Init (serviceName: " + serviceId + ")");
	}

	@Override
	public void destroy() {
	}

	@Override
	public void init() {

	}

	/**
	 * Get the ServiceIdentification via the metadataDao. This result in a ServiceIdentification in
	 * the cds-inspire-domain. Convert this to the deegree-metadata-domain
	 */
	@Override
	public ServiceIdentification getServiceIdentification() {
		technicalLog.debug("DatabaseMetadataProvider: getServiceIdentification");

		nl.ipo.cds.domain.metadata.ServiceIdentification serviceIdentification = this.metadataDao.findService(this.serviceId).getServiceIdentification();

		List<LanguageString> titles = Arrays.asList(new LanguageString[]{new LanguageString(serviceIdentification.getTitle(), null)});
		List<LanguageString> abstracts = Arrays.asList(new LanguageString[]{new LanguageString(serviceIdentification.getAbstract(), null)});

		List<Pair<List<LanguageString>, CodeType>> keywords = new ArrayList<Pair<List<LanguageString>, CodeType>>();
		for (Iterator<Keyword> iterator = serviceIdentification.getKeywords().iterator(); iterator.hasNext();) {
			Keyword keyword = iterator.next();
			Pair<List<LanguageString>, CodeType> deegreeKeword = new Pair<List<LanguageString>, CodeType>(Arrays.asList(new LanguageString[]{new LanguageString(keyword.getValue(), null)}), new CodeType(keyword.getValue(), keyword.getCodeSpace()));
			keywords.add(deegreeKeword);
		}

		List<Version> serviceTypeVersions = null;//Arrays.asList(new Version[]{new Version(1, 2, 3)});

		ServiceIdentification deegreeServiceIdentification = new ServiceIdentification(
				serviceIdentification.getServiceType(),
				titles,
				abstracts,
				keywords,
				new CodeType(serviceIdentification.getServiceType()),
				serviceTypeVersions,
				new ArrayList<String>(),
				serviceIdentification.getFees(),
				serviceIdentification.getAccessContraints());

		return deegreeServiceIdentification;
	}

	/**
	 * Extract the serviceName from the url to the metadata of this service
	 * The convention is that a metadata-service-name is like:
	 * 		http://url-to-deegree-workspace/<metadata-service-name>_metadata.xml
	 *
	 * @param url
	 * @return
	 */
	protected String retrieveServiceName(URL url) {
		technicalLog.debug("DatabaseMetadataProvider: retrieveServiceName " + url);

		String path = url.getPath();
		return path.substring(path.lastIndexOf("/") +1, path.indexOf("_"));
	}

	/**
	 * Get the ServiceProvider via the metadataDao. This result in a ServiceProvider in
	 * the cds-inspire-domain. Convert this to the deegree-metadata-domain
	 */
	@Override
	public ServiceProvider getServiceProvider() {
		technicalLog.debug("DatabaseMetadataProvider: getServiceProvider ");

		nl.ipo.cds.domain.metadata.ServiceProvider serviceProvider = this.metadataDao.findService(this.serviceId).getServiceProvider();

		// Address
		Address deegreeAddress = new Address();
		deegreeAddress.setAdministrativeArea(serviceProvider.getAdministrativeArea());
		deegreeAddress.setCity(serviceProvider.getCity());
		deegreeAddress.setCountry(serviceProvider.getCountry());
		deegreeAddress.setDeliveryPoint(serviceProvider.getDeliveryPoints());
		deegreeAddress.setElectronicMailAddress(serviceProvider.getEmailAddresses());
		deegreeAddress.setPostalCode(serviceProvider.getPostalCode());

		// ContactInfo
		ContactInfo deegreeContactInfo = new ContactInfo();
		deegreeContactInfo.setAddress(deegreeAddress);
		deegreeContactInfo.setContactInstructions(serviceProvider.getContactInstructions());
		deegreeContactInfo.setHoursOfService(serviceProvider.getHoursOfService());
		if(StringUtils.isNotBlank(serviceProvider.getOnlineResource()))
			try {
				deegreeContactInfo.setOnlineResource(new URL(serviceProvider.getOnlineResource()));
			} catch (MalformedURLException mue) {
				technicalLog.error("OnlineResource is not a valid URL", mue);
			}
		Telephone deegreePhone = new Telephone();
		deegreePhone.setFacsimile(serviceProvider.getFaxNumbers());
		deegreePhone.setVoice(serviceProvider.getPhoneNumbers());
		deegreeContactInfo.setPhone(deegreePhone );

		// ServiceContact
		ResponsibleParty deegreeServiceContact = new ResponsibleParty();
		deegreeServiceContact.setContactInfo(deegreeContactInfo);
		deegreeServiceContact.setIndividualName(serviceProvider.getIndividualName());
		deegreeServiceContact.setOrganizationName(serviceProvider.getOrganizationName());
		deegreeServiceContact.setPositionName(serviceProvider.getPositionName());
		if(StringUtils.isNotBlank(serviceProvider.getRole())){
			CodeType deegreeRole = new CodeType(serviceProvider.getRole());
			deegreeServiceContact.setRole(deegreeRole);
		}

		// ServiceProvider
		ServiceProvider deegreeServiceProvider = new ServiceProvider(serviceProvider.getProviderName(), serviceProvider.getProviderSite(), deegreeServiceContact);
		return deegreeServiceProvider;
	}
	
	private void replaceText(OMElement doc, String path, String replacementText) throws JaxenException {
		if(replacementText != null) {
			AXIOMXPath xpath = new AXIOMXPath(path);
			xpath.addNamespace("inspire_common", NS_INSPIRE_COMMON);
			xpath.addNamespace("inspire_dls", NS_INSPIRE_DLS);
			OMElement element = (OMElement)xpath.selectSingleNode(doc);
			
			if(element != null) {
				element.setText(replacementText);
			}
		}
	}

	/**
	 * Creating the ExtendedCapabilities is done, by retrieving the ExtendedCapabilities templates
	 * and replacing the values of elements <inspire_common:URL> and <inspire_dls:SpatialDataSetIdentifier> 
	 * with the content from the database.
	 */
	@Override
	public Map<String, List<OMElement>> getExtendedCapabilities() {
		technicalLog.debug("DatabaseMetadataProvider: getExtendedCapabilities for " + serviceId);
		
		ExtendedCapabilities databaseExtendedCapabilities = metadataDao.findService(this.serviceId).getExtendedCapabilities();
		if(databaseExtendedCapabilities == null) {
			technicalLog.warn("Database doesn't contain extended capabilities for " + serviceId);
		}
		
		Map<String, List<OMElement>> extendedCapabilities = new HashMap<String, List<OMElement>>();
		for (Entry<String,List<OMElement>> versionToTemplate : protocolVersionToExtendedCapabilitiesTemplates.entrySet()) {
			String version = versionToTemplate.getKey();
			List<OMElement> extendedCapabilitiesElements = new ArrayList<OMElement>();
			extendedCapabilities.put(version, extendedCapabilitiesElements);
			for (OMElement templateEl : versionToTemplate.getValue()) {
				try {
					OMElement extCapElement = templateEl.cloneOMElement();
					
					if(databaseExtendedCapabilities != null) {						
						replaceText(extCapElement, "//inspire_common:MetadataUrl/inspire_common:URL", databaseExtendedCapabilities.getMetadataUrl());
						
						SpatialDataSetIdentifier spatialDataSetIdentifier = databaseExtendedCapabilities.getSpatialDataSetIdentifier();
						if(spatialDataSetIdentifier != null) {
							replaceText(extCapElement, "//inspire_dls:SpatialDataSetIdentifier/inspire_common:Code", spatialDataSetIdentifier.getCode());
							replaceText(extCapElement, "//inspire_dls:SpatialDataSetIdentifier/inspire_common:Namespace", spatialDataSetIdentifier.getNamespace());							
						} else {
							technicalLog.warn("spatialDataSetIdentifier is missing");
						}
					}
					
					extendedCapabilitiesElements.add(extCapElement);
				} catch (Exception e) {
					String msg = "Error creating ExtendedCapabilities from template: " + e.getMessage();
					technicalLog.error(msg, e);
					throw new RuntimeException(msg, e);
				}
			}
		}
		return extendedCapabilities;
	}	

	@Override
	public List<DatasetMetadata> getDatasetMetadata() {
		technicalLog.debug("DatabaseMetadataProvider: getDatasetMetadata ");
		// source
		return new ArrayList<DatasetMetadata>(createDeegreeDatasetMetadatas().values());
	}

	/**
	 * Utility-method to build up the complete map with DatasetMetadata objects.
	 * DatasetMetadata-objects are used to print the MetadataURL/OnlineResource/@xlink:href xml-attribute in
	 * the GetCapabilities response
	 * @return
	 */
	private Map<QName, DatasetMetadata> createDeegreeDatasetMetadatas() {
		technicalLog.debug("DatabaseMetadataProvider: createDeegreeDatasetMetadatas ");

		List<nl.ipo.cds.domain.metadata.DatasetMetadata> datasetMetadatas = this.metadataDao.findService(this.serviceId).getDatasetMetadatas();

		//target
		Map<QName, DatasetMetadata> deegreeDatasetMetadatas = new HashMap<QName, DatasetMetadata>(datasetMetadatas.size());

		final List<StringPair> ipoId = Arrays.asList(new StringPair("NL.IPO", "ab22c651-6b55-11e0-ae3e-0800200c9a66")) ;
		for (Iterator<nl.ipo.cds.domain.metadata.DatasetMetadata> iterator = datasetMetadatas.iterator(); iterator
				.hasNext();) {
			nl.ipo.cds.domain.metadata.DatasetMetadata datasetMetadata = iterator.next();
			DatasetMetadata deegreeDatasetMetadata = new DatasetMetadata(new QName(datasetMetadata.getNamespace(), datasetMetadata.getName()), null, null, null, datasetMetadata.getUrl(), ipoId);
			deegreeDatasetMetadatas.put(deegreeDatasetMetadata.getQName(), deegreeDatasetMetadata);
		}
		return deegreeDatasetMetadatas;
	}

	@Override
	public DatasetMetadata getDatasetMetadata(QName qName) {
		return this.createDeegreeDatasetMetadatas().get(qName);
	}

	@Override
	public ResourceMetadata<? extends Resource> getMetadata() {
		return resourceMetadata;
	}

	@Override
	public Map<String, String> getExternalMetadataAuthorities() {
		return externalMetadataAuthorities;
	}
}
