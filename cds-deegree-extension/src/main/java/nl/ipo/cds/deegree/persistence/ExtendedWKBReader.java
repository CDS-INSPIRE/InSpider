//$HeadURL: https://svn.wald.intevation.org/svn/deegree/deegree3/trunk/deegree-core/deegree-core-geometry/src/main/java/org/deegree/geometry/io/WKBReader.java $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package nl.ipo.cds.deegree.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.LinearRing;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.standard.AbstractDefaultGeometry;
import org.deegree.geometry.standard.multi.DefaultMultiGeometry;
import org.deegree.geometry.standard.multi.DefaultMultiLineString;
import org.deegree.geometry.standard.multi.DefaultMultiPoint;
import org.deegree.geometry.standard.multi.DefaultMultiPolygon;
import org.deegree.geometry.standard.points.JTSPoints;
import org.deegree.geometry.standard.primitive.DefaultLineString;
import org.deegree.geometry.standard.primitive.DefaultLinearRing;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.deegree.geometry.standard.primitive.DefaultPolygon;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory;
import com.vividsolutions.jts.io.InputStreamInStream;
import com.vividsolutions.jts.io.ParseException;

/**
 * Reads {@link Geometry} objects encoded as Well-Known Binary (WKB).
 * 
 * TODO re-implement without delegating to JTS TODO add support for non-SFS geometries (e.g. non-linear curves)
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31641 $, $Date: 2011-08-24 21:24:55 +0200 (wo, 24 aug 2011) $
 */
public class ExtendedWKBReader {
    
    public static Geometry read( byte[] wkb, ICRS crs ) throws ParseException {
    	return read( wkb, crs, false );
    }

    public static Geometry read( byte[] wkb, ICRS crs, boolean swapAxis )
                            throws ParseException {
        // com.vividsolutions.jts.io.WKBReader() is not thread safe        
        return createFromJTS( new com.vividsolutions.jts.io.WKBReader().read( wkb ), crs, swapAxis );
    }
    
    public static Geometry read( InputStream is, ICRS crs ) throws IOException, ParseException {
    	return read( is, crs, false );
    }

    public static Geometry read( InputStream is, ICRS crs, boolean swapAxis )
                            throws IOException, ParseException {
        // com.vividsolutions.jts.io.WKBReader() is not thread safe
        return createFromJTS(
                                          new com.vividsolutions.jts.io.WKBReader().read( new InputStreamInStream( is ) ),
                                          crs, swapAxis );
    }
    
    public static AbstractDefaultGeometry createFromJTS( com.vividsolutions.jts.geom.Geometry jtsGeom, ICRS crs ) {
    	return createFromJTS( jtsGeom, crs, false );
    }
    
    @SuppressWarnings("unchecked")
    public static AbstractDefaultGeometry createFromJTS( com.vividsolutions.jts.geom.Geometry jtsGeom, ICRS crs, boolean swapAxis ) {

        AbstractDefaultGeometry geom = null;
        PrecisionModel pm = null;
		if ( jtsGeom instanceof com.vividsolutions.jts.geom.Point ) {
            com.vividsolutions.jts.geom.Point jtsPoint = (com.vividsolutions.jts.geom.Point) jtsGeom;
            if ( Double.isNaN( jtsPoint.getCoordinate().z ) ) {
            	if ( swapAxis ) {
            		geom = new DefaultPoint( null, crs, pm , new double[] { jtsPoint.getY(), jtsPoint.getX() } );
            	} else {
            		geom = new DefaultPoint( null, crs, pm, new double[] { jtsPoint.getX(), jtsPoint.getY() } );
            	}
            } else {
            	if ( swapAxis ) {
            		geom = new DefaultPoint( null, crs, pm, new double[] { jtsPoint.getY(), jtsPoint.getX(),
                            jtsPoint.getCoordinate().z } );
            	} else {
            		geom = new DefaultPoint( null, crs, pm, new double[] { jtsPoint.getX(), jtsPoint.getY(),
            				jtsPoint.getCoordinate().z } );
            	}
            }
        } else if ( jtsGeom instanceof com.vividsolutions.jts.geom.LinearRing ) {
            com.vividsolutions.jts.geom.LinearRing jtsLinearRing = (com.vividsolutions.jts.geom.LinearRing) jtsGeom;
            geom = new DefaultLinearRing( null, crs, pm, getAsPoints( jtsLinearRing.getCoordinateSequence(), crs, swapAxis ) );
        } else if ( jtsGeom instanceof com.vividsolutions.jts.geom.LineString ) {
            com.vividsolutions.jts.geom.LineString jtsLineString = (com.vividsolutions.jts.geom.LineString) jtsGeom;
            geom = new DefaultLineString( null, crs, pm, getAsPoints( jtsLineString.getCoordinateSequence(), crs, swapAxis ) );
        } else if ( jtsGeom instanceof com.vividsolutions.jts.geom.Polygon ) {
            com.vividsolutions.jts.geom.Polygon jtsPolygon = (com.vividsolutions.jts.geom.Polygon) jtsGeom;
            Points exteriorPoints = getAsPoints( jtsPolygon.getExteriorRing().getCoordinateSequence(), crs, swapAxis );
            LinearRing exteriorRing = new DefaultLinearRing( null, crs, pm, exteriorPoints );
            List<Ring> interiorRings = new ArrayList<Ring>( jtsPolygon.getNumInteriorRing() );
            for ( int i = 0; i < jtsPolygon.getNumInteriorRing(); i++ ) {
                Points interiorPoints = getAsPoints( jtsPolygon.getInteriorRingN( i ).getCoordinateSequence(), crs, swapAxis );
                interiorRings.add( new DefaultLinearRing( null, crs, pm, interiorPoints ) );
            }
            geom = new DefaultPolygon( null, crs, pm, exteriorRing, interiorRings );
        } else if ( jtsGeom instanceof com.vividsolutions.jts.geom.MultiPoint ) {
            com.vividsolutions.jts.geom.MultiPoint jtsMultiPoint = (com.vividsolutions.jts.geom.MultiPoint) jtsGeom;
            if ( jtsMultiPoint.getNumGeometries() > 0 ) {
                List<Point> members = new ArrayList<Point>( jtsMultiPoint.getNumGeometries() );
                for ( int i = 0; i < jtsMultiPoint.getNumGeometries(); i++ ) {
                    members.add( (Point) createFromJTS( jtsMultiPoint.getGeometryN( i ), crs, swapAxis ) );
                }
                geom = new DefaultMultiPoint( null, crs, pm, members );
            }
        } else if ( jtsGeom instanceof com.vividsolutions.jts.geom.MultiLineString ) {
            com.vividsolutions.jts.geom.MultiLineString jtsMultiLineString = (com.vividsolutions.jts.geom.MultiLineString) jtsGeom;
            if ( jtsMultiLineString.getNumGeometries() > 0 ) {
                List<LineString> members = new ArrayList<LineString>( jtsMultiLineString.getNumGeometries() );
                for ( int i = 0; i < jtsMultiLineString.getNumGeometries(); i++ ) {
                    Curve curve = (Curve) createFromJTS( jtsMultiLineString.getGeometryN( i ), crs, swapAxis );
                    members.add( curve.getAsLineString() );
                }
                geom = new DefaultMultiLineString( null, crs, pm, members );
            }
        } else if ( jtsGeom instanceof com.vividsolutions.jts.geom.MultiPolygon ) {
            com.vividsolutions.jts.geom.MultiPolygon jtsMultiPolygon = (com.vividsolutions.jts.geom.MultiPolygon) jtsGeom;
            if ( jtsMultiPolygon.getNumGeometries() > 0 ) {
                List<Polygon> members = new ArrayList<Polygon>( jtsMultiPolygon.getNumGeometries() );
                for ( int i = 0; i < jtsMultiPolygon.getNumGeometries(); i++ ) {
                    members.add( (Polygon) createFromJTS( jtsMultiPolygon.getGeometryN( i ), crs, swapAxis ) );
                }
                geom = new DefaultMultiPolygon( null, crs, pm, members );
            }
        } else if ( jtsGeom instanceof com.vividsolutions.jts.geom.GeometryCollection ) {
            com.vividsolutions.jts.geom.GeometryCollection jtsGeometryCollection = (com.vividsolutions.jts.geom.GeometryCollection) jtsGeom;
            if ( jtsGeometryCollection.getNumGeometries() > 0 ) {
                List<Geometry> members = new ArrayList<Geometry>( jtsGeometryCollection.getNumGeometries() );
                for ( int i = 0; i < jtsGeometryCollection.getNumGeometries(); i++ ) {
                    members.add( createFromJTS( jtsGeometryCollection.getGeometryN( i ), crs, swapAxis ) );
                }
                geom = new DefaultMultiGeometry( null, crs, pm, members );
            }
        } else {
            throw new RuntimeException( "Internal error. Encountered unhandled JTS geometry type '"
                                        + jtsGeom.getClass().getName() + "'." );
        }
        return geom;
    }
    
    private static Points getAsPoints( CoordinateSequence seq, ICRS crs, boolean swapAxis ) {
    	int dim = seq.getDimension();    	
    	double[] coordinates = new double[ seq.size() * dim ];
    	
    	int idx = 0;
    	for(int i = 0; i < seq.size(); i++) {
    		for(int j = 0; j < dim; j++) {
    			if(swapAxis) {
    				if(j == 0) {
    					coordinates[idx++] = seq.getOrdinate(i, 1);
    				} else if( j == 1) {
    					coordinates[idx++] = seq.getOrdinate(i, 0);
    				} else {
    					coordinates[idx++] = seq.getOrdinate(i, j);
    				}
    			} else {
    				coordinates[idx++] = seq.getOrdinate(i, j);
    			}
    		}
    	}
    	
    	PackedCoordinateSequenceFactory factory = new PackedCoordinateSequenceFactory();
    	seq = factory.create(coordinates, dim);
    	
        return new JTSPoints( crs, seq );
    }
}