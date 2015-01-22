package nl.ipo.cds.domain;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Tags a dataset with a certain label. This copies the dataset into a separate table.
 * This functionality is only enabled for datasets that have can be tagged (Determined by ThemeConfig#isTaggable()).
 */
@Entity
@DiscriminatorValue ("TAG")
public class TagJob extends EtlJob {

	private final static int PRIORITY = 200;

	/* We use a global parameters text field to store our tag. This field can be used to store other parameters for future job types. */
	@Column(table = "etljob", name = "parameters")
	private String tag;

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public TagJob() {
		super (PRIORITY);
	}
}
