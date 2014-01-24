package nl.ipo.cds.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

@Entity
public class DatasetFilter {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@ManyToOne
	private Dataset dataset;

	@OneToOne
	private FilterExpression rootExpression;
	
	private boolean isValid;

	public boolean isValid () {
		return isValid;
	}

	public void setValid (final boolean isValid) {
		this.isValid = isValid;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public FilterExpression getRootExpression() {
		return rootExpression;
	}

	public void setRootExpression(FilterExpression rootExpression) {
		this.rootExpression = rootExpression;
	}

	public Long getId() {
		return id;
	}
}
