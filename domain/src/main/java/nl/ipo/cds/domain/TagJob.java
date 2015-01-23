package nl.ipo.cds.domain;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.util.JSONWrappedObject;
import org.codehaus.jackson.type.TypeReference;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
		return (String)getParameter("tag");
	}

	/**
	 * Utility setter to set the tag in the job parameters.
	 * @param tag A string representing the tag label.
	 */
	public void setTag(String tag) {
		setParameter("tag", tag);
	}


	/**
	 * Utility getter to retrieve the table to copy the datasets that need to be tagged from.
	 * @return The name of the table.
	 */
	public String getSourceTable() {
		return (String)getParameter("table");
	}

	/**
	 * Set the table name of the table to copy the datasets that need to be tagged from.
	 * @param table The name of the table.
	 */
	public void setSourceTable(String table) {
		setParameter("table", table);
	}

	public TagJob() {
		super (PRIORITY);
	}
}
