package nl.ipo.cds.validation.callbacks;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import nl.ipo.cds.validation.AbstractExpression;
import nl.ipo.cds.validation.Expression;
import nl.ipo.cds.validation.ExpressionEvaluationException;
import nl.ipo.cds.validation.UnaryExpression;
import nl.ipo.cds.validation.ValidationMessage;
import nl.ipo.cds.validation.ValidatorContext;
import nl.ipo.cds.validation.execute.Compiler;
import nl.ipo.cds.validation.execute.CompilerException;
import nl.ipo.cds.validation.execute.ExpressionExecutor;

public class UnaryCallbackExpression<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>, ResultType, InputType> extends AbstractExpression<K, C, ResultType> implements  UnaryExpression<K, C, ResultType, InputType> {

	public final Class<ResultType> resultType;
	public final Expression<K, C, InputType> input;
	public final UnaryCallback<K, C, ResultType, InputType> callback;
	public final String label;
	
	public UnaryCallbackExpression (final Class<ResultType> resultType, final Expression<K, C, InputType> inputExpression, final UnaryCallback<K, C, ResultType, InputType> callback) {
		this (resultType, inputExpression, callback, null);
	}
	
	public UnaryCallbackExpression (final Class<ResultType> resultType, final Expression<K, C, InputType> inputExpression, final UnaryCallback<K, C, ResultType, InputType> callback, final String label) {
		if (resultType == null) {
			throw new NullPointerException ("resultType cannot be null");
		}
		if (inputExpression == null) {
			throw new NullPointerException ("inputExpression cannot be null");
		}
		if (callback == null) {
			throw new NullPointerException ("callback cannot be null");
		}
		
		this.resultType = resultType;
		this.input = inputExpression;
		this.callback = callback;
		this.label = label;
	}

	public UnaryCallbackExpression<K, C, ResultType, InputType> label (final String label) {
		return new UnaryCallbackExpression<K, C, ResultType, InputType> (resultType, input, callback, label);
	}

	@Override
	public Class<ResultType> getResultType () {
		return resultType;
	}

	public ResultType evaluate (final C context, final InputType input) {
		final InputType inputValue = input;
		
		try {
			return callback.call (inputValue, context);
		} catch (Exception e) {
			throw new ExpressionEvaluationException (e);
		}
	}

	@Override
	public Class<InputType> getInputType() {
		return input.getResultType ();
	}

	private final static MethodHandle evaluateHandle = Compiler
			.findMethod (
					UnaryCallbackExpression.class, 
					"evaluate", 
					MethodType.methodType (Object.class, ValidatorContext.class, Object.class)
				);
	
	@Override
	public ExpressionExecutor<C> getExecutor(final Compiler<C> compiler) throws CompilerException {
		return ExpressionExecutor.create (
				this, 
				input, 
				false, 
				false, 
				evaluateHandle.bindTo (this),
				false
			);
	}
}
