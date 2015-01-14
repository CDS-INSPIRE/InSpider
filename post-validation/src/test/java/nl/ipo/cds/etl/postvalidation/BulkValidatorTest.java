package nl.ipo.cds.etl.postvalidation;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import nl.ipo.cds.validation.domain.OverlapValidationPair;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.WKTReader;
import org.deegree.geometry.primitive.Polygon;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;

public class BulkValidatorTest {
	
	private H2GeometryStore<TestPersistableFeature> h2GeometryStore;
	private IBulkValidator<TestPersistableFeature> validator;
	private DataSource ds;
	private TestPersistableFeature tpf;
	private TestPersistableFeature tpf2;
	private TestPersistableFeature tpf3;
	private TestPersistableFeature tpf4;
	private TestPersistableFeature tpf5;
	private TestPersistableFeature tpf6;

	@Rule
	public final TemporaryFolder testFolder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
		h2GeometryStore = new H2GeometryStore<>();
		Field field = H2GeometryStore.class.getDeclaredField("JDBC_URL_FORMAT");
		field.setAccessible(true);
		field.set(h2GeometryStore, String.format("jdbc:h2:%s/", testFolder.getRoot()) + "%s");
		validator = new BulkValidator<>();
		ds = h2GeometryStore.createStore(UUID.randomUUID().toString());
		tpf = new TestPersistableFeature();
		tpf.setId("test-feature1-overlaps-with-2-and-3");
		tpf2 = new TestPersistableFeature();
		tpf2.setId("test-feature2-overlaps-with-1-and-3");
		tpf3 = new TestPersistableFeature();
		tpf3.setId("test-feature3-overlaps-with-1-and-2");
		tpf4 = new TestPersistableFeature();
		tpf4.setId("test-feature4-does-contain-5");
		tpf5 = new TestPersistableFeature();
		tpf5.setId("test-feature5-is-contained-within-4");
		tpf6 = new TestPersistableFeature();
		tpf6.setId("test-feature6-is-free");
	}
	
    @Test
    public void testOverlappingGeometries() throws Exception {
    	// Populate with overlapping and non-overlapping geometries.
		WKTReader reader = new WKTReader(null);
		tpf.setGeometry(reader.read("POLYGON((0 0, 0 10, 10 10, 10 0, 0 0))"));
		tpf2.setGeometry(reader.read("POLYGON((5 5, 5 6, 15 5, 5 5 ))"));
		tpf3.setGeometry(reader.read("POLYGON((0 1, 5 5.5, 11 2, 0 1))"));
		tpf4.setGeometry(reader.read("POLYGON((100 100, 100 200, 200 200, 100 100))"));
		tpf5.setGeometry(reader.read("POLYGON((110 110, 110 120, 120 120, 110 110))"));
		tpf6.setGeometry(reader.read("POLYGON((300 300, 300 400, 400 400, 300 300))"));

		h2GeometryStore.addToStore(ds, tpf.getGeometry(), tpf);
		h2GeometryStore.addToStore(ds, tpf2.getGeometry(), tpf2);
		h2GeometryStore.addToStore(ds, tpf3.getGeometry(), tpf3);
		h2GeometryStore.addToStore(ds, tpf4.getGeometry(), tpf4);
		h2GeometryStore.addToStore(ds, tpf5.getGeometry(), tpf5);
		h2GeometryStore.addToStore(ds, tpf6.getGeometry(), tpf6);

		List<OverlapValidationPair<TestPersistableFeature>> overlaps = validator.overlapValidation(ds);

		// Check correct result. We should get the first 2 features in 1 overlap. Feature 3 does not have geometry which overlaps any of the others.
		assertEquals(4, overlaps.size());

		// 1 overlaps with 2.
		OverlapValidationPair<TestPersistableFeature> pair = overlaps.get(0);
		assertEquals(tpf,pair.f1);
		assertEquals(tpf2,pair.f2);

		// 1 overlaps with 3.
		pair = overlaps.get(1);
		assertEquals(tpf, pair.f1);
		assertEquals(tpf3, pair.f2);

		// 2 overlaps with 3.
		pair = overlaps.get(2);
		assertEquals(tpf2, pair.f1);
		assertEquals(tpf3, pair.f2);

		// 5 is contained within 3 (also counts as overlap).
		pair = overlaps.get(3);
		assertEquals(tpf4, pair.f1);
		assertEquals(tpf5, pair.f2);


		// 6 should not be returned in any combination.
    }

}