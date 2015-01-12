package nl.ipo.cds.etl.postvalidation;

import nl.ipo.cds.etl.PersistableFeature;
import org.deegree.geometry.Geometry;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Geometry Store to temporarily store possible overlapping geometries.
 */
public interface IGeometryStore {

    public DataSource createStore(final String uuId) throws SQLException;

    public DataSource loadStore(final String uuId) throws SQLException;

    public void addToStore(final DataSource dataSource, Geometry geometry, PersistableFeature feature) throws SQLException;

    public void destroyStore(final DataSource dataSource) throws SQLException;

}
