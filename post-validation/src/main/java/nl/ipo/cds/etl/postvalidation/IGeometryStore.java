package nl.ipo.cds.etl.postvalidation;

import nl.ipo.cds.etl.PersistableFeature;

/**
 * Geometry Store to temporarily store possible overlapping geometries.
 */
public interface IGeometryStore {

    public void createStore(final String uuId);

    public void addToStore(final String uuId, PersistableFeature feature);
}
