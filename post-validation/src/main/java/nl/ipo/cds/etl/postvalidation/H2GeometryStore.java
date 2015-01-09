package nl.ipo.cds.etl.postvalidation;

import nl.ipo.cds.etl.PersistableFeature;
import org.springframework.stereotype.Service;

/**
 * H2 Geometry Store implementation.
 */
@Service
public class H2GeometryStore implements IGeometryStore {

    public void createStore(final String uuId) {

    }

    public void addToStore(final String uuId, PersistableFeature feature) {

    }

    // TODO: Add method for executing query on store.
}
