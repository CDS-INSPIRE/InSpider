package nl.ipo.cds.admin.ba.controller.gebruikersbeheer.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.ipo.cds.domain.Bronhouder;
import nl.ipo.cds.domain.Thema;

/**
 * Container bean for a bronhouder and associated themas (from the BronhouderThema entity). 
 */
public class BronhouderThemas {
	private final Bronhouder bronhouder;
	private final List<Thema> themas;
	
	/**
	 * @param bronhouder	The bronhouder.
	 * @param themas		All themes that are associated with the bronhouder.
	 */
	public BronhouderThemas (final Bronhouder bronhouder, final List<Thema> themas) {
		if (bronhouder == null) {
			throw new NullPointerException ("bronhouder cannot be null");
		}
		
		this.bronhouder = bronhouder;
		this.themas = themas == null || themas.isEmpty () ? Collections.<Thema>emptyList () : new ArrayList<Thema> (themas);
	}

	/**
	 * @return The bronhouder
	 */
	public Bronhouder getBronhouder () {
		return bronhouder;
	}

	/**
	 * 
	 * @return All themes associated with the bronhouder.
	 */
	public List<Thema> getThemas () {
		return Collections.unmodifiableList (themas);
	}
}
