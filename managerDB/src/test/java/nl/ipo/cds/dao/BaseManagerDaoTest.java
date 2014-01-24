package nl.ipo.cds.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import junit.framework.Assert;
import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.Dataset;
import nl.ipo.cds.domain.DatasetType;
import nl.ipo.cds.domain.JobType;
import nl.ipo.cds.domain.Thema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;

@ContextConfiguration({ "classpath:/nl/ipo/cds/dao/dao-applicationContext.xml",
		"classpath:/nl/ipo/cds/dao/dataSource-applicationContext.xml",
		"classpath:/nl/ipo/cds/context/propertyConfigurer-test.xml" })
@TransactionConfiguration(transactionManager = "transactionManager")
public abstract class BaseManagerDaoTest extends
		AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	protected ManagerDao managerDao;
	@PersistenceContext
	protected EntityManager entityManager;

    protected JobType jobTypeV, jobTypeI, jobTypeT;
    protected Bronhouder bronhouderDR, bronhouderLI, bronhouderNH, bronhouderOV;
    protected Thema thema ;
    protected DatasetType datasetType1, datasetType2, datasetType3;
    protected Dataset dataset1;
    
    /**
     * build up a database with base tables to be used in the testcases
     * @throws Exception 
     */
	public void buildDB () throws Exception {
        Assert.assertNotNull(managerDao);

        // STAMTABEL JOBTYPE
        // check if tables exists
        if (managerDao.getJobTypeByName("VALIDATE") == null){
        	// make new tables
	        jobTypeV = new JobType();
	        jobTypeV.setNaam("VALIDATE");
	        jobTypeV.setPrioriteit(300);
	        managerDao.create(jobTypeV);
	
	        jobTypeI = new JobType();
	        jobTypeI.setNaam("IMPORT");
	        jobTypeI.setPrioriteit(200);
	        managerDao.create(jobTypeI);
	
	        jobTypeT = new JobType();
	        jobTypeT.setNaam("TRANSFORM");
	        jobTypeT.setPrioriteit(100);
	        managerDao.create(jobTypeT);
        } else {
        	// get from existing tables
        	jobTypeV = managerDao.getJobTypeByName("VALIDATE");
        	jobTypeI = managerDao.getJobTypeByName("IMPORT");
        	jobTypeT = managerDao.getJobTypeByName("TRANSFORM");       	
        }       
        // STAMTABEL BRONHOUDER
        if (managerDao.getBronhouderByContactNaam("IDgis") == null){
        	/*
        		CBS Provincie codes
        		20 Groningen
				21 Friesland
				22 Drenthe
				23 Overijssel
				24 Flevoland
				25 Gelderland
				26 Utrecht
				27 Noord-Holland
				28 Zuid-Holland
				29 Zeeland
				30 Noord-Brabant
				31 Limburg
        	 */
	        bronhouderDR = new Bronhouder();
	        bronhouderDR.setContactNaam("D. Rent");
	        bronhouderDR.setNaam("Drenthe");
	        bronhouderDR.setCode("9922");
	        bronhouderDR.setContactEmailadres("mail@drenthe.nl");
	        bronhouderDR.setCommonName ("drenthe");
	        managerDao.create(bronhouderDR);
	
	        bronhouderLI = new Bronhouder();
	        bronhouderLI.setContactNaam("L. Imburg");
	        bronhouderLI.setNaam("Limburg");
	        bronhouderLI.setCode("9931");
	        bronhouderLI.setContactEmailadres("mail@limburg.nl");
	        bronhouderLI.setCommonName ("limburg"); 
	        managerDao.create(bronhouderLI);
	
	        bronhouderNH = new Bronhouder();
	        bronhouderNH.setContactNaam("N. Holland");
	        bronhouderNH.setNaam("Noord-Holland");
	        bronhouderNH.setCode("9927");
	        bronhouderNH.setContactEmailadres("mail@nh.nl");
	        bronhouderNH.setCommonName ("noordholland");
	        managerDao.create(bronhouderNH);
	
	        bronhouderOV = new Bronhouder();
	        bronhouderOV.setContactNaam("IDgis");
	        bronhouderOV.setContactPlaats("Rijssen");
	        bronhouderOV.setNaam("Overijssel");
	        bronhouderOV.setCode("9923");
	        bronhouderOV.setContactEmailadres("mail@overijssel.nl");
	        bronhouderOV.setCommonName ("overijssel");
	        managerDao.create(bronhouderOV);
        }else{
        	bronhouderDR = managerDao.getBronhouderByContactNaam("D. Rent");
        	bronhouderLI = managerDao.getBronhouderByContactNaam("L. Imburg");
        	bronhouderNH = managerDao.getBronhouderByContactNaam("N. Holland");
        	bronhouderOV = managerDao.getBronhouderByContactNaam("IDgis");
        }
        // STAMTABEL THEMA
	    if (managerDao.getThemaByName("Protected sites") == null){
	        thema = new Thema();
	        thema.setNaam("Protected sites");
	        managerDao.create(thema);
	    }else{
	    	thema = managerDao.getThemaByName("Protected sites");
	    }
        // STAMTABEL DATASET_TYPE
        if (managerDao.getDatasetTypeByName("EHS") == null){
	        datasetType1 = new DatasetType();
	        datasetType1.setThema(thema);
	        datasetType1.setNaam("EHS");
	        managerDao.create(datasetType1);
	        datasetType2 = new DatasetType();
	        datasetType2.setThema(thema);
	        datasetType2.setNaam("WAV");
	        managerDao.create(datasetType2);
	        datasetType3 = new DatasetType();
	        datasetType3.setThema(thema);
	        datasetType3.setNaam("ST");
	        managerDao.create(datasetType3);
        }else{
        	datasetType1 = managerDao.getDatasetTypeByName("EHS");
        	datasetType2 = managerDao.getDatasetTypeByName("WAV");
        	datasetType3 = managerDao.getDatasetTypeByName("ST");
        }
        
        if (managerDao.getDatasetsByBronhouder (bronhouderOV).size () == 0) {
        	dataset1 = new Dataset ();
        	dataset1.setBronhouder(bronhouderOV);
        	dataset1.setDatasetType(datasetType1);
        	dataset1.setUuid("{1234-5678}");
        	managerDao.create(dataset1);
        }
	}
}
