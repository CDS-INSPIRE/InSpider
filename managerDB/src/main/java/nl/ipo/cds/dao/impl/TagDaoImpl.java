/**
 * 
 */
package nl.ipo.cds.dao.impl;

import javax.inject.Inject;

import nl.ipo.cds.dao.TagDao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
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
		String sql = "select count(tag) from " + schemaName +"."+ tableName + "_tagged where tag= ?";
		log.debug("query" + sql + " will be executed");
		Integer res = jdbcTemplate.queryForObject(sql,Integer.class, tag);
		return res.intValue()>=1;
	}

}
