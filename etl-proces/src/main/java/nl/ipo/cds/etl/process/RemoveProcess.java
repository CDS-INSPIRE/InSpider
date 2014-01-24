package nl.ipo.cds.etl.process;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobLogger;
import nl.idgis.commons.jobexecutor.Process;
import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.DatasetType;
import nl.ipo.cds.domain.RemoveJob;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Transactional;

public class RemoveProcess implements Process<RemoveJob> {

	private static final Log log = LogFactory.getLog(RemoveProcess.class);

	private final DataSource dataSource;

	public RemoveProcess(final DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	@Transactional
	public boolean process(RemoveJob job, JobLogger logger) {
		log.debug("removing dataset started");

		// remove data from bronschema, a transform job will then remove data
		// from inspire schema
		Bronhouder bronhouder = job.getBronhouder();
		DatasetType datasetType = job.getDatasetType();
		String uuid = job.getUuid();

		log.debug("bronhouder: " + bronhouder);
		log.debug("datasetType: " + datasetType);

		Connection connection = DataSourceUtils.getConnection(dataSource);

		// / TODO loop over information_schema.tables
		PreparedStatement bron;
		try {
			bron = connection.prepareStatement("select * from information_schema.tables where table_schema='bron'");
			ResultSet bronResultSet = bron.executeQuery();
			// put all table names from bron in a list
			List<String> bronTableNames = new ArrayList<String>();
			while (bronResultSet.next()) {
				bronTableNames.add(bronResultSet.getString("table_name"));
			}
			bron.close();

			// loop over all tables in bron schema
			for (String tableName : bronTableNames) {
				log.debug("delete from bron." + tableName);
				try {
					PreparedStatement stmt = connection.prepareStatement("delete from bron." + tableName
							+ " where job_id in (" + "select id from manager.etljob " + "where bronhouder_id = ? "
							+ "and datasettype_id = ? and uuid=?)");
					stmt.setLong(1, bronhouder.getId());
					stmt.setLong(2, datasetType.getId());
					stmt.setString(3, uuid);
					log.debug("delete sql statement: " + stmt);
					log.debug("# of deleted features: " + stmt.executeUpdate());

					stmt.close();

				} catch (SQLException e) {
					log.debug("Failed deleting dataset from bron." + tableName);
					throw new RuntimeException("Couldn't remove existing data from bron." + tableName, e);
				} catch (Exception e) {
					throw new RuntimeException("Couldn't remove existing data from bron." + tableName, e);
				}
			}
		} catch (SQLException e1) {
			log.debug("Failed selecting from information_schema.table ");
			throw new RuntimeException("Couldn't select from information_schema.table ", e1);
		}
		DataSourceUtils.releaseConnection(connection, dataSource);

		log.debug("removing dataset finished");

		return true;
	}

	public List resultSetToArrayList(ResultSet rs) throws SQLException {
		ResultSetMetaData md = rs.getMetaData();
		int columns = md.getColumnCount();
		ArrayList list = new ArrayList(50);
		while (rs.next()) {
			HashMap row = new HashMap(columns);
			for (int i = 1; i <= columns; ++i) {
				row.put(md.getColumnName(i), rs.getObject(i));
			}
			list.add(row);
		}

		return list;
	}

	@Override
	public Class<? extends Job> getJobType() {
		return RemoveJob.class;
	}
}
