package nl.ipo.cds.etl.process;

import com.google.common.base.Joiner;
import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobLogger;
import nl.idgis.commons.jobexecutor.Process;
import nl.ipo.cds.domain.TagJob;
import nl.ipo.cds.etl.theme.ThemeDiscoverer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.*;

public class TagProcess implements Process<TagJob> {

	private static final Log log = LogFactory.getLog(TagProcess.class);

	private final DataSource dataSource;

	private ThemeDiscoverer themeDiscoverer;

	public TagProcess(final DataSource dataSource, final ThemeDiscoverer themeDiscoverer) {
		this.dataSource = dataSource;
		this.themeDiscoverer = themeDiscoverer;
	}

	@Override
	public boolean process(TagJob job, JobLogger logger) {
		log.debug("tagging dataset started");
		final String themaNaam = job.getDatasetType().getThema().getNaam();
		final String schemaName = themeDiscoverer.getThemeConfiguration(themaNaam).getSchemaName();
		log.debug("bronhouder: " + job.getBronhouder());
		log.debug("datasetType: " + job.getDatasetType());
		log.debug("uuid: " + job.getUuid());
		log.debug("schema name: " + schemaName);
		log.debug("tag: " + job.getTag());


		// Get the table name to copy from.
		// Copy and add tag.


		// Retrieve which feature set to tag.
		Map<String, Object> tableJobDict = findFeatureSet(job, schemaName);
		String tableName = (String) tableJobDict.get("table_name");
		// Now return all columns for the features in the table.
		Set<String> columnNames = retrieveColumns(schemaName, tableName);

		copyData(job, schemaName, tableJobDict, tableName, columnNames);

		log.debug("tagging dataset finished");

		return false;
	}

	@Transactional
	private void copyData(TagJob job, String schemaName, Map<String, Object> tableJobDict, String tableName, Set<String> columnNames) {
		Long jobId = (Long) tableJobDict.get("job_id");
		long numRecords = (Long)tableJobDict.get("num_records");


		String colStr = Joiner.on(',').join(columnNames);
		String destTable = String.format("%s.%s_tagged", schemaName, tableName);
		String srcTable = String.format("%s.%s", schemaName, tableName);

		// TODO: Do we change the destination job_id to the ID of the tag job ?
		NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(dataSource);
		MapSqlParameterSource namedParams = new MapSqlParameterSource();
		namedParams.addValue("tag", job.getTag());
		namedParams.addValue("job_id", jobId);
		long numCopied = jdbc.update(String.format("insert into %s (tag, %s) select :tag, %s from %s where job_id = :job_id", destTable, colStr, colStr, srcTable), namedParams);

		if (numCopied != numRecords) {
			throw new RuntimeException(String.format("Not all records where correctly copied to the _tagged table. Expected number of records: %d, actual: %d.", numRecords, numCopied));
		}
	}

	private Set<String> retrieveColumns(String schemaName, String tableName) {
		NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(dataSource);
		Map<String, String> params = new HashMap<String, String>();
		params.put("schema_name", schemaName);
		params.put("table_name", tableName);

		SqlRowSet columnResultSet = jdbc.queryForRowSet("select column_name from information_schema.columns where table_schema=:schema_name and table_name=:table_name", params);
		SortedSet<String> columnNames = new TreeSet<String>();
		while (columnResultSet.next()) {
			String columnName = columnResultSet.getString("column_name");
			if (!columnName.equals("id")) {
				columnNames.add(columnName);
			}
		}
		return columnNames;
	}

	/**
	 * Helper function for finding the feature table and job_id required to identify the features to be copied.
	 * @param job A TagJob.
	 * @param schemaName The name of the schema to find the feature table in.
	 * @return A dictionary containing the job_id and table name.
	 * @throws java.lang.RuntimeException when no feature set can be identified.
	 */
	private Map<String, Object> findFeatureSet(final TagJob job, final String schemaName) {
		NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(dataSource);
		Map<String, Object> tableJobDict = null;
		// Retrieve the table name of the table to tag datasets from. Exclude the tables with _tagged suffix.
		String sql = "select table_name from information_schema.tables where table_schema = :schema_name and table_type = 'BASE TABLE' and right(table_name, 6) != 'tagged'";
		Map<String, String> params = new HashMap<String, String>();
		params.put("schema_name", schemaName);
		SqlRowSet tableNameResultSet = jdbc.queryForRowSet(sql, params);

		// This iterates over all dataset tables but since the job_id is unique over all datasets, only one dataset actually matches.
		// Same implementation as RemoveJob.
		// After this we know which table to query and with which job_id.
		while (tableNameResultSet.next()) {
            String tableName = tableNameResultSet.getString("table_name");

            try {

                sql = String.format("select job_id, :table_name as table_name, count(*) as num_records from %s.%s where job_id in (select id from manager.etljob where bronhouder_id=:bronhouder_id and datasettype_id=:datasettype_id and uuid=:uuid) group by job_id", schemaName, tableName);
				MapSqlParameterSource namedParams = new MapSqlParameterSource();
                namedParams.addValue("bronhouder_id", job.getBronhouder().getId());
				namedParams.addValue("datasettype_id", job.getDatasetType().getId());
				namedParams.addValue("uuid", job.getUuid());
				namedParams.addValue("table_name", tableName);
                tableJobDict = jdbc.queryForMap(sql, namedParams);
                log.debug(String.format("Matching table found in %s.%s", schemaName, tableName));

                // We found the table and the jobId, jump out of the loop.
                break;
            } catch (IncorrectResultSizeDataAccessException e) {
                log.debug(String.format("Table %s.%s does not contain a matching job.", schemaName, tableName));
            }


        }
		if (tableJobDict == null) {
            log.debug("Failed finding data to be tagged in any table.");
            throw new RuntimeException("Failed finding data to be tagged in any table.");

        }
		return tableJobDict;
	}

	@Override
	public Class<? extends Job> getJobType() {
		return TagJob.class;
	}
}
