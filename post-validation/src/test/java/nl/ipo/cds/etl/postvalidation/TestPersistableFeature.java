package nl.ipo.cds.etl.postvalidation;

import com.vividsolutions.jts.io.ParseException;
import nl.ipo.cds.etl.PersistableFeature;
import nl.ipo.cds.etl.xml.bind.GmlElement;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.WKBReader;
import org.deegree.geometry.io.WKBWriter;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

@XmlRootElement(name="testPersistableFeature")
public class TestPersistableFeature extends PersistableFeature implements Serializable {

    @GmlElement
    private transient Geometry geometry;

    /**
     * Custom deserialization because Geometry type is not serializable by default, nor is CodeType.
     * @param ois The input stream.
     */
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException, ParseException {
        // Read default serializable properties.
        ois.defaultReadObject();


        // Also serialize ID since this is not serializable from PersistentFeature. This is a hack.
        setId(ois.readUTF());

        // Read the Geometry with corresponding coordinate system.
        ICRS icrs = (ICRS)ois.readObject();
        geometry = WKBReader.read(ois, icrs);
    }

    /**
     * Custom serialization because deegree types are not serializable.
     * @param oos The output stream.
     * @throws IOException
     */
    private void writeObject(ObjectOutputStream oos) throws IOException, ParseException {
        // Write default serializable properties.
        oos.defaultWriteObject();

        oos.writeUTF(getId());
        // Write Geometry and its coordinate system.
        oos.writeObject(geometry.getCoordinateSystem());
        WKBWriter.write(geometry, oos);

    }

    public boolean equals(Object o) {
        if(!(o instanceof TestPersistableFeature)) {
            return false;
        }

        TestPersistableFeature that = (TestPersistableFeature)o;
        return this.getId().equals(that.getId()) &&
                this.getGeometry().toString().equals(that.getGeometry().toString());
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public Geometry getGeometry(){
        return geometry;
    }
}

