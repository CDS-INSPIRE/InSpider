package nl.ipo.cds.validation.geometry;

import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import nl.ipo.cds.validation.AbstractBinaryTestExpression;
import nl.ipo.cds.validation.AbstractExpression;
import nl.ipo.cds.validation.AbstractUnaryTestExpression;
import nl.ipo.cds.validation.AttributeExpression;
import nl.ipo.cds.validation.Expression;
import nl.ipo.cds.validation.ExpressionEvaluationException;
import nl.ipo.cds.validation.GeometryValidationStatus;
import nl.ipo.cds.validation.ValidationMessage;
import nl.ipo.cds.validation.ValidatorContext;
import nl.ipo.cds.validation.execute.Compiler;
import nl.ipo.cds.validation.execute.CompilerException;
import nl.ipo.cds.validation.execute.ExpressionExecutor;

import org.deegree.geometry.Geometry;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.validation.GeometryValidator;

import com.vividsolutions.jts.algorithm.RobustLineIntersector;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geomgraph.GeometryGraph;
import com.vividsolutions.jts.operation.valid.ConnectedInteriorTester;

public class GeometryExpression<K extends Enum<K> & ValidationMessage<K, C>, C extends ValidatorContext<K, C>, T extends Geometry>
		extends AttributeExpression<K, C, T> {

	public GeometryExpression(final String name, final Class<T> type) {
		super(name, type);
	}

	public GeometryExpression(final String name, final Class<T> type, final String label) {
		super(name, type, label);
	}

	@Override
	public GeometryExpression<K, C, T> label(final String label) {
		return new GeometryExpression<K, C, T>(name, type, label);
	}

	public Expression<K, C, Boolean> isEmptyMultiGeometry() {
		return new AbstractUnaryTestExpression<K, C, T>(this, "IsEmptyMultiGeometry") {
			@Override
			public boolean test(final T value, final C context) {
				return value instanceof MultiGeometry<?> && ((MultiGeometry<?>) value).size() == 0;
			}
		};
	}

	public Expression<K, C, Boolean> isPoint() {
		return new AbstractUnaryTestExpression<K, C, T>(this, "IsPoint") {
			@Override
			public boolean test(final T value, final C context) {
				return value instanceof Point;
			}
		};
	}
	
	public Expression<K, C, Boolean> isPointOrMultiPoint() {
		return new AbstractUnaryTestExpression<K, C, T>(this, "IsPointOrMultiPoint") {
			@Override
			public boolean test(final T value, final C context) {
				if (value instanceof Point) {
					return true;
				}
				if (value instanceof MultiGeometry<?>) {
					for (Geometry member : ((MultiGeometry<?>) value)) {
						if (!(member instanceof Point)) {
							return false;
						}
					}
					return true;
				}
				return false;
			}
		};
	}

	public Expression<K, C, Boolean> isCurveOrMultiCurve() {
		return new AbstractUnaryTestExpression<K, C, T>(this, "IsCurveOrMultiCurve") {
			@Override
			public boolean test(final T value, final C context) {
				if (value instanceof Curve) {
					return true;
				}
				if (value instanceof MultiGeometry<?>) {
					for (Geometry member : ((MultiGeometry<?>) value)) {
						if (!(member instanceof Curve)) {
							return false;
						}
					}
					return true;
				}
				return false;
			}
		};
	}

	public Expression<K, C, Boolean> isSurfaceOrMultiSurface() {
		return new AbstractUnaryTestExpression<K, C, T>(this, "IsSurfaceOrMultiSurface") {
			@Override
			public boolean test(final T value, final C context) {
				if (value instanceof Surface) {
					return true;
				}
				if (value instanceof MultiGeometry<?>) {
					for (Geometry member : ((MultiGeometry<?>) value)) {
						if (!(member instanceof Surface)) {
							return false;
						}
					}
					return true;
				}
				return false;
			}
		};
	}

	public Expression<K, C, Boolean> isInteriorDisconnected() {
		return new AbstractUnaryTestExpression<K, C, T>(this, "IsInteriorDisconnected") {
			@Override
			public boolean test(T value, C context) {
				if (!(value instanceof Polygon)) {
					return false;
				}

				try {
					final GeometryValidator validator = context.validateGeometry(value).validator;
					final Polygon polygon = (Polygon) value;

					final GeometryFactory geometryFactory = new GeometryFactory();

					final LinearRing exteriorRing = getJTSRing(polygon.getExteriorRing(), validator);
					final ArrayList<LinearRing> interiorRings = new ArrayList<LinearRing>();
					for (final Ring ring : polygon.getInteriorRings()) {
						interiorRings.add(getJTSRing(ring, validator));
					}

					com.vividsolutions.jts.geom.Polygon jtsPolygon = geometryFactory.createPolygon(exteriorRing,
							interiorRings.toArray(new LinearRing[interiorRings.size()]));

					final GeometryGraph geometryGraph = new GeometryGraph(0, jtsPolygon);
					geometryGraph.computeSelfNodes(new RobustLineIntersector(), true);

					ConnectedInteriorTester connectedInteriorTester = new ConnectedInteriorTester(geometryGraph);

					return !connectedInteriorTester.isInteriorsConnected();
				} catch (InvocationTargetException e) {
					throw new ExpressionEvaluationException(e);
				} catch (IllegalArgumentException e) {
					throw new ExpressionEvaluationException(e);
				} catch (IllegalAccessException e) {
					throw new ExpressionEvaluationException(e);
				}
			}
		};
	}

	public Expression<K, C, Boolean> hasSrs() {
		return new AbstractUnaryTestExpression<K, C, T>(this, "HasSrs") {
			@Override
			public boolean test(final T value, final C context) {
				return value != null && value.getCoordinateSystem() != null;
			}
		};
	}

	public Expression<K, C, Boolean> isSrs(final Expression<K, C, String> srsName) {
		return new AbstractBinaryTestExpression<K, C, T, String>(this, srsName, "IsSrs") {
			@Override
			public boolean test(T a, String b, C context) {
				return a != null && b != null && a.getCoordinateSystem() != null
						&& a.getCoordinateSystem().getName().contains(b);
			}
		};
	}

	public Expression<K, C, Boolean> hasCurveDuplicatePoint() {
		return new AbstractUnaryTestExpression<K, C, T>(this, "HasCurveDuplicatePoint") {
			@Override
			public boolean test(final T value, final C context) {
				final GeometryValidationStatus status = context.validateGeometry(value).curvePointDuplication();
				if (status.status()) {
					context.setLastLocation(status.location());
					return true;
				}
				return false;
			}
		};
	}

	public Expression<K, C, Boolean> hasCurveDiscontinuity() {
		return new AbstractUnaryTestExpression<K, C, T>(this, "HasCurveDiscontinuity") {
			@Override
			public boolean test(final T value, final C context) {
				final GeometryValidationStatus status = context.validateGeometry(value).curveDiscontinuity();
				if (status.status()) {
					context.setLastLocation(status.location());
					return true;
				}
				return false;
			}
		};
	}

	public Expression<K, C, Boolean> hasCurveSelfIntersection() {
		return new AbstractUnaryTestExpression<K, C, T>(this, "HasCurveSelfIntersection") {
			@Override
			public boolean test(final T value, final C context) {
				final GeometryValidationStatus status = context.validateGeometry(value).curveSelfIntersection();
				if (status.status()) {
					context.setLastLocation(status.location());
					return true;
				}
				return false;
			}
		};
	}

	public Expression<K, C, Boolean> hasUnclosedRing() {
		return new AbstractUnaryTestExpression<K, C, T>(this, "HasUnclosedRing") {
			@Override
			public boolean test(final T value, final C context) {
				final GeometryValidationStatus status = context.validateGeometry(value).ringNotClosed();
				if (status.status()) {
					context.setLastLocation(status.location());
					return true;
				}
				return false;
			}
		};
	}

	public Expression<K, C, Boolean> hasRingSelfIntersection() {
		return new AbstractUnaryTestExpression<K, C, T>(this, "HasRingSelfIntersection") {
			@Override
			public boolean test(final T value, final C context) {
				final GeometryValidationStatus status = context.validateGeometry(value).ringSelfIntersection();
				if (status.status()) {
					context.setLastLocation(status.location());
					return true;
				}
				return false;
			}
		};
	}

	public Expression<K, C, Boolean> hasExteriorRingCW() {
		return new AbstractUnaryTestExpression<K, C, T>(this, "HasExteriorRingCW") {
			@Override
			public boolean test(final T value, final C context) {
				final GeometryValidationStatus status = context.validateGeometry(value).exteriorRingCW();
				if (status.status()) {
					context.setLastLocation(status.location());
					return true;
				}
				return false;
			}
		};
	}

	public Expression<K, C, Boolean> hasInteriorRingCCW() {
		return new AbstractUnaryTestExpression<K, C, T>(this, "HasInteriorRingCCW") {
			@Override
			public boolean test(final T value, final C context) {
				final GeometryValidationStatus status = context.validateGeometry(value).interiorRingCCW();
				if (status.status()) {
					context.setLastLocation(status.location());
					return true;
				}
				return false;
			}
		};
	}

	public Expression<K, C, Boolean> hasTouchingInteriorRings() {
		return new AbstractUnaryTestExpression<K, C, T>(this, "HasTouchingInteriorRings") {
			@Override
			public boolean test(final T value, final C context) {
				final GeometryValidationStatus status = context.validateGeometry(value).interiorRingsTouch();
				if (status.status()) {
					context.setLastLocation(status.location());
					return true;
				}
				return false;
			}
		};
	}

	public Expression<K, C, Boolean> hasIntersectingInteriorRings() {
		return new AbstractUnaryTestExpression<K, C, T>(this, "HasIntersectingInteriorRings") {
			@Override
			public boolean test(final T value, final C context) {
				final GeometryValidationStatus status = context.validateGeometry(value).interiorRingsIntersect();
				if (status.status()) {
					context.setLastLocation(status.location());
					return true;
				}
				return false;
			}
		};
	}

	public Expression<K, C, Boolean> hasInteriorRingsWithin() {
		return new AbstractUnaryTestExpression<K, C, T>(this, "HasInteriorRingsWithin") {
			@Override
			public boolean test(final T value, final C context) {
				final GeometryValidationStatus status = context.validateGeometry(value).interiorRingsWithin();
				if (status.status()) {
					context.setLastLocation(status.location());
					return true;
				}
				return false;
			}
		};
	}

	public Expression<K, C, Boolean> hasInteriorRingTouchingExterior() {
		return new AbstractUnaryTestExpression<K, C, T>(this, "HasInteriorRingTouchingExterior") {
			@Override
			public boolean test(final T value, final C context) {
				final GeometryValidationStatus status = context.validateGeometry(value).interiorRingTouchesExterior();
				if (status.status()) {
					context.setLastLocation(status.location());
					return true;
				}
				return false;
			}
		};
	}

	public Expression<K, C, Boolean> hasInteriorRingIntersectingExterior() {
		return new AbstractUnaryTestExpression<K, C, T>(this, "HasInteriorRingIntersectingExterior") {
			@Override
			public boolean test(final T value, final C context) {
				final GeometryValidationStatus status = context.validateGeometry(value).interiorRingTouchesExterior();
				if (status.status()) {
					context.setLastLocation(status.location());
					return true;
				}
				return false;
			}
		};
	}

	public Expression<K, C, Boolean> hasInteriorRingOutsideExterior() {
		return new AbstractUnaryTestExpression<K, C, T>(this, "HasInteriorRingOutsideExterior") {
			@Override
			public boolean test(final T value, final C context) {
				final GeometryValidationStatus status = context.validateGeometry(value).interiorRingOutsideExterior();
				if (status.status()) {
					context.setLastLocation(status.location());
					return true;
				}
				return false;
			}
		};
	}

	public class SrsNameExpression extends AbstractExpression<K, C, String> {
		@Override
		public Class<String> getResultType() {
			return String.class;
		}

		public String evaluate(final C context, final Geometry input) {
			final Geometry value = input;
			return value == null || value.getCoordinateSystem() == null ? null : value.getCoordinateSystem().getName();
		}

		@Override
		public ExpressionExecutor<C> getExecutor(final Compiler<C> compiler) throws CompilerException {
			return ExpressionExecutor.create(
					this,
					GeometryExpression.this,
					false,
					true,
					Compiler.findMethod(SrsNameExpression.class, "evaluate",
							MethodType.methodType(String.class, ValidatorContext.class, Geometry.class)).bindTo(this),
					false);
		}
	}

	public Expression<K, C, String> srsName() {
		return new SrsNameExpression();
	}

	private final static Method getJTSRingMethod;

	static {
		try {
			getJTSRingMethod = GeometryValidator.class.getDeclaredMethod("getJTSRing", Ring.class);
			getJTSRingMethod.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static LinearRing getJTSRing(final Ring exteriorRing, final GeometryValidator geometryValidator)
			throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return (LinearRing) getJTSRingMethod.invoke(geometryValidator, exteriorRing);
	}
}
