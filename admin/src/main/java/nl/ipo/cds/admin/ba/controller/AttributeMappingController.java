package nl.ipo.cds.admin.ba.controller;

import static nl.ipo.cds.admin.ba.attributemapping.AttributeMappingUtils.getAttributeDescriptor;
import static nl.ipo.cds.admin.ba.attributemapping.AttributeMappingUtils.getAttributeDescriptors;
import static nl.ipo.cds.admin.ba.attributemapping.AttributeMappingUtils.makeOperationTree;
import static nl.ipo.cds.admin.ba.attributemapping.AttributeMappingUtils.makeOperationTrees;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import nl.idgis.commons.jobexecutor.Job;
import nl.idgis.commons.jobexecutor.JobLogger;
import nl.idgis.commons.jobexecutor.JobLogger.LogLevel;
import nl.idgis.commons.utils.DateTimeUtils;
import nl.ipo.cds.admin.ba.UnauthorizedException;
import nl.ipo.cds.admin.ba.attributemapping.AttributeMappingUtils;
import nl.ipo.cds.admin.ba.attributemapping.AttributeMappingValidatorLogger;
import nl.ipo.cds.admin.ba.attributemapping.FeatureTypeCache;
import nl.ipo.cds.admin.ba.attributemapping.MappingFactory;
import nl.ipo.cds.admin.ba.controller.beans.AttributeDescriptorsResponse;
import nl.ipo.cds.admin.ba.controller.beans.AttributeNotFoundExceptionResponse;
import nl.ipo.cds.admin.ba.controller.beans.HarvesterExceptionResponse;
import nl.ipo.cds.admin.ba.controller.beans.InputAttributesResponse;
import nl.ipo.cds.admin.ba.controller.beans.MappingParserExceptionResponse;
import nl.ipo.cds.admin.ba.controller.beans.OperationTypeResponse;
import nl.ipo.cds.admin.ba.controller.beans.OperationTypesResponse;
import nl.ipo.cds.admin.ba.controller.beans.PingResponse;
import nl.ipo.cds.admin.ba.controller.beans.PreviewLogLineResponse;
import nl.ipo.cds.admin.ba.controller.beans.PreviewMappingResponse;
import nl.ipo.cds.admin.ba.controller.beans.ThemeNotFoundExceptionResponse;
import nl.ipo.cds.admin.ba.controller.beans.filtering.DatasetFilterBean;
import nl.ipo.cds.admin.ba.controller.beans.mapping.Mapping;
import nl.ipo.cds.admin.ba.controller.beans.mapping.Mappings;
import nl.ipo.cds.admin.ba.filtering.DatasetFilterBeanFactory;
import nl.ipo.cds.admin.ba.filtering.DatasetFilterFactory;
import nl.ipo.cds.attributemapping.operations.OperationType;
import nl.ipo.cds.attributemapping.operations.discover.OperationDiscoverer;
import nl.ipo.cds.attributemapping.operations.discover.PropertyBeanIntrospector;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.dao.attributemapping.AttributeMappingDao;
import nl.ipo.cds.dao.attributemapping.OperationDTO;
import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.Dataset;
import nl.ipo.cds.domain.DatasetFilter;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.domain.FeatureType;
import nl.ipo.cds.domain.Gebruiker;
import nl.ipo.cds.domain.GebruikersRol;
import nl.ipo.cds.domain.Rol;
import nl.ipo.cds.domain.ValidateJob;
import nl.ipo.cds.etl.DatasetHandlers;
import nl.ipo.cds.etl.FeatureOutputStream;
import nl.ipo.cds.etl.GenericFeature;
import nl.ipo.cds.etl.PersistableFeature;
import nl.ipo.cds.etl.attributemapping.AttributeMappingFactory;
import nl.ipo.cds.etl.attributemapping.AttributeMappingValidator;
import nl.ipo.cds.etl.filtering.DatasetFiltererFactory;
import nl.ipo.cds.etl.filtering.FilterExpressionValidator;
import nl.ipo.cds.etl.filtering.FilterExpressionValidator.MessageKey;
import nl.ipo.cds.etl.log.EventLogger;
import nl.ipo.cds.etl.log.LogStringBuilder;
import nl.ipo.cds.etl.process.HarvesterException;
import nl.ipo.cds.etl.process.ValidateFeatureProcessor;
import nl.ipo.cds.etl.process.ValidateProcess;
import nl.ipo.cds.etl.theme.AttributeDescriptor;
import nl.ipo.cds.etl.theme.ThemeConfig;
import nl.ipo.cds.etl.theme.ThemeDiscoverer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.deegree.geometry.Geometry;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

@Controller
@RequestMapping ("/ba/attributemapping/{datasetId}")
public class AttributeMappingController {

	private final static Log log = LogFactory.getLog (AttributeMappingController.class);
	
	@Inject
	private ManagerDao managerDao;
	
	@Inject
	private OperationDiscoverer operationDiscoverer;
	
	@Inject
	private PropertyBeanIntrospector propertyBeanIntrospector;
	
	@Inject
	private ThemeDiscoverer themeDiscoverer;

	@Inject
	private ConversionService conversionService;

	@Inject
	private AttributeMappingFactory attributeMappingValidatorFactory;

	@Inject
	private ValidateProcess validateProcess;

	@Inject
	private AttributeMappingFactory attributeMappingFactory;
	
	@Inject
	private DatasetFiltererFactory datasetFiltererFactory;
	
	@Inject
	private FeatureTypeCache featureTypeCache;
	
	@Inject
	@Named ("harvesterMessages")
	private Properties harvesterMessages;
	
	@InitBinder
	public void initBinder (final WebDataBinder binder) {
		binder.setDisallowedFields ("*");
	}
	
	@ModelAttribute("viewName")
	public String overrideViewName () {
		/* Don't use the default viewName, but the viewName belonging to the GebruikerSbeheerController.
		 * Therefore it seems to stay on the same tab(-folder)
		 */
		return "/ba/datasetconfig";
	}
	
	@ModelAttribute("dataset")
	public Dataset getDataset (final @PathVariable("datasetId") long datasetId, final Principal principal, final HttpServletRequest request) throws NoSuchRequestHandlingMethodException, UnauthorizedException {
		final Dataset dataset = managerDao.getDataSet (datasetId);
		if (dataset == null) {
			throw new NoSuchRequestHandlingMethodException (request);
		}
		
		// Check whether the current user has access to the dataset:
		final Bronhouder bronhouder = dataset.getBronhouder ();
		final Gebruiker gebruiker = managerDao.getGebruiker (principal.getName ());
		final GebruikersRol rol = managerDao.getGebruikersRollenByGebruiker (gebruiker).get (0);
		
		if (!Rol.BEHEERDER.equals (rol.getRol ()) && !managerDao.isUserAuthorizedForBronhouder (bronhouder, principal.getName ())) {
			throw new UnauthorizedException (String.format ("Not authorized for dataset %d", datasetId));
		}
		
		return dataset;
	}
	
	@RequestMapping (method = RequestMethod.GET)
	public String attributeMappingForm (final Model model) {
		return "/ba/attributemapping/form";
	}
	
	@ExceptionHandler (HarvesterException.class)
	@ResponseBody
	public HarvesterExceptionResponse handleHarvesterException (final HarvesterException ex) {
		
		final String messageKey = ex.getMessageKey ().name ();
		final String message;
		final List<String> values = new ArrayList<String> (Arrays.asList (ex.getParameters ()));
		
		// Add the URL to the values:
		values.add (0, ex.getUrl ());
		
		if (harvesterMessages.containsKey (messageKey)) {
			message = LogStringBuilder.createStringMessage (
					harvesterMessages, 
					messageKey,
					values.toArray (new String[0])
				);
		} else {
			message = messageKey;
		}
		
		log.error (message, ex);
		
		return new HarvesterExceptionResponse (message, ex);
	}
	
	@RequestMapping (value = "/ping", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public PingResponse pingAction () {
		return new PingResponse ();
	}
	
	@RequestMapping (value = "/inputs", method = RequestMethod.GET)
	@ResponseBody
	public InputAttributesResponse getInputs (final @ModelAttribute("dataset") Dataset dataset) throws HarvesterException {
		final FeatureType featureType = featureTypeCache.getFeatureType (dataset);
		
		return new InputAttributesResponse (featureType);
	}
	
	@RequestMapping (value = "/operations", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public OperationTypesResponse getOperations (final @ModelAttribute("dataset") Dataset dataset) {
		final List<OperationTypeResponse> operationTypes = new ArrayList<OperationTypeResponse> ();
		
		for (final OperationType operationType: operationDiscoverer.getPublicOperationTypes ()) {
			operationTypes.add (new OperationTypeResponse (
					operationType,
					operationType.getPropertyBeanClass () != null 
						? propertyBeanIntrospector.getDescriptorForBeanClass(operationType.getPropertyBeanClass ())
						: null
				));
		}
		
		return new OperationTypesResponse (operationTypes);
	}
	
	@ExceptionHandler (ThemeNotFoundException.class)
	@ResponseBody
	public ThemeNotFoundExceptionResponse handleThemeNotFoundException (final ThemeNotFoundException ex) {
		return new ThemeNotFoundExceptionResponse (ex);
	}
	
	@ExceptionHandler (AttributeNotFoundException.class)
	@ResponseBody
	public AttributeNotFoundExceptionResponse handleAttributeNotFoundException (final AttributeNotFoundException ex) {
		return new AttributeNotFoundExceptionResponse (ex);
	}
	
	@RequestMapping (value = "/attributes", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public AttributeDescriptorsResponse getAttributes (final @ModelAttribute("dataset") Dataset dataset) throws ThemeNotFoundException {
		final ThemeConfig<?> themeConfig = themeDiscoverer.getThemeConfiguration (
			dataset
				.getDatasetType ()
				.getThema ()
				.getNaam ()
			);
		
		if (themeConfig == null) {
			throw new ThemeNotFoundException (dataset.getDatasetType ().getThema ().getNaam ());
		}
		
		return new AttributeDescriptorsResponse (themeConfig.getAttributeDescriptors ());
	}
	
	@RequestMapping (value = "/mapping", method = RequestMethod.GET, produces = "application/json")
	@Transactional
	@ResponseBody
	public Mappings getAllMappings (final @ModelAttribute("dataset") Dataset dataset) throws ThemeNotFoundException, HarvesterException {
		final Set<AttributeDescriptor<?>> attributeDescriptors = getAttributeDescriptors (themeDiscoverer, dataset);
		final FeatureType featureType = featureTypeCache.getFeatureType (dataset);
		final AttributeMappingDao dao = new AttributeMappingDao (managerDao, operationDiscoverer.getOperationTypes ());
		final List<Mapping> mappings = new ArrayList<Mapping> ();
		
		for (final AttributeDescriptor<?> attributeDescriptor: attributeDescriptors) {
			mappings.add (AttributeMappingUtils.getMapping (dataset, attributeDescriptor, featureType, dao));
		}
		
		final Mappings result = new Mappings ();
		result.setMappings (mappings);
		return result;
	}
	
	@RequestMapping (value = "/mapping/{attributeName}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public Mapping getMapping (final @ModelAttribute("dataset") Dataset dataset, final @PathVariable("attributeName") String attributeName) throws ThemeNotFoundException, AttributeNotFoundException, HarvesterException {
		final AttributeDescriptor<?> attributeDescriptor = getAttributeDescriptor (themeDiscoverer, dataset, attributeName);
		final FeatureType featureType = featureTypeCache.getFeatureType (dataset);
		final AttributeMappingDao dao = new AttributeMappingDao (managerDao, operationDiscoverer.getOperationTypes ());
		
		return AttributeMappingUtils.getMapping (dataset, attributeDescriptor, featureType, dao);
	}
	
	@ExceptionHandler (MappingParserException.class)
	@ResponseBody
	public MappingParserExceptionResponse handleMappingParserException (final MappingParserException ex) {
		return new MappingParserExceptionResponse (ex);
	}
	
	@RequestMapping (value = "/mapping/{attributeName}", method = RequestMethod.POST, consumes = "application/json")
	@ResponseBody
	@Transactional
	public Mapping postMapping (final @ModelAttribute("dataset") Dataset dataset, final @PathVariable("attributeName") String attributeName, final Reader reader) throws ThemeNotFoundException, AttributeNotFoundException, JsonParseException, JsonMappingException, IOException, HarvesterException, MappingParserException {
		final Set<AttributeDescriptor<?>> attributeDescriptors = getAttributeDescriptors (themeDiscoverer, dataset);
		final AttributeDescriptor<?> attributeDescriptor = getAttributeDescriptor (dataset, attributeName, attributeDescriptors);
		final FeatureType featureType = featureTypeCache.getFeatureType (dataset);
		final OperationDTO operationTree = makeOperationTree (operationDiscoverer, conversionService, dataset, attributeDescriptor, featureType, reader);
		
		// Determine whether the mapping is valid:
		final OperationDTO rootOperation = (OperationDTO) operationTree.getInputs().get(0).getOperation();
		final boolean isValid = AttributeMappingUtils.isMappingValid(rootOperation, attributeDescriptor, featureType);
		
		// Save the mapping:
		final AttributeMappingDao dao = new AttributeMappingDao (managerDao);
		dao.putAttributeMapping (dataset, attributeDescriptor, rootOperation, isValid);
		
		// Convert the operation tree back into a mapping:
		final MappingFactory mappingFactory = new MappingFactory (operationTree, featureType);
		
		return mappingFactory.buildMapping ();
	}

	@RequestMapping (value = "/preview", method = RequestMethod.POST, consumes = "application/json")
	@ResponseBody
	public PreviewMappingResponse previewFeature (final @ModelAttribute("dataset") Dataset dataset, final Reader reader) throws Exception {
		final Set<AttributeDescriptor<?>> attributeDescriptors = getAttributeDescriptors (themeDiscoverer, dataset);
		final FeatureType featureType = featureTypeCache.getFeatureType (dataset);
		final Map<AttributeDescriptor<?>, OperationDTO> rootOperations = makeOperationTrees (operationDiscoverer, conversionService, dataset, attributeDescriptors, featureType, reader);
		
		final ValidateJob job = new ValidateJob ();
		
		job.setDatasetType (dataset.getDatasetType ());
		job.setBronhouder (dataset.getBronhouder ());
		job.setCreateTime (DateTimeUtils.now ());
		job.setMaxFeatures (10);
		job.setForceExecution (true);
		job.setUuid (dataset.getUuid ());
		job.setVerversen (true);
		job.setIgnoreInvalidMapping (true);
		
		final List<PreviewLogLineResponse> messages = new ArrayList<PreviewLogLineResponse> ();
		final List<Map<String, String>> inputFeatures = new ArrayList<Map<String, String>> ();
		final List<Map<String, String>> features = new ArrayList<Map<String,String>> ();
		
		// Create a feature output stream to intercept input features:
		final FeatureOutputStream<GenericFeature> inputInterceptor = new FeatureOutputStream<GenericFeature> () {
			@Override
			public void writeFeature (final GenericFeature feature) {
				if (inputFeatures.size () > 10) {
					return;
				}
				
				final Map<String, String> values = new HashMap<String, String> ();
				
				for (final Map.Entry<String, Object> entry: feature.getValues ().entrySet ()) {
					final String value;
					if (entry.getValue () == null) {
						value = "";
					} else if (entry.getValue () instanceof Geometry) {
						value = "(geometrie)";
					} else {
						value = entry.getValue ().toString ();
					}
					
					values.put (entry.getKey (), value);
				}
				
				inputFeatures.add (values);
			}
		};
		
		// Create a feature output stream to intercept the validated features:
		final FeatureOutputStream<PersistableFeature> interceptor = new FeatureOutputStream<PersistableFeature> () {
			@Override
			public void writeFeature (final PersistableFeature feature) {
				if (inputFeatures.size () > 10) {
					return;
				}
				
				final Map<String, String> values = new HashMap<String, String> ();
				
				for (final AttributeDescriptor<?> ad: attributeDescriptors) {
					try {
						final Object value = ad.getPropertyDescriptor ().getReadMethod ().invoke (feature);
						final String stringValue;
						if (value == null) {
							stringValue = "";
						} else if (value instanceof Geometry) {
							stringValue = "(geometrie)";
						} else if (value.getClass ().isArray ()) {
							final Object[] list = (Object[])value;
							final StringBuilder builder = new StringBuilder ();
							for (int i = 0; i < list.length; ++ i) {
								if (i > 0) {
									builder.append ("|");
								}
								builder.append (list[i].toString ());
							}
							stringValue = builder.toString ();
						} else {
							stringValue = value.toString ();
						}
						values.put (ad.getName (), stringValue);
					} catch (IllegalArgumentException e) {
					} catch (IllegalAccessException e) {
					} catch (InvocationTargetException e) {
					}
				}
				
				// Insert blank padding in the array when features have been skipped (due to filtering):
				while (features.size () < inputFeatures.size () - 1) {
					features.add (null);
				}
				
				// Insert this feature:
				features.add (values);
			}
		};
		
		// Create a feature processor
		final ValidateFeatureProcessor featureProcessor = new ValidateFeatureProcessor (attributeMappingFactory, datasetFiltererFactory) {
			@Override
			protected Map<AttributeDescriptor<?>, OperationDTO> getAttributeMappings (final EtlJob job, final DatasetHandlers<PersistableFeature> datasetHandlers, final FeatureType featureType) {
				return rootOperations;
			}
			
			@Override
			protected FeatureOutputStream<PersistableFeature> getFeatureInterceptor () {
				return interceptor;
			}
			
			@Override
			protected FeatureOutputStream<GenericFeature> getInputFeatureInterceptor () {
				return inputInterceptor;
			}
		};
		
		// Create a logger that intercepts validation messages:
		final JobLogger logger = new JobLogger () {

			@Override
			public void logString(Job job, String key, LogLevel logLevel,
					String message) {
				messages.add (new PreviewLogLineResponse (logLevel, message, null));
			}

			@Override
			public void logString(Job job, String key, LogLevel logLevel,
					String message, Map<String, Object> context) {
				final String attributeName;
				
				if (context.containsKey ("attributeLabel")) {
					attributeName = context.get ("attributeLabel").toString ();
				} else {
					attributeName = null;
				}
				
				messages.add (new PreviewLogLineResponse (
						logLevel, 
						message, 
						attributeName
					));
			}
		};
		
		validateProcess.process (job, featureProcessor, logger, 10);
		
		return new PreviewMappingResponse (messages, inputFeatures, features);
	}
	
	@RequestMapping (value = "/filter", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	@Transactional
	public DatasetFilterBean getDatasetFilter (final @ModelAttribute("dataset") Dataset dataset) {
		final DatasetFilter datasetFilter = managerDao.getDatasetFilter (dataset);
		
		// Return an empty dataset filter if the dataset currently doesn't have a filter:
		if (datasetFilter == null || datasetFilter.getRootExpression () == null) {
			return new DatasetFilterBean ();
		}
		
		// Convert the root expression to expression beans:
		final DatasetFilterBeanFactory beanFactory = new DatasetFilterBeanFactory ();
		
		return beanFactory.createDatasetFilter (datasetFilter);
	}
	
	@RequestMapping (value = "/filter", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	@Transactional
	public DatasetFilterBean postDatasetFilter (final @ModelAttribute("dataset") Dataset dataset, final Reader reader) throws JsonParseException, JsonMappingException, IOException, MappingParserException, HarvesterException {
		final FeatureType featureType = featureTypeCache.getFeatureType (dataset);
		
		// Load the beans:
		final ObjectMapper objectMapper = new ObjectMapper ();
		final DatasetFilterBean datasetFilter = objectMapper.readValue (reader, DatasetFilterBean.class);
		
		// Load the existing conditions (if any):
		final DatasetFilter originalFilter = managerDao.getDatasetFilter (dataset);
		
		// Convert the dataset filter into JPA entities:
		final DatasetFilterFactory factory = new DatasetFilterFactory (managerDao, dataset, featureType);
		final DatasetFilter filter = factory.createDatasetFilter (datasetFilter, originalFilter);
		
		// Bail out early if the filter is empty:
		if (filter == null) {
			return new DatasetFilterBean ();
		}
		
		// Validate the filter:
		final FilterExpressionValidator validator = new FilterExpressionValidator (featureType, new EventLogger<FilterExpressionValidator.MessageKey> () {
			@Override
			public String logEvent (final Job job, final MessageKey messageKey, final LogLevel logLevel, final String... messageValues) {
				return null;
			}

			@Override
			public String logEvent (final Job job, final MessageKey messageKey, final LogLevel logLevel, final double x, final double y, final String gmlId, final String... messageValues) {
				return null;
			}

			@Override
			public String logEvent (final Job job, final MessageKey messageKey, final LogLevel logLevel, final Map<String, Object> context, final String... messageValues) {
				return null;
			}
		});
		filter.setValid (validator.isValid (new ValidateJob (), filter));
		
		// Save changes:
		managerDao.update (filter);
		
		// Convert back into beans:
		final DatasetFilterBeanFactory beanFactory = new DatasetFilterBeanFactory ();
		
		return beanFactory.createDatasetFilter (filter);
	}
}