package nl.ipo.cds.etl.test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.Point;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;

public class GeometryConstants {

	private final GeometryFactory geometryFactory = new GeometryFactory ();
	public final String srsName;
	private final ICRS srs;

	public GeometryConstants (final String srsName) throws Exception {
		this.srsName = srsName;
		this.srs = getSrs (srsName);

	}

	public Geometry point (final double x, final double y) {
		return point (x, y, srs);
	}

	public Geometry point (final double x, final double y, final ICRS crs) {
		return geometryFactory.createPoint(null, new double[] { x,  y}, crs);
	}

	public Geometry lineString () throws Exception {
		return lineString (srs);
	}

	public Geometry lineString (final ICRS srs) throws Exception {
		return readGml ("<gml:LineString xmlns:gml=\"http://www.opengis.net/gml\"><gml:posList>109086.3 420613.5 109084.5 420612.6 109082.6 420612 109082.5 420612</gml:posList></gml:LineString>", srs);
	}

	public Geometry lineStringDuplicatePoint () throws Exception {
		return readGml ("<gml:LineString xmlns:gml=\"http://www.opengis.net/gml\"><gml:posList>109086.3 420613.5 109084.5 420612.6 109082.6 420612 109082.5 420612 109082.5 420612</gml:posList></gml:LineString>", srs);
	}

	public Geometry lineStringSelfIntersection () throws Exception {
		return readGml ("<gml:LineString xmlns:gml=\"http://www.opengis.net/gml\"><gml:posList>0 0 10 0 10 10 5 10 5 -10</gml:posList></gml:LineString>", srs);
	}

	public Geometry polygon () throws Exception {
		return polygon (srs);
	}

	public Geometry polygon (final ICRS srs) throws Exception {
		return readGml ("<gml:Polygon xmlns:gml=\"http://www.opengis.net/gml\"><gml:exterior>" +
				"<gml:LinearRing><gml:posList>0 0 10 0 10 10 0 10 0 0" +
				"</gml:posList></gml:LinearRing></gml:exterior></gml:Polygon>",
				srs
			);
	}

	public Geometry multiPoint () throws Exception {
		return multiPoint (srs);
	}

	public Geometry multiPoint (final ICRS srs) throws Exception {
		List<Point> members = new ArrayList<Point>();
		members.add((Point) point(47.0, 11.0, srs));
		members.add((Point) point(48.0, 12.0, srs));
		return geometryFactory.createMultiPoint(null, srs, members);
	}

	public Geometry multiPolygon () throws Exception {
		return multiPolygon (srs);
	}

	public Geometry multiPolygon (final ICRS srs) throws Exception {
		return readGml ("<gml:MultiSurface xmlns:gml=\"http://www.opengis.net/gml\"><gml:surfaceMember><gml:Polygon><gml:exterior>" +
				"<gml:LinearRing><gml:posList>0 0 10 0 10 10 0 10 0 0" +
				"</gml:posList></gml:LinearRing></gml:exterior></gml:Polygon></gml:surfaceMember></gml:MultiSurface>",
				srs
			);
	}

	public Geometry emptyMultiPolygon () throws Exception {
		return readGml ("<gml:MultiSurface xmlns:gml=\"http://www.opengis.net/gml\"/>", srs);
	}

	public ICRS getSrs (final String srsName) throws Exception {
		final String gml = String.format ("<gml:LineString srsName=\"%s\" srsDimension=\"2\" xmlns:gml=\"http://www.opengis.net/gml\"><gml:posList>0.0 0.0 0.0 0.0 0.0 0 0.0 0</gml:posList></gml:LineString>", srsName);
		final Geometry geom = readGml (gml);

		return geom.getCoordinateSystem ();
	}

	private Geometry readGml (final String gmlString, final ICRS crs) throws Exception {
		final Geometry geom = readGml (gmlString);
		geom.setCoordinateSystem (crs);
		return geom;
	}

	private Geometry readGml (final String gmlString) throws Exception {
		final XMLInputFactory inputFactory = XMLInputFactory.newInstance ();
		final XMLStreamReader streamReader = inputFactory.createXMLStreamReader(new StringReader(gmlString));
		streamReader.next();
		final GMLStreamReader gmlStreamReader = GMLInputFactory.createGMLStreamReader(GMLVersion.GML_31, streamReader);
		return gmlStreamReader.readGeometry();
	}
}
