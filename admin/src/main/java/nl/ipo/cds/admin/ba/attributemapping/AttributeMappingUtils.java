package nl.ipo.cds.admin.ba.attributemapping;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import nl.ipo.cds.admin.ba.controller.AttributeNotFoundException;
import nl.ipo.cds.admin.ba.controller.MappingParserException;
import nl.ipo.cds.admin.ba.controller.ThemeNotFoundException;
import nl.ipo.cds.admin.ba.controller.beans.mapping.Mapping;
import nl.ipo.cds.attributemapping.operations.OperationType;
import nl.ipo.cds.attributemapping.operations.discover.OperationDiscoverer;
import nl.ipo.cds.dao.attributemapping.AttributeMappingDao;
import nl.ipo.cds.dao.attributemapping.OperationDTO;
import nl.ipo.cds.dao.attributemapping.OperationInputDTO;
import nl.ipo.cds.dao.attributemapping.TransformOperationDTO;
import nl.ipo.cds.domain.Dataset;
import nl.ipo.cds.domain.FeatureType;
import nl.ipo.cds.etl.process.HarvesterException;
import nl.ipo.cds.etl.theme.AttributeDescriptor;
import nl.ipo.cds.etl.theme.ThemeConfig;
import nl.ipo.cds.etl.theme.ThemeDiscoverer;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.core.convert.ConversionService;

public class AttributeMappingUtils {

	public static Map<AttributeDescriptor<?>, OperationDTO> makeOperationTrees (final OperationDiscoverer operationDiscoverer, final ConversionService conversionService, final Dataset dataset, final Set<AttributeDescriptor<?>> attributeDescriptors, final FeatureType featureType, final Reader reader) throws JsonProcessingException, IOException, AttributeNotFoundException, MappingParserException {
		final Collection<OperationType> operationTypes = operationDiscoverer.getOperationTypes ();
		final ObjectMapper mapper = new ObjectMapper ();
		
		// Turn attribute descriptors into a map:
		final Map<String, AttributeDescriptor<?>> descriptorMap = new HashMap<String, AttributeDescriptor<?>> ();
		for (final AttributeDescriptor<?> ad: attributeDescriptors) {
			descriptorMap.put (ad.getName (), ad);
		}
		
		// Parse root operations for each attribute:
		final Map<AttributeDescriptor<?>, OperationDTO> rootOperations = new HashMap<AttributeDescriptor<?>, OperationDTO> ();
		final JsonNode root = mapper.readTree (reader);
		final Iterator<Map.Entry<String, JsonNode>> fields = root.getFields ();
		while (fields.hasNext ()) {
			final Map.Entry<String, JsonNode> entry = fields.next ();
			final Mapping mapping = mapper.readValue (entry.getValue (), Mapping.class);
			final AttributeDescriptor<?> attributeDescriptor = descriptorMap.get (entry.getKey ());
			if (attributeDescriptor == null) {
				throw new AttributeNotFoundException (dataset.getDatasetType ().getThema ().getNaam (), entry.getKey ());
			}
			
			final OperationFactory factory = new OperationFactory (attributeDescriptor, operationTypes, featureType, conversionService);
			final OperationDTO operationTree = factory.buildOperationCommand (mapping);
			
			rootOperations.put (attributeDescriptor, (OperationDTO)operationTree.getInputs ().get (0).getOperation ());
		}
		
		return rootOperations;
	}
	
	public static OperationDTO makeOperationTree (final OperationDiscoverer operationDiscoverer, final ConversionService conversionService, final Dataset dataset, final AttributeDescriptor<?> attributeDescriptor, final FeatureType featureType, final Reader reader) throws HarvesterException, JsonParseException, JsonMappingException, IOException, MappingParserException {
		final Collection<OperationType> operationTypes = operationDiscoverer.getOperationTypes ();
		
		final ObjectMapper mapper = new ObjectMapper ();
		
		final Mapping mapping = mapper.readValue (reader, Mapping.class);
		
		// Convert mapping to an operation tree used in the dao:
		final OperationFactory factory = new OperationFactory (attributeDescriptor, operationTypes, featureType, conversionService);
		
		return factory.buildOperationCommand (mapping);
	}
	
	public static Set<AttributeDescriptor<?>> getAttributeDescriptors (final ThemeDiscoverer themeDiscoverer, final Dataset dataset) throws ThemeNotFoundException {
		final ThemeConfig<?> themeConfig = themeDiscoverer.getThemeConfiguration (
				dataset
					.getDatasetType ()
					.getThema ()
					.getNaam ()
				);
				
		if (themeConfig == null) {
			throw new ThemeNotFoundException (dataset.getDatasetType ().getThema ().getNaam ());
		}
			
		return themeConfig.getAttributeDescriptors ();
	}
	
	public static AttributeDescriptor<?> getAttributeDescriptor (final ThemeDiscoverer themeDiscoverer, final Dataset dataset, final String attributeName) throws ThemeNotFoundException, AttributeNotFoundException {
		return getAttributeDescriptor (dataset, attributeName, getAttributeDescriptors (themeDiscoverer, dataset));
	}
	
	public static AttributeDescriptor<?> getAttributeDescriptor (final Dataset dataset, final String attributeName, final Set<AttributeDescriptor<?>> attributeDescriptors) throws ThemeNotFoundException, AttributeNotFoundException {
		for (final AttributeDescriptor<?> ad: attributeDescriptors) {
			if (ad.getName ().equals (attributeName)) {
				return ad;
			}
		}
		
		throw new AttributeNotFoundException (dataset.getDatasetType ().getThema ().getNaam (), attributeName);
	}
	
	public static Mapping getMapping (final Dataset dataset, final AttributeDescriptor<?> attributeDescriptor, final FeatureType featureType, final AttributeMappingDao dao) {
		final OperationDTO rootOperation = dao.getAttributeMapping (dataset, attributeDescriptor);
		final Mapping mapping;
		
		if (rootOperation == null) {
			mapping = new Mapping ();
		} else {
			final TransformOperationDTO op = new TransformOperationDTO (
					attributeDescriptor, 
					Arrays.asList (new OperationInputDTO[] { new OperationInputDTO (rootOperation) }), 
					null
				);
			
			final MappingFactory factory = new MappingFactory (op, featureType);
			mapping = factory.buildMapping ();
		}
		
		mapping.setAttributeName (attributeDescriptor.getName ());
		mapping.setFeatureTypeName (featureType.getName ().getLocalPart ());
		mapping.setFeatureTypeNamespace (featureType.getName ().getNamespace ());
		
		return mapping;
	}
}
