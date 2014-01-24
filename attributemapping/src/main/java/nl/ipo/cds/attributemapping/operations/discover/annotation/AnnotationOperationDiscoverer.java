package nl.ipo.cds.attributemapping.operations.discover.annotation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import nl.ipo.cds.attributemapping.operations.InputOperationType;
import nl.ipo.cds.attributemapping.operations.OperationType;
import nl.ipo.cds.attributemapping.operations.OutputOperationType;
import nl.ipo.cds.attributemapping.operations.TransformOperationType;
import nl.ipo.cds.attributemapping.operations.annotation.MappingOperation;
import nl.ipo.cds.attributemapping.operations.discover.OperationDiscoverer;
import nl.ipo.cds.attributemapping.operations.discover.OperationDiscovererException;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

public class AnnotationOperationDiscoverer implements OperationDiscoverer, ApplicationContextAware {

	private ApplicationContext applicationContext;
	private Collection<OperationType> operationTypes;
	
	@PostConstruct
	public void discoverOperations () {
		final Map<String, Object> candidates = getCandidateBeans ();
		final MessageSource messageSource = createMessageSourceForBeans (candidates);
		
		operationTypes = createOperationTypesForBeans (candidates, messageSource);
	}

	@Override
	public Collection<OperationType> getOperationTypes () {
		return Collections.unmodifiableCollection (operationTypes);
	}
	
	@Override
	public Collection<OperationType> getPublicOperationTypes () {
		final List<OperationType> types = new ArrayList<OperationType> ();
				
		for (final OperationType ot: operationTypes) {
			if (ot instanceof AnnotationOperationType && !((AnnotationOperationType)ot).isInternal ()) {
				types.add (ot);
			}
		}
		
		return Collections.unmodifiableCollection (types);
	}
	
	@Override
	public Collection<TransformOperationType> getTransformOperationTypes () {
		return null;
	}
	
	@Override
	public Collection<InputOperationType> getInputOperationTypes () {
		return null;
	}
	
	@Override
	public Collection<OutputOperationType> getOutputOperationTypes () {
		return null;
	}

	private MessageSource createMessageSourceForBeans (final Map<String, Object> beans) {
		final Set<String> baseNames = new HashSet<String> ();

		for (final Map.Entry<String, Object> entry: beans.entrySet ()) {
			final Object bean = entry.getValue ();
			final Class<?> cls = bean.getClass ();
			final MappingOperation annotation = cls.getAnnotation (MappingOperation.class);
			
			// Add message references from the annotation:
			if (annotation != null) {
				for (final String messageSource: annotation.messageSources ()) {
					baseNames.add (messageSource);
				}
			}
			
			// Add the default resource bundles:
			baseNames.add (cls.getCanonicalName ());
			baseNames.add (String.format ("%s.messages", cls.getPackage ().getName ()));
		}
		
		final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource ();
		
		messageSource.setBasenames (baseNames.toArray (new String[baseNames.size ()]));
		
		return messageSource;
	}
	
	private Collection<OperationType> createOperationTypesForBeans (final Map<String, Object> beans, final MessageSource messageSource) {
		final List<OperationType> operationTypes = new ArrayList<OperationType> ();
		
		for (final Map.Entry<String, Object> entry: beans.entrySet ()) {
			operationTypes.add (createOperationTypeForBean (entry.getValue (), entry.getKey (), messageSource));
		}
		
		return operationTypes;
	}
	
	private OperationType createOperationTypeForBean (final Object bean, final String name, final MessageSource messageSource) {
		final Method operationMethod = AnnotationOperationType.getOperationMethod (bean);
		
		if (AnnotationOperationType.isInput (operationMethod)) {
			return new AnnotationInputOperationType (bean, name, messageSource);
		} else if (AnnotationOperationType.isOutput (operationMethod)) {
			return new AnnotationOutputOperationType (bean, name, messageSource);
		} else if (AnnotationOperationType.isTransform (operationMethod)) {
			return new AnnotationTransformOperationType (bean, name, messageSource);
		}
		
		throw new OperationDiscovererException (String.format ("Unknown operation type for %s", name));
	}
	
	/**
	 * Returns a collection of candidate beans, containing the MappingOperation.
	 * 
	 * @return All beans in the application context having the MappingOperation annotation.
	 */
	private Map<String, Object> getCandidateBeans () {
		return Collections.unmodifiableMap (applicationContext.getBeansWithAnnotation (MappingOperation.class));
	}
	
	@Override
	public void setApplicationContext (final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
