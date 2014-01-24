package nl.ipo.cds.etl;

import static nl.ipo.cds.etl.process.HarvesterMessageKey.METADATA_FEATURETYPE_INVALID;
import static nl.ipo.cds.etl.process.HarvesterMessageKey.METADATA_FEATURETYPE_NOT_FOUND;

import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobLogger.LogLevel;
import nl.ipo.cds.attributemapping.operations.OperationType;
import nl.ipo.cds.attributemapping.operations.discover.OperationDiscoverer;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.dao.attributemapping.AttributeMappingDao;
import nl.ipo.cds.dao.attributemapping.InputOperationDTO;
import nl.ipo.cds.dao.attributemapping.OperationDTO;
import nl.ipo.cds.dao.attributemapping.OperationInputDTO;
import nl.ipo.cds.dao.attributemapping.TransformOperationDTO;
import nl.ipo.cds.dao.impl.ManagerDaoImpl;
import nl.ipo.cds.domain.AttributeType;
import nl.ipo.cds.domain.Dataset;
import nl.ipo.cds.domain.FeatureType;
import nl.ipo.cds.domain.FeatureTypeAttribute;
import nl.ipo.cds.domain.ValidateJob;
import nl.ipo.cds.etl.attributemapping.AttributeMappingValidator;
import nl.ipo.cds.etl.attributemapping.AttributeMappingValidator.MessageKey;
import nl.ipo.cds.etl.featuretype.FeatureTypeNotFoundException;
import nl.ipo.cds.etl.featuretype.GMLFeatureTypeParser;
import nl.ipo.cds.etl.log.EventLogger;
import nl.ipo.cds.etl.operations.transform.ConditionalTransform;
import nl.ipo.cds.etl.operations.transform.SplitStringTransform;
import nl.ipo.cds.etl.process.DatasetMetadata;
import nl.ipo.cds.etl.process.HarvesterException;
import nl.ipo.cds.etl.process.HarvesterFactory;
import nl.ipo.cds.etl.process.MetadataHarvester;
import nl.ipo.cds.etl.theme.AttributeDescriptor;
import nl.ipo.cds.etl.theme.ThemeConfig;
import nl.ipo.cds.etl.theme.ThemeDiscoverer;
import nl.ipo.cds.etl.util.BlockingExecutor;
import nl.ipo.cds.executor.ConfigDir;

import org.deegree.commons.xml.XMLProcessingException;
import org.deegree.feature.types.AppSchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GenerateAttributeMappings implements Runnable {

	private static final int ADVISORY_LOCK_KEY = 0;
	
	@Configuration
	@ComponentScan (basePackageClasses = { nl.ipo.cds.etl.config.Package.class, nl.ipo.cds.etl.theme.Package.class })
	@ImportResource ({
		"/nl/ipo/cds/dao/dataSource-applicationContext.xml",
		"/nl/ipo/cds/dao/dao-applicationContext.xml"
	})
	public static class Config {
		@Bean
		public ConfigDir configDir (final @Value("file:${CONFIGDIR}") String configDirPath) {
			return new ConfigDir (configDirPath);
		}
		
		@Bean
		public GenerateAttributeMappings verifyDatasetSchemas () {
			return new GenerateAttributeMappings ();
		}
		
		@Bean
		public Executor executor () {
			return new BlockingExecutor (1);
		}
	}
	
	private @Inject ManagerDao managerDao;
	private @Inject HarvesterFactory harvesterFactory;
	private @Inject DataSource dataSource;
	private @Inject ThemeDiscoverer themeDiscoverer;
	private @Inject OperationDiscoverer operationDiscoverer;
	private @Inject ConfigDir configDir;
	private Collection<OperationType> operationTypes;
	private AttributeMappingDao attributeMappingDao;
	
	@PostConstruct
	public void init () {
		this.operationTypes = operationDiscoverer.getOperationTypes ();
		this.attributeMappingDao = new AttributeMappingDao (managerDao);
	}
	
	public static void main (final String ... args) {
		try {
			final AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext (Config.class);
			
			applicationContext.getBean (GenerateAttributeMappings.class).run ();
			
			applicationContext.close ();
		} catch(Exception e) {
			e.printStackTrace ();
			System.exit(1);
		}		
		System.exit(0);
	}

	@Override
	public void run () {
		try {
			Connection connection = DataSourceUtils.getConnection(dataSource);
			PreparedStatement stmt = connection.prepareStatement("select pg_try_advisory_lock(?)");
			stmt.setLong(1, ADVISORY_LOCK_KEY);
			ResultSet rs = stmt.executeQuery();
			boolean result = false;
			while(rs.next()) {
				result = rs.getBoolean(1);
				
			}
			
			rs.close();
			stmt.close();
			
			if(!result) {
				System.err.println("Couldn't acquire lock (JobExecutor already running?)");				
				return;
			}
			
			for (final Dataset ds: managerDao.getAllDatasets ()) {
				processDataset (ds);
			}
			
			DataSourceUtils.releaseConnection(connection, dataSource);
		} catch (Exception e) {
			e.printStackTrace ();
		}
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void processDataset (final Dataset dataset) {
		try {
			final FeatureType featureType = getFeatureType (dataset);
			final Set<AttributeDescriptor<?>> attributeDescriptors = getAttributeDescriptors (dataset);
			
			for (final AttributeDescriptor<?> attributeDescriptor: attributeDescriptors) {
				processAttributeDescriptor (dataset, attributeDescriptor, featureType);
			}
		} catch (Exception e) {
			e.printStackTrace ();
		}
	}
	
	@Transactional (propagation = Propagation.REQUIRES_NEW)
	public void processAttributeDescriptor (final Dataset dataset, final AttributeDescriptor<?> attributeDescriptor, final FeatureType featureType) throws Exception {
		final List<OperationInputDTO> transformInputs = new ArrayList<OperationInputDTO> (); 
		
		// Locate a feature type attribute:
		final FeatureTypeAttribute featureTypeAttribute = getFeatureTypeAttribute (dataset, attributeDescriptor, featureType);
		if (featureTypeAttribute == null) {
			return;
		}
		
		final OperationDTO operation = createAttributeOperation (dataset, attributeDescriptor, featureTypeAttribute);
		transformInputs.add (new OperationInputDTO (operation));
		
		// Create a conditional transform operation:
		final TransformOperationDTO transformOperation = new TransformOperationDTO (getConditionalTransformOperationType (), transformInputs, new ConditionalTransform.Settings ());
		
		// Validate the mapping:
		final AttributeMappingValidator validator = new AttributeMappingValidator (attributeDescriptor, featureType, new EventLogger<AttributeMappingValidator.MessageKey> () {
			@Override
			public String logEvent(Job job, MessageKey messageKey, LogLevel logLevel,
					Map<String, Object> context, String... messageValues) {
				return null;
			}
			
			@Override
			public String logEvent(Job job, MessageKey messageKey, LogLevel logLevel,
					double x, double y, String gmlId, String... messageValues) {
				return null;
			}
			
			@Override
			public String logEvent(Job job, MessageKey messageKey, LogLevel logLevel,
					String... messageValues) {
				return null;
			}
		});
		final boolean isValid = validator.isValid (new ValidateJob (), transformOperation);
		
		// Store the mapping:
		try {
			attributeMappingDao.putAttributeMapping (dataset, attributeDescriptor, transformOperation, isValid);
			((ManagerDaoImpl)managerDao).getEntityManager ().flush ();
		} catch (Exception e) {
			e.printStackTrace ();
			System.exit (1);
		}
	}
	
	private OperationDTO createAttributeOperation (final Dataset dataset, final AttributeDescriptor<?> attributeDescriptor, final FeatureTypeAttribute attribute) {
		final InputOperationDTO inputOperation = new InputOperationDTO (attribute, attribute.getName ().getLocalPart (), attribute.getType ());
		
		// Convert string to list:
		if (attributeDescriptor.getAttributeType ().equals (String[].class)) {
			final SplitStringTransform.Settings settings = new SplitStringTransform.Settings ();
			
			settings.setBoundary ("|");
			
			return new TransformOperationDTO (
					getTransformOperationType ("splitStringTransform"), 
					Arrays.asList (new OperationInputDTO[] { new OperationInputDTO (inputOperation) }), 
					settings
				);
		}
		
		// Convert string to date:
		if (attributeDescriptor.getAttributeType ().equals (Date.class) && attribute.getType ().equals (AttributeType.STRING)) {
			return new TransformOperationDTO (
					getTransformOperationType ("toDateTransform"),
					Arrays.asList (new OperationInputDTO[] { new OperationInputDTO (inputOperation) }),
					null
				);
		}
		
		// Convert datetime to date:
		if (attributeDescriptor.getAttributeType ().equals (Date.class) && attribute.getType ().equals (AttributeType.DATE_TIME)) {
			return new TransformOperationDTO (
					getTransformOperationType ("timestampToDateTransform"),
					Arrays.asList (new OperationInputDTO[] { new OperationInputDTO (inputOperation) }),
					null
				);
		}
		
		return inputOperation;
	}
	
	private FeatureTypeAttribute getFeatureTypeAttribute (final Dataset dataset, final AttributeDescriptor<?> attributeDescriptor, final FeatureType featureType) throws Exception {
		final String attributeName = attributeDescriptor.getName ();
		
		// Try to locate an exact match:
		final FeatureTypeAttribute exactMatch = getFeatureTypeAttribute (featureType, attributeName);
		if (exactMatch != null) {
			return exactMatch;
		}

		// Locate a mapped match:
		final File[] files = new File[] {
			new File (new File (configDir.getPath ().substring (5)), "nl/ipo/cds/etl/protectedSite/config/generic.xml"),
			new File (new File (configDir.getPath ().substring (5)), "nl/ipo/cds/etl/protectedSite/config/" + dataset.getBronhouder ().getCode () + ".xml"),
			new File (new File (configDir.getPath ().substring (5)), "nl/ipo/cds/etl/protectedSite/config/" + dataset.getBronhouder ().getCode () + "." + dataset.getDatasetType ().getNaam () + ".xml")
		};
		
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder builder = factory.newDocumentBuilder ();
		final XPathFactory xpathFactory = XPathFactory.newInstance ();
		final XPath xpath = xpathFactory.newXPath ();
		final XPathExpression expr = xpath.compile ("/config/replace/*");
		for (final File file: files) {
			if (!file.exists ()) {
				continue;
			}
			
			final Document document = builder.parse (file);
			final NodeList nodeList = (NodeList)expr.evaluate (document, XPathConstants.NODESET);
			
			for (int i = 0; i < nodeList.getLength (); ++ i) {
				final Node node = nodeList.item (i);
				if (node.getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}
				
				final Element element = (Element)node;
				final String textContent = element.getTextContent ();
				if (textContent == null) {
					continue;
				}
				
				if (!textContent.substring (5).equals (attributeName)) {
					continue;
				}
				
				final String replacedFullName = element.getTagName ();
				final String replacedName = replacedFullName.substring (replacedFullName.indexOf (':') + 1);
				
				return getFeatureTypeAttribute (featureType, replacedName);
			}
		}
		
		return null;
	}
	
	private FeatureTypeAttribute getFeatureTypeAttribute (final FeatureType featureType, final String name) {
		for (final FeatureTypeAttribute a: featureType.getAttributes ()) {
			if (a.getName ().getLocalPart ().equals (name)) {
				return a;
			}
		}
		
		return null;
	}
	
	private FeatureType getFeatureType (final Dataset dataset) throws HarvesterException {
		final String uuid = dataset.getUuid ();		
		final MetadataHarvester harvester = harvesterFactory.createMetadataHarvester ();
		final DatasetMetadata metadata = harvester.parseMetadata (uuid);
		String ftNameWithColon = metadata.getFeatureTypeName();		
		String featureTypeName = ftNameWithColon.substring (ftNameWithColon.indexOf (':') + 1);
		ThemeConfig<?> themeConfig = themeDiscoverer.getThemeConfiguration(dataset.getDatasetType().getThema().getNaam());		
		try {
			AppSchema appSchema = themeConfig.getSchemaHarvester().parseApplicationSchema(metadata);			
			return new GMLFeatureTypeParser().parseSchema(appSchema, featureTypeName);
		} catch (FeatureTypeNotFoundException e) {
			throw new HarvesterException (e, METADATA_FEATURETYPE_NOT_FOUND, metadata.getSchemaUrl(), featureTypeName);
		} catch (XMLProcessingException e) {
			throw new HarvesterException (e, METADATA_FEATURETYPE_INVALID, metadata.getSchemaUrl(), featureTypeName, e.getMessage());
		}
	}
	
	private Set<AttributeDescriptor<?>> getAttributeDescriptors (final Dataset dataset) {
		final ThemeConfig<?> themeConfig = themeDiscoverer.getThemeConfiguration (
				dataset
					.getDatasetType ()
					.getThema ()
					.getNaam ()
				);
				
		if (themeConfig == null) {
			throw new IllegalArgumentException (String.format ("Theme not found: %s", dataset.getDatasetType ().getThema ().getNaam ()));
		}
			
		return themeConfig.getAttributeDescriptors ();
	}
	
	private OperationType getTransformOperationType (final String operationName) {
		for (final OperationType operationType: operationTypes) {
			if (operationType.getName ().equals (operationName)) {
				return operationType;
			}
		}
		
		return null;
	}
	
	private OperationType getConditionalTransformOperationType () {
		for (final OperationType operationType: operationTypes) {
			if (operationType.getPropertyBeanClass () != null && operationType.getPropertyBeanClass ().equals (ConditionalTransform.Settings.class)) {
				return operationType;
			}
		}
		
		throw new IllegalStateException ("No conditional transform operation type found");
	}
}
