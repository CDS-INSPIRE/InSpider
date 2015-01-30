/**
 * 
 */
package nl.ipo.cds.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;

import nl.ipo.cds.dao.TagDao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author annes
 *
 */
@Transactional
public class TagDaoImpl implements TagDao {

	private static final Log log = LogFactory.getLog(TagDaoImpl.class);

	@Inject
	private JdbcTemplate jdbcTemplate;

	/*
	 * This method checks if the given tag already exists in the _tagged table from that theme.
	 * 
	 * @see nl.ipo.cds.dao.TagDao#doesTagExist(java.lang.String)
	 */
	@Override
	public Boolean doesTagExist(String tag, String schemaName, String tableName) {
		log.debug("entered method doesTagExist");
		String sql = "select count(tag) from " + schemaName + "." + tableName + "_tagged where tag= ?";
		log.debug("query" + sql + " will be executed");
		Integer res = jdbcTemplate.queryForObject(sql, Integer.class, tag);
		return res.intValue() >= 1;
	}

	@Override
	public Boolean doesTagJobWithIdExist(String tag) {
		log.debug("entered method doesTagJobWithIdExist");
		// "SELECT fname,lname,isbn from author inner join books on author.AUTHORID = books.AUTHORID"

		String sql = "select parameters from manager.etljob inner join manager.job on manager.etljob.ID = manager.job.ID where manager.job.job_type = 'TAG' and (manager.job.status = 'PREPARED' or manager.job.status = 'CREATED' or manager.job.status = 'STARTED')";
		log.debug("query" + sql + " will be executed");
		List<String> columnResultSet = jdbcTemplate.query(sql, new RowMapper<String>() {
		      public String mapRow(ResultSet resultSet, int i) throws SQLException {
		          return resultSet.getString(1);
		        }
		      }); 
		// String sql = "select count(parameters) from manager.etljob where parameters= ?";
		// columnResultSet.contains(tag);
		if (!columnResultSet.isEmpty()) {
			for (String string : columnResultSet) {
				if (string.isEmpty() || (!string.contains("\"tag\":\""+tag))) {
					continue;
				} else {
					return string.contains("\"tag\":\""+ tag);
				}
			}

		}
		return false;
	}

}
