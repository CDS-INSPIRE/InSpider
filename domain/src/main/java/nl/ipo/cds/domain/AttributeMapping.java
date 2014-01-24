package nl.ipo.cds.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

@Entity
public class AttributeMapping {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@ManyToOne
	private Dataset dataset;
	
	private String attributeName;

	@OneToOne
	private MappingOperation rootOperation;
	
	@NotNull
	private boolean valid = false;
	
	AttributeMapping () {
	}
	
	public AttributeMapping (final Dataset dataset, final String attributeName) {
		this.dataset = dataset;
		this.attributeName = attributeName;
	}

	public Long getId () {
		return id;
	}
	
	public MappingOperation getRootOperation() {
		return rootOperation;
	}

	public void setRootOperation(MappingOperation rootOperation) {
		this.rootOperation = rootOperation;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}
}
