package nl.ipo.cds.etl.postvalidation;

import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class H2GeometryStoreTest {

    private H2GeometryStore h2GeometryStore;
    private final String DB_NAME = "test-db-1337";
    private final String DB_DIR = "dbs";

    @Before
    public void setUp() throws Exception {
        h2GeometryStore = new H2GeometryStore();
        h2GeometryStore.createStore(DB_NAME);
    }



    @Test
    public void testCreateStore() throws Exception {
        H2GeometryStore h2 = new H2GeometryStore();
        String dbName = "test";
        h2.createStore(dbName);
        assertTrue(Files.exists(Paths.get(DB_DIR, dbName + ".data.db")));
        assertTrue(Files.exists(Paths.get(DB_DIR, dbName + ".index.db")));

    }

    @Test
    public void testAddToStore() throws Exception {

    }

    @Test
    public void testDestroyStore() throws Exception {
        h2GeometryStore.destroyStore(DB_NAME);
        assertFalse(Files.exists(Paths.get(DB_DIR, DB_NAME + ".data.db")));
        assertFalse(Files.exists(Paths.get(DB_DIR, DB_NAME + ".index.db")));
    }
}