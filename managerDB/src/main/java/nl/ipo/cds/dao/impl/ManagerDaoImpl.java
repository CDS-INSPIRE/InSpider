package nl.ipo.cds.dao.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import nl.idgis.commons.jobexecutor.AbstractJob;
import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobLogger.LogLevel;
import nl.ipo.cds.dao.DatasetCriteria;
import nl.ipo.cds.dao.JobCriteria;
import nl.ipo.cds.dao.JobInfo;
import nl.ipo.cds.dao.JobLogCriteria;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.dao.SortField;
import nl.ipo.cds.dao.SortOrder;
import nl.ipo.cds.dao.impl.ldap.GebruikerAttributesMapper;
import nl.ipo.cds.dao.impl.ldap.GebruikerContextMapper;
import nl.ipo.cds.domain.AttributeMapping;
import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.BronhouderThema;
import nl.ipo.cds.domain.CodeListMapping;
import nl.ipo.cds.domain.Dataset;
import nl.ipo.cds.domain.DatasetFilter;
import nl.ipo.cds.domain.DatasetType;
import nl.ipo.cds.domain.DbGebruiker;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.domain.FilterExpression;
import nl.ipo.cds.domain.Gebruiker;
import nl.ipo.cds.domain.GebruikerThemaAutorisatie;
import nl.ipo.cds.domain.Identity;
import nl.ipo.cds.domain.JobLog;
import nl.ipo.cds.domain.JobType;
import nl.ipo.cds.domain.LdapGebruiker;
import nl.ipo.cds.domain.MappingOperation;
import nl.ipo.cds.domain.MetadataDocument;
import nl.ipo.cds.domain.Thema;
import nl.ipo.cds.domain.TypeGebruik;
import nl.ipo.cds.utils.DateTimeUtils;

import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.WKBReader;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.vividsolutions.jts.io.ParseException;

public class ManagerDaoImpl implements ManagerDao {

	private EntityManager entityManager;
	private LdapTemplate ldapTemplate;
	
	/**
	 * The base DN for the LDAP server.
	 */
	private String ldapBase = "dc=inspire,dc=idgis,dc=eu";
	
	/**
	 * The DN of the LDAP group that contains all CDS users. Any inetOrgPerson that is a member of this group
	 * has access to the CDS.
	 */
	private String ldapGroupDn = "cn=cds-gebruikers,ou=Group";
	
	/**
	 * Base DN to use when creating new users. Existing users are persisted using their original DN.
	 */
	private String ldapPeopleBaseDn = "ou=People";
	
	@PersistenceContext(unitName = "cds")
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	public EntityManager getEntityManager () {
		return entityManager;
	}

	public ManagerDaoImpl(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}
	
	public void setLdapTemplate (final LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}
	
	public LdapTemplate getLdapTemplate () {
		return ldapTemplate;
	}

	public void setLdapBase (final String ldapBase) {
		this.ldapBase = ldapBase;
	}
	
	public String getLdapBase () {
		return ldapBase;
	}
	
	public void setLdapGroupDn (final String ldapGroupDn) {
		this.ldapGroupDn = ldapGroupDn;
	}
	
	public String getLdapGroupDn () {
		return ldapGroupDn;
	}
	
	public void setLdapPeopleBaseDn (final String baseDn) {
		this.ldapPeopleBaseDn = baseDn;
	}
	
	public String getLdapPeopleBaseDn () {
		return ldapPeopleBaseDn;
	}
	
	// -------------
	// Werk tabellen
	// -------------
	// JOB
	@Override
	@Transactional(propagation=Propagation.MANDATORY) // New transaction because a job is process-data; is not allowed to roll-back
	public void create(AbstractJob job) {
		job.setCreateTime(DateTimeUtils.now());
		this.entityManager.persist(job);
	}
	
	@Override
	public AbstractJob getJob(Long pk) {
		AbstractJob job = this.entityManager.find(AbstractJob.class, pk);
		return job;
	}
 
	@Override
	public AbstractJob getJob (Job job) {
		return getJob (job.getId ());
	}
	
	@Override
	public List<AbstractJob> getAllJobs() {
		final TypedQuery<AbstractJob> jobQuery;
		jobQuery = entityManager.createQuery("from EtlJob as job order by job.createTime desc", AbstractJob.class);
		return jobQuery.getResultList();
	}

	@Override
	public List<EtlJob> getJobsByStatus(Job.Status status) {
		final TypedQuery<EtlJob> jobQuery;
		jobQuery = entityManager.createQuery("from EtlJob as job where job.status = ?1 order by job.createTime desc", EtlJob.class)
				.setParameter(1, status);
		return jobQuery.getResultList();
	}

	@Override
	public List<EtlJob> getJobsByDataset(Bronhouder bronhouder, DatasetType datasetType, String uuid) {
		final TypedQuery<EtlJob> jobQuery;
		if (uuid==null){
			jobQuery = entityManager.createQuery("from EtlJob as job where (job.bronhouder = ?1 and job.datasettype = ?2) order by job.id desc", EtlJob.class)
			.setParameter (1, bronhouder)
			.setParameter (2, datasetType);
		} else {
			jobQuery = entityManager.createQuery("from EtlJob as job where (job.bronhouder = ?1 and job.datasettype = ?2  and job.uuid = ?3) order by job.id desc", EtlJob.class)
			.setParameter (1, bronhouder)
			.setParameter (2, datasetType)
			.setParameter (3, uuid);
		}
		return jobQuery.getResultList();
	}

	@Override
	public AbstractJob getLastJob() {
		AbstractJob job = null;
		final TypedQuery<AbstractJob> jobQuery;
		jobQuery = entityManager.createQuery("from AbstractJob as job where job.status = ?1 order by job.priority desc, job.id desc", AbstractJob.class)
				.setMaxResults(1)
				.setParameter(1, Job.Status.CREATED);
		List<AbstractJob> jobs = jobQuery.getResultList();
		if ((jobs != null)  && (jobs.size() >= 1) ) {
			// should only be one task
			job = jobs.get(0);
		}
		return job;
	}

	@Override
	public EtlJob getLastCompletedJob (Bronhouder bronhouder, DatasetType datasetType, String uuid) {
		final TypedQuery<EtlJob> query = entityManager.createQuery (
				"from EtlJob as job where (job.status in (?1, ?2) and (job.bronhouder = ?3 and job.datasettype = ?4  and job.uuid = ?5) and job.verversen = true and job.finishTime is not null) order by job.finishTime desc",
				EtlJob.class
			);
		
		query
			.setParameter (1, Job.Status.FINISHED)
			.setParameter (2, Job.Status.ABORTED)
			.setParameter (3, bronhouder)
			.setParameter (4, datasetType)
			.setParameter (5, uuid)
			.setMaxResults (1);
		
		try {
			return query.getSingleResult ();
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public EtlJob getLastCompletedJob(EtlJob job) {
		return getLastCompletedJob(job.getBronhouder(), job.getDatasetType(), job.getUuid());
	}

	@Override
	public EtlJob getLastValidationJob (Bronhouder bronhouder, DatasetType datasetType, String uuid) {
		final TypedQuery<EtlJob> query = entityManager.createQuery (
				"from EtlJob as job " +
				"where " +
					"job.status in (?1, ?2) " +
					"and type (job) = ValidateJob " +
					"and  (job.bronhouder = ?4 and job.datasettype = ?5  and job.uuid = ?6) " +
					"and job.finishTime is not null " +
					"and not exists (from EtlJob as job2 " +
						"where job.bronhouder = job2.bronhouder " +
						"and job.datasettype = job2.datasettype " +
						"and job.createTime < job2.createTime " +
						"and type (job2) = RemoveJob)" +
				"order by job.finishTime desc",
				EtlJob.class
			);
		
		query
			.setParameter (1, Job.Status.FINISHED)
			.setParameter (2, Job.Status.ABORTED)
			.setParameter (4, bronhouder)
			.setParameter (5, datasetType)
			.setParameter (6, uuid)
			.setMaxResults (1);
		
		try {
			return query.getSingleResult ();
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (NoResultException e) {
			return null;
		}
	}
	
	@Override
	public EtlJob getLastValidationJob(EtlJob job) {
		return getLastValidationJob(job.getBronhouder(), job.getDatasetType(), job.getUuid());
	}

	
	@Override
	public EtlJob getLastJobThatValidated (Bronhouder bronhouder, DatasetType datasetType, String uuid) {
		final TypedQuery<EtlJob> query = entityManager.createQuery (
				"from EtlJob as job " +
				"where " +
					"job.status in (?1, ?2) " +
					"and (type (job) = ValidateJob or (type (job) = ImportJob and (job.verversen = true or job.metadataUpdateDatum is null))) " +
					"and (job.bronhouder = ?5 and job.datasettype = ?6  and job.uuid = ?7) " +
					"and job.finishTime is not null " +
					"and not exists (from EtlJob as job2 " +
						"where job.bronhouder = job2.bronhouder " +
						"and job.datasettype = job2.datasettype " +
						"and job.createTime < job2.createTime " +
						"and type (job2) = RemoveJob)" +
				"order by job.finishTime desc",
				EtlJob.class
			);
		
		query
			.setParameter (1, Job.Status.FINISHED)
			.setParameter (2, Job.Status.ABORTED)
			.setParameter (5, bronhouder)
			.setParameter (6, datasetType)
			.setParameter (7, uuid)
			.setMaxResults (1);
		
		try {
			return query.getSingleResult ();
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (NoResultException e) {
			return null;
		}
	}
	
	@Override
	public EtlJob getLastImportJob (Bronhouder bronhouder, DatasetType datasetType, String uuid) {
		final TypedQuery<EtlJob> query = entityManager.createQuery (
				"from EtlJob as job " +
				"where " +
					"job.status = ?1 " +
					"and type (job) = ImportJob " +
					"and (job.bronhouder = ?3 and job.datasettype = ?4 and job.uuid = ?5) " +
					"and job.verversen = true " +
					"and job.finishTime is not null " +
					"and not exists (from EtlJob as job2 " +
						"where job.bronhouder = job2.bronhouder " +
						"and job.datasettype = job2.datasettype " +
						"and job.createTime < job2.createTime " +
						"and type (job2) = RemoveJob)" +
				"order by job.finishTime desc",
				EtlJob.class
			);
		
		query
			.setParameter (1, Job.Status.FINISHED)
			.setParameter (3, bronhouder)
			.setParameter (4, datasetType)
			.setParameter (5, uuid)
			.setMaxResults (1);
		
		try {
			return query.getSingleResult ();
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (NoResultException e) {
			return null;
		}
	}
	
	@Override
	public EtlJob getLastImportJob(EtlJob job) {
		return getLastImportJob(job.getBronhouder(), job.getDatasetType(), job.getUuid());
	}

	@Override
	public EtlJob getLastSuccessfullImportJob(Bronhouder bronhouder, DatasetType datasetType, String uuid) {
		final TypedQuery<EtlJob> query = entityManager.createQuery (
				"from EtlJob as job " +
				"where " +
					"job.status in (?1, ?2) " +
					"and type (job) = ImportJob " +
					"and (job.bronhouder = ?4 and job.datasettype = ?5 and job.uuid = ?6) " +
					"and job.finishTime is not null " +
					"and not exists(from EtlJob as job2 " +
						"where job.bronhouder = job2.bronhouder " +
						"and job.datasettype = job2.datasettype " +
						"and job.createTime < job2.createTime " +
						"and type (job2) = RemoveJob) " +
					"and not exists(from JobLog as jobLog " +
						"where jobLog.job = job " +
						"and jobLog.logLevel = ?8) " +
				"order by job.finishTime desc",
				EtlJob.class
			);
		
		query
			.setParameter (1, Job.Status.FINISHED)
			.setParameter (2, Job.Status.ABORTED)
			.setParameter (4, bronhouder)
			.setParameter (5, datasetType)
			.setParameter (6, uuid)
			.setParameter (8, LogLevel.ERROR)
			.setMaxResults (1);
		
		try {
			return query.getSingleResult ();
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public EtlJob getLastSuccessfullImportJob(EtlJob job) {
		return getLastSuccessfullImportJob(job.getBronhouder(), job.getDatasetType(), job.getUuid());
	}

	@Override
	public List<Thema> getImportedThemasWithoutSubsequentTransform() {
		final TypedQuery<Thema> query = entityManager.createQuery (
				"select distinct thema " +		
				"from " +
					"EtlJob import " +
					"join import.datasettype as dst " +
					"join dst.thema as thema " +
				"where " +
					"import.status = ?1 " +
					"and import.verversen = true " +
					"and type (import) = ImportJob " +
					"and not exists(from EtlJob as transform " +
						"where transform.status = ?1 " +
						"and type (transform) = TransformJob " +
						"and transform.startTime >= import.finishTime)",
				Thema.class
			);
		
		query.setParameter (1, Job.Status.FINISHED);

		try {
			return query.getResultList();
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (NoResultException e) {
			return null;
		}
	}	

	@Override
	public List<Thema> getRemovedThemasWithoutSubsequentTransform() {
		final TypedQuery<Thema> query = entityManager.createQuery (
				"select distinct thema " +		
				"from " +
					"EtlJob job " +
					"join job.datasettype as dst " +
					"join dst.thema as thema " +
				"where " +
					"job.status = ?1 " +
					"and type (job) = RemoveJob " +
					"and not exists(from EtlJob as transform " +
						"where transform.status = ?1 " +
						"and type (transform) = TransformJob " +
						"and transform.startTime >= job.finishTime)",
				Thema.class
			);
		query.setParameter (1, Job.Status.FINISHED);
		try {
			return query.getResultList();
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (NoResultException e) {
			return null;
		}
	}
	
	@Override
	public EtlJob getLastTransformJob(Job.Status jobStatus) {
		Assert.notNull(jobStatus, "jobStatus is mandatory because otherwise the order By clause doesn't work because date could have null values");
		
		String queryString = "from EtlJob as job where type (job) = TransformJob";
		if(jobStatus != null){
			queryString += " and job.status = ?1";
		}
		queryString += " order by job.finishTime desc, job.createTime desc";
		
		final TypedQuery<EtlJob> query = entityManager.createQuery (
				 queryString,
				EtlJob.class
			);
		
		query
			.setMaxResults (1);
		if(jobStatus != null){
			query.setParameter (1, jobStatus);
		}
		
		try {
			return query.getSingleResult ();
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (NoResultException e) {
			return null;
		}
	}


	@Override
	public EtlJob getPendingJob (Bronhouder bronhouder, DatasetType datasetType, String uuid) {
		final TypedQuery<EtlJob> query = entityManager.createQuery (
				"from EtlJob as job where job.status in (?1, ?2) and (job.bronhouder = ?3 and job.datasettype = ?4  and job.uuid = ?5) order by job.createTime desc",
				EtlJob.class
			);
		
		query
			.setParameter (1, Job.Status.CREATED)
			.setParameter (2, Job.Status.STARTED)
			.setParameter (3, bronhouder)
			.setParameter (4, datasetType)
			.setParameter (5, uuid)
			.setMaxResults (1);
		
		try {
			return query.getSingleResult ();
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (NoResultException e) {
			return null;
		}
	}
	
	@Override
	public EtlJob getPendingJob(EtlJob job) {
		return getPendingJob(job.getBronhouder(), job.getDatasetType(), job.getUuid());
	}


	
	@Override
	public List<JobInfo> getLastCompletedJobs () {
		/*
		final Query query = entityManager.createQuery (
				"select " +
					"new nl.ipo.cds.dao.JobInfo(" +
						"job," +
						"COUNT(log) as logMessageCount" +
					") " +
				"from " +
					"JobLog as log " +
					"left join log.job as job " +
					//"join job.bronhouder as b " +
				"where " +
					"job.status in (?1, ?2) " +
					"and log.job = job " +
					"and job.id = " +
						"(select max(lastJob.id) from EtlJob as lastJob where (lastJob.bronhouder = job.bronhouder and type (lastJob) = type (job) and lastJob.uuid = job.uuid) and job.status in (?1, ?2) order by job.id desc) " +
				"group by " +
					"job " + "");
				//"order by " +
				//	"min(job.bronhouder.provincie) asc, min(type (job)) asc");
				 */
		final Query query = entityManager.createQuery (
				"select " +
					"new nl.ipo.cds.dao.JobInfo(" +
						"job," +
						"(select count(log) from JobLog as log where log.job = job)" +
					") " +
				"from " +
					"EtlJob job " +
					"join job.bronhouder as b " +
				"where " +
					"job.status in (?1, ?2) " +
					"and job.id = " +
						"(select max(lastJob.id) from EtlJob as lastJob where (lastJob.bronhouder = job.bronhouder and type (lastJob) = type (job) and lastJob.uuid = job.uuid) and job.status in (?1, ?2) order by job.id desc) " +
				"order by " +
					"b.naam asc, type (job) asc"
			);
		
		query
			.setParameter (1, Job.Status.FINISHED)
			.setParameter (2, Job.Status.ABORTED);
		
		// For some reason this query cannot be performed as a typed query, therefore the following statemenet is unchecked:
		@SuppressWarnings("unchecked")
		final List<JobInfo> jobInfos = query.getResultList ();
		
		return jobInfos;
	}
	
	@Override
	public long getJobLogCount (AbstractJob job) {
		final TypedQuery<Long> query = entityManager.createQuery("select count(*) from JobLog as log where log.job = ?1", Long.class);
		
		query
			.setParameter (1, job)
			.setMaxResults (1);
		
		return query.getSingleResult ();
	}
	
	@Override
	public long getJobLogCount (AbstractJob job, LogLevel logLevel) {
		final TypedQuery<Long> query = entityManager.createQuery("select count(*) from JobLog as l where l.job = ?1 and l.logLevel = ?2", Long.class);
		
		query
			.setParameter (1, job)
			.setParameter (2, logLevel)
			.setMaxResults (1);
		
		return query.getSingleResult ();
	}
	
	@Override
	@Transactional(propagation=Propagation.MANDATORY) // New transaction because a job is process-data; is not allowed to roll-back
	public void update(AbstractJob job) {
		this.entityManager.merge(job);
	}
	
	@Override
	@Transactional(propagation=Propagation.MANDATORY)
	public void update (EtlJob job) {
		this.entityManager.merge (job);
	}

	@Override
	@Transactional
	public void delete(AbstractJob job) {
		AbstractJob jobToDelete = this.entityManager.getReference(AbstractJob.class, job.getId());
		this.entityManager.remove(jobToDelete);
//		this.entityManager.remove(job);//this alone will raise exception 'Removing a detached instance'
	}
	
	@Override
	@Transactional
	public void delete (EtlJob job) {
		final EtlJob jobToDelete = entityManager.getReference (EtlJob.class, job.getId ());
		entityManager.remove (jobToDelete);
	}

	// DATASET
	@Transactional
	@Override
	public void create(Dataset dataSet) {
		this.entityManager.persist(dataSet);
	}

	@Override
	public Dataset getDataSet(Long pk) {
		return this.entityManager.find(Dataset.class, pk);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Dataset> getAllDatasets() {
		Query datasetQuery = null;
		datasetQuery = entityManager.createQuery("from Dataset");
		return datasetQuery.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Dataset> getDatasetsByDatasetType(DatasetType dataSetType) {
		Query datasetQuery = null;
		datasetQuery = entityManager.createQuery(
				"from Dataset as dataset where dataset.type.id = ?1").setParameter(1,
				dataSetType.getId());
		return datasetQuery.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Dataset> getDatasetsByBronhouder(Bronhouder bronhouder) {
		Query datasetQuery = null;
		datasetQuery = entityManager.createQuery(
				"from Dataset as dataset where dataset.bronhouder.id = ?1").setParameter(1,
				bronhouder.getId ());
		return datasetQuery.getResultList();
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<Dataset> getDatasetsByUUID(String uuid) {
		Query datasetQuery = null;
		datasetQuery = entityManager.createQuery(
				"from Dataset as dataset where dataset.uuid = ?1").setParameter(1,
				uuid);
		return datasetQuery.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Dataset getDataset(Bronhouder bronhouder, DatasetType datasetType, String uuid) {
		Query datasetQuery = null;
		datasetQuery = entityManager.createQuery(
				"from Dataset as dataset where dataset.bronhouder.id = ?1 and dataset.type.id = ?2 and dataset.uuid = ?3")
				.setParameter(1, bronhouder.getId ())
				.setParameter(2, datasetType.getId ())
				.setParameter(3, uuid);
		List<Dataset> datasetList =  datasetQuery.getResultList();
		if (datasetList.size()>0){
			return datasetList.get(0);
		}else{
			return null;
		}
	}
	
	//W1502 019
		@SuppressWarnings("unchecked")
		@Override
		public Dataset getDatasetBy(Bronhouder bronhouder, DatasetType datasetType, String uuid) {
			Query datasetQuery = null;
			datasetQuery = entityManager.createQuery(
					"from Dataset as dataset where dataset.bronhouder.id = ?1 and dataset.type.id = ?2 and dataset.uuid = ?3 and dataset.actief=true")
					.setParameter(1, bronhouder.getId ())
					.setParameter(2, datasetType.getId ())
					.setParameter(3, uuid);
			List<Dataset> datasetList =  datasetQuery.getResultList();
			if (datasetList.size()>0){
				return datasetList.get(0);
			}else{
				return null;
			}
		}
	
	@Override
	@Transactional
	public void update(Dataset dataSet) {
		this.entityManager.merge(dataSet);
	}

	@Override
	@Transactional
	public void delete(Dataset dataSet) {
//		Dataset datasetToDelete = this.entityManager.find(Dataset.class, dataSet.getId());//this alone will raise exception 'Removing a detached instance'
		Dataset datasetToDelete = this.entityManager.getReference(Dataset.class, dataSet.getId());
		this.entityManager.remove(datasetToDelete);
	}

	// -------------
	// Stam tabellen
	// -------------

	@Transactional
	@Override
	public void create(JobType jobType) {
		this.entityManager.persist(jobType);
	}

	@Override
	public JobType getJobType(Long pk) {
		return this.entityManager.find(JobType.class, pk);
	}

	@Override
	public List<JobType> getAllJobTypes() {
		final TypedQuery<JobType> jobTypeQuery;
		jobTypeQuery = entityManager.createQuery("from JobType as jobType order by jobType.naam", JobType.class);
		return jobTypeQuery.getResultList();
	}

	@Override
	public JobType getJobTypeByName(String naam) {
		final TypedQuery<JobType> jobTypeQuery;
		jobTypeQuery = entityManager.createQuery(
				"from JobType as jobtype where jobtype.naam = ?1", JobType.class).setParameter(1, naam);
		List <JobType> jobTypes = jobTypeQuery.getResultList();
		if ((jobTypes != null && jobTypes.size() >= 1)) {
			// should only be one task
			return jobTypes.get(0);
		}
		return null;
	}

	@Transactional
	@Override
	public void delete(JobType jobType) {
		JobType jobTypeToDelete = this.entityManager.getReference(JobType.class, jobType.getId());
		this.entityManager.remove(jobTypeToDelete);
	}

	@Transactional
	@Override
	public void create(Bronhouder bronhouder) {
		this.entityManager.persist(bronhouder);
	}

	@Transactional
	@Override
	public void update(Bronhouder bronhouder) {
		this.entityManager.merge(bronhouder);
	}

	@Override
	public Bronhouder getBronhouder(Long pk) {
		return this.entityManager.find(Bronhouder.class, pk);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Bronhouder> getAllBronhouders() {
		Query bronhouderQuery = null;
		bronhouderQuery = entityManager.createQuery("from Bronhouder as bronhouder order by bronhouder.contactNaam");
		return bronhouderQuery.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Bronhouder getBronhouderByContactNaam(String naam) {
		Bronhouder bronhouder = null;
		Query bronhouderQuery = null;
		bronhouderQuery = entityManager.createQuery("from Bronhouder as bronhouder where bronhouder.contactNaam = ?1")
				.setParameter(1, naam);
		List <Bronhouder> bronhouders = bronhouderQuery.getResultList();
		if ((bronhouders != null && bronhouders.size() >= 1)) {
			// should only be one task
			bronhouder = bronhouders.get(0);
		}
		return bronhouder ;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Bronhouder getBronhouderByNaam(String naam) {
		Bronhouder bronhouder = null;
		Query bronhouderQuery = null;
		bronhouderQuery = entityManager.createQuery("from Bronhouder as bronhouder where bronhouder.naam = ?1")
				.setParameter(1, naam);
		List <Bronhouder> bronhouders = bronhouderQuery.getResultList();
		if ((bronhouders != null && bronhouders.size() >= 1)) {
			// should only be one task
			bronhouder = bronhouders.get(0);
		}
		return bronhouder ;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Bronhouder getBronhouderByCommonName (final String commonName) {
		Bronhouder bronhouder = null;
		Query bronhouderQuery = null;
		bronhouderQuery = entityManager.createQuery("from Bronhouder as bronhouder where bronhouder.commonName = ?1")
				.setParameter(1, commonName);
		List <Bronhouder> bronhouders = bronhouderQuery.getResultList();
		if ((bronhouders != null && bronhouders.size() >= 1)) {
			// should only be one task
			bronhouder = bronhouders.get(0);
		}
		return bronhouder ;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Bronhouder getBronhouderByCode (final String code) {
		if (code == null) {
			return null;
		}
		
		final List<Bronhouder> bronhouders = entityManager
				.createQuery("from Bronhouder as bronhouder where bronhouder.code = ?1", Bronhouder.class)
				.setParameter (1, code)
				.getResultList ();

		// Code has a unique constraint, therefore there can be at most one bronhouder
		// in the list.
		return bronhouders.isEmpty () ? null : bronhouders.get (0);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isUserAuthorizedForBronhouder (Bronhouder bronhouder, String userName) {
		if (bronhouder == null) {
			throw new NullPointerException ("bronhouder shouldn't be null");
		}
		if (userName == null) {
			throw new NullPointerException ("userName cannot be null");
		}
		
		final Gebruiker gebruiker = getGebruiker (userName);
		
		return gebruiker != null && gebruiker.isSuperuser ();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isUserAuthorizedForThema (final Bronhouder bronhouder, final Thema theme, final String username, final TypeGebruik typeGebruik) {
		if (bronhouder == null) {
			throw new NullPointerException ("bronhouder cannot be null");
		}
		if (theme == null) {
			throw new NullPointerException ("theme cannot be null");
		}
		if (username == null) {
			throw new NullPointerException ("username cannot be null");
		}
		if (typeGebruik == null) {
			throw new NullPointerException ("typeGebruik cannot be null");
		}
		
		final Gebruiker gebruiker = getGebruiker (username);
		final BronhouderThema bronhouderThema = getBronhouderThema (bronhouder, theme);
		if (gebruiker == null || bronhouderThema == null) {
			return false;
		}
		
		for (final GebruikerThemaAutorisatie gta: getGebruikerThemaAutorisatie (gebruiker)) {
			if (gta.getBronhouderThema ().equals (bronhouderThema) && gta.getTypeGebruik ().isAllowed (typeGebruik)) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Deletes the given bronhouder, as well as related entities from the following entity types:
	 * - bronhouder geometry
	 * - BronhouderTheme
	 * - Dataset
	 * - EtlJob
	 * - GebruikerThemaAutorisatie
	 * - JobLog
	 * - Job
	 */
	@Transactional
	@Override
	public void delete(Bronhouder bronhouder) {
		// Delete joblog:
		entityManager
			.createNativeQuery ("delete from manager.joblog where job_id in (select job_id from manager.etljob where bronhouder_id = ?1)")
			.setParameter (1, bronhouder.getId ())
			.executeUpdate ();
		
		// Delete jobs:
		entityManager
			.createNativeQuery ("select id into temporary table delete_job_id_temp from manager.etljob where bronhouder_id = ?1")
			.setParameter (1, bronhouder.getId ())
			.executeUpdate ();
		
		entityManager
			.createNativeQuery ("delete from manager.etljob where id in (select id from delete_job_id_temp)")
			.executeUpdate ();
		
		entityManager
			.createNativeQuery ("delete from manager.job where id in (select id from delete_job_id_temp)")
			.executeUpdate ();
		
		// Delete authorization:
		entityManager
			.createNativeQuery ("delete from manager.gebruikerthemaautorisatie where bronhouderthema_bronhouder_id = ?1")
			.setParameter (1, bronhouder.getId ())
			.executeUpdate ();
		
		// Delete bronhouderthema:
		entityManager
			.createNativeQuery ("delete from manager.bronhouderthema where bronhouder_id = ?1")
			.setParameter (1, bronhouder.getId ())
			.executeUpdate ();
		
		// Delete bronhouder geometry:
		entityManager
			.createNativeQuery ("delete from manager.bronhouder_geometry where bronhouder_id = ?1")
			.setParameter (1, bronhouder.getId ())
			.executeUpdate ();
		
		// Delete the bronhouder itself:
		final Bronhouder bronhouderToDelete = this.entityManager.getReference (Bronhouder.class, bronhouder.getId ());
		this.entityManager.remove (bronhouderToDelete);
	}

	@Override
	public Geometry getBronhouderGeometry (final Bronhouder bronhouder) {
		final Query query = entityManager.createNativeQuery ("select ST_AsBinary(geom) from manager.bronhouder_geometry where bronhouder_id = ?1");
		
		query.setParameter(1, bronhouder.getId ());
		
		byte[] value;
		
		try {
			value = (byte[])query.getSingleResult ();
		} catch (NoResultException e) {
			return null;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}

		try {
			return WKBReader.read (value, null);
		} catch (ParseException e) {
			return null;
		}
	}
	

	@Transactional
	@Override
	public void create(DatasetType datasetType) {
		this.entityManager.persist(datasetType);
	}

	@Override
	public DatasetType getDatasetType(Long pk) {
		return this.entityManager.find(DatasetType.class, pk);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DatasetType> getAllDatasetTypes() {
		Query datasetTypeQuery = null;
		datasetTypeQuery = entityManager.createQuery("from DatasetType as datasetType order by datasetType.naam");
		return datasetTypeQuery.getResultList();
	}

	@Override
	public DatasetType getDatasetTypeByName(String naam) {
		DatasetType datasetType = null;
		Query datasetTypeQuery = null;
		datasetTypeQuery = entityManager.createQuery("from DatasetType as datasetType where datasetType.naam = ?1")
				.setParameter(1, naam);
		@SuppressWarnings("unchecked")
		List <DatasetType> datasetTypes = datasetTypeQuery.getResultList();
		if ((datasetTypes != null && datasetTypes.size() >= 1)) {
			// should only be one task
			datasetType = datasetTypes.get(0);
		}
		return datasetType;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<DatasetType> getDatasetTypesByThema(Thema thema) {
		if (thema == null){
			throw new IllegalArgumentException ("An instance of Thema must be provided in the search criteria.");
		}
		Query datasetTypeQuery = null;
		datasetTypeQuery = entityManager.createQuery("from DatasetType as datasetType where thema.id = ?1 order by datasetType.naam")
				.setParameter(1, thema.getId());
		return datasetTypeQuery.getResultList();
	}

	@Transactional
	@Override
	public void delete(DatasetType datasetType) {
		DatasetType datasetTypeToDelete = this.entityManager.getReference(DatasetType.class, datasetType.getId());
		this.entityManager.remove(datasetTypeToDelete);
	}

	@Transactional
	@Override
	public void create(Thema thema) {
		this.entityManager.persist(thema);
	}

	@Override
	public Thema getThema(Long pk) {
		return this.entityManager.find(Thema.class, pk);
	}

	@Transactional
	@Override
	public void update(Thema thema) {
		this.entityManager.merge(thema);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Thema> getAllThemas() {
		Query themaQuery = null;
		themaQuery = entityManager.createQuery("from Thema as thema order by thema.naam");		
		return themaQuery.getResultList();
	}
	
	@Override
	public List<Thema> getAllThemas (final Bronhouder bronhouder) {
		final TypedQuery<Thema> query = entityManager.createQuery ("select a.thema from BronhouderThema a where a.bronhouder = ?1", Thema.class)
				.setParameter (1, bronhouder);
		
		return query.getResultList ();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Thema getThemaByName(String naam) {
		Thema thema = null;
		Query themaQuery = null;
		themaQuery = entityManager.createQuery("from Thema as thema where thema.naam = ?1")
				.setParameter(1, naam);
		List <Thema> themas = themaQuery.getResultList();
		if ((themas != null && themas.size() >= 1)) {
			// should only be one task
			thema = themas.get(0);
		}
		return thema;
	}

	@Transactional
	@Override
	public void delete(Thema thema) {
		Thema themaToDelete = this.entityManager.getReference(Thema.class, thema.getId());
		this.entityManager.remove(themaToDelete);
	}

	@Override
	@Transactional(propagation=Propagation.MANDATORY)
//	@Transactional (propagation = Propagation.REQUIRES_NEW)
	public void create(JobLog log) {
		this.entityManager.persist(log);
	}

	/* (non-Javadoc)
	 * @see nl.ipo.cds.dao.ManagerDao#delete(nl.ipo.cds.domain.JobLog)
	 */
	@Override
	@Transactional
	public void delete(JobLog jobLog) {
		JobLog jobLogToDelete = this.entityManager.getReference(JobLog.class, jobLog.getId());
		this.entityManager.remove(jobLogToDelete);
	}

	@Override
	public long getJobFaseLogCount (AbstractJob job) {
		final TypedQuery<Long> query = entityManager.createQuery("select count(*) from JobLog as l where l.job = ?1", Long.class);
		
		query
			.setParameter (1, job)
			.setMaxResults (1);
		
		return query.getSingleResult ();
	}
	
	@Override
	public List<JobLog> getLastCompletedJobsLog () {
		final TypedQuery<JobLog> query = entityManager.createQuery ("select log from JobLog as log join log.job as job where job.status in (?1, ?2) and job.id = " +
				"(select lastJob.id from Job as lastJob where (lastJob.bronhouder = job.bronhouder and lastJob.type = job.type and lastJob.uuid = job.uuid) and job.status in (?1, ?2) order by job.id desc)",
				JobLog.class);
		
		query
			.setParameter (1, Job.Status.FINISHED)
			.setParameter (2, Job.Status.ABORTED);
		
		return query.getResultList ();
	}
	
	@Override
	public List<JobLog> findJobLog (AbstractJob job) {
		return findJobLog (new JobLogCriteria (job));
	}
	
	@Override
	public List<JobLog> findJobLog (JobLogCriteria criteria) {

		if (!criteria.hasJob ()) {
			throw new IllegalArgumentException ("Either an instance of Job or JobFase must be provided in the search criteria.");
		}
		
		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder ();
		final CriteriaQuery<JobLog> query = criteriaBuilder.createQuery (JobLog.class);
		final Root<JobLog> from = query.from (JobLog.class);
		final List<Predicate> predicates = new ArrayList<Predicate> ();
		
		// Collect predicates:
		if (criteria.hasJob ()) {
			predicates.add (criteriaBuilder.equal (from.get ("job"), criteria.getJob ()));
		}
		
		// Determine order:
		Order order;
		String sortField;
		if (criteria.getSortField () == SortField.BaseSortField.ID) {
			sortField = "id";
		} else {
			sortField = "message";
		}
		if (criteria.getSortOrder () == SortOrder.ASCENDING) {
			order = criteriaBuilder.asc (from.get (sortField));
		} else {
			order = criteriaBuilder.desc (from.get (sortField));
		}
		
		
		CriteriaQuery<JobLog> select = query.select (from);

		query
			.select (from)
			.where (predicates.toArray(new Predicate[predicates.size ()]))
			.orderBy (order);
		
		final TypedQuery<JobLog> typedQuery = entityManager.createQuery (select);
		typedQuery.setFirstResult (criteria.getOffset ());
		if (criteria.hasLimit ()) {
			typedQuery.setMaxResults (criteria.getLimit ());
		}
		
		return typedQuery.getResultList ();
	}

	@Override
	public List<EtlJob> findJob(JobCriteria criteria) {
		if (!criteria.hasDatasetType()) {
			throw new IllegalArgumentException ("An instance of Dataset must be provided in the search criteria.");
		}
		
		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder ();
		final CriteriaQuery<EtlJob> query = criteriaBuilder.createQuery (EtlJob.class);
		final Root<EtlJob> from = query.from (EtlJob.class);
		final List<Predicate> predicates = new ArrayList<Predicate> ();
		
		// Collect predicates:
		if(criteria.getBronhouder() != null){
			predicates.add (criteriaBuilder.equal(from.get ("bronhouder"), criteria.getBronhouder()));
		}
		if(criteria.getDatasetType() != null){
			predicates.add (criteriaBuilder.equal(from.get ("datasettype"), criteria.getDatasetType()));
		}
		if(criteria.getCreatieTijd() != null){
			predicates.add (criteriaBuilder.greaterThan(from.get("createTime").as(Timestamp.class), criteria.getCreatieTijd()));
		}
		if(criteria.isVerversen() != null){
			predicates.add (criteriaBuilder.equal(from.get("verversen").as(Boolean.class), criteria.isVerversen()));
		}

		if(criteria.getJobStatus() != null){
			predicates.add (criteriaBuilder.equal(from.get("status").as(Job.Status.class), criteria.getJobStatus()));
		}
		
		// Determine order:
		Order order;
		String sortField = "id";

		if (criteria.getSortOrder () == SortOrder.ASCENDING) {
			order = criteriaBuilder.asc (from.get (sortField));
		} else {
			order = criteriaBuilder.desc (from.get (sortField));
		}
		
		CriteriaQuery<EtlJob> select = query.select (from);

		query
			.select (from)
			.where (predicates.toArray(new Predicate[predicates.size ()]))
			.orderBy (order);
		
		final TypedQuery<EtlJob> typedQuery = entityManager.createQuery (select);
		typedQuery.setFirstResult (criteria.getOffset ());
		if (criteria.hasLimit ()) {
			typedQuery.setMaxResults (criteria.getLimit ());
		}
		
		return typedQuery.getResultList ();
	}

	@Override
	public List<Dataset> findDataset(DatasetCriteria criteria) {
		if (!criteria.hasBronhouder()) {
			throw new IllegalArgumentException ("An instance of Bronhouder must be provided in the search criteria.");
		}
		
		final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder ();
		final CriteriaQuery<Dataset> query = criteriaBuilder.createQuery (Dataset.class);
		final Root<Dataset> from = query.from (Dataset.class);
		final List<Predicate> predicates = new ArrayList<Predicate> ();
		
		// Collect predicates:
		predicates.add(criteriaBuilder.equal(from.get("bronhouder"), criteria.getBronhouder()));
		if(criteria.getId() != null){
			predicates.add(criteriaBuilder.equal(from.get("id").as(Long.class), criteria.getId()));
		}
		if(criteria.getDatasetType() != null){
			predicates.add(criteriaBuilder.equal(from.get("type"), criteria.getDatasetType()));
		}
		if(criteria.getThema () != null) {
			predicates.add (criteriaBuilder.equal (from.get ("type").get ("thema"), criteria.getThema ()));
		}
		
		// Determine order:
		Order order;
		String sortField = "id";

		if (criteria.getSortOrder () == SortOrder.ASCENDING) {
			order = criteriaBuilder.asc (from.get (sortField));
		} else {
			order = criteriaBuilder.desc (from.get (sortField));
		}
		
		CriteriaQuery<Dataset> select = query.select (from);

		query
			.select (from)
			.where (predicates.toArray(new Predicate[predicates.size ()]))
			.orderBy (order);
		
		final TypedQuery<Dataset> typedQuery = entityManager.createQuery (select);
		typedQuery.setFirstResult (criteria.getOffset ());
		if (criteria.hasLimit ()) {
			typedQuery.setMaxResults (criteria.getLimit ());
		}
		
		return typedQuery.getResultList ();
	}

	// -------------
	// Gebruikers:
	// -------------
	/**
	 * Convenience method for binding or rebinding a user, see create and update for details.
	 * 
	 * @param gebruiker
	 * @param rebind
	 */
	private void bindGebruiker (LdapGebruiker gebruiker, boolean rebind) {
		final DistinguishedName dn;
		if (gebruiker.getDistinguishedName () == null) {
			dn = new DistinguishedName (getLdapPeopleBaseDn ());
			dn.add ("uid", gebruiker.getGebruikersnaam ());
		} else {
			dn = new DistinguishedName (gebruiker.getDistinguishedName ());
		}
		
		try {
			final Attributes attributes = new GebruikerAttributesMapper ().toAttributes (gebruiker);
			
			// Bind or rebind the user:
			if (rebind) {
				ldapTemplate.rebind(dn, null, attributes);
			} else {
				ldapTemplate.bind (dn, null, attributes);
			}
			
			// Add group membership:
			final DistinguishedName gebruikerDn = new DistinguishedName (getLdapBase ()).append (new DistinguishedName (dn));
			if (!getUserDns ().contains (gebruikerDn)) { 
				final Attribute attribute = new BasicAttribute ("member", gebruikerDn.toString ());
				final ModificationItem modificationItem = new ModificationItem (DirContext.ADD_ATTRIBUTE, attribute);
				
				ldapTemplate.modifyAttributes (new DistinguishedName (getLdapGroupDn ()), new ModificationItem[] { modificationItem });
			}
		} catch (NamingException e) {
			throw new RuntimeException (e);
		}
	}
	
	private void createDbGebruiker (final DbGebruiker gebruiker) {
		this.entityManager.persist (gebruiker);
	}
	
	private void updateDbGebruiker (final DbGebruiker gebruiker) {
		this.entityManager.merge (gebruiker);
	}
	
	/**
	 * Persists a new user. The user must have a value for all required fields and must be unique.
	 * The user is created using an LDAP bind on the server.
	 * 
	 * @param gebruiker
	 */
	@Override
	@Transactional
	public void create (Gebruiker gebruiker) {
		if (getGebruiker (gebruiker.getGebruikersnaam ()) != null) {
			throw new IllegalArgumentException (String.format ("User %s already exists", gebruiker.getGebruikersnaam ()));
		}
		
		if (gebruiker.getLdapGebruiker () != null) {
			bindGebruiker (gebruiker.getLdapGebruiker (), false);
		}
		if (gebruiker.getDbGebruiker () != null) {
			final DbGebruiker existing = getDbGebruiker (gebruiker.getGebruikersnaam ());

			if (existing != null) {
				entityManager.merge (gebruiker.getDbGebruiker ());
			} else {
				createDbGebruiker (gebruiker.getDbGebruiker ());
			}
		}
	}
	
	/**
	 * Updates an existing user. The user must have been previously persisted.
	 * The user is updated using an LDAP rebind on the server, which is effectively the same as
	 * deleting and creating the user.
	 * 
	 * @param gebruiker
	 */
	@Override
	public void update(Gebruiker gebruiker) {
		// Test if the user exists, otherwise the unbind would be ignored:
		if (getGebruiker (gebruiker.getGebruikersnaam ()) == null) {
			throw new IllegalArgumentException ("Gebruiker `" + gebruiker.getGebruikersnaam () + "` does not exist.");
		}
		
		if (gebruiker.getLdapGebruiker () != null) {
			bindGebruiker (gebruiker.getLdapGebruiker (), true);
		}
		if (gebruiker.getDbGebruiker () != null) {
			updateDbGebruiker (gebruiker.getDbGebruiker ());
		}
		
	}
	
	/**
	 * Produces a set of DN's of all users that are a member of the CDS group in LDAP.
	 * 
	 * @return The DN's of all members of the CDS group in LDAP.
	 */
	@SuppressWarnings("unchecked")
	private Set<DistinguishedName> getUserDns () {
		return (Set<DistinguishedName>)ldapTemplate.lookup (
			getLdapGroupDn (),
			new AttributesMapper () {
				@Override
				public Object mapFromAttributes (final Attributes attributes) throws NamingException {
					final Attribute attribute = attributes.get ("member");
					final Set<DistinguishedName> values = new HashSet<DistinguishedName> ();
					
					for (int i = 0; i < attribute.size (); ++ i) {
						values.add (new DistinguishedName (attribute.get (i).toString ()));
					}
					
					return values;
				}
			});
		
	}
	
	private LdapGebruiker getLdapGebruiker (final String gebruikersnaam) {
		return getLdapGebruiker (gebruikersnaam, getUserDns ());
	}
	
	private LdapGebruiker getLdapGebruiker (final String gebruikersnaam, final Set<DistinguishedName> members) {
		final String query = String.format ("(&(objectClass=inetOrgPerson)(uid=%s))", gebruikersnaam);
		
		@SuppressWarnings("unchecked")
		final List<LdapGebruiker> searchResult = (List<LdapGebruiker>)ldapTemplate.search (
				new DistinguishedName (),
				query,
				new GebruikerContextMapper ()
			);

		if (searchResult.isEmpty ()) {
			return null;
		}
		
		final DistinguishedName gebruikerDn = new DistinguishedName (getLdapBase ()).append (new DistinguishedName (searchResult.get (0).getDistinguishedName ()));
		
		return members.contains (gebruikerDn) ? searchResult.get (0) : null;
	}
	
	private DbGebruiker getDbGebruiker (final String gebruikersnaam) {
		return this.entityManager.find (DbGebruiker.class, gebruikersnaam);
	}
	
	/**
	 * Returns a user by username, the username also corresponds with the 'uid' field in LDAP. Returns null
	 * if the user doesn't exist.
	 * 
	 * @param gebruikersnaam
	 * @return The user, or null.
	 */
	@Override
	public Gebruiker getGebruiker(String gebruikersnaam) {
		final LdapGebruiker ldapGebruiker = getLdapGebruiker (gebruikersnaam);
		final DbGebruiker dbGebruiker = getDbGebruiker (gebruikersnaam);
		
		// LDAP user is mandatory, return null if the user doesn't exist in LDAP:
		if (ldapGebruiker == null) {
			return null;
		}
		
		// DB user is optional, no need to check if it could be found.
		return new Gebruiker (ldapGebruiker, dbGebruiker);
	}
	
	private List<LdapGebruiker> getAllLdapGebruikers () {
		final Set<DistinguishedName> userDns = getUserDns ();
		final List<LdapGebruiker> gebruikers = new ArrayList<LdapGebruiker> ();
		
		for (final DistinguishedName dn: userDns) {
			final String username = dn.getValue ("uid");
			if (username == null) {
				continue;
			}
			
			final LdapGebruiker gebruiker = getLdapGebruiker (username);
			
			if (gebruiker != null) {
				gebruikers.add (gebruiker);
			}
		}
		
		Collections.sort (gebruikers, new Comparator<LdapGebruiker> () {
			@Override
			public int compare (final LdapGebruiker o1, final LdapGebruiker o2) {
				return o1.getGebruikersnaam ().compareTo (o2.getGebruikersnaam ());
			}
		});
		
		return Collections.unmodifiableList (gebruikers);
	}
	
	private List<DbGebruiker> getAllDbGebruikers () {
		return entityManager.createQuery ("from DbGebruiker as gebruiker order by gebruiker.gebruikersnaam", DbGebruiker.class).getResultList ();
		
	}
	
	/**
	 * Returns a list containing all available users, ordered by username. An empty list is returned
	 * if no users could be found.
	 * 
	 * @return A list containing all users.
	 */
	@Override
	public List<Gebruiker> getAllGebruikers() {
		final Map<String, DbGebruiker> dbGebruikers = new HashMap<String, DbGebruiker> ();
		
		for (final DbGebruiker dbGebruiker: getAllDbGebruikers ()) {
			dbGebruikers.put (dbGebruiker.getGebruikersnaam (), dbGebruiker);
		}
		
		final List<LdapGebruiker> ldapGebruikers = getAllLdapGebruikers ();
		final List<Gebruiker> gebruikers = new ArrayList<Gebruiker> (ldapGebruikers.size ());
		
		for (final LdapGebruiker ldapGebruiker: ldapGebruikers) {
			gebruikers.add (new Gebruiker (ldapGebruiker, dbGebruikers.get (ldapGebruiker.getGebruikersnaam ())));
		}
		
		return Collections.unmodifiableList (gebruikers);
	}
	
	/**
	 * Deletes the given user. The user must have been previously persisted.
	 * Performs an unbind on the LDAP server.
	 * 
	 * @param gebruiker The user to delete.
	 */
	@Override
	public void delete(Gebruiker gebruiker) {
		// Test if the user exists, otherwise the unbind would be ignored:
		if (getGebruiker (gebruiker.getGebruikersnaam ()) == null) {
			throw new IllegalArgumentException ("Gebruiker `" + gebruiker.getGebruikersnaam () + "` does not exist.");
		}
			
		if (gebruiker.getLdapGebruiker () != null) {
			final DistinguishedName dn;
			if (gebruiker.getLdapGebruiker ().getDistinguishedName () == null) {
				dn = new DistinguishedName (getLdapPeopleBaseDn ());
				dn.add ("uid", gebruiker.getLdapGebruiker ().getGebruikersnaam ());
			} else {
				dn = new DistinguishedName (gebruiker.getLdapGebruiker ().getDistinguishedName ());
			}
			
			ldapTemplate.unbind (dn);
			
			// Remove group membership:
			final DistinguishedName gebruikerDn = new DistinguishedName (getLdapBase ()).append (new DistinguishedName (dn));
			if (getUserDns ().contains (gebruikerDn)) { 
				final Attribute attribute = new BasicAttribute ("member", gebruikerDn.toString ());
				final ModificationItem modificationItem = new ModificationItem (DirContext.REMOVE_ATTRIBUTE, attribute);
				
				ldapTemplate.modifyAttributes (new DistinguishedName (getLdapGroupDn ()), new ModificationItem[] { modificationItem });
			}
		}
		
		if (gebruiker.getDbGebruiker () != null) {
			try {
				final DbGebruiker dbGebruikerToDelete = entityManager.getReference (DbGebruiker.class, gebruiker.getDbGebruiker ().getGebruikersnaam ());
				entityManager.remove (dbGebruikerToDelete);
			} catch (JpaObjectRetrievalFailureException e) {
				// Ignore this exception: it is valid for a user not to have database backing.
			} catch (EntityNotFoundException e) {
				// Ignore this exception: it is valid for a user not to have database backing.
			}
		}
	}
	
	/**
	 * Performs authentication for the given user by testing the given plaintext password.
	 * Authentication is performed using a bind operation on the LDAP server.
	 * 
	 * @param gebruiker
	 * @param wachtwoord
	 * @return True if the password is correct, false otherwise.
	 */
	@Override
	public boolean authenticate (final String gebruikersNaam, final String wachtwoord) {
		final LdapGebruiker ldapGebruiker = getLdapGebruiker (gebruikersNaam);
		if (ldapGebruiker == null) {
			return false;
		}
		
		final String query = String.format ("(&(objectClass=inetOrgPerson)(uid=%s))", gebruikersNaam);
		return ldapTemplate.authenticate (
				new DistinguishedName (), 
				query, 
				wachtwoord
			);
	}
	
	// -----------------------
	// -- Gebruikersrollen: --
	// -----------------------
	/* (non-Javadoc)
	 * @see nl.ipo.cds.dao.ManagerDao#getIdentity(java.lang.Long)
	 */
	@Override
	public Identity getIdentity(Class<Identity> identity, Long i) {
		return this.entityManager.find(identity, i);
		
	}

	@Override
	public AttributeMapping getAttributeMapping (final Dataset dataset, final String attributeName) {
		final TypedQuery<AttributeMapping> query = entityManager.createQuery (
				"from AttributeMapping mapping where mapping.dataset = ?1 and attributeName = ?2",
				AttributeMapping.class
			)
			.setParameter (1, dataset)
			.setParameter (2, attributeName);
		
		try {
			return query.getSingleResult ();
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (NoResultException e) {
			return null;
		}
	}
	
	@Override
	public List<AttributeMapping> getAttributeMappings (final Dataset dataset) {
		return entityManager.createQuery (
				"from AttributeMapping as mapping where mapping.dataset = ?1",
				AttributeMapping.class
			)
			.setParameter (1, dataset)
			.getResultList ();
	}
	
	@Override
	public List<AttributeMapping> getValidAttributeMappings (final Dataset dataset) {
		return entityManager.createQuery (
				"from AttributeMapping as mapping where mapping.dataset = ?1 and mapping.valid = true",
				AttributeMapping.class
			)
			.setParameter (1, dataset)
			.getResultList ();
	}
	
	@Override
	public void create (final AttributeMapping attributeMapping) {
		entityManager.persist (attributeMapping);
	}
	
	@Override
	public void update (final AttributeMapping attributeMapping) {
		entityManager.merge (attributeMapping);
	}
	
	@Override
	public void delete (final AttributeMapping attributeMapping) {
		final AttributeMapping deleteAttributeMapping = entityManager.getReference (AttributeMapping.class, attributeMapping.getId ());
		entityManager.remove (deleteAttributeMapping);
	}
	
	@Override
	public void create (final MappingOperation mappingOperation) {
		entityManager.persist (mappingOperation);
	}
	
	@Override
	public void update (final MappingOperation mappingOperation) {
		entityManager.merge (mappingOperation);
	}
	
	@Override
	public void delete (final MappingOperation mappingOperation) {
		final MappingOperation deleteMappingOperation = entityManager.getReference (MappingOperation.class, mappingOperation.getId ());
		entityManager.remove (deleteMappingOperation);
	}

	@Override
	public DatasetFilter getDatasetFilter (final Dataset dataset) {
		try {
			return entityManager
					.createQuery ("from DatasetFilter as filter where filter.dataset = ?1", DatasetFilter.class)
					.setParameter (1, dataset)
					.getSingleResult ();
		} catch (EmptyResultDataAccessException e) {
			return null;
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public void create (final DatasetFilter datasetFilter) {
		entityManager.persist (datasetFilter);
	}

	@Override
	public void update (final DatasetFilter datasetFilter) {
		entityManager.merge (datasetFilter);
	}

	@Override
	public void delete (final DatasetFilter datasetFilter) {
		final DatasetFilter deleteFilter = entityManager.getReference (DatasetFilter.class, datasetFilter.getId ());
		entityManager.remove (deleteFilter);
	}

	@Override
	public void create (final FilterExpression expression) {
		entityManager.persist (expression);
	}

	@Override
	public void update (final FilterExpression expression) {
		entityManager.merge (expression);
	}

	@Override
	public void delete (final FilterExpression expression) {
		final FilterExpression deleteExpression = entityManager.getReference (FilterExpression.class, expression.getId ());
		entityManager.remove (deleteExpression);
	}
	
	public List<CodeListMapping> getCodeListMappings () {
		return entityManager
			.createQuery ("from CodeListMapping order by codeSpace", CodeListMapping.class)
			.getResultList ();
	}
	
	public void create (final CodeListMapping mapping) {
		entityManager.persist (mapping);
	}
	
	public void update (final CodeListMapping mapping) {
		entityManager.merge (mapping);
	}
	
	public void delete (final CodeListMapping mapping) {
		final CodeListMapping deleteMapping = entityManager.getReference (CodeListMapping.class, mapping.getCodeSpace ());
		entityManager.remove (deleteMapping);
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> getChangedMetadataDocuments() {
		final Query query = entityManager.createNativeQuery("select * from manager.metadatadocument_update_datum");
		return query.getResultList();
	}
	
	@Transactional
	public List<MetadataDocument> getAllMetadataDocuments() {
		TypedQuery<MetadataDocument> metadataQuery = entityManager.createQuery("from MetadataDocument as md order by md.documentName", MetadataDocument.class);
		return metadataQuery.getResultList();
	}

	@Override
	@Transactional
	public MetadataDocument getMetadataDocument(Long id) {
		return entityManager.find(MetadataDocument.class, id);
	}

	@Override
	@Transactional
	public void create(MetadataDocument metatataDocument) {
		entityManager.persist(metatataDocument);
	}

	@Override
	@Transactional
	public void update(MetadataDocument metatataDocument) {
		entityManager.merge(metatataDocument);		
	}

	@Override
	@Transactional
	public void delete(MetadataDocument metatataDocument) {
		entityManager.remove(metatataDocument);		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BronhouderThema getBronhouderThema (final Bronhouder bronhouder, final Thema thema) {
		final List<BronhouderThema> result = entityManager
			.createQuery ("from BronhouderThema bt where bt.bronhouder = ?1 and bt.thema = ?2", BronhouderThema.class)
			.setParameter (1, bronhouder)
			.setParameter (2, thema)
			.getResultList ();
		
		if (result.size () != 1) {
			return null;
		}
		
		return result.get (0);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<BronhouderThema> getBronhouderThemas () {
		return entityManager
			.createQuery ("from BronhouderThema b order by b.bronhouder.naam asc, b.thema.naam asc", BronhouderThema.class)
			.getResultList ();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<BronhouderThema> getBronhouderThemas (final Bronhouder bronhouder) {
		return entityManager
			.createQuery ("from BronhouderThema b where b.bronhouder = ?1 order by b.thema.naam asc", BronhouderThema.class)
			.setParameter (1, bronhouder)
			.getResultList ();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<BronhouderThema> getBronhouderThemas (final Thema thema) {
		return entityManager
			.createQuery ("from BronhouderThema b where b.thema = ?1 order by b.bronhouder.naam asc", BronhouderThema.class)
			.setParameter (1, thema)
			.getResultList ();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create (final BronhouderThema bronhouderThema) {
		entityManager.persist (bronhouderThema);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public void delete (final BronhouderThema bronhouderThema) {
		// Delete related GebruikerThemaAutorisatie:
		final List<GebruikerThemaAutorisatie> gebruikerAuth = new ArrayList<GebruikerThemaAutorisatie> (
					entityManager
						.createQuery ("from GebruikerThemaAutorisatie gta where gta.bronhouderThema = ?1", GebruikerThemaAutorisatie.class)
						.setParameter (1, bronhouderThema)
						.getResultList()
				);
		
		for (final GebruikerThemaAutorisatie gat: gebruikerAuth) {
			entityManager.remove (gat);
		}

		// Delete the BronhouderThema:
		final BronhouderThema bronhouderThemaToDelete = entityManager
				.createQuery ("from BronhouderThema bt where bt.bronhouder = ?1 and bt.thema = ?2", BronhouderThema.class)
				.setParameter (1, bronhouderThema.getBronhouder ())
				.setParameter (2, bronhouderThema.getThema ())
				.getSingleResult ();
		
		entityManager.remove (bronhouderThemaToDelete);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public GebruikerThemaAutorisatie createGebruikerThemaAutorisatie (final Gebruiker gebruiker, final BronhouderThema bronhouderThema, final TypeGebruik typeGebruik) {
		if (gebruiker == null) {
			throw new NullPointerException ("gebruiker cannot be null");
		}
		if (bronhouderThema == null) {
			throw new NullPointerException ("bronhouderThema cannot be null");
		}
		if (typeGebruik == null) {
			throw new NullPointerException ("typeGebruik cannot be null");
		}
		
		// Make sure the user has a database backing:
		update (gebruiker);
		
		// Create a new GebruikerThemaAutorisatie:
		final GebruikerThemaAutorisatie gta = new GebruikerThemaAutorisatie (gebruiker.getDbGebruiker (), bronhouderThema, typeGebruik);
		
		entityManager.persist (gta);
		
		return gta;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional
	public void delete (final GebruikerThemaAutorisatie gebruikerThemaAutorisatie) {
		if (gebruikerThemaAutorisatie == null) {
			throw new NullPointerException ("gebruikerThemaAutorisatie cannot be null");
		}
		
		final GebruikerThemaAutorisatie instanceToDelete = entityManager
			.createQuery ("from GebruikerThemaAutorisatie gta where gta.gebruiker = ?1 and gta.bronhouderThema = ?2", GebruikerThemaAutorisatie.class)
			.setParameter (1, gebruikerThemaAutorisatie.getGebruiker ())
			.setParameter (2, gebruikerThemaAutorisatie.getBronhouderThema ())
			.getSingleResult ();
		
		entityManager.remove (instanceToDelete);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<GebruikerThemaAutorisatie> getGebruikerThemaAutorisatie () {
		return entityManager
			.createQuery ("from GebruikerThemaAutorisatie gta order by gta.gebruiker.gebruikersnaam asc, gta.bronhouderThema.thema.naam asc, gta.bronhouderThema.bronhouder.naam asc", GebruikerThemaAutorisatie.class)
			.getResultList ();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<GebruikerThemaAutorisatie> getGebruikerThemaAutorisatie (final Gebruiker gebruiker) {
		if (gebruiker == null) {
			throw new NullPointerException ("gebruiker cannot be null");
		}
		
		return entityManager
			.createQuery ("from GebruikerThemaAutorisatie gta where gta.gebruiker = ?1 order by gta.gebruiker.gebruikersnaam asc, gta.bronhouderThema.thema.naam asc, gta.bronhouderThema.bronhouder.naam asc", GebruikerThemaAutorisatie.class)
			.setParameter (1, gebruiker.getDbGebruiker ())
			.getResultList ();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<GebruikerThemaAutorisatie> getGebruikerThemaAutorisatie (final Bronhouder bronhouder) {
		if (bronhouder == null) {
			throw new NullPointerException ("bronhouder cannot be null");
		}
		
		return entityManager
				.createQuery ("from GebruikerThemaAutorisatie gta where gta.bronhouderThema.bronhouder = ?1 order by gta.gebruiker.gebruikersnaam asc, gta.bronhouderThema.thema.naam asc, gta.bronhouderThema.bronhouder.naam asc", GebruikerThemaAutorisatie.class)
				.setParameter (1, bronhouder)
				.getResultList ();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<GebruikerThemaAutorisatie> getGebruikerThemaAutorisatie(final Thema thema) {
		if (thema == null) {
			throw new NullPointerException ("thema cannot be null");
		}
		
		return entityManager
				.createQuery ("from GebruikerThemaAutorisatie gta where gta.bronhouderThema.thema = ?1 order by gta.gebruiker.gebruikersnaam asc, gta.bronhouderThema.thema.naam asc, gta.bronhouderThema.bronhouder.naam asc", GebruikerThemaAutorisatie.class)
				.setParameter (1, thema)
				.getResultList ();
	}
}
