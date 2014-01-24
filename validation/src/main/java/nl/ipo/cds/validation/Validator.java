package nl.ipo.cds.validation;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.*;

import nl.ipo.cds.validation.execute.*;
import nl.ipo.cds.validation.execute.Compiler;

public class Validator<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>> extends AbstractExpression<K, C, Boolean> implements UnaryExpression<K, C, Boolean, Boolean>{

	public final Expression<K, C, Boolean> expression;
	public final K messageKey;
	public final List<Expression<K, C, ?>> parameters;
	public final boolean isBlocking;

    private final List<Executor<C>> parameterExecutors = new ArrayList<> ();
	
	public Validator (final Expression<K, C, Boolean> expression, final K messageKey, final List<Expression<K, C, ?>> parameters) {
		this (expression, messageKey, parameters, messageKey != null ? messageKey.isBlocking () : true);
	}
	
	public Validator (final Expression<K, C, Boolean> expression, final K messageKey, final List<Expression<K, C, ?>> parameters, final boolean isBlocking) {
		if (expression == null) {
			throw new NullPointerException ("expression cannot be null");
		}
		
		this.expression = expression;
		this.messageKey = messageKey;
		this.parameters = createParameters (messageKey, parameters);
		this.isBlocking = isBlocking;
	}
	
	private List<Expression<K, C, ?>> createParameters (final K messageKey, final List<Expression<K, C, ?>> explicitParameters) {
		final List<Expression<K, C, ?>> messageParameters = messageKey != null ? messageKey.getMessageParameters () : Collections.<Expression<K, C, ?>>emptyList ();
		final List<Expression<K, C, ?>> parameters = new ArrayList<> ();
		
		if (messageParameters != null) {
			parameters.addAll (messageParameters);
		}
		
		if (explicitParameters != null) {
			parameters.addAll (explicitParameters);
		}
		
		return parameters.size () == 0 ? null : parameters;
	}

	public Validator<K, C> message (final K messageKey) {
		return message (messageKey, (List<Expression<K, C, ?>>)null);
	}
	
	public Validator<K, C> message (final K messageKey, final Expression<K, C, ?> a) {
		final ArrayList<Expression<K, C, ?>> l = new ArrayList<Expression<K, C, ?>> (1);
		l.add (a);
		return message (messageKey, l);
	}
	
	public Validator<K, C> message (final K messageKey, final Expression<K, C, ?> a, final Expression<K, C, ?> b) {
		final ArrayList<Expression<K, C, ?>> l = new ArrayList<Expression<K, C, ?>> (2);
		l.add (a);
		l.add (b);
		return message (messageKey, l);
	}
	
	public Validator<K, C> message (final K messageKey, final Expression<K, C, ?> a, final Expression<K, C, ?> b, final Expression<K, C, ?> c) {
		final ArrayList<Expression<K, C, ?>> l = new ArrayList<Expression<K, C, ?>> (3);
		l.add (a);
		l.add (b);
		l.add (c);
		return message (messageKey, l);
	}
	
	public Validator<K, C> message (final K messageKey, final Expression<K, C, ?> a, final Expression<K, C, ?> b, final Expression<K, C, ?> c, final Expression<K, C, ?> d) {
		final ArrayList<Expression<K, C, ?>> l = new ArrayList<Expression<K, C, ?>> (4);
		l.add (a);
		l.add (b);
		l.add (c);
		l.add (d);
		return message (messageKey, l);
	}
	
	public Validator<K, C> message (final K messageKey, final List<Expression<K, C, ?>> parameters) {
		return new Validator<K, C> (expression, messageKey, parameters);
	}

	public Validator<K, C> nonBlocking () {
		final List<Expression<K, C, ?>> messageParams = (messageKey != null ? messageKey.getMessageParameters () : null);
		final int start = messageParams == null ? 0 : messageParams.size ();
		
		return new Validator<K, C> (expression, messageKey, parameters != null && start <= parameters.size () ? parameters.subList (start, parameters.size ()) : parameters, false);
	}
	
	@Override
	public Class<Boolean> getResultType () {
		return Boolean.class;
	}

	public Boolean evaluate (final Object[] objects, final C context, final Boolean input) throws ExecutorException {
		final Boolean result = input;

		if (result == null || !result) {
			reportValidationError (context, objects);
		}
		
		return isBlocking ? result : true;
	}

	@Override
	public Class<Boolean> getInputType() {
		return Boolean.class;
	}
	
	private void reportValidationError (final C context, final Object[] objects) throws ExecutorException {
		if (context.getReporter () == null || messageKey == null) {
			return;
		}
		
		// Collect parameters by evaluating parameter expressions (if any):
		final Object[] logParameters;
		if (parameters != null) {
			logParameters = new Object[parameters.size ()];
			for (int i = 0; i < parameters.size (); ++ i) {
				final Object value = parameterExecutors.get (i).execute (context, objects);
				logParameters[i] = value;
			}
		} else {
			logParameters = new Object[0];
		}

		// Invoke the reporter:
		context.getReporter ().reportValidationError (this, context, messageKey, logParameters);
	}
	
	@Override
	public String toString () {
		return String.format ("validate(%s)", expression.toString ());
	}

	private final static MethodHandle evaluateHandle = Compiler
			.findMethod (
					Validator.class, 
					"evaluate", 
					MethodType.methodType (Boolean.class, Object[].class, ValidatorContext.class, Boolean.class)
				);
	
	@Override
	public ExpressionExecutor<C> getExecutor(final Compiler<C> compiler) throws CompilerException {
        parameterExecutors.clear ();
        if (parameters != null) {
	        for (final Expression<K, C, ?> parameterExpression: parameters) {
	            parameterExecutors.add (compiler.compile (parameterExpression));
	        }
        }

		return ExpressionExecutor.create (
				this, 
				expression, 
				false, 
				true, 
				evaluateHandle.bindTo (this),
				false
			);
	}
}
