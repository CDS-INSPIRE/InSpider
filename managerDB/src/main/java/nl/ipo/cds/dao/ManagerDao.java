package nl.ipo.cds.dao;

import java.util.List;

import nl.idgis.commons.jobexecutor.AbstractJob;
import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobLogger.LogLevel;
import nl.ipo.cds.domain.AttributeMapping;
import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.CodeListMapping;
import nl.ipo.cds.domain.Dataset;
import nl.ipo.cds.domain.DatasetFilter;
import nl.ipo.cds.domain.DatasetType;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.domain.FilterExpression;
import nl.ipo.cds.domain.Gebruiker;
import nl.ipo.cds.domain.GebruikersRol;
import nl.ipo.cds.domain.Identity;
import nl.ipo.cds.domain.JobLog;
import nl.ipo.cds.domain.JobType;
import nl.ipo.cds.domain.MappingOperation;
import nl.ipo.cds.domain.MetadataDocument;
import nl.ipo.cds.domain.Rol;
import nl.ipo.cds.domain.Thema;

import org.deegree.geometry.Geometry;

public interface ManagerDao {

	// -------------
	// Werk tabellen
	// -------------
	// JOB
	
	public void create(AbstractJob job); // C

	public AbstractJob getJob(Long i); // R
	public AbstractJob getJob (Job job);
	
	public List<AbstractJob> getAllJobs();

	/**
	 * Get the last job that has status CREATED.
	 * @return Job or null if no job available.
	 */
	public AbstractJob getLastJob();

	/**
	 * @param bronhouder
	 * @param datasetType 
	 * @param uuid 
	 * @return list of jobs conformimg to the arguments, order by job.id (descending)
	 */
	public List<EtlJob> getJobsByDataset(Bronhouder bronhouder, DatasetType datasetType, String uuid);

	public List<EtlJob> getJobsByStatus(Job.Status status);
	
	/**
	 * Returns the current 'pending' job for the given argument. These jobs have the status CREATED or STARTED.
	 * 
	 * @param bronhouder
	 * @param datasetType 
	 * @param uuid 
	 * @return The current pending job for the given arguments, or null.
	 */
	public EtlJob getPendingJob (Bronhouder bronhouder, DatasetType datasetType, String uuid);
	
	/**
	 * Returns the current 'pending' job that matches the job argument. These jobs have the status CREATED or STARTED.
	 * 
	 * @param job to match .
	 * @return
	 */
	public EtlJob getPendingJob (EtlJob job);
	
	/**
	 * Returns the last completed (FINISHED or ABORTED) job for the given argument, or
	 * null if no such job exists.
	 * 
	 * @param bronhouder
	 * @param datasetType 
	 * @param uuid 
	 * @return The last completed job for the given arguments, or null.
	 */
	public EtlJob getLastCompletedJob (Bronhouder bronhouder, DatasetType datasetType, String uuid);

	/**
	 * Returns the last completed (FINISHED or ABORTED) job for the given argument, or
	 * null if no such job exists.
	 * 
	 * @param job to match
	 * @return
	 */
	public EtlJob getLastCompletedJob (EtlJob job);

	/**
	 * Returns the last completed validation (FINISHED or ABORTED) job for the given argument,
	 * null if no such job exists.
	 * @param bronhouder 
	 * @param datasetType 
	 * @param uuid 
	 * 
	 * @return The last completed job for the given arguments, or null.
	 */
	public EtlJob getLastValidationJob (Bronhouder bronhouder, DatasetType datasetType, String uuid);

	/**
	 * Returns the last completed validation (FINISHED or ABORTED) job for the given argument,
	 * null if no such job exists.
	 * @param job  to match
	 * @return
	 */
	public EtlJob getLastValidationJob(EtlJob job);	

	/**
	 * Returns the last successful (FINISHED) import job for the given dataset, or
	 * null if no such job exists.
	 * 
	 * @param bronhouder
	 * @param datasetType 
	 * @param uuid 
	 * @return The last import job for this given arguments with status FINISHED.
	 */
	public EtlJob getLastImportJob (Bronhouder bronhouder, DatasetType datasetType, String uuid);
	
	/**
	 * Returns the last successful (FINISHED) import job for the given dataset, or
	 * null if no such job exists.
	 * @param job to match
	 * @return
	 */
	public EtlJob getLastImportJob (EtlJob job);
	
	/**
	 * Returns the last successful (FINISHED) transform job for the given argument, or
	 * null if no such job exists.
	 * 
	 * @param jobStatus
	 * @return The last transform job for this given argument with status FINISHED.
	 */
	public EtlJob getLastTransformJob (Job.Status jobStatus);

	/**
	 * Returns all themas for which data has been successfully imported, but not transformed yet.
	 * 
	 * @return list of themas, for which data needs to be transformed
	 */	
	List<Thema> getImportedThemasWithoutSubsequentTransform();

	/**
	 * Returns all themas for which data has been successfully removed, but not transformed yet.
	 * 
	 * @return list of themas, for which data needs to be removed
	 */		
	List<Thema> getRemovedThemasWithoutSubsequentTransform();	
	
	/**
	 * Returns the last completed jobs. Results are ordered by 'bronhouder'
	 * and 'dataset'.
	 * 
	 * @return A list of last completed jobs.
	 */
	public List<JobInfo> getLastCompletedJobs ();
	
	public long getJobLogCount (AbstractJob job);
	public long getJobLogCount (AbstractJob job, LogLevel logLevel);
	
	public void update(AbstractJob job); // U
	
	public void update (EtlJob job);

	public void delete(AbstractJob job); // D
	
	public void delete (EtlJob job);
	
	// DATASET
	public void create(Dataset dataSet); // C

	public Dataset getDataSet(Long i); // R

	public List<Dataset> getAllDatasets();

	public List<Dataset> getDatasetsByDatasetType(DatasetType dataSetType);

	public List<Dataset> getDatasetsByBronhouder(Bronhouder bronhouder);

	public List<Dataset> getDatasetsByUUID(String uuid);

	public Dataset getDataset(Bronhouder bronhouder, DatasetType datasetType, String uuid);
	
	public void update(Dataset dataSet); // U

	public void delete(Dataset dataSet); // D

	// -------------
	// Stam tabellen
	// -------------
	// JOB_TYPE
	public void create(JobType jobType); // C

	public JobType getJobType(Long i); // R

	public List<JobType> getAllJobTypes();

	public JobType getJobTypeByName(String naam); // R

	public void delete(JobType jobType); // D

	// BRONHOUDER
	public void create(Bronhouder bronhouder); // C

	public void update(Bronhouder bronhouder); // U

	public Bronhouder getBronhouder(Long i); // R

	public List<Bronhouder> getAllBronhouders();

	public Bronhouder getBronhouderByContactNaam(String naam); // R

	public Bronhouder getBronhouderByNaam(String naam); // R

	public Bronhouder getBronhouderByCommonName(String code); // R
	
	public List<Bronhouder> getBronhoudersByUsername(String username); // R

	public void delete(Bronhouder bronhouder); // D
	
	public Geometry getBronhouderGeometry (Bronhouder bronhouder);
	
	// GEBRUIKER
	/**
	 * Persists a new user. The user must have a value for all required fields and must be unique.
	 * 
	 * @param gebruiker
	 */
	public void create(Gebruiker gebruiker);
	
	/**
	 * Updates an existing user. The user must have been previously persisted.
	 * 
	 * @param gebruiker
	 */
	public void update(Gebruiker gebruiker);

	/**
	 * Returns a user by username, the username also corresponds with the 'uid' field in LDAP. Returns null
	 * if the user doesn't exist.
	 * 
	 * @param gebruikersnaam
	 * @return The user, or null.
	 */
	public Gebruiker getGebruiker(String gebruikersnaam);
	
	/**
	 * Returns a list containing all available users, ordered by username. An empty list is returned
	 * if no users could be found.
	 * 
	 * @return A list containing all users.
	 */
	public List<Gebruiker> getAllGebruikers();

	/**
	 * Deletes the given user. The user must have been previously persisted.
	 * 
	 * @param gebruiker The user to delete.
	 */
	public void delete(Gebruiker gebruiker);
	
	/**
	 * Performs authentication for the given user by testing the given plaintext password.
	 * 
	 * @param gebruikersNaam
	 * @param wachtwoord
	 * @return True if the password is correct, false otherwise.
	 */
	public boolean authenticate (String gebruikersNaam, String wachtwoord);
	
	// GEBRUIKERSROL
	/**
	 * Creates a new relation between a user, a role and a 'bronhouder'. If the role is 'BEHEERDER', the bronhouder argument
	 * must be null. Otherwise, if the role is 'BRONHOUDER', the bronhouder argument must be set to a bronhouder instance.
	 * The relation can only be added if it doesn't currently exist.
	 * 
	 * @param gebruiker
	 * @param rol
	 * @param bronhouder
	 * @return A new GebruikersRol instance representing the relation.
	 */
	public GebruikersRol createGebruikersRol (Gebruiker gebruiker, Rol rol, Bronhouder bronhouder);

	/**
	 * Deletes the given user role. The role can only be removed if it currently exists (e.g. it can't be deleted twice).
	 * 
	 * @param gebruikersRol The role to delete.
	 */
	public void delete(GebruikersRol gebruikersRol);

	/**
	 * Returns all roles for the given user. If the user has no roles, an empty array is returned.
	 * 
	 * @param gebruiker
	 * @return A list containing all roles for this user.
	 */
	public List<GebruikersRol> getGebruikersRollenByGebruiker(Gebruiker gebruiker);
	
	// DATASET_TYPE
	public void create(DatasetType datasetType); // C

	public DatasetType getDatasetType(Long i); // R

	public List<DatasetType> getAllDatasetTypes();

	public DatasetType getDatasetTypeByName(String naam); // R

	public List<DatasetType> getDatasetTypesByThema(Thema thema); //R

	public void delete(DatasetType datasetType); // D

	// THEMA
	public void create(Thema thema); // C

	public Thema getThema(Long i); // R

	public List<Thema> getAllThemas();
	public List<Thema> getAllThemas (Bronhouder bronhouder);

	public Thema getThemaByName(String naam); // R

	public void delete(Thema thema); // D

	void create(JobLog log);

	/**
	 * Returns the most recently completed job logs for each dataset (if any), ordered by "bronhouder"
	 * and "dataset".
	 * 
	 * @return A (possibly empty) list of jobs.
	 */
	public List<JobLog> getLastCompletedJobsLog ();
	
	public List<JobLog> findJobLog (AbstractJob job);
	public List<JobLog> findJobLog (JobLogCriteria criteria);

	public List<EtlJob> findJob (JobCriteria criteria);

	public List<Dataset> findDataset (DatasetCriteria criteria);

	public long getJobFaseLogCount (AbstractJob job);

	public boolean isUserAuthorizedForBronhouder(Bronhouder bronhouder, String userName);

	public Bronhouder getFirstAuthorizedBronhouder(String userName);

	/**
	 * Returns the last validated job for the given arguments, or
	 * null if no such job exists.
	 * 
	 * @param bronhouder
	 * @param datasetType 
	 * @param uuid 
	 * @return The last validated job for the given arguments, or null.
	 */
	public EtlJob getLastJobThatValidated(Bronhouder bronhouder, DatasetType datasetType, String uuid);

	/**
	 * Returns the last succesfull import job for the given arguments, or
	 * null if no such job exists.
	 * 
	 * @param bronhouder
	 * @param datasetType 
	 * @param uuid 
	 * @return The last succesfull import job for the given arguments, or null.
	 */
	public EtlJob getLastSuccessfullImportJob(Bronhouder bronhouder, DatasetType datasetType, String uuid);

	/**
	 * Returns the last succesfull import job for the given argument, or
	 * null if no such job exists.
	 * @param job to match
	 * @return
	 */
	public EtlJob getLastSuccessfullImportJob(EtlJob job);

	public void delete(JobLog jobLog); // D

	/**
	 * @param identity
	 * @param i
	 * @return
	 */
	public Identity getIdentity(Class<Identity> identity, Long i);

	void update(Thema thema);

	// -----------------
	// Attributemapping:
	// -----------------

	AttributeMapping getAttributeMapping (Dataset dataset, String attributeName);
	List<AttributeMapping> getAttributeMappings (Dataset dataset);
	List<AttributeMapping> getValidAttributeMappings (Dataset dataset);
	
	void create (AttributeMapping attributeMapping);
	void update (AttributeMapping attributeMapping);
	void delete (AttributeMapping attributeMapping);
	
	void create (MappingOperation mappingOperation);
	void update (MappingOperation mappingOperation);
	void delete (MappingOperation mappingOperation);

	// ------------------
	// Filtering:
	// ------------------
	DatasetFilter getDatasetFilter (final Dataset dataset);
	
	void create (DatasetFilter datasetFilter);
	void update (DatasetFilter datasetFilter);
	void delete (DatasetFilter datasetFilter);
	
	void create (FilterExpression expression);
	void update (FilterExpression expression);
	void delete (FilterExpression expression);
	
	// ------------------
	// Code list mapping:
	// ------------------
	List<CodeListMapping> getCodeListMappings ();
	
	void create (CodeListMapping mapping);
	void update (CodeListMapping mapping);
	void delete (CodeListMapping mapping);
	
	List<Object[]> getChangedMetadataDocuments();
	List<MetadataDocument> getAllMetadataDocuments();
	MetadataDocument getMetadataDocument(Long id);
	
	void create(MetadataDocument metatataDocument);
	void update(MetadataDocument metatataDocument);
	void delete(MetadataDocument metatataDocument);

}
