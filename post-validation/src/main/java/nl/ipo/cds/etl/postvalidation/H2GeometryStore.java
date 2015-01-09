package nl.ipo.cds.etl.postvalidation;

import geodb.GeoDB;
import org.deegree.geometry.Geometry;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * H2 Geometry Store implementation.
 */
@Service
public class H2GeometryStore implements IGeometryStore {

    // TODO: Add as configuration property.
    private final String DB_LOCATION = "dbs";

    public void createStore(final String uuId) throws SQLException {
        Connection conn = DriverManager.getConnection(String.format("jdbc:h2:dbs/%s", uuId));
        GeoDB.InitGeoDB(conn);
        conn.close();
    }

    @Override
    public void addToStore(String uuId, Geometry geometry, Serializable feature) {

    }

    @Override
    public void destroyStore(String uuId) {
        File dir = new File(DB_LOCATION);
        for (File f: dir.listFiles()) {
            if (f.getName().startsWith(uuId))
                f.delete();
        }
    }


    // TODO: Add method for executing query on store.
}
