package nl.ipo.cds.etl.postvalidation;

import geodb.GeoDB;
import org.apache.commons.dbcp.BasicDataSource;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.WKTReader;
import org.deegree.geometry.primitive.Polygon;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class H2GeometryStoreTest {



    private H2GeometryStore<TestPersistableFeature> h2GeometryStore;
    private BasicDataSource dataSource;
    private final static String DB_NAME = "test-db-1337";

    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        h2GeometryStore = new H2GeometryStore<>();
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
        TestPersistableFeature tpf = new TestPersistableFeature();
        tpf.setGeometry(g);
        tpf.setId("test-feature");

        h2GeometryStore.addToStore(dataSource, g, tpf );

        JdbcTemplate t = new JdbcTemplate(dataSource);

        // Test if the Geometry can properly get stored/retrieved.
        Geometry g2 = (Polygon) reader.read(GeoDB.ST_AsText(t.queryForObject("SELECT geometry FROM geometries LIMIT 1", byte[].class)));
        assertEquals(g.toString(), g2.toString());

        // Test if the Feature can properly get stored/retrieved.
        ByteArrayInputStream bis = new ByteArrayInputStream(t.queryForObject("SELECT feature FROM geometries LIMIT 1", byte[].class));

        ObjectInputStream ois = new ObjectInputStream(bis);
        TestPersistableFeature tpf2 = (TestPersistableFeature) ois.readObject();
        assertEquals(tpf, tpf2);



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