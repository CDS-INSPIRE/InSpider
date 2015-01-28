package nl.ipo.cds.etl.postvalidation;

import com.vividsolutions.jts.io.ParseException;
import nl.ipo.cds.etl.PersistableFeature;
import org.deegree.geometry.Geometry;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;

/**
 * Geometry Store to temporarily store possible overlapping geometries.
 */
public interface IGeometryStore<T extends Serializable> {

    public DataSource createStore(final String uuId) throws SQLException;

    public DataSource loadStore(final String uuId) throws SQLException;

    public void addToStore(final DataSource dataSource, Geometry geometry, T feature) throws SQLException, ParseException, IOException;

    public void destroyStore(final DataSource dataSource);

}
