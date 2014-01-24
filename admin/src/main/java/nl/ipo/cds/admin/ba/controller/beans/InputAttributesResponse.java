package nl.ipo.cds.admin.ba.controller.beans;

import static nl.ipo.cds.domain.AttributeType.DOUBLE;
import static nl.ipo.cds.domain.AttributeType.GEOMETRY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.ipo.cds.domain.FeatureType;
import nl.ipo.cds.domain.FeatureTypeAttribute;
import nl.ipo.cds.etl.featuretype.DefaultQName;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@JsonSerialize (include = Inclusion.ALWAYS)
public class InputAttributesResponse {

	@JsonIgnore
	private final FeatureType featureType;
	
	public InputAttributesResponse (final FeatureType featureType) {
		this.featureType = featureType;
	}
	
	public QNameResponse getName () {
		return new QNameResponse (featureType.getName ());
	}
	
	public List<InputAttributeResponse> getAttributes () {
		final List<InputAttributeResponse> result = new ArrayList<InputAttributeResponse> ();
		
		for (final FeatureTypeAttribute attr: featureType.getAttributes ()) {
			result.add (new InputAttributeResponse (
					attr.getType ().getJavaType ().toString (), 
					attr.getName(),
					attr.getName ().getLocalPart (),
					false
				));
			
			if (attr.getType() == GEOMETRY) {
				addOppervlakteFilterAttribute(result, attr);
			}
		}		
		return Collections.unmodifiableList (result);
	}

	private void addOppervlakteFilterAttribute(
			final List<InputAttributeResponse> result,
			final FeatureTypeAttribute attr) {
		
		final DefaultQName oppervlakteName = new DefaultQName(attr.getName().getNamespace(), attr.getName().getLocalPart() + "/area");
		result.add (new InputAttributeResponse (
				DOUBLE.getJavaType().toString(), 
				oppervlakteName,
				String.format ("Oppervlakte %s", attr.getName ().getLocalPart ()),
				true
			));
	}
}
