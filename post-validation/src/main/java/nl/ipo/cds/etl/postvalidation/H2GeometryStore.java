package nl.ipo.cds.etl.postvalidation;

import com.vividsolutions.jts.io.ParseException;
import geodb.GeoDB;
import org.apache.commons.dbcp.BasicDataSource;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.WKBWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * H2 Geometry Store implementation.
 */
@Service
public class H2GeometryStore implements IGeometryStore {


    @Value("${bulkValidator.jdbcUrlFormat:jdbc\\:h2\\:%s}")
    private String JDBC_URL_FORMAT;
    
    @Value("${bulkValidator.jdbcDriverClassName:org.h2.Driver}")
	private String driverClassName;





    @Override
    public DataSource createStore(final String uuId) throws SQLException {
        DataSource dataSource = loadStore(uuId);
        GeoDB.InitGeoDB(dataSource.getConnection());
        JdbcTemplate t = new JdbcTemplate(dataSource);
        t.execute("CREATE TABLE geometries (id INT AUTO_INCREMENT PRIMARY KEY, geometry BLOB, feature_identifier VARCHAR, feature_local_id VARCHAR);");
        GeoDB.CreateSpatialIndex(dataSource.getConnection(), "PUBLIC", "GEOMETRIES", "GEOMETRY", "28992");
        return dataSource;
    }

    @Override
    public DataSource loadStore(final String uuId) throws SQLException {
        BasicDataSource d = new BasicDataSource();
        d.setUrl(String.format(JDBC_URL_FORMAT, uuId));
        d.setDriverClassName(driverClassName);
        // Initialize the DataSource.
        d.getConnection();
        return d;
    }

    @Override
    public void addToStore(final DataSource dataSource, final Geometry geometry, String identifier, String localId) throws
            SQLException,
            ParseException, IOException {
        final NamedParameterJdbcTemplate t = new NamedParameterJdbcTemplate(dataSource);
        final String insertStatement = "INSERT INTO geometries (geometry, feature_identifier, feature_local_id) VALUES (:geometry, :identifier, :local_id)";
        final Map<String, Object> params = new HashMap<>();

        // srid=28992
        params.put("geometry", GeoDB.ST_GeomFromWKB(WKBWriter.write(geometry), 28992)); //geometry.getCoordinateSystem().)
        params.put("identifier", identifier);
        params.put("local_id", localId);
        t.update(insertStatement, params);
    }

    @Override
    public void destroyStore(final DataSource dataSource) {
        // Delete all objects, and delete the file when all connections close.
        JdbcTemplate t = new JdbcTemplate(dataSource);

        // This does not work on Windows since the .lobs.db and .trace.db are still in use somehow. Also using SHUTDOWN and deleting the files manually does not work.
        // Production environment is Linux however.
        try {
            t.execute("DROP ALL OBJECTS DELETE FILES;");
            t.execute("SHUTDOWN IMMEDIATELY;");
        } catch (Exception e) {
            System.err.print(e.getMessage());

        }

    }
}
