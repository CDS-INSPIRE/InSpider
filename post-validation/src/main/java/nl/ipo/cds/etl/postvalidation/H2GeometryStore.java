package nl.ipo.cds.etl.postvalidation;

import com.vividsolutions.jts.io.ParseException;
import geodb.GeoDB;
import nl.ipo.cds.etl.PersistableFeature;
import nl.ipo.cds.etl.theme.protectedSite.ProtectedSite;
import org.apache.commons.dbcp.BasicDataSource;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.WKBWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * H2 Geometry Store implementation.
 */
@Service
public class H2GeometryStore implements IGeometryStore {


    @Value("bulkValidator.jdbcUrlFormat")
    private String JDBC_URL_FORMAT;





    @Override
    public DataSource createStore(final String uuId) throws SQLException {
        DataSource dataSource = loadStore(uuId);
        GeoDB.InitGeoDB(dataSource.getConnection());
        JdbcTemplate t = new JdbcTemplate(dataSource);
        t.execute("CREATE TABLE geometries (id INT AUTO_INCREMENT PRIMARY KEY, geometry BLOB, feature TEXT);");
        return dataSource;
    }

    @Override
    public DataSource loadStore(final String uuId) throws SQLException {
        BasicDataSource d = new BasicDataSource();
        d.setUrl(String.format(JDBC_URL_FORMAT, uuId));

        // Initialize the DataSource.
        d.getConnection();
        return d;
    }

    @Override
    public void addToStore(final DataSource dataSource, final Geometry geometry, final PersistableFeature feature) throws SQLException {
        final NamedParameterJdbcTemplate t = new NamedParameterJdbcTemplate(dataSource);
        final String insertStatement = "INSERT INTO geometries (geometry, feature) VALUES (:geometry, :feature)";
        final Map<String, byte[]> params = new HashMap<String, byte[]>();
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();

        // srid=28992
        try {
            final JAXBContext jaxbContext = JAXBContext.newInstance(feature.getClass());
            final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.marshal(feature, bos);

            params.put("geometry", GeoDB.ST_GeomFromWKB(WKBWriter.write(geometry), 28992)); //geometry.getCoordinateSystem().)
            params.put("feature", bos.toByteArray());
            t.update(insertStatement, params);


        } catch (ParseException | JAXBException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void destroyStore(final DataSource dataSource) throws SQLException {
        Connection conn = dataSource.getConnection();
        final String connectString = conn.getMetaData().getURL();
        final File connectPath = new File(connectString.substring(8));


        // Delete all objects, and delete the file when all connections close.
        JdbcTemplate t = new JdbcTemplate(dataSource);
        t.execute("DROP ALL OBJECTS DELETE FILES;");

        // Close all possible connections that are still on the store.
        // The BasicDataSource actually provides a close() which closes all connections in the pool.
        // However, we do not want to only accept a BasicDataSource, also other data sources.
        // So in this case other connections might still be open. We assume this is managed by the caller.
        dataSource.getConnection().close();

    }
}
