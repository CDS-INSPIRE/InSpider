package nl.ipo.cds.validation.callbacks;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import nl.ipo.cds.validation.AbstractExpression;
import nl.ipo.cds.validation.BinaryExpression;
import nl.ipo.cds.validation.Expression;
import nl.ipo.cds.validation.ExpressionEvaluationException;
import nl.ipo.cds.validation.ValidationMessage;
import nl.ipo.cds.validation.ValidatorContext;
import nl.ipo.cds.validation.execute.Compiler;
import nl.ipo.cds.validation.execute.CompilerException;
import nl.ipo.cds.validation.execute.ExpressionExecutor;

public class BinaryCallbackExpression<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>, ResultType, TypeA, TypeB> extends AbstractExpression<K, C, ResultType> implements BinaryExpression<K, C, ResultType, TypeA, TypeB> {

	public final Class<ResultType> resultType;
	public final Expression<K, C, TypeA> a;
	public final Expression<K, C, TypeB> b;
	public final BinaryCallback<K, C, ResultType, TypeA, TypeB> callback;
	public final String label;
	
	public BinaryCallbackExpression (final Class<ResultType> resultType, final Expression<K, C, TypeA> a, final Expression<K, C, TypeB> b, final BinaryCallback<K, C, ResultType, TypeA, TypeB> callback) {
		this (resultType, a, b, callback, null);
	}
	
	public BinaryCallbackExpression (final Class<ResultType> resultType, final Expression<K, C, TypeA> a, final Expression<K, C, TypeB> b, final BinaryCallback<K, C, ResultType, TypeA, TypeB> callback, final String label) {
		if (resultType == null) {
			throw new NullPointerException ("resultType cannot be null");
		}
		if (a == null || b == null) {
			throw new NullPointerException ("inputs cannot be null");
		}
		if (callback == null) {
			throw new NullPointerException ("callback cannot be null");
		}
		
		this.resultType = resultType;
		this.a = a;
		this.b = b;
		this.callback = callback;
		this.label = label;
	}
	
	public ResultType evaluate (final C context, final TypeA a, final TypeB b) {
		try {
			return callback.call (a, b, context);
		} catch (Exception e) {
			throw new ExpressionEvaluationException (e);
		}
	}
	
	@Override
	public Class<ResultType> getResultType () {
		return resultType;
	}

	private final static MethodHandle evaluateHandle = Compiler
			.findMethod (
					BinaryCallbackExpression.class, 
					"evaluate", 
					MethodType.methodType (Object.class, ValidatorContext.class, Object.class, Object.class)
				);
	
	@Override
	public ExpressionExecutor<C> getExecutor (final Compiler<C> compiler) throws CompilerException {
		return ExpressionExecutor.create (
				this, 
				a,
				b,
				false, 
				false, 
				evaluateHandle.bindTo (this),
				false
			);
	}

	@Override
	public Class<TypeA> getTypeA () {
		return a.getResultType ();
	}

	@Override
	public Class<TypeB> getTypeB () {
		return b.getResultType ();
	}
}
