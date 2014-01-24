package nl.ipo.cds.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

@Entity
public class AttributeExpression extends FilterExpression {

	@Column (name = "attribute_name")
	@NotNull
	private String attributeName;
	
	@Column (name = "attribute_type")
	@NotNull
	private AttributeType attributeType;

	public String getAttributeName () {
		return attributeName;
	}

	public void setAttributeName (final String attributeName) {
		this.attributeName = attributeName;
	}

	public AttributeType getAttributeType () {
		return attributeType;
	}

	public void setAttributeType (final AttributeType attributeType) {
		this.attributeType = attributeType;
	}
}
