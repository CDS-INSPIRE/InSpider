package nl.ipo.cds.validation;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Set;

import nl.ipo.cds.validation.callbacks.BinaryCallback;
import nl.ipo.cds.validation.callbacks.BinaryCallbackExpression;
import nl.ipo.cds.validation.callbacks.Callback;
import nl.ipo.cds.validation.callbacks.CallbackExpression;
import nl.ipo.cds.validation.callbacks.UnaryCallback;
import nl.ipo.cds.validation.callbacks.UnaryCallbackExpression;
import nl.ipo.cds.validation.constants.Constant;
import nl.ipo.cds.validation.flow.ForEachExpression;
import nl.ipo.cds.validation.flow.IfExpression;
import nl.ipo.cds.validation.flow.SplitExpression;
import nl.ipo.cds.validation.geometry.GeometryExpression;
import nl.ipo.cds.validation.gml.CodeExpression;
import nl.ipo.cds.validation.gml.ValidateCodeSpaceExpression;
import nl.ipo.cds.validation.logical.AndExpression;
import nl.ipo.cds.validation.logical.NotExpression;
import nl.ipo.cds.validation.logical.OrExpression;
import nl.ipo.cds.validation.operators.Compare;
import nl.ipo.cds.validation.operators.In;
import nl.ipo.cds.validation.string.Strings;

import org.deegree.geometry.Geometry;

public class Validation<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>> {

	// =========================================================================
	// Strings:
	// =========================================================================
	public Strings.IsBlank<K, C> isBlank (final Expression<K, C, String> input) {
		return new Strings.IsBlank<K, C> (input);
	}
	
	public Strings.IsUrl<K, C> isUrl (final Expression<K, C, String> input) {
		return new Strings.IsUrl<K, C> (input);
	}
	
	public Strings.IsUUID<K, C> isUUID (final Expression<K, C, String> input) {
		return new Strings.IsUUID<K, C> (input);
	}
	
	public Strings.Join<K, C> join (final Expression<K, C, String[]> input, final Expression<K, C, String> separator) {
		return new Strings.Join<K, C> (input, separator);
	}
	
	public Strings.Length<K, C> length (final Expression<K, C, String> input) {
		return new Strings.Length<K, C> (input);
	}
	
	public Strings.Matches<K, C> matches (final Expression<K, C, String> input, final Expression<K, C, String> pattern) {
		return new Strings.Matches<> (input, pattern);
	}
	
	// =========================================================================
	// GML:
	// =========================================================================
	public GeometryExpression<K, C, Geometry> geometry (final String name) {
		return new GeometryExpression<K, C, Geometry> (name, Geometry.class);
	}
	
	public <T extends Geometry> GeometryExpression<K, C, T> geometry (final String name, final Class<T> type) {
		return new GeometryExpression<K, C, T> (name, type);
	}
	
	public LastLocationExpression<K, C> lastLocation () {
		return new LastLocationExpression<K, C> ();
	}
	
	public CodeExpression<K, C> code (final String name) {
		return new CodeExpression<> (name);
	}
	
	public ValidateCodeSpaceExpression<K, C> codeSpace (final Expression<K, C, String> codeSpace) {
		return new ValidateCodeSpaceExpression<> (codeSpace);
	}
	
	// =========================================================================
	// Operators:
	// =========================================================================
	public <T extends Comparable<T>> Compare.Equals<K, C, T> eq (final Expression<K, C, T> a, final Expression<K, C, T> b) {
		return new Compare.Equals<K, C, T> (a, b);
	}
	
	public <T extends Comparable<T>> Compare.NotEquals<K, C, T> ne (final Expression<K, C, T> a, final Expression<K, C, T> b) {
		return new Compare.NotEquals<K, C, T> (a, b);
	}
	
	public <T extends Comparable<T>> Compare.LessThan<K, C, T> lt (final Expression<K, C, T> a, final Expression<K, C, T> b) {
		return new Compare.LessThan<K, C, T> (a, b);
	}
	
	public <T extends Comparable<T>> Compare.GreaterThan<K, C, T> gt (final Expression<K, C, T> a, final Expression<K, C, T> b) {
		return new Compare.GreaterThan<K, C, T> (a, b);
	}
	
	public <T extends Comparable<T>> Compare.LessThanEquals<K, C, T> lte (final Expression<K, C, T> a, final Expression<K, C, T> b) {
		return new Compare.LessThanEquals<K, C, T> (a, b);
	}
	
	public <T extends Comparable<T>> Compare.GreaterThanEquals<K, C, T> gte (final Expression<K, C, T> a, final Expression<K, C, T> b) {
		return new Compare.GreaterThanEquals<K, C, T> (a, b);
	}
	
	public <T> In<K, C, T> in (final Expression<K, C, T> a, final Expression<K, C, Set<T>> b) {
		return new In<K, C, T> (a, b);
	}
	
	// =========================================================================
	// Callbacks:
	// =========================================================================
	public <T> CallbackExpression<K, C, T> callback (final Class<T> type, final Callback<K, C, T> callback) {
		return new CallbackExpression<K, C, T> (type, callback);
	}
	
	public <R, I> UnaryCallbackExpression<K, C, R, I> callback (final Class<R> type, final Expression<K, C, I> input, final UnaryCallback<K, C, R, I> callback) {
		return new UnaryCallbackExpression<K, C, R, I> (type, input, callback);
	}
	
	public <R, A, B> BinaryCallbackExpression<K, C, R, A, B> callback (final Class<R> type, final Expression<K, C, A> a, final Expression<K, C, B> b, final BinaryCallback<K, C, R, A, B> callback) {
		return new BinaryCallbackExpression<> (type, a, b, callback);
	}
	
	// =========================================================================
	// Constants:
	// =========================================================================
	public Constant<K, C, Byte> constant (final byte v) {
		return new Constant<K, C, Byte> (v, Byte.class);
	}
	
	public Constant<K, C, Character> constant (final char v) {
		return new Constant<K, C, Character> (v, Character.class);
	}
	
	public Constant<K, C, Short> constant (final short v) {
		return new Constant<K, C, Short> (v, Short.class);
	}
	
	public Constant<K, C, Integer> constant (final int v) {
		return new Constant<K, C, Integer> (v, Integer.class);
	}
	
	public Constant<K, C, Long> constant (final long v) {
		return new Constant<K, C, Long> (v, Long.class);
	}
	
	public Constant<K, C, Float> constant (final float v) {
		return new Constant<K, C, Float> (v, Float.class);
	}
	
	public Constant<K, C, Double> constant (final double v) {
		return new Constant<K, C, Double> (v, Double.class);
	}
	
	public Constant<K, C, Boolean> constant (final boolean v) {
		return new Constant<K, C, Boolean> (v, Boolean.class);
	}
	
	public Constant<K, C, String> constant (final String v) {
		return new Constant<K, C, String> (v, String.class);
	}
	
	@SuppressWarnings("unchecked")
	public <T> Constant<K, C, Set<T>> constant (final Set<T> v) {
		return new Constant<K, C, Set<T>> (v, (Class<Set<T>>)v.getClass ());
	}
	
	// =========================================================================
	// Control-flow expressions:
	// =========================================================================
	public <T> IfExpression<K, C, T> ifExp (final Expression<K, C, Boolean> condition, final Expression<K, C, T> a, final Expression<K, C, T> b) {
		return new IfExpression<K, C, T> (condition, a, b);
	}

	public <T> ForEachExpression<K, C, T> forEach (final String variableName, final Expression<K, C, T[]> input, final Validator<K, C> validator) {
		return new ForEachExpression<K, C, T> (variableName, input, validator);
	}
	
	public SplitExpression<K, C> split (final Expression<K, C, String> input, final Expression<K, C, String> splitter, final Validator<K, C> validator) {
		return new SplitExpression<K, C> (input, splitter, validator);
	}
	
	// =========================================================================
	// Logical operators:
	// =========================================================================
	public AndExpression<K, C> and (final Expression<K, C, Boolean> a, final Expression<K, C, Boolean> b) {
		final ArrayList<Expression<K, C, Boolean>> l = new ArrayList<Expression<K, C, Boolean>> (2);
		l.add (a);
		l.add (b);
		return new AndExpression<K, C> (l);
	}
	
	public AndExpression<K, C> and (final Expression<K, C, Boolean> a, final Expression<K, C, Boolean> b, final Expression<K, C, Boolean> c) {
		final ArrayList<Expression<K, C, Boolean>> l = new ArrayList<Expression<K, C, Boolean>> (3);
		l.add (a);
		l.add (b);
		l.add (c);
		return new AndExpression<K, C> (l);
	}
	
	public AndExpression<K, C> and (final Expression<K, C, Boolean> a, final Expression<K, C, Boolean> b, final Expression<K, C, Boolean> c, final Expression<K, C, Boolean> d) {
		final ArrayList<Expression<K, C, Boolean>> l = new ArrayList<Expression<K, C, Boolean>> (4);
		l.add (a);
		l.add (b);
		l.add (c);
		l.add (d);
		return new AndExpression<K, C> (l);
	}
	
	public AndExpression<K, C> and (final Expression<K, C, Boolean> a, final Expression<K, C, Boolean> b, final Expression<K, C, Boolean> c, final Expression<K, C, Boolean> d, final Expression<K, C, Boolean> e) {
		final ArrayList<Expression<K, C, Boolean>> l = new ArrayList<Expression<K, C, Boolean>> (5);
		l.add (a);
		l.add (b);
		l.add (c);
		l.add (d);
		l.add (e);
		return new AndExpression<K, C> (l);
	}
	
	public AndExpression<K, C> and (final Expression<K, C, Boolean> a, final Expression<K, C, Boolean> b, final Expression<K, C, Boolean> c, final Expression<K, C, Boolean> d, final Expression<K, C, Boolean> e, final Expression<K, C, Boolean> f) {
		final ArrayList<Expression<K, C, Boolean>> l = new ArrayList<Expression<K, C, Boolean>> (6);
		l.add (a);
		l.add (b);
		l.add (c);
		l.add (d);
		l.add (e);
		l.add (f);
		return new AndExpression<K, C> (l);
	}
	
	public AndExpression<K, C> and (final Expression<K, C, Boolean> a, final Expression<K, C, Boolean> b, final Expression<K, C, Boolean> c, final Expression<K, C, Boolean> d, final Expression<K, C, Boolean> e, final Expression<K, C, Boolean> f, final Expression<K, C, Boolean> g) {
		final ArrayList<Expression<K, C, Boolean>> l = new ArrayList<Expression<K, C, Boolean>> (7);
		l.add (a);
		l.add (b);
		l.add (c);
		l.add (d);
		l.add (e);
		l.add (f);
		l.add (g);
		return new AndExpression<K, C> (l);
	}
	
	public AndExpression<K, C> and (final Expression<K, C, Boolean> a, final Expression<K, C, Boolean> b, final Expression<K, C, Boolean> c, final Expression<K, C, Boolean> d, final Expression<K, C, Boolean> e, final Expression<K, C, Boolean> f, final Expression<K, C, Boolean> g, final Expression<K, C, Boolean> h) {
		final ArrayList<Expression<K, C, Boolean>> l = new ArrayList<Expression<K, C, Boolean>> (8);
		l.add (a);
		l.add (b);
		l.add (c);
		l.add (d);
		l.add (e);
		l.add (f);
		l.add (g);
		l.add (h);
		return new AndExpression<K, C> (l);
	}
	
	public AndExpression<K, C> and (final Expression<K, C, Boolean> a, final Expression<K, C, Boolean> b, final Expression<K, C, Boolean> c, final Expression<K, C, Boolean> d, final Expression<K, C, Boolean> e, final Expression<K, C, Boolean> f, final Expression<K, C, Boolean> g, final Expression<K, C, Boolean> h, final Expression<K, C, Boolean> i) {
		final ArrayList<Expression<K, C, Boolean>> l = new ArrayList<Expression<K, C, Boolean>> (9);
		l.add (a);
		l.add (b);
		l.add (c);
		l.add (d);
		l.add (e);
		l.add (f);
		l.add (g);
		l.add (h);
		l.add (i);
		return new AndExpression<K, C> (l);
	}
	
	public AndExpression<K, C> and (final Expression<K, C, Boolean> a, final Expression<K, C, Boolean> b, final Expression<K, C, Boolean> c, final Expression<K, C, Boolean> d, final Expression<K, C, Boolean> e, final Expression<K, C, Boolean> f, final Expression<K, C, Boolean> g, final Expression<K, C, Boolean> h, final Expression<K, C, Boolean> i, final Expression<K, C, Boolean> j) {
		final ArrayList<Expression<K, C, Boolean>> l = new ArrayList<Expression<K, C, Boolean>> (10);
		l.add (a);
		l.add (b);
		l.add (c);
		l.add (d);
		l.add (e);
		l.add (f);
		l.add (g);
		l.add (h);
		l.add (i);
		l.add (j);
		return new AndExpression<K, C> (l);
	}
	
	public OrExpression<K, C> or (final Expression<K, C, Boolean> a, final Expression<K, C, Boolean> b) {
		final ArrayList<Expression<K, C, Boolean>> l = new ArrayList<Expression<K, C, Boolean>> (2);
		l.add (a);
		l.add (b);
		return new OrExpression<K, C> (l);
	}
	
	public OrExpression<K, C> or (final Expression<K, C, Boolean> a, final Expression<K, C, Boolean> b, final Expression<K, C, Boolean> c) {
		final ArrayList<Expression<K, C, Boolean>> l = new ArrayList<Expression<K, C, Boolean>> (3);
		l.add (a);
		l.add (b);
		l.add (c);
		return new OrExpression<K, C> (l);
	}
	
	public OrExpression<K, C> or (final Expression<K, C, Boolean> a, final Expression<K, C, Boolean> b, final Expression<K, C, Boolean> c, final Expression<K, C, Boolean> d) {
		final ArrayList<Expression<K, C, Boolean>> l = new ArrayList<Expression<K, C, Boolean>> (3);
		l.add (a);
		l.add (b);
		l.add (c);
		l.add (d);
		return new OrExpression<K, C> (l);
	}
	
	public NotExpression<K, C> not (final Expression<K, C, Boolean> input) {
		return new NotExpression<K, C> (input);
	}
	
	// =========================================================================
	// Attributes:
	// =========================================================================
	public <T> AttributeExpression<K, C, T> attribute (final String name, final Class<T> type, final String label) {
		return new AttributeExpression<K, C, T> (name, type, label);
	}
	
	public <T> AttributeExpression<K, C, T> attribute (final String name, final Class<T> type) {
		return new AttributeExpression<K, C, T> (name, type);
	}
	
	public AttributeExpression<K, C, Byte> byteAttr (final String name) {
		return new AttributeExpression<K, C, Byte> (name, Byte.class);
	}
	
	public AttributeExpression<K, C, Character> charAttr (final String name) {
		return new AttributeExpression<K, C, Character> (name, Character.class);
	}
	
	public AttributeExpression<K, C, Short> shortAttr (final String name) {
		return new AttributeExpression<K, C, Short> (name, Short.class);
	}
	
	public AttributeExpression<K, C, Integer> intAttr (final String name) {
		return new AttributeExpression<K, C, Integer> (name, Integer.class);
	}
	
	public AttributeExpression<K, C, Long> longAttr (final String name) {
		return new AttributeExpression<K, C, Long> (name, Long.class);
	}

	public AttributeExpression<K, C, BigInteger> bigIntegerAttr (final String name) {
		return new AttributeExpression<K, C, BigInteger> (name, BigInteger.class);
	}
	
	public AttributeExpression<K, C, Float> floatAttr (final String name) {
		return new AttributeExpression<K, C, Float> (name, Float.class);
	}
	
	public AttributeExpression<K, C, Double> doubleAttr (final String name) {
		return new AttributeExpression<K, C, Double> (name, Double.class);
	}
	
	public AttributeExpression<K, C, Boolean> booleanAttr (final String name) {
		return new AttributeExpression<K, C, Boolean> (name, Boolean.class);
	}
	
	public AttributeExpression<K, C, String> stringAttr (final String name) {
		return new AttributeExpression<K, C, String> (name, String.class);
	}
	
	public AttributeExpression<K, C, Timestamp> timestampAttr (final String name) {
		return new AttributeExpression<K, C, Timestamp> (name, Timestamp.class);
	}
	
	public <T> AttributeExpression<K, C, T> attr (final String name, final Class<T> type) {
		return new AttributeExpression<K, C, T> (name, type);
	}

	// =========================================================================
	// Constructing a validator:
	// =========================================================================
	public Validator<K, C> validate (final Expression<K, C, Boolean> input) {
		return new Validator<K, C> (input, null, null);
	}
}
