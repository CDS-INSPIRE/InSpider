/**
 * 
 */
package nl.ipo.cds.dao.impl;

import nl.ipo.cds.dao.TagDao;
import nl.ipo.cds.domain.TagJob;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.sql.DataSource;

/**
 * @author annes
 *
 */
@Transactional
public class TagDaoImpl implements TagDao {

	private static final Log log = LogFactory.getLog(TagDaoImpl.class);

	private EntityManager entityManager;

	private JdbcTemplate jdbcTemplate;

	@PersistenceContext(unitName = "cds")
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public EntityManager getEntityManager () {
		return entityManager;
	}

	@Inject
	public void setDataSource(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

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
		return res > 0;
	}

	
	/* This method checks if the given tag already exists in the etljob table joined with the job table.
	 * @see nl.ipo.cds.dao.TagDao#doesTagJobWithIdExist(java.lang.String)
	 */
	@Override
	public Boolean doesTagJobWithIdExist(String tag, String thema) {
		final TypedQuery<TagJob> jobQuery;
		jobQuery = entityManager.createQuery("from TagJob as job where job.status = 'PREPARED' or job.status='CREATED' or job.status='STARTED'", TagJob.class);
		for (TagJob job : jobQuery.getResultList()) {
			if (job.getTag().equals(tag) && job.getThema().equals(thema)) {
				return true;
			}
		}
		return false;
	}

}
