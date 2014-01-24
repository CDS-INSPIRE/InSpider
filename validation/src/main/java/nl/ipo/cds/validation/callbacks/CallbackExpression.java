package nl.ipo.cds.validation.callbacks;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import nl.ipo.cds.validation.AbstractExpression;
import nl.ipo.cds.validation.ExpressionEvaluationException;
import nl.ipo.cds.validation.ValidationMessage;
import nl.ipo.cds.validation.ValidatorContext;
import nl.ipo.cds.validation.execute.Compiler;
import nl.ipo.cds.validation.execute.CompilerException;
import nl.ipo.cds.validation.execute.ExpressionExecutor;

public class CallbackExpression<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>, T> extends AbstractExpression<K, C, T> {
	public final Class<T> type;
	public final Callback<K, C, T> callback;
	public final String label;
	
	public CallbackExpression (final Class<T> type, final Callback<K, C, T> callback) {
		this (type, callback, null);
	}
	
	public CallbackExpression (final Class<T> type, final Callback<K, C, T> callback, final String label) {
		if (type == null) {
			throw new NullPointerException ("type cannot be null");
		}
		if (callback == null) {
			throw new NullPointerException ("callback cannot be null");
		}
		
		this.type = type;
		this.callback = callback;
		this.label = label;
	}

	public CallbackExpression<K, C, T> label (final String label) {
		return new CallbackExpression<K, C, T> (type, callback, label);
	}

	@Override
	public Class<T> getResultType () {
		return type;
	}

	public T evaluate (final C context) {
		try {
			return callback.call (context);
		} catch (Exception e) {
			throw new ExpressionEvaluationException (e);
		}
	}

	private final static MethodHandle evaluateHandle = Compiler
			.findMethod (
					Callback.class, 
					"call", 
					MethodType.methodType (Object.class, ValidatorContext.class)
				);
	
	@Override
	public ExpressionExecutor<C> getExecutor(final Compiler<C> compiler) throws CompilerException {
		return ExpressionExecutor.create (
				this, 
				false, 
				false, 
				evaluateHandle.bindTo (callback),
				false
			);
	}
}
