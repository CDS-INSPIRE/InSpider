package nl.ipo.cds.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Entity
public class OperatorExpression extends FilterExpression {

	public enum OperatorType {
		AND,
		OR,
		
		EQUALS,
		NOT_EQUALS,
		LESS_THAN,
		LESS_THAN_EQUAL,
		GREATER_THAN,
		GREATER_THAN_EQUAL,
		
		LIKE,
		
		IN,
		
		NOT_NULL
	}

	@Column (name = "operator_type")
	@Enumerated (EnumType.STRING)
	private OperatorType operatorType;
	
	@Column (name = "case_sensitive")
	private boolean caseSensitive;

	public OperatorType getOperatorType () {
		return operatorType;
	}

	public void setOperatorType (final OperatorType operatorType) {
		this.operatorType = operatorType;
	}

	public boolean isCaseSensitive () {
		return caseSensitive;
	}

	public void setCaseSensitive (final boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}
}
