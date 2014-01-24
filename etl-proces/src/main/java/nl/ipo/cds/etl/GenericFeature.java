package nl.ipo.cds.etl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GenericFeature implements Feature {

	private final String id;
	private final Map<String, Object> values;
	
	public GenericFeature (final String id, final Map<String, Object> values) {
		this.id = id;
		this.values = new HashMap<String, Object> (values);
	}

	public Map<String, Object> getValues () {
		return Collections.unmodifiableMap (values);
	}
	
	public Object get (final String propertyName) {
		return values.get (propertyName);
	}
	
	public boolean hasProperty (final String propertyName) {
		return values.containsKey (propertyName);
	}

	@Override
	public String getId () {
		return id;
	}
}
