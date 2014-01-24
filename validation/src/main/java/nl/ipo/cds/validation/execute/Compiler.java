package nl.ipo.cds.validation.execute;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Compiler<C> {

    private final static MethodHandle mapGetHandle = findMethod (Map.class, "get", MethodType.methodType (Object.class, Object.class));
    private final static MethodHandle foldIntObjectsHandle = findStaticMethod (Compiler.class, "fold", MethodType.methodType (Object.class, Integer.TYPE, Object[].class));
    private final static MethodHandle foldMapObjectsHandle = findStaticMethod (Compiler.class, "fold", MethodType.methodType (Map.class, int[].class, String.class, Object[].class));

	public static MethodHandle findMethod (final Class<?> cls, final String name, final MethodType methodType) {
		try {
			return MethodHandles.lookup ().findVirtual (cls, name, methodType);
		} catch (NoSuchMethodException | IllegalAccessException e) {
			throw new AssertionError (e);
		}
	}

    public static MethodHandle findStaticMethod (final Class<?> cls, final String name, final MethodType methodType) {
        try {
            return MethodHandles.lookup ().findStatic (cls, name, methodType);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new AssertionError (e);
        }
    }
	
	private final static HashMap<Class<?>, Class<?>> primitiveTypeMap = new HashMap<Class<?>, Class<?>> ();
	static {
		primitiveTypeMap.put (Byte.TYPE, Byte.class);
		primitiveTypeMap.put (Character.TYPE, Character.class);
		primitiveTypeMap.put (Short.TYPE, Short.class);
		primitiveTypeMap.put (Integer.TYPE, Integer.class);
		primitiveTypeMap.put (Long.TYPE, Long.class);
		primitiveTypeMap.put (Float.TYPE, Float.class);
		primitiveTypeMap.put (Double.TYPE, Double.class);
		primitiveTypeMap.put (Boolean.TYPE, Boolean.class);
		primitiveTypeMap.put (Void.TYPE, Void.class);
	}
	
	public final Class<C> contextClass;
	private final LinkedHashMap<String, Class<?>> beanClasses;
	
	public Compiler (final Class<C> contextClass) {
		this (contextClass, new LinkedHashMap<String, Class<?>> ());
	}
	
	private Compiler (
			final Class<C> contextClass, 
			final LinkedHashMap<String, Class<?>> beanClasses) {
		
		if (contextClass == null) {
			throw new NullPointerException ("contextClass cannot be null");
		}
		if (Object.class.equals (contextClass)) {
			throw new IllegalArgumentException ("cannot use Object as a context class");
		}
		
		this.contextClass = contextClass;
		this.beanClasses = beanClasses;
	}
	
	public Compiler<C> addMap (final String name) {
		return addBean (name, Map.class);
	}
	
	public Compiler<C> addBean (final String name, final Class<?> beanClass) {
		final LinkedHashMap<String, Class<?>> beans = new LinkedHashMap<String, Class<?>> (beanClasses);
		beans.put (name, beanClass);
		return new Compiler<C> (contextClass, beans);
	}
	
	public ExpressionExecutor<C> createGetAttributeExecutor (final String name, final ExecutableExpression<C, ?> expression) throws CompilerException {
		final Class<?> type = expression.getResultType ();
		
		// Numeric attribute names are resolved by looking for a "get" method with a single integer parameter,
		// such as the get method of java.util.List.
		if (name.matches ("^[0-9]+$")) {
			final int index = Integer.parseInt (name);
			int n = 0;
			for (final Map.Entry<String, Class<?>> entry: beanClasses.entrySet ()) {
				final Class<?> beanClass = entry.getValue ();
				
				try {
					final Method method = beanClass.getMethod ("get", Integer.TYPE);
					if (method.getReturnType ().equals (type)) {
                        final MethodHandle methodHandle = MethodHandles.lookup ().unreflect (method);

						return createGetAttributeListExecutor (expression, n, beanClass, methodHandle, index);
					}
				} catch (NoSuchMethodException | IllegalAccessException e) {
				}
				
				++ n;
			}
			
			throw new CompilerException ("No list found for index: " + name);
		}
		
		// Locate a getter in one of the input classes:
		final String methodName = String.format ("get%s%s", name.substring (0, 1).toUpperCase (), name.substring (1));
		int n = 0;
		for (final Map.Entry<String, Class<?>> entry: beanClasses.entrySet ()) {
			final Class<?> beanClass = entry.getValue ();
			final Method method;
			
			try {
				method = beanClass.getMethod (methodName);
				if (method.getReturnType ().equals (type) || (method.getReturnType ().isPrimitive () && primitiveTypeMap.get (method.getReturnType ()).equals (type))) {
					method.setAccessible (true);
					final MethodHandle methodHandle = MethodHandles
							.lookup ()
							.unreflect (method);
					
					return createGetAttributeExecutor (expression, n, beanClass, methodHandle);
				}
			} catch (NoSuchMethodException | IllegalAccessException e) {
			}
			
			++ n;
		}
		
		// Locate a getter in the context object:
		try {
			final Method method = contextClass.getMethod (methodName);
			if (method.getReturnType ().equals (type) || (method.getReturnType ().isPrimitive () && primitiveTypeMap.get (method.getReturnType ()).equals (type))) {
				method.setAccessible (true);
				final MethodHandle methodHandle = MethodHandles
						.lookup ()
						.unreflect (method);
				
				return createGetAttributeExecutorForContext (expression, methodHandle);
			}
		} catch (NoSuchMethodException | IllegalAccessException e) {
		}
		
		// Fetch attributes from the maps:
		final List<Integer> indices = new ArrayList<Integer> ();
		n = 0;
		for (final Map.Entry<String, Class<?>> entry: beanClasses.entrySet ()) {
			final Class<?> beanClass = entry.getValue ();
			
			if (Map.class.isAssignableFrom (beanClass)) {
				indices.add (n);
			}
			
			++ n;
		}
		
		if (indices.isEmpty ()) {
			throw new CompilerException (String.format ("Unable to locate attribute `%s`", name));
		}
		
		try {
			if (indices.size () == 1) {
				return createGetAttributeExecutor (expression, indices.get (0), name);
			} else {
				return createGetAttributeExecutor (expression, indices, name);
			}
		} catch (NoSuchMethodException e) {
			throw new CompilerException (e);
		} catch (SecurityException e) {
			throw new CompilerException (e);
		}
	}
	
	private ExpressionExecutor<C> createGetAttributeExecutor (final ExecutableExpression<C, ?> expression, final int n, final Class<?> beanClass, final MethodHandle methodHandle) throws CompilerException, NoSuchMethodException, SecurityException {
		
		// The method may be from a superclass of the beanClass, in which case the accepted "this"
		// reference is not of type beanClass and should be cast to the proper type (see asType invocation
		// below):
		final Class<?> firstParameterType = methodHandle.type ().parameterType (0);
		
		assert (firstParameterType.isAssignableFrom (beanClass));
		
        // Create a method handle that accepts an object array and returns the value from the getter.
        // Index N is taken from the object array (using the method referred to by foldIntObjectsHandle).
        // The N parameter is bound to the foldIntObjectsHandle methodhandle before using it as a filter.
        final MethodHandle handle =
                MethodHandles.filterArguments (
                        methodHandle,
                        0,
                        MethodHandles.insertArguments (
                                foldIntObjectsHandle,
                                0,
                                n
                        ).asType (
                                MethodType.methodType (
                                        firstParameterType,
                                        Object[].class
                                )
                        )
                );

		return new ExpressionExecutor<C> (
				expression,
				Collections.<ExecutableExpression<C, ?>>emptyList (),
				false,
				true,
                handle,
				false
			);
	}
	
	private ExpressionExecutor<C> createGetAttributeExecutor (final ExecutableExpression<C, ?> expression, final int n, final String name) throws CompilerException, NoSuchMethodException, SecurityException {
        final MethodHandle handle =
                MethodHandles.insertArguments (
                        MethodHandles.filterArguments (
                                mapGetHandle,
                                0,
                                MethodHandles.insertArguments (
                                        foldIntObjectsHandle,
                                        0,
                                        n
                                ).asType (
                                        MethodType.methodType (
                                                Map.class,
                                                Object[].class
                                        )
                                )
                        ),
                        1,
                        name
                );
		
		return new ExpressionExecutor<C> (
				expression,
				Collections.<ExecutableExpression<C, ?>>emptyList (),
				false,
				true,
                handle,
				false
			);
	}

	private ExpressionExecutor<C> createGetAttributeExecutor (final ExecutableExpression<C, ?> expression, final List<Integer> indicesList, final String name) throws NoSuchMethodException, SecurityException, CompilerException {
		final int[] indices = new int[indicesList.size ()];
		for (int i = 0; i < indices.length; ++ i) {
			indices[i] = indicesList.get (i);
		}

        final MethodHandle handle =
                MethodHandles.insertArguments (
                        MethodHandles.filterArguments (
                                mapGetHandle,
                                0,
                                MethodHandles.insertArguments (
                                        foldMapObjectsHandle,
                                        0,
                                        indices,
                                        name
                                ).asType (
                                        MethodType.methodType (
                                                Map.class,
                                                Object[].class
                                        )
                                )
                        ),
                        1,
                        name
                );

		return new ExpressionExecutor<C> (
				expression,
				Collections.<ExecutableExpression<C, ?>>emptyList (),
				false,
				true,
                handle,
				false
			);
	}
	
	public ExpressionExecutor<C> createGetAttributeListExecutor (final ExecutableExpression<C, ?> expression, final int n, final Class<?> beanClass, final MethodHandle methodHandle, final int index) throws CompilerException, NoSuchMethodException, SecurityException {
        final MethodHandle handle =
                MethodHandles.insertArguments (
                        MethodHandles.filterArguments (
                                methodHandle,
                                0,
                                MethodHandles.insertArguments (
                                        foldIntObjectsHandle,
                                        0,
                                        n
                                ).asType (
                                        MethodType.methodType (
                                                beanClass,
                                                Object[].class
                                        )
                                )
                        ),
                        1,
                        Integer.valueOf (index)
                );

		return new ExpressionExecutor<C> (
				expression, 
				Collections.<ExecutableExpression<C, ?>>emptyList (), 
				false, 
				true, 
                handle,
				false
			);
	}
	
	public ExpressionExecutor<C> createGetAttributeExecutorForContext (final ExecutableExpression<C, ?> expression, final MethodHandle methodHandle) throws CompilerException {
		return new ExpressionExecutor<C> (
				expression,
				Collections.<ExecutableExpression<C, ?>>emptyList (),
				false,
				true,
				methodHandle.asType (MethodType.methodType (expression.getResultType (), contextClass)),
				false
			);
	}
	
	public Executor<C> compile (final ExecutableExpression<C, ?> rootExpression) throws CompilerException {
		final ExecutionPlan<C> plan = new ExecutionPlan<C> ();
		
		compileExpression (rootExpression, plan);
		
		final List<Class<?>> beanTypes = new ArrayList<> (beanClasses.size ());
		for (final Map.Entry<String, Class<?>> entry: beanClasses.entrySet ()) {
			beanTypes.add (entry.getValue ());
		}
		
		return new Executor<C> (contextClass, beanTypes, plan);
	}
	
	public <I> I compile (final ExecutableExpression<C, ?> rootExpression, final Class<I> iface) throws CompilerException {
		return compile (rootExpression).forInterface (iface);
	}
	
	private void compileExpression (final ExecutableExpression<C, ?> expression, final ExecutionPlan<C> plan) throws CompilerException {
		final ExpressionExecutor<C> existingExecutor = plan.getExecutor (expression);
		
		if (existingExecutor != null) {
			return;
		}
		
		// Create a new executor for this expression:
		final ExpressionExecutor<C> executor = expression.getExecutor (this);

		// Compile all children of this expression:
		for (final ExecutableExpression<C, ?> inputExpression: executor.inputs) {
			compileExpression (inputExpression, plan);
		}

		// Add this executor to the execution plan:
		plan.addExecutor (expression, executor);
		plan.addExecutionStep (executor);
	}

	
    @SuppressWarnings("unused")
	private static Object fold (final int n, final Object[] objects) {
        return objects[n];
    }

    @SuppressWarnings("unused")
	private static Map<?, ?> fold (final int[] indices, final String name, final Object[] objects) {
        for (final int i: indices) {
            final Map<?, ?> map = (Map<?, ?>)objects[i];
            if (map.containsKey (name)) {
                return map;
            }
        }

        return Collections.emptyMap ();
    }
}