/**
 * 
 */
package nl.ipo.cds.dao.metadata;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolationException;

import nl.ipo.cds.domain.metadata.DatasetMetadata;
import nl.ipo.cds.domain.metadata.ExtendedCapabilities;
import nl.ipo.cds.domain.metadata.Service;
import nl.ipo.cds.domain.metadata.ServiceIdentification;
import nl.ipo.cds.domain.metadata.ServiceProvider;
import nl.ipo.cds.domain.metadata.SpatialDataSetIdentifier;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;


/**
 * @author eshuism
 * 13 jan 2012
 */
@ContextConfiguration({ "classpath:/nl/ipo/cds/dao/metadata/dao-applicationContext.xml",
	"classpath:/nl/ipo/cds/dao/metadata/dataSource-applicationContext.xml",
	"classpath:/nl/ipo/cds/context/propertyConfigurer-test.xml" })
@TransactionConfiguration(defaultRollback = true, transactionManager = "transactionManagerMetadata")
public class MetadataDaoTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	protected MetadataDao metadataDao;
	@PersistenceContext(unitName="cds-metadata")
	protected EntityManager entityManager;

	@Before
	public void init(){
		Assert.assertNotNull("Spring configuration incorrect", metadataDao);
	}
	
	@Test
	public void testInsertAndGetService(){
		Service service = createService();
		this.entityManager.flush();

		// Get one service by ID
		Assert.assertNotNull(service.getId());
		Service refoundService = this.metadataDao.getService(service.getId());
		Assert.assertNotNull(refoundService);
		
		List<Service> services = this.metadataDao.getAllServices();
		Assert.assertNotNull(services);
		Assert.assertTrue(services.size() > 0);
	}

	@Test
	public void testServiceExtendedCapabilities(){
		Service service = createService();
		
		SpatialDataSetIdentifier spatialDataSetIdentifier = new SpatialDataSetIdentifier();
		spatialDataSetIdentifier.setCode("code");
		spatialDataSetIdentifier.setNamespace("namespace");
		
		ExtendedCapabilities extendedCapabilities = new ExtendedCapabilities();
		extendedCapabilities.setMetadataUrl("http://www.idgis.eu");
		extendedCapabilities.setSpatialDataSetIdentifier(spatialDataSetIdentifier);
		
		service.setExtendedCapabilities(extendedCapabilities);
		
		metadataDao.update(service);
		entityManager.flush();
	}
	
	@Test
	public void testServiceDatasetMetadata(){
		Service service = createService();
		List<DatasetMetadata> datasetMetadatas = new ArrayList<DatasetMetadata>();

		DatasetMetadata datasetMetadata1 = new DatasetMetadata();
		datasetMetadata1.setName("");
		datasetMetadata1.setUrl("http://www.idgis.eu");
		datasetMetadatas.add(datasetMetadata1);
		service.setDatasetMetadatas(datasetMetadatas);
		try {
			this.metadataDao.update(service);
			this.entityManager.flush();
		} catch (ConstraintViolationException cve) {
			return;
		}
		
		//Assert.fail();
	}

	private Service createService() {
		String serviceName = "view";
		Service service = new Service(serviceName, "Inspire view");
		this.metadataDao.create(service);
		return service;
	}

	@Test
	public void testInsertAndGetServiceServiceIdentification(){
		Service service = new Service("view", "Inspire view");
		
		ServiceIdentification serviceIdentification = new ServiceIdentification();
		serviceIdentification.setTitle("title");
		serviceIdentification.setAbstract("samenvatting");
		serviceIdentification.setServiceType("WMS");
		serviceIdentification.setServicePath("ProtectedSites/services");
		//this.metadataDao.create(serviceIdentification);
		service.setServiceIdentification(serviceIdentification);
		this.metadataDao.create(service);
		
		this.entityManager.flush();

		// Get one service by ID
		Assert.assertNotNull(service.getId());
		Service refoundService = this.metadataDao.getService(service.getId());
		Assert.assertNotNull(refoundService);
		
		serviceIdentification = refoundService.getServiceIdentification();
		Assert.assertNotNull(serviceIdentification);
		
		List<Service> services = this.metadataDao.getAllServices();
		Assert.assertNotNull(services);
		Assert.assertTrue(services.size() > 0);
	}

	@Test
	public void testUpdateServiceServiceIdentification(){
		Service service = new Service("view", "Inspire view");
		
		ServiceIdentification serviceIdentification = new ServiceIdentification();
		serviceIdentification.setTitle("title");
		serviceIdentification.setAbstract("samenvatting");
		serviceIdentification.setServiceType("WMS");
		serviceIdentification.setServicePath("ProtectedSites/services");
		service.setServiceIdentification(serviceIdentification);
		this.metadataDao.create(service);
		
		this.entityManager.flush();

		// Get one service by ID
		Service refoundService = this.metadataDao.getService(service.getId());
		Assert.assertNotNull(refoundService);
		
		serviceIdentification = refoundService.getServiceIdentification();
		Assert.assertNotNull(serviceIdentification);
		
		String newTitle = serviceIdentification.getTitle()+"Changed";
		serviceIdentification.setTitle(newTitle);
		
		this.metadataDao.update(refoundService);
		this.entityManager.flush();

		refoundService = this.metadataDao.getService(service.getId());
		Assert.assertEquals(newTitle, refoundService.getServiceIdentification().getTitle());


	}

	@Test
	public void testInsertServiceProvider() throws MalformedURLException{

		Service service = new Service("download", "Inspire download");

		// Set service
		ServiceProvider serviceProvider = new ServiceProvider("GBO provincies");
		
		// Set other props
		serviceProvider.setAdministrativeArea("administrativeArea");
		serviceProvider.setCity("Rijssen");
		serviceProvider.setContactInstructions("contactInstructions");
		serviceProvider.setCountry("Nederland");
		serviceProvider.setDeliveryPoints(Arrays.asList(new String[]{"point 1", "point 2"}));
		serviceProvider.setEmail("email@email.nl");
		serviceProvider.setFax("+311234567890");
		serviceProvider.setHoursOfService("between 13:59 and 14:00");
		serviceProvider.setIndividualName("Functioneel beheerder Provinciaal Georegister");
		serviceProvider.setOnlineResource("http://www.idgis.eu/OnlineResource");
		serviceProvider.setOrganizationName("GBO provincies");
		serviceProvider.setPhone("+310123456789");
		serviceProvider.setPostalCode("1234AB");
		serviceProvider.setProviderSite("http://www.idgis.eu/ProviderSite");
		serviceProvider.setRole("PointOfContact");

		service.setServiceProvider(serviceProvider);
		this.metadataDao.create(service);
		
		this.entityManager.flush();
		
	}

}
