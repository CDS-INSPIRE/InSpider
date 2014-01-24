package nl.ipo.cds.deegree.metadata.provider;

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;
import nl.ipo.cds.deegree.metadata.jaxb.DeegreeServicesMetadata;

import org.deegree.services.metadata.OWSMetadataProvider;
import org.deegree.spring.ApplicationContextHolder;
import org.deegree.spring.ApplicationContextHolderProvider;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.AbstractResourceMetadata;
import org.deegree.workspace.standard.AbstractResourceProvider;
import org.deegree.workspace.standard.DefaultResourceIdentifier;

class DatabaseMetadataProviderMetadata extends AbstractResourceMetadata<OWSMetadataProvider> {

	DatabaseMetadataProviderMetadata( Workspace workspace, ResourceLocation<OWSMetadataProvider> location,
			AbstractResourceProvider<OWSMetadataProvider> provider ) {
		super( workspace, location, provider );
	}

	@Override
	public ResourceBuilder<OWSMetadataProvider> prepare() {
		try {
			DeegreeServicesMetadata config = (DeegreeServicesMetadata) unmarshall( "nl.ipo.cds.deegree.metadata.jaxb",
					provider.getSchema(), location.getAsStream(),
					workspace );
			final String applicationContextHolder = config.getApplicationContextHolder();
			dependencies.add( new DefaultResourceIdentifier<ApplicationContextHolder>(
					ApplicationContextHolderProvider.class,
					applicationContextHolder ) );
			return new DatabaseMetadataProviderBuilder( config, this, workspace );
		} catch ( Exception e ) {
			throw new ResourceInitException( "Unable to read service metadata config: " + e.getLocalizedMessage(), e );
		}
	}
}
