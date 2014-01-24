/**
 * 
 */
package nl.ipo.cds.domain.metadata;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.hibernate.validator.constraints.NotBlank;

/**
 * @author eshuism
 * 17 jan 2012
 */
@Embeddable
public class Keyword {

	@Column(nullable=false, columnDefinition="text")
	@NotBlank(message="Verplicht")
	private String value;
	
	@Column(nullable=true, columnDefinition="text")
	private String codeSpace;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getCodeSpace() {
		return codeSpace;
	}

	public void setCodeSpace(String codeSpace) {
		this.codeSpace = codeSpace;
	}
}
