package nl.ipo.cds.etl.process;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.sql.DataSource;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobLogger;
import nl.idgis.commons.jobexecutor.Process;
import nl.ipo.cds.domain.TagJob;
import nl.ipo.cds.etl.theme.ThemeDiscoverer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Joiner;

@Transactional
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
		log.debug("table: " + job.getSourceTable());


		// Now return all columns for the features in the table.
		Set<String> columnNames = retrieveColumns(schemaName, job.getSourceTable());

		// Actually copy the data.
		copyData(job, schemaName, columnNames);

		log.debug("tagging dataset finished");

		return false;
	}

	private void copyData(TagJob job, String schemaName, Set<String> columnNames) {

		String colStr = Joiner.on(',').join(columnNames);
		String srcTable = String.format("%s.%s", schemaName, job.getSourceTable());
		String destTable = String.format("%s_tagged", srcTable);

		NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(dataSource);
		MapSqlParameterSource namedParams = new MapSqlParameterSource();
		namedParams.addValue("tag", job.getTag());
		int numRecords = jdbc.queryForObject(String.format("select count(*) from %s", srcTable), new MapSqlParameterSource(), Integer.class);

		int numCopied = jdbc.update(String.format("insert into %s (tag, %s) select :tag, %s from %s", destTable, colStr, colStr, srcTable), namedParams);

		if (numCopied != numRecords) {
			throw new RuntimeException(String.format("Not all records where correctly copied to the _tagged table. Expected number of records: %d, actual: %d.", numRecords, numCopied));
		}
	}

	/**
	 * Retrieve a set of columns for a table in a certain schema.
	 */
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

	@Override
	public Class<? extends Job> getJobType() {
		return TagJob.class;
	}
}
