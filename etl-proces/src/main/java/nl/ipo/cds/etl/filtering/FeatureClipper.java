package nl.ipo.cds.etl.filtering;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import nl.ipo.cds.etl.Feature;
import nl.ipo.cds.etl.FeatureFilter;
import nl.ipo.cds.etl.FeatureOutputStream;
import nl.ipo.cds.etl.PersistableFeature;

import org.deegree.geometry.Geometry;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * FeatureOutputStream implementation that filters and clips features based
 * on a given geometry and writes the resulting features to a wrapped output
 * stream.
 * 
 * Incomming features that are completely disjoint with the clip geometry are
 * discarded (they are not written to the wrapped output stream). Other features
 * have their geometry intersected with the clip geometry before they are
 * written to the output stream.
 * 
 * Currently feature classes should have a single single property of type
 * org.deegree.geometry.Geometry.
 */
public class FeatureClipper implements FeatureFilter<PersistableFeature, PersistableFeature> {

	private final Geometry clipGeometry;
	private final Class<? extends PersistableFeature> featureClass;
	private final PropertyDescriptor geometryDescriptor;
	
	public FeatureClipper (final Geometry clipGeometry, final Class<? extends PersistableFeature> featureClass) {
		if (clipGeometry == null) {
			throw new NullPointerException ("clipGeometry cannot be null");
		}
		if (featureClass == null) {
			throw new NullPointerException ("featureClass cannot be null");
		}
		
		this.clipGeometry = clipGeometry;
		this.featureClass = featureClass;
		
		// Locate the geometry property to use:
		final BeanWrapper wrapper = new BeanWrapperImpl (featureClass);
		final List<PropertyDescriptor> geometryDescriptors = new ArrayList<PropertyDescriptor> ();
		
		for (final PropertyDescriptor pd: wrapper.getPropertyDescriptors()) {
			if (Geometry.class.isAssignableFrom (pd.getPropertyType ()) && pd.getReadMethod () != null && pd.getWriteMethod () != null) {
				geometryDescriptors.add (pd);
			}
		}
		
		if (geometryDescriptors.size () > 1) {
			// In the future we might want to support multiple geometry properties, currently exactly one must exist.
			throw new IllegalArgumentException (String.format ("Feature class %s has multiple geometry properties", featureClass.getCanonicalName ()));
		} else if (geometryDescriptors.size () == 1) {
			geometryDescriptor = geometryDescriptors.get (0);
		} else {
			throw new IllegalArgumentException (String.format ("Feature class %s has no (writable) geometry property", featureClass.getCanonicalName ()));
		}
	}

	public Geometry getClipGeometry () {
		return clipGeometry;
	}
	
	public Class<? extends PersistableFeature> getFeatureClass () {
		return featureClass;
	}
	
	@Override
	public void processFeature (final PersistableFeature feature, final FeatureOutputStream<PersistableFeature> outputStream, final FeatureOutputStream<Feature> errorOutputStream) {
		if (feature == null) {
			throw new NullPointerException ("feature cannot be null");
		}

		
		final Geometry geometry = getGeometry (feature);
		if (geometry == null) {
			// The feature doesn't require clipping:
			outputStream.writeFeature (feature);
			return;
		}
		
		clipGeometry.setCoordinateSystem (geometry.getCoordinateSystem ());
		
		if (clipGeometry.isDisjoint (geometry)) {
			// The feature is completely outside the clip area:
			return;
		}
		
		final Geometry clippedGeometry = geometry.getIntersection (clipGeometry);
		if (clippedGeometry == null) {
			return;
		}

		setGeometry (feature, clippedGeometry);
		
		outputStream.writeFeature (feature);
	}
	
	private Geometry getGeometry (final PersistableFeature feature) {
		try {
			return (Geometry)geometryDescriptor.getReadMethod ().invoke (feature);
		} catch (IllegalAccessException e) {
			throw new RuntimeException (e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException (e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException (e);
		}
	}
	
	private void setGeometry (final PersistableFeature feature, final Geometry geometry) {
		try {
			geometryDescriptor.getWriteMethod ().invoke (feature, geometry);
		} catch (IllegalAccessException e) {
			throw new RuntimeException (e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException (e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException (e);
		}
	}

	@Override
	public void finish () {
	}
}
