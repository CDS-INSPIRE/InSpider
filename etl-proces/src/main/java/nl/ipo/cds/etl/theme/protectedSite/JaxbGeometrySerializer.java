package nl.ipo.cds.etl.theme.protectedSite;

import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.WKTReader;
import org.deegree.geometry.standard.points.JTSPoints;
import org.deegree.geometry.standard.primitive.DefaultPolygon;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLStreamWriter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

/**
 * Used to also serialize Geometry objects attached to a ProtectedSite.
 */
public class JaxbGeometrySerializer extends XmlAdapter<String, Geometry> {

    @Override
    public Geometry unmarshal(String str) throws Exception {
        //XMLStreamReader xmlStreamReader = new XMLStri
        //GMLStreamReader gmlStreamReader = GMLInputFactory.createGMLStreamReader()

        WKTReader reader = new WKTReader(null);
        return reader.read("SRID=28992;POLYGON((111446.5 566602,112035.5 566602,112035.5 566886,111446.5 566886,111446.5 566602))");
    }

    @Override
    public String marshal(Geometry g) throws Exception {
        //GMLStreamWriter gmlStreamWriter = new GMLStreamWriter();
        XMLStreamWriter xmlStreamWriter;
        return "";
    }

}
