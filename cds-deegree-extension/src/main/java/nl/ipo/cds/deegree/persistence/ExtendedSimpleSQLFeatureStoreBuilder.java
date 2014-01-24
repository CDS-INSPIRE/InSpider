package nl.ipo.cds.deegree.persistence;

import java.util.LinkedList;

import nl.ipo.cds.deegree.persistence.jaxb.ExtendedSimpleSQLFeatureStoreConfig;
import nl.ipo.cds.deegree.persistence.jaxb.ExtendedSimpleSQLFeatureStoreConfig.LODStatement;

import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.CollectionUtils.Mapper;
import org.deegree.db.ConnectionProvider;
import org.deegree.db.ConnectionProviderProvider;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.Workspace;

import static org.deegree.commons.utils.CollectionUtils.map;

public class ExtendedSimpleSQLFeatureStoreBuilder implements ResourceBuilder<FeatureStore> {

    private static Mapper<Pair<Integer, String>, LODStatement> lodMapper = new Mapper<Pair<Integer, String>, LODStatement>() {
        public Pair<Integer, String> apply( LODStatement u ) {
            return new Pair<Integer, String>( u.getAboveScale(), u.getValue() );
        }
    };

    private final Workspace workspace;

    private final ExtendedSimpleSQLFeatureStoreConfig config;

    private final ExtendedSimpleSQLFeatureStoreMetadata metadata;

    public ExtendedSimpleSQLFeatureStoreBuilder( Workspace workspace, ExtendedSimpleSQLFeatureStoreMetadata metadata,
                                                 ExtendedSimpleSQLFeatureStoreConfig config ) {
        this.workspace = workspace;
        this.metadata = metadata;
        this.config = config;
    }

    @Override
    public FeatureStore build() {
        String srs = config.getStorageCRS();
        String stmt = config.getSQLStatement();
        String name = config.getFeatureTypeName();
        String ns = config.getFeatureTypeNamespace();
        String prefix = config.getFeatureTypePrefix();
        String bbox = config.getBBoxStatement();
        LinkedList<Pair<Integer, String>> lods = map( config.getLODStatement(), lodMapper );

        String connId = config.getConnectionPoolId();
        if ( connId == null ) {
            connId = config.getJDBCConnId();
        }

        ConnectionProvider connProvider = workspace.getResource( ConnectionProviderProvider.class, connId );
        return new ExtendedSimpleSQLFeatureStore( metadata, connProvider, srs, stmt, name, ns, prefix, bbox, lods );
    }
}
