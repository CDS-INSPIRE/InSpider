package nl.ipo.cds.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;

@Entity
public class ValueExpression extends FilterExpression {

	public enum ValueType {
		STRING,
		INTEGER,
		DOUBLE,
		DATE,
		TIME,
		DATE_TIME,
		BOOLEAN
	}
	
	@Column (name = "value_type")
	@NotNull
	@Enumerated (EnumType.STRING)
	private ValueType valueType;
	
	@Column (name = "string_value")
	@NotNull
	private String stringValue;
	
	public ValueType getValueType () {
		return valueType;
	}
	
	public void setValueType (final ValueType valueType) {
		this.valueType = valueType;
	}

	public String getStringValue () {
		return stringValue;
	}

	public void setStringValue (final String stringValue) {
		this.stringValue = stringValue;
	}
}
