package nl.ipo.cds.etl.postvalidation;

import nl.ipo.cds.etl.PersistableFeature;
import nl.ipo.cds.etl.xml.bind.GmlElement;
import org.deegree.geometry.Geometry;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name="testPersistableFeature")
public class TestPersistableFeature extends PersistableFeature {

    @GmlElement
    private Geometry geometry;

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    @XmlJavaTypeAdapter(JaxbGeometrySerializer.class)
    public Geometry getGeometry(){
        return geometry;
    }
}

