package nl.ipo.cds.etl.postvalidation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import nl.ipo.cds.validation.domain.OverlapValidationPair;

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
				ObjectInputStream ois = new ObjectInputStream(rs.getBinaryStream(2));				
				T feature1 = (T) ois.readObject();
				ois.close();
				ois = new ObjectInputStream(rs.getBinaryStream(4));
				T feature2 = (T) ois.readObject();
				ois.close();

                OverlapValidationPair<T> overlapEntry = new OverlapValidationPair<>(feature1,feature2);
                result.add(overlapEntry);
			}
			preparedStatement.close();
			
			return result;

    }
}
