package nl.ipo.cds.deegree.metadata.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.dom.DOMSource;

import nl.ipo.cds.dao.metadata.MetadataDao;
import nl.ipo.cds.deegree.metadata.jaxb.DeegreeServicesMetadata;
import nl.ipo.cds.deegree.metadata.jaxb.ExtendedCapabilitiesType;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.services.metadata.OWSMetadataProvider;
import org.deegree.spring.ApplicationContextHolder;
import org.deegree.spring.ApplicationContextHolderProvider;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.Workspace;

class DatabaseMetadataProviderBuilder implements ResourceBuilder<OWSMetadataProvider> {

	private final DeegreeServicesMetadata config;

	private final DatabaseMetadataProviderMetadata metadata;

	private final Workspace workspace;

	DatabaseMetadataProviderBuilder( DeegreeServicesMetadata md, DatabaseMetadataProviderMetadata metadata, Workspace workspace ) {
		this.config = md;
		this.metadata = metadata;
		this.workspace = workspace;
	}

	@Override
	public DatabaseMetadataProvider build() {
		try {
			Map<String, List<OMElement>> extendedCapabilities = new HashMap<String, List<OMElement>>();
			if ( config.getExtendedCapabilities() != null ) {
				for ( ExtendedCapabilitiesType ex : config.getExtendedCapabilities() ) {
					String version = ex.getProtocolVersions();
					if ( version == null ) {
						version = "default";
					}
					List<OMElement> list = extendedCapabilities.get( version );
					if ( list == null ) {
						list = new ArrayList<OMElement>();
						extendedCapabilities.put( version, list );
					}
					DOMSource domSource = new DOMSource( ex.getAny() );
					XMLStreamReader xmlStream;
					try {
						xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( domSource );
					} catch ( Exception t ) {
						throw new ResourceInitException( "Error extracting extended capabilities: " + t.getMessage(), t );
					}
					list.add( new XMLAdapter( xmlStream ).getRootElement() );
				}
			}
			ApplicationContextHolder contextHolder = workspace.getResource(ApplicationContextHolderProvider.class, config.getApplicationContextHolder());
			MetadataDao metadataDao = contextHolder.getApplicationContext().getBean(MetadataDao.class);
			return new DatabaseMetadataProvider(metadataDao, extendedCapabilities, metadata );
		} catch ( Exception e ) {
			throw new ResourceInitException( "Unable to read service metadata config: " + e.getLocalizedMessage(), e );
		}
	}

}
