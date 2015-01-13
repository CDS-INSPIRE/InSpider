package nl.ipo.cds.etl.postvalidation;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import nl.ipo.cds.validation.domain.OverlapValidationPair;

import org.springframework.stereotype.Service;

/**
 * Responsible for validating that geometries do not overlap.
 */
@Service
public class BulkValidator implements IBulkValidator {

	/**
	 * Detect if any of the geometries in the provided database have any
	 * overlap.
	 * 
	 * @throws SQLException
	 */
	@Override
    public List<OverlapValidationPair<Blob, Blob>> overlapValidation(final DataSource dataSource) throws SQLException {

    	List<OverlapValidationPair<Blob, Blob>> result = new java.util.ArrayList<OverlapValidationPair<Blob, Blob>>();
    	
    	String sql = "g1.id as id1, select g1.feature as feature1, g2.id as id2, g2.feature as feature2 " +
                     "from geometries g1 " +
                     ",    geometries g2 " +
                     "where g1.id != g2.id " +
                     "and ST_Overlaps(g1.geom,g2.geom) = true";
    	
			PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement(sql);
			// features with overlapping geometries.
			// write feature info with overlap error to context. 
			ResultSet rs = preparedStatement.executeQuery(sql);
			while (rs.next()) {
			Blob feature1 = rs.getBlob(2);
			Blob feature2 = rs.getBlob(4);
			OverlapValidationPair<Blob, Blob> overlapEntry = new OverlapValidationPair<Blob, Blob>(feature1,feature2);
			result.add(overlapEntry);
			}
			preparedStatement.close();
			
			return result;

    }
}
