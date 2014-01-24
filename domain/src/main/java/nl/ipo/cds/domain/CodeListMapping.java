package nl.ipo.cds.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class CodeListMapping {

	@Id
	private String codeSpace;
	
	private String url;
	
	CodeListMapping () {
	}
	
	public CodeListMapping (final String codeSpace, final String url) {
		this.codeSpace = codeSpace;
		this.url = url;
	}

	public String getCodeSpace() {
		return codeSpace;
	}

	public String getUrl() {
		return url;
	}
	
	public void setUrl (final String url) {
		this.url = url;
	}
}
