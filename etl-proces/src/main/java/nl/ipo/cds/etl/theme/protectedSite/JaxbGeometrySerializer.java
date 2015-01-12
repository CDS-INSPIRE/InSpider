package nl.ipo.cds.etl.theme.protectedSite;

import org.apache.axiom.util.stax.XMLFragmentStreamReader;
import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.WKTReader;
import org.deegree.geometry.standard.points.JTSPoints;
import org.deegree.geometry.standard.primitive.DefaultPolygon;
import org.deegree.gml.*;
import org.deegree.gml.feature.GMLFeatureWriter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.stream.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Used to also serialize Geometry objects attached to a ProtectedSite.
 */
public class JaxbGeometrySerializer extends XmlAdapter<String, Geometry> {


    /**
     * Deserializes a GML 3.2 string to a deegree Geometry object.
     * @param str The GML string.
     * @return A Geometry object.
     */
    @Override
    public Geometry unmarshal(String str) throws XMLStreamException, UnknownCRSException {
        ByteArrayInputStream bis = new ByteArrayInputStream(str.getBytes());
        XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(bis);
        GMLStreamReader gmlStreamReader = GMLInputFactory.createGMLStreamReader(GMLVersion.GML_32, xmlStreamReader);

        return (Geometry)gmlStreamReader.read();
    }

    /**
     * Serialize a Geometry object (deegree) to GML 3.2 string.
     * @param g The Geometry object.
     * @return The GML 3.2 string.
     */
    @Override
    public String marshal(Geometry g) throws TransformationException, UnknownCRSException, XMLStreamException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(bos);
        GMLStreamWriter gmlStreamWriter = GMLOutputFactory.createGMLStreamWriter(GMLVersion.GML_32, xmlStreamWriter);
        gmlStreamWriter.write(g);

        // Flush to the byte buffer.
        xmlStreamWriter.flush();
        gmlStreamWriter.close();
        return bos.toString();
    }

}
