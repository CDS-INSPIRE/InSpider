package nl.ipo.cds.deegree.persistence;

import static nl.ipo.cds.deegree.persistence.ExtendedSimpleSQLFeatureStoreProvider.CONFIG_SCHEMA;

import javax.xml.bind.JAXBException;

import nl.ipo.cds.deegree.persistence.jaxb.ExtendedSimpleSQLFeatureStoreConfig;

import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.db.ConnectionProvider;
import org.deegree.db.ConnectionProviderProvider;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.AbstractResourceMetadata;
import org.deegree.workspace.standard.AbstractResourceProvider;
import org.deegree.workspace.standard.DefaultResourceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtendedSimpleSQLFeatureStoreMetadata extends AbstractResourceMetadata<FeatureStore> {

    private static final String CONFIG_JAXB_PACKAGE = "nl.ipo.cds.deegree.persistence.jaxb";

    private static final Logger LOG = LoggerFactory.getLogger( ExtendedSimpleSQLFeatureStoreMetadata.class );

    public ExtendedSimpleSQLFeatureStoreMetadata( Workspace workspace, ResourceLocation<FeatureStore> location,
                                                  AbstractResourceProvider<FeatureStore> provider ) {
        super( workspace, location, provider );
    }

    @Override
    public ResourceBuilder<FeatureStore> prepare() {
        try {
            ExtendedSimpleSQLFeatureStoreConfig config = (ExtendedSimpleSQLFeatureStoreConfig) JAXBUtils.unmarshall( CONFIG_JAXB_PACKAGE,
                                                                                                                     CONFIG_SCHEMA,
                                                                                                                     location.getAsStream(),
                                                                                                                     workspace );
            String connId = config.getConnectionPoolId();
            if ( connId == null ) {
                connId = config.getJDBCConnId();
            }

            dependencies.add( new DefaultResourceIdentifier<ConnectionProvider>( ConnectionProviderProvider.class,
                                                                                 connId ) );

            return new ExtendedSimpleSQLFeatureStoreBuilder( workspace, this, config );
        } catch ( JAXBException e ) {
            String msg = "Error in feature store configuration file '" + location + "': " + e.getMessage();

            LOG.error( msg );
            throw new ResourceInitException( msg, e );
        }
    }
}
