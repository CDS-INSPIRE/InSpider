package nl.ipo.cds.etl.postvalidation;

import java.sql.SQLException;
import java.util.UUID;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class BulkValidatorTest {
	
	private H2GeometryStore h2GeometryStore;
	private DataSource ds;
	
	private void init() throws SQLException {
		h2GeometryStore = new H2GeometryStore();
		ds = h2GeometryStore.createStore(UUID.randomUUID().toString());
		//h2GeometryStore.addToStore(ds, geometry, feature);
	}
	
    @Test
    public void testOverlapValidation() throws Exception {
    	
    	// create H2 database
    	init();
    	// populate
    	
    	// check overlaps

    }
}