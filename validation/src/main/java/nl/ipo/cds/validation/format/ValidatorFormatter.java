package nl.ipo.cds.validation.format;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.ipo.cds.validation.Expression;
import nl.ipo.cds.validation.ValidationMessage;
import nl.ipo.cds.validation.Validator;
import nl.ipo.cds.validation.ValidatorContext;

public abstract class ValidatorFormatter<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>> {

	private final Map<Class<?>, Method> methods = new HashMap<Class<?>, Method> ();
	public final Validator<K, C> validator;
	
	public ValidatorFormatter (final Validator<K, C> validator) {
		this.validator = validator;
		
		this.reflect ();
	}

	public String format () {
		return formatExpression (validator);
	}
	
	public String formatExpression (final Expression<K, C, ?> expression) {
		final List<Class<?>> classes = new ArrayList<Class<?>> (1);
		classes.add (expression.getClass ());
		
		try {
			return invokeFormatter (expression, classes);
		} catch (IllegalAccessException e) {
			throw new RuntimeException (e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException (e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException (e);
		}
	}
	
	public String format (final Expression<K, C, ?> expression) {
		return "";
	}
	
	private String invokeFormatter (final Expression<K, C, ?> exp, final List<Class<?>> classes) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		if (classes.size () == 0) {
			return format (exp);
		}
		
		final List<Class<?>> newClasses = new ArrayList<Class<?>> ();
		for (final Class<?> cls: classes) {
			if (methods.containsKey (cls)) {
				return (String)methods.get (cls).invoke (this, exp);
			}
			
			final Class<?> superClass = cls.getSuperclass ();
			if (superClass != null) {
				newClasses.add (superClass);
			}
			
			for (final Class<?> iface: cls.getInterfaces ()) {
				newClasses.add (iface);
			}
		}

		return invokeFormatter (exp, newClasses);
	}
	
	private void reflect () {
		for (final Method method: getClass ().getDeclaredMethods ()) {
			if (!method.getReturnType ().equals (String.class)) {
				continue;
			}
			
			final Class<?>[] parameters = method.getParameterTypes ();
			if (parameters.length != 1) {
				continue;
			}
			
			if (!Expression.class.isAssignableFrom (parameters[0])) {
				continue;
			}
			
			methods.put (parameters[0], method);
		}
	}
}
