package nl.ipo.cds.etl.process;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

		PreparedStatement bron;
		try {
			bron = connection.prepareStatement("select table_name from information_schema.tables where table_schema = 'bron' and table_type = 'BASE TABLE'");
			ResultSet bronResultSet = bron.executeQuery();
			while (bronResultSet.next()) {
				String tableName = bronResultSet.getString(1);
				
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
			
			bronResultSet.close();
			bron.close();
		} catch (SQLException e1) {
			log.debug("Failed selecting from information_schema.table ");
			throw new RuntimeException("Couldn't select from information_schema.table ", e1);
		}
		DataSourceUtils.releaseConnection(connection, dataSource);

		log.debug("removing dataset finished");

		return true;
	}

	@Override
	public Class<? extends Job> getJobType() {
		return RemoveJob.class;
	}
}
