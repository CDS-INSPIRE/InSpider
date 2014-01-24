package nl.ipo.cds.etl;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import nl.idgis.commons.jobexecutor.JobLogger;
import nl.idgis.commons.jobexecutor.JobLogger.LogLevel;
import nl.ipo.cds.domain.EtlJob;
import nl.ipo.cds.etl.log.EventLogger;
import nl.ipo.cds.etl.log.LogStringBuilder;
import nl.ipo.cds.validation.Expression;
import nl.ipo.cds.validation.Validation;
import nl.ipo.cds.validation.ValidationReporter;
import nl.ipo.cds.validation.ValidatorContext;
import nl.ipo.cds.validation.execute.Compiler;
import nl.ipo.cds.validation.execute.CompilerException;
import nl.ipo.cds.validation.gml.codelists.CodeListFactory;
import nl.ipo.cds.validation.logical.AndExpression;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deegree.geometry.primitive.Point;

public abstract class AbstractValidator<T extends PersistableFeature, Keys extends Enum<Keys> & ValidatorMessageKey<Keys, Context>, Context extends ValidatorContext<Keys, Context>> 
	extends Validation<Keys, Context> 
	implements Validator<T> {

	private static final Log log = LogFactory.getLog (AbstractValidator.class);
	
	private final Class<Context> contextClass;
	private final Class<T> featureClass;
	private final Map<Object, Object> validatorMessages;
	
	private SortedMap<String, nl.ipo.cds.validation.Validator<Keys, Context>> validators = null;
	private Map<String, ValidatorExecutor<T, Keys, Context>> executors = null;
	private ValidatorExecutor<T, Keys, Context> executor = null;
	
	private SortedMap<String, nl.ipo.cds.validation.Validator<Keys, Context>> preValidators = null;
	private Map<String, GlobalValidatorExecutor<Keys, Context>> preExecutors = null;
	private GlobalValidatorExecutor<Keys, Context> preExecutor = null;
	
	private SortedMap<String, nl.ipo.cds.validation.Validator<Keys, Context>> postValidators = null;
	private Map<String, GlobalValidatorExecutor<Keys, Context>> postExecutors = null;
	private GlobalValidatorExecutor<Keys, Context> postExecutor = null;
	
	@Retention (RetentionPolicy.RUNTIME)
	public @interface Precondition {
	}
	
	@Retention (RetentionPolicy.RUNTIME)
	public @interface Postcondition {
	}
	
	public AbstractValidator (final Class<Context> contextClass, final Class<T> featureClass, final Map<Object, Object> validatorMessages) {
		if (contextClass == null) {
			throw new NullPointerException ("contextClass cannot be null");
		}
		
		this.contextClass = contextClass;
		this.featureClass = featureClass;
		this.validatorMessages = new HashMap<Object, Object> (validatorMessages);
	}
	
	public final void compile () throws CompilerException {
		this.validators = new TreeMap<String, nl.ipo.cds.validation.Validator<Keys, Context>> (findValidators (null));
		this.preValidators = new TreeMap<String, nl.ipo.cds.validation.Validator<Keys, Context>> (findValidators (Precondition.class));
		this.postValidators = new TreeMap<String, nl.ipo.cds.validation.Validator<Keys, Context>> (findValidators (Postcondition.class));
		this.executors = createExecutors (contextClass, validators);
		this.preExecutors = createGlobalExecutors (contextClass, preValidators);
		this.postExecutors = createGlobalExecutors (contextClass, postValidators);
		this.executor = createExecutor (contextClass, validators);
		this.preExecutor = createGlobalExecutor (contextClass, preValidators);
		this.postExecutor = createGlobalExecutor (contextClass, postValidators);
	}
	
	public Map<String, nl.ipo.cds.validation.Validator<Keys, Context>> getValidators () {
		if (validators == null) {
			throw new IllegalStateException ("Validator is not compiled");
		}
		
		return Collections.unmodifiableMap (validators);
	}
	
	public nl.ipo.cds.validation.Validator<Keys, Context> getValidator (final String name) {
		if (validators == null) {
			throw new IllegalStateException ("Validator is not compiled");
		}
		
		return validators.get (name);
	}
	
	private Map<String, nl.ipo.cds.validation.Validator<Keys, Context>> findValidators (final Class<? extends Annotation> annotationClass) {
		final Map<String, nl.ipo.cds.validation.Validator<Keys, Context>> validators = new HashMap<String, nl.ipo.cds.validation.Validator<Keys, Context>> ();
		
		for (final Method method: getClass ().getMethods ()) {
			if (Modifier.isStatic (method.getModifiers ()) 
					|| !nl.ipo.cds.validation.Validator.class.isAssignableFrom (method.getReturnType ())
					|| method.getParameterTypes ().length > 0) {
				continue;
			}

			if (annotationClass == null && (method.getAnnotation (Precondition.class) != null || method.getAnnotation (Postcondition.class) != null)) {
				continue;
			}
			
			if (annotationClass != null && method.getAnnotation (annotationClass) == null) {
				continue;
			}
			
			method.setAccessible (true);
			
			try {
				@SuppressWarnings("unchecked")
				final nl.ipo.cds.validation.Validator<Keys, Context> validator = (nl.ipo.cds.validation.Validator<Keys, Context>)method.invoke (this);
				validators.put (demangleMethodName (method.getName ()), validator);
			} catch (IllegalAccessException e) {
				throw new RuntimeException (e);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException (e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException (e);
			}
		}
		
		return validators;
	}
	
	private static String demangleMethodName (final String input) {
		final String nameWithoutGet;
		if (input.startsWith ("get")) {
			nameWithoutGet = input.substring (3, 4).toLowerCase () + input.substring (4);
		} else {
			nameWithoutGet = input;
		}
		
		if (nameWithoutGet.endsWith ("Validator")) {
			return nameWithoutGet.substring (0, nameWithoutGet.length () - 9);
		}
		
		return nameWithoutGet;
	}
	
	private Map<String, ValidatorExecutor<T, Keys, Context>> createExecutors (final Class<Context> contextClass, final Map<String, nl.ipo.cds.validation.Validator<Keys, Context>> validators) throws CompilerException {
		final Map<String, ValidatorExecutor<T, Keys, Context>> executors = new HashMap<String, ValidatorExecutor<T, Keys, Context>> ();
		final Compiler<Context> compiler = new Compiler<Context> (contextClass).addBean ("feature", featureClass);
		
		@SuppressWarnings("unchecked")
		final Class<ValidatorExecutor<T, Keys, Context>> executorClass = (Class<ValidatorExecutor<T, Keys, Context>>)((Class<?>)ValidatorExecutor.class);
		
		for (final Map.Entry<String, nl.ipo.cds.validation.Validator<Keys, Context>> entry: validators.entrySet ()) {
			executors.put (entry.getKey (), compiler.compile (entry.getValue (), executorClass));
		}
		
		return executors;
	}
	
	private Map<String, GlobalValidatorExecutor<Keys, Context>> createGlobalExecutors (final Class<Context> contextClass, final Map<String, nl.ipo.cds.validation.Validator<Keys, Context>> validators) throws CompilerException {
		final Map<String, GlobalValidatorExecutor<Keys, Context>> executors = new HashMap<String, GlobalValidatorExecutor<Keys, Context>> ();
		final Compiler<Context> compiler = new Compiler<Context> (contextClass);
		
		@SuppressWarnings("unchecked")
		final Class<GlobalValidatorExecutor<Keys, Context>> executorClass = (Class<GlobalValidatorExecutor<Keys, Context>>)((Class<?>)GlobalValidatorExecutor.class);
		
		for (final Map.Entry<String, nl.ipo.cds.validation.Validator<Keys, Context>> entry: validators.entrySet ()) {
			executors.put (entry.getKey (), compiler.compile (entry.getValue (), executorClass));
		}
		
		return executors;
	}
	
	private ValidatorExecutor<T, Keys, Context> createExecutor (final Class<Context> contextClass, final Map<String, nl.ipo.cds.validation.Validator<Keys, Context>> validators) throws CompilerException {
		// Create a single validator that combines all validation rules:
		final List<Expression<Keys, Context, Boolean>> inputs = new ArrayList<Expression<Keys, Context, Boolean>> ();
		for (final Map.Entry<String, nl.ipo.cds.validation.Validator<Keys, Context>> entry: validators.entrySet ()) {
			inputs.add (entry.getValue ());
		}
		
		final AndExpression<Keys, Context> andExpression = new AndExpression<Keys, Context> (inputs);
		
		// Compile a validator that contains the and expression: 
		@SuppressWarnings("unchecked")
		final Class<ValidatorExecutor<T, Keys, Context>> executorClass = (Class<ValidatorExecutor<T, Keys, Context>>)((Class<?>)ValidatorExecutor.class);
		
		final Compiler<Context> compiler = new Compiler<Context> (contextClass).addBean ("feature", featureClass);
		return compiler.compile (this.validate (andExpression), executorClass);
	}
	
	private GlobalValidatorExecutor<Keys, Context> createGlobalExecutor (final Class<Context> contextClass, final Map<String, nl.ipo.cds.validation.Validator<Keys, Context>> validators) throws CompilerException {
		// Create a single validator that combines all validation rules:
		final List<Expression<Keys, Context, Boolean>> inputs = new ArrayList<Expression<Keys, Context, Boolean>> ();
		for (final Map.Entry<String, nl.ipo.cds.validation.Validator<Keys, Context>> entry: validators.entrySet ()) {
			inputs.add (entry.getValue ());
		}
		
		if (inputs.size () == 0) {
			return null;
		}
		
		final Expression<Keys, Context, Boolean> andExpression;
		if (inputs.size () == 1) {
			andExpression = inputs.get (0);
		} else {
			andExpression = new AndExpression<Keys, Context> (inputs);
		}
		
		// Compile a validator that contains the and expression: 
		@SuppressWarnings("unchecked")
		final Class<GlobalValidatorExecutor<Keys, Context>> executorClass = (Class<GlobalValidatorExecutor<Keys, Context>>)((Class<?>)GlobalValidatorExecutor.class);
		
		final Compiler<Context> compiler = new Compiler<Context> (contextClass);
		return compiler.compile (this.validate (andExpression), executorClass);
	}
	
	public Context beforeJob (final EtlJob job, final CodeListFactory codeListFactory, final ValidationReporter<Keys, Context> reporter) {
		try {
			return contextClass.newInstance ();
		} catch (InstantiationException e) {
			throw new RuntimeException (e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException (e);
		}
	}
	
	public void afterJob (final EtlJob job, final EventLogger<Keys> logger, final Context context) {
	}
	
	public void beforeFeature (final EtlJob job, final EventLogger<Keys> logger, final Context context, final T feature) {
	}
	
	public void afterFeature (final EtlJob job, final EventLogger<Keys> logger, final Context context, final T feature) {
	}
	
	private boolean validateFeature (final EtlJob job, final EventLogger<Keys> logger, final T feature, final Context context, ValidatorExecutor<T, Keys, Context> executor) {
		final Boolean result = executor.validate (context, feature);
		return result != null && result;
	}
	
	@Override
	public FeatureFilter<T, T> getFilterForJob (final EtlJob etlJob, final CodeListFactory codeListFactory, final JobLogger logger) {
		return getFilterForJob (etlJob, codeListFactory, logger, executor, preExecutor, postExecutor);
	}
	
	public FeatureFilter<T, T> getFilterForJob (final EtlJob etlJob, final CodeListFactory codeListFactory, final JobLogger logger, final String validationName) {
		// Locate the executor:
		final ValidatorExecutor<T, Keys, Context> executor = executors.get (validationName);
		if (executor == null) {
			throw new IllegalArgumentException (String.format ("No validation named %s exists.", validationName));
		}
		
		return getFilterForJob (etlJob, codeListFactory, logger, executor, null, null);
	}
	
	
	private FeatureFilter<T, T> getFilterForJob (
			final EtlJob etlJob, 
			final CodeListFactory codeListFactory,
			final JobLogger logger, 
			final ValidatorExecutor<T, Keys, Context> executor, 
			final GlobalValidatorExecutor<Keys, Context> preExecutor, 
			final GlobalValidatorExecutor<Keys, Context> postExecutor) {
		
		// Create a log string builder for this job:
		final LogStringBuilder<Keys> logStringBuilder = new LogStringBuilder<Keys> ();
		logStringBuilder.setProperties (validatorMessages);
		logStringBuilder.setJobLogger (logger);
		
		return getFilterForJob (etlJob, codeListFactory, logStringBuilder, executor, preExecutor, postExecutor);
	}

	/**
	 * Create a filter for the given job with an existing event logger. Usefull mostly for use in
	 * unit-tests where logging needs to be intercepted.
	 * 
	 * @param etlJob
	 * @param logger
	 * @return
	 */
	public FeatureFilter<T, T> getFilterForJob (final EtlJob etlJob, final CodeListFactory codeListFactory, final EventLogger<Keys> logger) {
		return getFilterForJob (etlJob, codeListFactory, logger, executor, preExecutor, postExecutor);
	}
	
	public FeatureFilter<T, T> getFilterForJob (final EtlJob etlJob, final CodeListFactory codeListFactory, final EventLogger<Keys> logger, final String validationName) {
		// Locate the executor:
		final ValidatorExecutor<T, Keys, Context> executor = executors.get (validationName);
		if (executor == null) {
			throw new IllegalArgumentException (String.format ("No validation named %s exists.", validationName));
		}
		
		return getFilterForJob (etlJob, codeListFactory, logger, executor, null, null);
	}
	
	private FeatureFilter<T, T> getFilterForJob (
			final EtlJob etlJob,
			final CodeListFactory codeListFactory,
			final EventLogger<Keys> logger, 
			final ValidatorExecutor<T, Keys, Context> executor,
			final GlobalValidatorExecutor<Keys, Context> preExecutor,
			final GlobalValidatorExecutor<Keys, Context> postExecutor) {
		
		final Reporter reporter = new Reporter (etlJob, logger);
		final Context context = beforeJob (etlJob, codeListFactory, reporter);
		
		if (preExecutor != null) {
			preExecutor.validate (context);
		}
		
		return new FeatureFilter<T, T> () {
			
			@Override
			public void processFeature (final T feature, final FeatureOutputStream<T> outputStream,
					final FeatureOutputStream<Feature> errorOutputStream) {
				
				log.debug("validating feature: " + feature.getId());

				beforeFeature (etlJob, logger, context, feature);
				
				if (validateFeature (etlJob, logger, feature, context, executor)) {
					outputStream.writeFeature (feature);
				} else {
					errorOutputStream.writeFeature (feature);
				}
				
				afterFeature (etlJob, logger, context, feature);
			}

			@Override
			public void finish() {
				if (postExecutor != null) {
					postExecutor.validate (context);
				}
				
				afterJob (etlJob, logger, context);
				
				if (reporter.hasErrors ()){
					etlJob.setGeometryErrorCount (reporter.getGeometryErrorCount ());
				}
			}
		};
	}
	
	public static interface ValidatorExecutor<T extends PersistableFeature, Keys extends Enum<Keys> & ValidatorMessageKey<Keys, Context>, Context extends ValidatorContext<Keys, Context>> {
		Boolean validate (final Context context, final T feature);
	}
	
	public static interface GlobalValidatorExecutor<Keys extends Enum<Keys> & ValidatorMessageKey<Keys, Context>, Context extends ValidatorContext<Keys, Context>> {
		Boolean validate (Context context);
	}
	
	public class Reporter implements ValidationReporter<Keys, Context> {
		private int geometryErrorCount = 0;
		private int errorCount = 0;
		private final HashMap<Keys, Integer> eventCounters = new HashMap<Keys, Integer> ();
		private final EtlJob job;
		private final EventLogger<Keys> logger;
		
		public Reporter (final EtlJob job, final EventLogger<Keys> logger) {
			this.job = job;
			this.logger = logger;
		}
		
		@Override
		public void reportValidationError (
				final nl.ipo.cds.validation.Validator<Keys, Context> validator, 
				final Context context, 
				final Keys messageKey, 
				final Object[] parameters) {
			
			// Convert message values to string:
			final String[] messageValues;
			if (parameters != null) {
				messageValues = new String[parameters.length];
				for (int i = 0; i < parameters.length; ++ i) {
					messageValues[i] = parameters[i] == null ? "" : parameters[i].toString ();
				}
			} else {
				messageValues = new String[0];
			}
			
			// Get the current inspire ID:
			final String currentInspireId = messageValues.length >= 2 ? messageValues[1] : "";
			
			if (messageKey.isAddToShapeFile ()) {
				logEvent (context, messageKey, context.getLastLocation (), currentInspireId, messageValues);
			} else {
				logEvent (context, messageKey, messageValues);
			}
		}
		
		public void logEvent (final Context context, Keys messageKey, String... messageValues) {
			this.logEvent(context, messageKey, null, null, messageValues);
		}
		
		public void logEvent (final Context context, Keys messageKey, Point point, String inspireId, String... messageValues) {
			final String currentId = messageValues.length >= 1 && messageValues[0] != null && messageValues[0].length () > 0 ? messageValues[0] : "[onbekend]";
			// final String currentInspireId = messageValues.length >= 2 ? messageValues[1] : "";
			
			messageValues = messageValues.length > 2 ? Arrays.copyOfRange (messageValues, 2, messageValues.length) : new String[0];
			
			if(messageKey.isAddToShapeFile()){
				++ geometryErrorCount;
			}

			if(messageKey.getLogLevel () == LogLevel.ERROR) {
				++ errorCount;
			}
			
			Integer counter = eventCounters.get(messageKey);
			if(counter == null) {
				log.debug("new counter for message: " + messageKey);
				counter = 1;
				eventCounters.put(messageKey, counter);
			} else {
				log.debug("incrementing counter for message: " + messageKey + " current value: " + counter);
				eventCounters.put(messageKey, counter + 1);
			}
			
			if(counter < messageKey.getMaxMessageLog()) {
				String[] idMessageValues = new String[messageValues.length + 1];			
				idMessageValues[0] = currentId;
				System.arraycopy(messageValues, 0, idMessageValues, 1, messageValues.length);
				log.debug("event logged: " + messageKey);
				if(point != null){
					logger.logEvent(job, messageKey, messageKey.getLogLevel (), point.get0(), point.get1(), inspireId, idMessageValues);
				} else{
					logger.logEvent(job, messageKey, messageKey.getLogLevel (), idMessageValues);
				}
			} else if(counter == messageKey.getMaxMessageLog()) {
				log.debug("max message log reached for message: " + messageKey);
				logger.logEvent(job, messageKey.getMaxMessageKey (), messageKey.getMaxMessageKey ().getLogLevel (), 
					messageKey.toString());
			} else {
				log.debug("event ignored: " + messageKey);
			}
		}
		
		public boolean hasErrors () {
			return errorCount > 0;
		}
		
		public int getGeometryErrorCount () {
			return geometryErrorCount;
		}
	}
}
