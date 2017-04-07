/**
 * 
 */
package nl.ipo.cds.domain;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author annes
 *
 */

public class TagDTO {

	@NotEmpty
	public String thema;
	
	@NotEmpty
	public String tagId;

	public String getThema() {
		return thema;
	}

	public void setThema(String thema) {
		this.thema = thema;
	}

	public String getTagId() {
		return tagId;
	}

	public void setTagId(String tagId) {
		this.tagId = tagId;
	}
	
	
	
	
}
