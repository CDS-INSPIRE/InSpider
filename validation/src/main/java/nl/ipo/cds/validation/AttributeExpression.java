package nl.ipo.cds.validation;

import java.lang.invoke.MethodType;

import org.deegree.commons.tom.ows.CodeType;

import nl.ipo.cds.validation.execute.Compiler;
import nl.ipo.cds.validation.execute.CompilerException;
import nl.ipo.cds.validation.execute.ExpressionExecutor;
import nl.ipo.cds.validation.gml.CodeExpression;
import nl.ipo.cds.validation.gml.CodeExpression.GetCodeSpaceExpression;

public class AttributeExpression<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>, T> extends AbstractExpression<K, C, T> {
	public final String name;
	public final Class<T> type;
	public final String label;

	public AttributeExpression (final String name, final Class<T> type) {
		this (name, type, null);
	}

	public AttributeExpression (final String name, final Class<T> type, final String label) {
		if (name == null) {
			throw new NullPointerException ("name cannot be null");
		}
		if (type == null) {
			throw new NullPointerException ("type cannot be null");
		}

		this.name = name;
		this.type = type;
		this.label = label;
	}

	public AttributeExpression<K, C, T> label (final String label) {
		return new AttributeExpression<K, C, T> (name, type, label);
	}

	@Override
	public Class<T> getResultType () {
		return type;
	}

	public GetValueExpression value() {
		return new GetValueExpression();
	}

	@Override
	public String toString () {
		return name;
	}

	public Expression<K, C, Boolean> is (final Class<? extends T> compareType) {
		return new AbstractUnaryTestExpression<K, C, T> (this, "Is") {
			@Override
			public boolean test(T value, C context) {
				if (value == null) {
					return false;
				}

				return compareType.isAssignableFrom (value.getClass ());
			}

			@Override
			public boolean equals (final Object o) {
				return o == this;
			}
		};
	}

	public <NewT> Expression<K, C, NewT> as (final Class<NewT> cls) {
		assert cls != null : "cls cannot be null";
		assert cls.isAssignableFrom (getResultType ()) : String.format ("%s must be a superclass of %s", cls, getResultType ());

		return new AbstractExpression<K, C, NewT>() {
			@Override
			public Class<NewT> getResultType () {
				return cls;
			}

			@Override
			public ExpressionExecutor<C> getExecutor (final Compiler<C> compiler) throws CompilerException {
				final ExpressionExecutor<C> executor = AttributeExpression.this.getExecutor (compiler);

				return ExpressionExecutor.create (
						this,
						executor.isConstant,
						executor.isDeterministic,
						executor.methodHandle.asType (
							executor.methodHandle.type ().changeReturnType (cls)
						),
						false
					);
			}
		};

	}

	public Expression<K, C, Boolean> isNull () {
		return new AbstractUnaryTestExpression<K, C, T> (this, "IsNull") {
			@Override
			public boolean test (final T value, final C context) {
				return value == null;
			}
		};
	}

	@Override
	public ExpressionExecutor<C> getExecutor (final Compiler<C> compiler) throws CompilerException {
		return compiler.createGetAttributeExecutor (name, this);
	}

	@Override
	public boolean equals (final Object o) {
		if (o == null) {
			return false;
		}

		if (!(o instanceof AttributeExpression)) {
			return false;
		}

		final AttributeExpression<?, ?, ?> other = (AttributeExpression<?, ?, ?>)o;

		return name.equals (other.name) && type.equals (other.type);
	}

	@Override
	public int hashCode () {
		return name.hashCode () ^ type.hashCode ();
	}

	public class GetValueExpression extends AbstractExpression<K, C, T> {

		@Override
		public Class<T> getResultType () {
			return type;
		}

		public T evaluate (final C context, final T input) {
			return input;
		}

		@Override
		public ExpressionExecutor<C> getExecutor (final Compiler<C> compiler) throws CompilerException {
			return ExpressionExecutor.create (
					this,
					AttributeExpression.this,
					false,
					true,
					Compiler.findMethod (GetValueExpression.class, "evaluate", MethodType.methodType (type, ValidatorContext.class, type)).bindTo (this),
					false
				);
		}

		public Expression<K, C, Boolean> isNull () {
			return new AbstractUnaryTestExpression<K, C, T> (this, "IsNull") {
				@Override
				public boolean test (final T value, final C context) {
					return value == null;
				}
			};
		}
	}
}
