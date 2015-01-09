package nl.ipo.cds.etl.postvalidation;

import org.deegree.geometry.Geometry;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * H2 Geometry Store implementation.
 */
@Service
public class H2GeometryStore implements IGeometryStore {

    public void createStore(final String uuId) {

    }

    @Override
    public void addToStore(String uuId, Geometry geometry, Serializable feature) {

    }


    // TODO: Add method for executing query on store.
}
