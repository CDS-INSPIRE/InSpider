package nl.ipo.cds.validation;

import java.util.List;


import org.deegree.geometry.Geometry;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.patches.PolygonPatch;
import org.deegree.geometry.validation.GeometryValidationEventHandler;
import org.deegree.geometry.validation.GeometryValidator;

public class GeometryValidationResult {
	public final Geometry geometry;
	public final GeometryValidator validator;
	
	private GeometryValidationStatus curvePointDuplication = new GeometryValidationStatus ();
	private GeometryValidationStatus curveDiscontinuity = new GeometryValidationStatus ();
	private GeometryValidationStatus curveSelfIntersection = new GeometryValidationStatus ();
	private GeometryValidationStatus ringNotClosed = new GeometryValidationStatus ();
	private GeometryValidationStatus ringSelfIntersection = new GeometryValidationStatus ();
	private GeometryValidationStatus exteriorRingCW = new GeometryValidationStatus ();
	private GeometryValidationStatus interiorRingCCW = new GeometryValidationStatus ();
	private GeometryValidationStatus interiorRingsTouch = new GeometryValidationStatus ();
	private GeometryValidationStatus interiorRingsIntersect = new GeometryValidationStatus ();
	private GeometryValidationStatus interiorRingsWithin = new GeometryValidationStatus ();
	private GeometryValidationStatus interiorRingTouchesExterior = new GeometryValidationStatus ();
	private GeometryValidationStatus interiorRingIntersectsExterior = new GeometryValidationStatus ();
	private GeometryValidationStatus interiorRingOutsideExterior = new GeometryValidationStatus ();
	
	public GeometryValidationResult (final Geometry geometry) {
		validator = new GeometryValidator (new GeometryValidationEventHandler () {
			@Override
			public boolean ringSelfIntersection(Ring ring, Point location,
					List<Object> affectedGeometryParticles) {
				ringSelfIntersection = new GeometryValidationStatus (location);
				return false;
			}
			
			@Override
			public boolean ringNotClosed(Ring ring,
					List<Object> affectedGeometryParticles) {
				ringNotClosed = new GeometryValidationStatus (ring.getEndPoint());
				return false;
			}
			
			@Override
			public boolean interiorRingsWithin(PolygonPatch patch, int ring1Idx,
					int ring2Idx, List<Object> affectedGeometryParticles) {
				interiorRingsWithin = new GeometryValidationStatus (patch.getInteriorRings().get(ring2Idx).getCentroid());
				return false;
			}
			
			@Override
			public boolean interiorRingsTouch(PolygonPatch patch, int ring1Idx,
					int ring2Idx, Point location, List<Object> affectedGeometryParticles) {
				interiorRingsTouch = new GeometryValidationStatus (location);
				return false;
			}
			
			@Override
			public boolean interiorRingsIntersect(PolygonPatch patch, int ring1Idx,
					int ring2Idx, Point location, List<Object> affectedGeometryParticles) {
				interiorRingsIntersect = new GeometryValidationStatus (location);
				return false;
			}
			
			@Override
			public boolean interiorRingTouchesExterior(PolygonPatch patch, int ringIdx,
					Point location, List<Object> affectedGeometryParticles) {
				interiorRingTouchesExterior = new GeometryValidationStatus (location);
				return false;
			}
			
			@Override
			public boolean interiorRingOutsideExterior(PolygonPatch patch, int ringIdx,
					List<Object> affectedGeometryParticles) {
				interiorRingOutsideExterior = new GeometryValidationStatus (patch.getInteriorRings().get(ringIdx).getCentroid());
				return false;
			}
			
			@Override
			public boolean interiorRingIntersectsExterior(PolygonPatch patch,
					int ringIdx, Point location, List<Object> affectedGeometryParticles) {
				interiorRingIntersectsExterior = new GeometryValidationStatus (location);
				return false;
			}
			
			@Override
			public boolean interiorRingCCW(PolygonPatch patch, int ringIdx,
					List<Object> affectedGeometryParticles) {
				interiorRingCCW = new GeometryValidationStatus (patch.getInteriorRings().get(ringIdx).getCentroid());
				return false;
			}
			
			@Override
			public boolean exteriorRingCW(PolygonPatch patch,
					List<Object> affectedGeometryParticles) {
				exteriorRingCW = new GeometryValidationStatus (patch.getExteriorRing ().getCentroid ());
				return false;
			}
			
			@Override
			public boolean curveSelfIntersection(Curve curve, Point location,
					List<Object> affectedGeometryParticles) {
				curveSelfIntersection = new GeometryValidationStatus (location);
				return false;
			}
			
			@Override
			public boolean curvePointDuplication(Curve curve, Point point,
					List<Object> affectedGeometryParticles) {
				curvePointDuplication = new GeometryValidationStatus (point);
				return false;
			}
			
			@Override
			public boolean curveDiscontinuity(Curve curve, int segmentIdx,
					List<Object> affectedGeometryParticles) {
				curveDiscontinuity = new GeometryValidationStatus (curve.getCurveSegments().get(segmentIdx).getStartPoint());
				return false;
			}
		});
		if (geometry != null) {
			validator.validateGeometry (geometry);
		}
		
		this.geometry = geometry;
	}
	
	public GeometryValidationStatus curvePointDuplication () {
		return curvePointDuplication;
	}
	public GeometryValidationStatus curveDiscontinuity () {
		return curveDiscontinuity;
	}
	public GeometryValidationStatus curveSelfIntersection () {
		return curveSelfIntersection;
	}
	public GeometryValidationStatus ringNotClosed () {
		return ringNotClosed;
	}
	public GeometryValidationStatus ringSelfIntersection () {
		return ringSelfIntersection;
	}
	public GeometryValidationStatus exteriorRingCW () {
		return exteriorRingCW;
	}
	public GeometryValidationStatus interiorRingCCW () {
		return interiorRingCCW;
	}
	public GeometryValidationStatus interiorRingsTouch () {
		return interiorRingsTouch;
	}
	public GeometryValidationStatus interiorRingsIntersect () {
		return interiorRingsIntersect;
	}
	public GeometryValidationStatus interiorRingsWithin () {
		return interiorRingsWithin;
	}
	public GeometryValidationStatus interiorRingTouchesExterior () {
		return interiorRingTouchesExterior;
	}
	public GeometryValidationStatus interiorRingIntersectsExterior () {
		return interiorRingIntersectsExterior;
	}
	public GeometryValidationStatus interiorRingOutsideExterior () {
		return interiorRingOutsideExterior;
	}
}