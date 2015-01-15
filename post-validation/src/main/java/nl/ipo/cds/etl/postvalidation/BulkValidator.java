package nl.ipo.cds.etl.postvalidation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import nl.ipo.cds.validation.domain.OverlapValidationPair;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Responsible for validating that geometries do not overlap.
 */
@Service
public class BulkValidator<T extends Serializable> implements IBulkValidator<T> {

	/**
	 * Detect if any of the geometries in the provided database have any
	 * overlap.
	 * 
	 * @throws SQLException
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	@Override
	@SuppressWarnings("unchecked")
    public List<OverlapValidationPair<T>> overlapValidation(final DataSource dataSource) throws SQLException, IOException, ClassNotFoundException {

    	List<OverlapValidationPair<T>> result = new ArrayList<>();
    	
    	String sql = "select g1.feature as feature1, g2.feature as feature2 " +
                     "from geometries g1 " +
                     ",    geometries g2 " +
                     "where g1.id < g2.id " +
                     "and (ST_Overlaps(g1.geometry,g2.geometry) " +
                          "or ST_Equals(g1.geometry, g2.geometry) " +
                          "or ST_Within(g1.geometry, g2.geometry) " +
                          "or ST_Within(g2.geometry, g1.geometry))";

        JdbcTemplate t = new JdbcTemplate(dataSource);
        List<Map<String,Object>> res = t.queryForList(sql);

        for (Map<String, Object> row : res) {
            ByteArrayInputStream bis = new ByteArrayInputStream((byte[])row.get("FEATURE1"));
            ObjectInputStream ois = new ObjectInputStream(bis);
            T feature1 = (T) ois.readObject();
            ois.close();

            bis = new ByteArrayInputStream((byte[])row.get("FEATURE2"));
            ois = new ObjectInputStream(bis);
            T feature2 = (T)ois.readObject();
            ois.close();

            OverlapValidationPair<T> overlapEntry = new OverlapValidationPair<>(feature1,feature2);
            result.add(overlapEntry);

        }
        return result;

    }
}
