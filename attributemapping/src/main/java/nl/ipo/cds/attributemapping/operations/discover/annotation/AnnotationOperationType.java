package nl.ipo.cds.attributemapping.operations.discover.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import nl.ipo.cds.attributemapping.MapperContext;
import nl.ipo.cds.attributemapping.MappingDestination;
import nl.ipo.cds.attributemapping.MappingSource;
import nl.ipo.cds.attributemapping.executer.OperationExecuter;
import nl.ipo.cds.attributemapping.executer.OperationExecutionException;
import nl.ipo.cds.attributemapping.operations.OperationInputType;
import nl.ipo.cds.attributemapping.operations.OperationType;
import nl.ipo.cds.attributemapping.operations.annotation.After;
import nl.ipo.cds.attributemapping.operations.annotation.Before;
import nl.ipo.cds.attributemapping.operations.annotation.Execute;
import nl.ipo.cds.attributemapping.operations.annotation.Input;
import nl.ipo.cds.attributemapping.operations.annotation.MappingOperation;
import nl.ipo.cds.attributemapping.operations.discover.OperationDiscovererException;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import com.googlecode.gentyref.GenericTypeReflector;

public abstract class AnnotationOperationType implements OperationType {
	
	private final static ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer ();

	private final String name;
	private final MessageSource messageSource;
	private final Object bean;
	private final Class<?> cls;
	private final Method operationMethod;
	private final List<AnnotationOperationInputType> inputs;
	private final Class<?> propertiesClass;
	private final boolean internal;
	private final Method beforeMethod;
	private final Method afterMethod;
	
	AnnotationOperationType (final Object bean, final String name, final MessageSource messageSource) {
		this.name = name;
		this.messageSource = messageSource;
		this.bean = bean;
		this.cls = bean.getClass ();
		this.operationMethod = getOperationMethod (bean);
		this.internal = isInternal (bean);
		propertiesClass = getPropertiesClass (bean);
		this.beforeMethod = getBeforeMethod (bean, propertiesClass);
		this.afterMethod = getAfterMethod (bean, propertiesClass);
		
		// Collect inputs:
		this.inputs = createInputs ();
		
	}
	
	public boolean isInternal () {
		return internal;
	}
	
	@Override
	public String getName () {
		return name;
	}
	
	@Override
	public String getDescription (final Locale locale) {
		try {
			return messageSource.getMessage (String.format ("%s.description", getMessageKey ()), null, locale);
		} catch (NoSuchMessageException e) {
			return name;
		}
	}
	
	@Override
	public String getLabel (final Locale locale) {
		try {
			return messageSource.getMessage (String.format ("%s.label", getMessageKey ()), null, locale);
		} catch (NoSuchMessageException e) {
			return getDescription (locale);
		}
	}
	
	@Override
	public String getFormatLabel (final Locale locale) {
		try {
			return messageSource.getMessage (String.format ("%s.formatLabel", getMessageKey ()), null, locale);
		} catch (NoSuchMessageException e) {
			return getLabel (locale);
		}
	}

	@Override
	public Type getReturnType () {
		return operationMethod.getGenericReturnType ();
	}
	
	@Override
	public Class<?> getPropertyBeanClass () {
		final MappingOperation mappingOperation = bean.getClass ().getAnnotation (MappingOperation.class);
		
		if (mappingOperation == null || Object.class.equals (mappingOperation.propertiesClass ())) {
			return null;
		}
		
		return mappingOperation.propertiesClass ();
	}
	
	@Override
	public List<OperationInputType> getInputs () {
		return Collections.<OperationInputType>unmodifiableList (inputs);
	}
	
	@Override
	public OperationExecuter createExecuter (final Object operationProperties, final MapperContext context) {
		// Substitute:
		// - Operation properties (static)
		// - Mapper context (static)
		// - Source (dynamic)
		// - Destination (dynamic)
		// - Inputs (dynamic)

		final int argumentCount = operationMethod.getParameterTypes().length;
		final Class<?>[] parameterTypes = operationMethod.getParameterTypes ();
		
		// Calculate a mapping from inputs to arguments:
		final int[] inputMapping = new int[inputs.size ()];
		final int inputCount = inputs.size ();
		for (int i = 0; i < inputs.size (); ++ i) {
			inputMapping[i] = inputs.get (i).getParameterIndex ();
		}
		
		// Determine a mapping for static properties:
		final int contextOffset = getParameterIndexOfType (operationMethod, MapperContext.class);
		final int propertiesOffset = propertiesClass == null ? -1 : getParameterIndexOfType (operationMethod, propertiesClass);
		
		// Determine a mapping for dynamic properties:
		final int sourceOffset = getParameterIndexOfType (operationMethod, MappingSource.class);
		final int destOffset = getParameterIndexOfType (operationMethod, MappingDestination.class);
		
		// Varargs:
		final boolean isVarArgs = operationMethod.isVarArgs ();
		final Class<?> varArgsClass;
		final int copyInputCount;
		final int varArgsOffset;
		if (isVarArgs) {
			varArgsClass = parameterTypes[parameterTypes.length - 1].getComponentType ();
			copyInputCount = inputCount - 1;
			varArgsOffset = operationMethod.getParameterTypes().length - 1;
		} else {
			varArgsClass = null;
			copyInputCount = inputCount;
			varArgsOffset = 0;
		}
		
		return new OperationExecuter() {
			@Override
			public Object execute (final MappingSource source, final MappingDestination destination,
					final List<Object> inputs) throws OperationExecutionException {
				
				final Object[] args = new Object[argumentCount];
				
				// Copy the inputs:
				int i;
				for (i = 0; i < copyInputCount; ++ i) {
					args[inputMapping[i]] = inputs.get (i);
				}
				
				if (isVarArgs) {
					final Object[] a = (Object[])Array.newInstance (varArgsClass, inputs.size () - copyInputCount);
					for (int len = inputs.size (), j = 0; i < len; ++ i, ++ j) {
						a[j] = inputs.get (i);
					}
					args[varArgsOffset] = a;
				}
				
				// Copy static properties:
				if (contextOffset >= 0) {
					args[contextOffset] = context;
				}
				if (propertiesOffset >= 0) {
					args[propertiesOffset] = operationProperties;
				}
				
				// Copy dynamic properties:
				if (sourceOffset >= 0) {
					args[sourceOffset] = source;
				}
				if (destOffset >= 0) {
					args[destOffset] = destination;
				}
				
				try {
					return operationMethod.invoke (bean, args);
				} catch (Throwable e) {
					throw new OperationExecutionException (String.format ("An error has occured while executing %s.%s: %s", operationMethod.getDeclaringClass ().getCanonicalName (), operationMethod.getName (), e.getMessage ()), e);
				}
			}

			@Override
			public void before () throws OperationExecutionException {
				executeBeforeAfterMethod (beforeMethod);
			}

			@Override
			public void after () throws OperationExecutionException {
				executeBeforeAfterMethod (afterMethod);
			}
			
			private void executeBeforeAfterMethod (final Method method) throws OperationExecutionException {
				if (method == null) {
					return;
				}
				
				try {
					if (method.getParameterTypes ().length == 1) {
						method.invoke (bean, operationProperties);
					} else {
						method.invoke (bean);
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new OperationExecutionException (String.format ("An error has occured while executing %s.%s: %s", operationMethod.getDeclaringClass ().getCanonicalName (), afterMethod.getName (), e.getMessage ()), e);
				}
			}
		};
	}
	
	public MessageSource getMessageSource () {
		return messageSource;
	}
	
	public String getMessageKey () {
		return cls.getCanonicalName ();
	}
	
	public Object getBean () {
		return bean;
	}
	
	private List<AnnotationOperationInputType> createInputs () {
		final List<AnnotationOperationInputType> inputTypes = new ArrayList<AnnotationOperationInputType> ();
		final Set<String> names = new HashSet<String> ();
		
		for (final ParameterAnnotation<Input> param: getInputParameters (operationMethod)) {
			final String name = param.annotation.value ().length() > 0 ? param.annotation.value () : param.name;
			
			// Check for duplicate names:
			if (names.contains (name)) {
				throw new OperationDiscovererException (String.format (
						"Execute method `%s.%s` has a duplicate input parameter named `%s`",
						operationMethod.getDeclaringClass ().getCanonicalName (),
						operationMethod.getName (),
						name
					));
			}
			
			names.add (name);
			
			inputTypes.add (new AnnotationOperationInputType (
					this,
					name,
					param.parameterIndex,
					param.parameterType,
					param.varArgs
				));
		}
		
		return inputTypes;
	}
	
	static boolean isInternal (final Object bean) {
		final MappingOperation annotation = bean.getClass ().getAnnotation (MappingOperation.class);
		
		if (annotation == null) {
			return false;
		}
		
		return annotation.internal ();
	}
	
	static boolean isInput (final Method method) {
		return !method.getReturnType ().equals (Void.TYPE) && getInputParameters (method).size () == 0;
	}
	
	static boolean isOutput (final Method method) {
		return method.getReturnType ().equals (Void.TYPE) && getInputParameters (method).size () == 1;
	}
	
	static boolean isTransform (final Method method) {
		return !method.getReturnType ().equals (Void.TYPE) && getInputParameters (method).size () > 0;
	}
	
	private static int getParameterIndexOfType (final Method method, final Class<?> cls) {
		final Class<?>[] parameterTypes = method.getParameterTypes ();
		final Annotation[][] parameterAnnotations = method.getParameterAnnotations ();
		
		for (int i = 0; i < parameterTypes.length; ++ i) {
			if (parameterTypes[i].equals (cls) && !hasAnnotation (parameterAnnotations[i], Input.class)) {
				return i;
			}
		}
		
		return -1;
	}
	
	private static boolean hasAnnotation (final Annotation[] annotations, final Class<? extends Annotation> cls) {
		for (final Annotation annotation: annotations) {
			if (cls.isAssignableFrom (annotation.getClass ())) {
				return true;
			}
		}
		
		return false;
	}
	
	private static List<ParameterAnnotation<Input>> getInputParameters (final Method method) {
		final Type[] parameterTypes = method.getGenericParameterTypes ();
		final String[] parameterNames = parameterNameDiscoverer.getParameterNames (method);
		final Annotation[][] parameterAnnotations = method.getParameterAnnotations ();
		final List<ParameterAnnotation<Input>> inputParameters = new ArrayList<ParameterAnnotation<Input>> ();
				
		for (int i = 0; i < parameterTypes.length; ++ i) {
			// Locate the Input annotation:
			Input annotation = null;
			for (final Annotation a: parameterAnnotations[i]) {
				if (a instanceof Input) {
					annotation = (Input)a;
					break;
				}
			}
			
			if (annotation == null) {
				continue;
			}

			final boolean isVarargs = (i == parameterTypes.length - 1 && method.isVarArgs () && GenericTypeReflector.erase (parameterTypes[i]).isArray ());
			final Type parameterType;
			if (isVarargs) {
				parameterType = GenericTypeReflector.getArrayComponentType (parameterTypes[i]);
			} else {
				parameterType = parameterTypes[i];
			}
			
			inputParameters.add (new ParameterAnnotation<Input> (
					parameterType,
					parameterNames == null ? ("input" + inputParameters.size ()) : parameterNames[i],
					i,
					isVarargs, 
					annotation
				));
		}
		
		return inputParameters; 
	}
	
	/**
	 * Returns the method on the given bean that implements the operation.
	 * 
	 * @param bean
	 * @return The method that implements the operation.
	 */
	public static Method getOperationMethod (final Object bean) {
		final Class<?> cls = bean.getClass ();
		
		// Look for a method that is annotated with the Execute annotation:
		final Method annotatedMethod = findMethodWithAnnotation (cls, Execute.class);
		if (annotatedMethod != null) {
			validateOperationMethod (annotatedMethod);
			return annotatedMethod;
		}
		
		// If the bean has a single public method, use that as the operation method:
		final Method[] methods = cls.getDeclaredMethods ();
		if (methods.length == 1) {
			validateOperationMethod (methods[0]);
			return methods[0];
		}
		
		throw new OperationDiscovererException (String.format ("Bean of class `%s` has no execute method.", bean.getClass ().getCanonicalName ()));
	}
	
	public static Method getBeforeMethod (final Object bean, final Class<?> propertiesClass) {
		final Method beforeMethod = findMethodWithAnnotation (bean.getClass (), Before.class);
		
		if (beforeMethod != null) {
			validateBeforeAfterMethod (beforeMethod, propertiesClass);
		}
		
		return beforeMethod;
	}
	
	public static Method getAfterMethod (final Object bean, final Class<?> propertiesClass) {
		final Method afterMethod = findMethodWithAnnotation (bean.getClass (), After.class);
		
		if (afterMethod != null) {
			validateBeforeAfterMethod (afterMethod, propertiesClass);
		}
		
		return afterMethod;
	}
	
	static void validateOperationMethod (final Method method) {
		if (method.getReturnType ().equals (Void.TYPE) && getInputParameters (method).size () == 0) {
			throw new OperationDiscovererException (String.format ("Mapping operation method `%s.%s` must not have a void return value and an empty argument list.", method.getDeclaringClass ().getCanonicalName (), method.getName ()));
		}
		
		if (!isInput (method) && !isOutput (method) && !isTransform (method)) {
			throw new OperationDiscovererException (String.format ("Mapping operation `%s.%s` has an unknown type", method.getDeclaringClass ().getCanonicalName (), method.getName ()));
		}
	}
	
	static void validateBeforeAfterMethod (final Method method, final Class<?> propertiesClass) {
		if (!method.getReturnType ().equals (Void.TYPE)) {
			throw new OperationDiscovererException (String.format ("Mapping operation method `%s.%s` must have a void return type", method.getDeclaringClass ().getCanonicalName (), method.getName ()));
		}

		final Class<?>[] parameterTypes = method.getParameterTypes ();
		if (parameterTypes.length > 1) {
			throw new OperationDiscovererException (String.format ("Mapping operation method `%s.%s` can have at most one parameter", method.getDeclaringClass ().getCanonicalName (), method.getName ()));
		}
		
		if (parameterTypes.length == 1 && propertiesClass == null) {
			throw new OperationDiscovererException (String.format ("Mapping operation method `%s.%s` cannot have an argument: there is no settings class", method.getDeclaringClass ().getCanonicalName (), method.getName ()));
		}
		
		if (parameterTypes.length == 1 && !propertiesClass.isAssignableFrom (parameterTypes[0])) {
			throw new OperationDiscovererException (String.format ("Mapping operation method `%s.%s` has a settings parameter of wrong type `%s`, should be `%s`", method.getDeclaringClass ().getCanonicalName (), method.getName (), parameterTypes[0], propertiesClass));
		}
	}
	
	static Method findMethodWithAnnotation (final Class<?> cls, final Class<? extends Annotation> annotation) {
		Method result = null;
		
		for (final Method method: cls.getMethods ()) {
			if (method.isAnnotationPresent (annotation)) {
				if (result != null) {
					throw new OperationDiscovererException (String.format ("Multiple methods annotated with %s in %s", annotation.getName (), cls.getCanonicalName ()));
				}
				result = method;
			}
		}
		
		return result;
	}

	static Class<?> getPropertiesClass (final Object bean) {
		final Class<?> cls = bean.getClass ();
		final MappingOperation operation = cls.getAnnotation (MappingOperation.class);
		
		if (operation == null || Object.class.equals (operation.propertiesClass ())) {
			return null;
		}
		
		return operation.propertiesClass ();
	}
	
	private static class ParameterAnnotation<T extends Annotation> {
		public final Type parameterType;
		public final String name;
		public final int parameterIndex;
		public final T annotation;
		public final boolean varArgs;
		
		ParameterAnnotation (final Type parameterType, final String name, final int parameterIndex, final boolean varArgs, final T annotation) {
			this.parameterType = parameterType;
			this.name = name;
			this.parameterIndex = parameterIndex;
			this.annotation = annotation;
			this.varArgs = varArgs;
		}
	}
}
