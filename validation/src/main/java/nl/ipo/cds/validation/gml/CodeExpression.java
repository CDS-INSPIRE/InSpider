package nl.ipo.cds.validation.gml;

import java.lang.invoke.MethodType;

import nl.ipo.cds.validation.AbstractBinaryTestExpression;
import nl.ipo.cds.validation.AbstractExpression;
import nl.ipo.cds.validation.AbstractUnaryTestExpression;
import nl.ipo.cds.validation.AttributeExpression;
import nl.ipo.cds.validation.Expression;
import nl.ipo.cds.validation.ValidationMessage;
import nl.ipo.cds.validation.ValidatorContext;
import nl.ipo.cds.validation.execute.Compiler;
import nl.ipo.cds.validation.execute.CompilerException;
import nl.ipo.cds.validation.execute.ExpressionExecutor;
import nl.ipo.cds.validation.gml.codelists.CodeList;
import nl.ipo.cds.validation.gml.codelists.CodeListException;

import org.deegree.commons.tom.ows.CodeType;

public class CodeExpression<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>> extends AttributeExpression<K, C, CodeType> {

	public CodeExpression (final String name) {
		super (name, CodeType.class);
	}
	
	public CodeExpression (final String name, final String label) {
		super (name, CodeType.class, label);
	}
	
	public CodeExpression<K, C> label (final String label) {
		return new CodeExpression<> (name, label);
	}
	
	public Expression<K, C, Boolean> hasCodeSpace (final Expression<K, C, String> codeSpace) {
		return new AbstractBinaryTestExpression<K, C, CodeType, String> (this, codeSpace, "HasCodeSpace") {
			@Override
			public boolean test (final CodeType a, final String b, final C context) {
				if (a == null) {
					return false;
				}
				
				final String codeSpace = a.getCodeSpace ();
				
				if (codeSpace == null) {
					return b == null;
				}
				
				return codeSpace.equals (b);
			}
		};
	}
	
	public Expression<K, C, Boolean> isValid () {
		return new AbstractUnaryTestExpression<K, C, CodeType> (this, "CodeTypeIsValid") {
			@Override
			public boolean test (final CodeType value, final C context) {
				try {
					final String codeSpace = value.getCodeSpace ();
					if (codeSpace == null) {
						return false;
					}
					
					final CodeList codeList = context.getCodeListFactory ().getCodeList (codeSpace);
					if (codeList == null) {
						return false;
					}
					
					return codeList.hasCode (value.getCode ());
				} catch (CodeListException e) {
					return false;
				}
			}
		};
	}
	
	public class GetCodeExpression extends AbstractExpression<K, C, String> {

		@Override
		public Class<String> getResultType () {
			return String.class;
		}

		public String evaluate (final C context, final CodeType input) {
			if (input == null) {
				return null;
			}
			
			return input.getCode ();
		}
		
		@Override
		public ExpressionExecutor<C> getExecutor (final Compiler<C> compiler) throws CompilerException {
			return ExpressionExecutor.create (
					this,
					CodeExpression.this,
					false, 
					true, 
					Compiler.findMethod (GetCodeExpression.class, "evaluate", MethodType.methodType (String.class, ValidatorContext.class, CodeType.class)).bindTo (this), 
					false
				);
		}
		
		public Expression<K, C, Boolean> isNull () {
			return new AbstractUnaryTestExpression<K, C, String> (this, "IsNull") {
				@Override
				public boolean test (final String value, final C context) {
					return value == null;
				}
			};
		}
	}
	
	public GetCodeExpression code () {
		return new GetCodeExpression ();
	}

	public class GetCodeSpaceExpression extends AbstractExpression<K, C, String> {

		@Override
		public Class<String> getResultType () {
			return String.class;
		}

		public String evaluate (final C context, final CodeType input) {
			if (input == null) {
				return null;
			}			
			return input.getCodeSpace();
		}
		
		@Override
		public ExpressionExecutor<C> getExecutor (final Compiler<C> compiler) throws CompilerException {
			return ExpressionExecutor.create (
					this,
					CodeExpression.this,
					false, 
					true, 
					Compiler.findMethod (GetCodeSpaceExpression.class, "evaluate", MethodType.methodType (String.class, ValidatorContext.class, CodeType.class)).bindTo (this), 
					false
				);
		}
		
		public Expression<K, C, Boolean> isNull () {
			return new AbstractUnaryTestExpression<K, C, String> (this, "IsNull") {
				@Override
				public boolean test (final String value, final C context) {
					return value == null;
				}
			};
		}
	}
	
	public GetCodeSpaceExpression codeSpace() {
		return new GetCodeSpaceExpression();
	}
}
