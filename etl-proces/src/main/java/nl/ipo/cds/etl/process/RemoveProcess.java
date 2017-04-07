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
import nl.ipo.cds.etl.db.annotation.Table;
import nl.ipo.cds.etl.theme.ThemeDiscoverer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Transactional;

public class RemoveProcess implements Process<RemoveJob> {

	private static final Log log = LogFactory.getLog(RemoveProcess.class);

	private final DataSource dataSource;

	private ThemeDiscoverer themeDiscoverer;

	public RemoveProcess(final DataSource dataSource, ThemeDiscoverer themeDiscoverer) {
		this.dataSource = dataSource;
		this.themeDiscoverer = themeDiscoverer;
	}

	@Override
	@Transactional
	public boolean process(RemoveJob job, JobLogger logger) {
		log.debug("removing dataset and bron data started");

		final String themaNaam = job.getDatasetType().getThema().getNaam();
		Table table = themeDiscoverer.getThemeConfiguration(themaNaam).getFeatureTypeClass().getAnnotation(Table.class);
		final String schemaName = table.schema();
		Bronhouder bronhouder = job.getBronhouder();
		DatasetType datasetType = job.getDatasetType();
		String uuid = job.getUuid();

		log.debug("bronhouder: " + bronhouder);
		log.debug("datasetType: " + datasetType);
		log.debug("uuid: " + uuid);
		log.debug("schema: " + schemaName);

		Connection connection = DataSourceUtils.getConnection(dataSource);

		// remove data from bron schema, 
		// a transform job will then remove data from inspire schema
		PreparedStatement bron;
		try {
			bron = connection.prepareStatement(String.format("select table_name from information_schema.tables where table_schema = '%s' and table_type = 'BASE TABLE' and right(table_name,7) != '_tagged'", schemaName));
			ResultSet bronResultSet = bron.executeQuery();
			while (bronResultSet.next()) {
				String tableName = bronResultSet.getString(1);
				String fullTableName = String.format("%s.%s", schemaName, tableName);

				log.debug("delete from " + fullTableName);
				try {
					String sql = String.format("delete from %s where job_id in (select id from manager.etljob where bronhouder_id = ? and datasettype_id = ? and uuid=?)", fullTableName);
					PreparedStatement stmt = connection.prepareStatement(sql);
					stmt.setLong(1, bronhouder.getId());
					stmt.setLong(2, datasetType.getId());
					stmt.setString(3, uuid);
					log.debug("delete sql statement: " + stmt);
					log.debug("# of deleted features: " + stmt.executeUpdate());

					stmt.close();

				} catch (SQLException e) {
					log.debug("Failed deleting dataset from " + fullTableName);
					throw new RuntimeException("Couldn't remove existing data from " + fullTableName, e);
				} catch (Exception e) {
					throw new RuntimeException("Couldn't remove existing data from " + fullTableName, e);
				}
			}
			
			bronResultSet.close();
			bron.close();
		} catch (SQLException e1) {
			log.debug("Failed selecting from information_schema.table ");
			throw new RuntimeException("Couldn't select from information_schema.table ", e1);
		}
		DataSourceUtils.releaseConnection(connection, dataSource);

		log.debug("removing dataset and bron data finished");

		return false;
	}

	@Override
	public Class<? extends Job> getJobType() {
		return RemoveJob.class;
	}
}
