package nl.ipo.cds.admin.ba.controller.beans.mapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize
public class Mappings {

	private List<Mapping> mappings;

	public List<Mapping> getMappings () {
		return Collections.unmodifiableList (mappings);
	}

	public void setMappings (final List<Mapping> mappings) {
		this.mappings = new ArrayList<Mapping> (mappings);
	}
}
