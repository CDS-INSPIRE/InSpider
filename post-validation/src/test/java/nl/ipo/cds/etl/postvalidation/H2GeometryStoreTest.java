package nl.ipo.cds.etl.postvalidation;

import geodb.GeoDB;
import nl.ipo.cds.etl.PersistableFeature;
import nl.ipo.cds.etl.theme.protectedSite.ProtectedSite;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.WKTReader;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.standard.primitive.DefaultPolygon;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import javax.sql.DataSource;
import javax.sql.RowSet;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class H2GeometryStoreTest {

    private H2GeometryStore h2GeometryStore;
    private BasicDataSource dataSource;
    private final static String DB_NAME = "test-db-1337";

    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        h2GeometryStore = new H2GeometryStore();
        Field field = H2GeometryStore.class.getDeclaredField("JDBC_URL_FORMAT");
        field.setAccessible(true);
        field.set(h2GeometryStore, String.format("jdbc:h2:%s/", testFolder.getRoot()) + "%s");
        dataSource = (BasicDataSource)h2GeometryStore.createStore(DB_NAME);

    }

    @After
    public void tearDown() throws Exception {
        h2GeometryStore.destroyStore(dataSource);
    }




    @Test
    public void testCreateStore() throws Exception {
        String dbName = "test";
        DataSource d = h2GeometryStore.createStore(dbName);
        assertTrue(Files.exists(Paths.get(testFolder.getRoot().getPath(), dbName + ".data.db")));
        assertTrue(Files.exists(Paths.get(testFolder.getRoot().getPath(), dbName + ".index.db")));
        h2GeometryStore.destroyStore(d);

    }

    @Test
    public void testAddToStore() throws Exception {
        WKTReader reader = new WKTReader(null);
        Geometry g = (Polygon) reader.read("SRID=28992;POLYGON((111446.5 566602,112035.5 566602,112035.5 566886,111446.5 566886,111446.5 566602))");
        ProtectedSite ps = new ProtectedSite();
        ps.setGeometry(g);
        ps.setId("test-feature");

        h2GeometryStore.addToStore(dataSource, g, ps );

        JdbcTemplate t = new JdbcTemplate(dataSource);
        Geometry g2 = (Polygon) reader.read(GeoDB.ST_AsText(t.queryForObject("SELECT geometry FROM geometries LIMIT 1", byte[].class)));


        ByteArrayInputStream bis = new ByteArrayInputStream(t.queryForObject("SELECT feature FROM geometries LIMIT 1", byte[].class));

        JAXBContext jaxbContext = JAXBContext.newInstance(ProtectedSite.class);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        ProtectedSite ps2 = (ProtectedSite) jaxbUnmarshaller.unmarshal(bis);
        System.out.print(ps2);



    }

    @Test
    public void testDestroyStore() throws Exception {
        h2GeometryStore.destroyStore(dataSource);
        // TODO: Add proper checking. Problem is that the H2 database files are only removed after the last connection closes.
        // This cleanup apparently occurs after this test code is run.
        //assertFalse(Files.exists(Paths.get(testFolder.getRoot().getPath(), DB_NAME + ".data.db")));
        //assertFalse(Files.exists(Paths.get(testFolder.getRoot().getPath(), DB_NAME + ".index.db")));
    }
}