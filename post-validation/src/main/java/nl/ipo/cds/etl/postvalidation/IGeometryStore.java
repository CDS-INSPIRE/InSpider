package nl.ipo.cds.etl.postvalidation;

import com.vividsolutions.jts.io.ParseException;
import org.deegree.geometry.Geometry;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Geometry Store to temporarily store possible overlapping geometries.
 */
public interface IGeometryStore {

    DataSource createStore(final String uuId) throws SQLException;

    DataSource loadStore(final String uuId) throws SQLException;

    void addToStore(final DataSource dataSource, Geometry geometry, String identifier, String localId) throws
            SQLException,
            ParseException, IOException;

    void destroyStore(final DataSource dataSource);

}
