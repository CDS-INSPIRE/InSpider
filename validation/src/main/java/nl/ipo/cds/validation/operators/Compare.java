package nl.ipo.cds.validation.operators;

import nl.ipo.cds.validation.Expression;
import nl.ipo.cds.validation.ValidationMessage;
import nl.ipo.cds.validation.ValidatorContext;

public class Compare {

	public static class Equals<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>, T extends Comparable<T>> extends AbstractCompareOperator<K, C, T> {

		public Equals (final Expression<K, C, T> a, final Expression<K, C, T> b) {
			super(a, b);
		}


		@Override
		public Boolean evaluate (final T a, final T b, final C context) {
			return a.compareTo (b) == 0;
		}
		
		@Override
		public String getOperatorName () {
			return "==";
		}
	}
	
	public static class NotEquals<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>, T extends Comparable<T>> extends AbstractCompareOperator<K, C, T> {

		public NotEquals (final Expression<K, C, T> a, final Expression<K, C, T> b) {
			super(a, b);
		}


		@Override
		public Boolean evaluate (final T a, final T b, final C context) {
			return a.compareTo (b) != 0;
		}
		
		@Override
		public String getOperatorName () {
			return "!=";
		}
	}
	
	public static class LessThan<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>, T extends Comparable<T>> extends AbstractCompareOperator<K, C, T> {

		public LessThan (final Expression<K, C, T> a, final Expression<K, C, T> b) {
			super(a, b);
		}


		@Override
		public Boolean evaluate (final T a, final T b, final C context) {
			return a.compareTo (b) < 0;
		}
		
		@Override
		public String getOperatorName () {
			return "<";
		}
	}
	
	public static class GreaterThan<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>, T extends Comparable<T>> extends AbstractCompareOperator<K, C, T> {

		public GreaterThan (final Expression<K, C, T> a, final Expression<K, C, T> b) {
			super(a, b);
		}


		@Override
		public Boolean evaluate (final T a, final T b, final C context) {
			return a.compareTo (b) > 0;
		}
		
		@Override
		public String getOperatorName () {
			return ">";
		}
	}
	
	public static class LessThanEquals<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>, T extends Comparable<T>> extends AbstractCompareOperator<K, C, T> {

		public LessThanEquals (final Expression<K, C, T> a, final Expression<K, C, T> b) {
			super(a, b);
		}


		@Override
		public Boolean evaluate (final T a, final T b, final C context) {
			return a.compareTo (b) <= 0;
		}
		
		@Override
		public String getOperatorName () {
			return "<=";
		}
	}
	
	public static class GreaterThanEquals<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>, T extends Comparable<T>> extends AbstractCompareOperator<K, C, T> {

		public GreaterThanEquals (final Expression<K, C, T> a, final Expression<K, C, T> b) {
			super(a, b);
		}


		@Override
		public Boolean evaluate (final T a, final T b, final C context) {
			return a.compareTo (b) >= 0;
		}
		
		@Override
		public String getOperatorName () {
			return ">=";
		}
	}
}
