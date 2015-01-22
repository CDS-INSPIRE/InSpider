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


	/**
	 * Utility getter to retrieve the tag from the job parameters.
	 * @return The tag label.
	 */
	public String getTag() {
		return getParameters();
	}

	/**
	 * Utility setter to set the tag in the job parameters.
	 * @param tag A string representing the tag label.
	 */
	public void setTag(String tag) {
		setParameters(tag);
	}

	public TagJob() {
		super (PRIORITY);
	}
}
