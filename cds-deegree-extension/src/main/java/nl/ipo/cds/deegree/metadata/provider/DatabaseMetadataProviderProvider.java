package nl.ipo.cds.deegree.metadata.provider;

import java.net.URL;

import org.deegree.services.metadata.OWSMetadataProvider;
import org.deegree.services.metadata.provider.OWSMetadataProviderProvider;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;

public class DatabaseMetadataProviderProvider extends OWSMetadataProviderProvider {

	private static final URL CONFIG_SCHEMA = DatabaseMetadataProviderProvider.class.getResource( "/META-INF/schemas/metadatadao/3.4.0/metadatadao.xsd" );

	@Override
	public String getNamespace() {
		return "urn:cds-inspire:deegree-extension:metadata";
	}

	@Override
	public URL getSchema() {
		return CONFIG_SCHEMA;
	}

	@Override
	public ResourceMetadata<OWSMetadataProvider> createFromLocation( Workspace workspace,
			ResourceLocation<OWSMetadataProvider> location ) {
		return new DatabaseMetadataProviderMetadata( workspace, location, this );
	}
}
