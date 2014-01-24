/**
 *
 */
package nl.ipo.cds.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Dataset koppelt een Job aan Bronhouder en DatasetType.<br>
 * <em>Stamtabel<em>.
 *
 * @author Rob
 *
 */
@Entity
//@Table(name="dataset", schema="manager")
public class Dataset implements Identity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne
	private DatasetType type;

	@ManyToOne
	private Bronhouder bronhouder;

	@NotNull
	private String uuid;

	@NotNull
	private Boolean actief = true;

	private String naam;

	/**
	 * @return the id
	 */
	@Override
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the datasetType
	 */
	public DatasetType getDatasetType() {
		return type;
	}

	/**
	 * @param datasetType
	 *            the datasetType to set
	 */
	public void setDatasetType(DatasetType datasetType) {
		this.type = datasetType;
	}

	/**
	 * @return the bronhouder
	 */
	public Bronhouder getBronhouder() {
		return bronhouder;
	}

	/**
	 * @param bronhouder
	 *            the bronhouder to set
	 */
	public void setBronhouder(Bronhouder bronhouder) {
		this.bronhouder = bronhouder;
	}

	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * @param uuid the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * @return the actief
	 */
	public Boolean getActief() {
		return actief;
	}

	/**
	 * @param actief the actief to set
	 */
	public void setActief(Boolean actief) {
		this.actief = actief;
	}

	public String getNaam() {
		return naam;
	}

	public void setNaam(String naam) {
		this.naam = naam;
	}

	public String toString(){
		return "## Dataset <Actief="+actief+">(id: " + id + ", uuid: " + uuid + ", type: " + type + ", bronhouder: " + bronhouder + ")";
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Dataset == false)
		{
			return false;
		}
		if (this == obj)
		{
			return true;
		}
		final Dataset otherObject = (Dataset) obj;

		return new EqualsBuilder()
		.append(this.id, otherObject.id)
		.isEquals();
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder()
		.append(this.id)
		.toHashCode();
	}
}
