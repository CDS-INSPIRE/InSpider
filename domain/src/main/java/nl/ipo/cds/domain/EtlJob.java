package nl.ipo.cds.domain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SecondaryTable;
import javax.persistence.Transient;

import nl.idgis.commons.jobexecutor.AbstractJob;
import nl.idgis.commons.jobexecutor.JobTypeIntrospector;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

@Entity
@SecondaryTable (name = "etljob"/*, pkJoinColumns = @PrimaryKeyJoinColumn (name = "id")*/)
public abstract class EtlJob extends AbstractJob {

	//@Id
	//private Long id;
	
	@ManyToOne
	@JoinColumn (table = "etljob")
	private DatasetType datasettype;

	@ManyToOne
	@JoinColumn (table = "etljob")
	private Bronhouder bronhouder;

	@Column (table = "etljob")
	private String uuid;
	
	@Column(name = "metadata_update_datum", table = "etljob")
	private Timestamp metadataUpdateDatum;

	@Column(table = "etljob", nullable = false, columnDefinition="bool default false")
	private Boolean verversen = false; // default waarde ipv null
	
	@Column(name = "metadata_url", table = "etljob")
	private String metadataUrl;

	@Column( name = "dataset_url", table = "etljob")
	private String datasetUrl;

	@Column (table = "etljob")
	private String wfsUrl;

	@Column( name = "feature_count", table = "etljob")
	private Integer featureCount;

	@Column( name = "geometry_error_count", table = "etljob")
	private Integer geometryErrorCount;
	
	@Column( name = "force_execution", table = "etljob", nullable = false, columnDefinition="bool default false" )
	private Boolean forceExecution = false;



	/* We use a global parameters text field to store all kinds of job parameters. This field can be used to store other parameters for future job types. */
	@Column(table = "etljob", name = "parameters", columnDefinition="TEXT")
	private String parameters;

	@Transient
	private Integer maxFeatures = null;
	
	@Transient
	private boolean ignoreInvalidMapping = false;

	protected EtlJob () {
	}
	
	public EtlJob (final int priority) {
		super (priority);
	}
	
	/**
	 * @return the datasetType
	 */
	public DatasetType getDatasetType() {
		return datasettype;
	}

	/**
	 * @param datasetType
	 *            the datasetType to set
	 */
	public void setDatasetType(DatasetType datasetType) {
		this.datasettype = datasetType;
	}

	/**
	 * @return the bronhouder
	 */
	public Bronhouder getBronhouder() {
		return bronhouder;
	}

	/**
	 * @param bronhouder the bronhouder to set
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
	 * @return the metadataUpdateDatum
	 */
	public Timestamp getMetadataUpdateDatum() {
		return metadataUpdateDatum;
	}

	/**
	 * @param metadataUpdateDatum
	 *            the metadataUpdateDatum to set
	 */
	public void setMetadataUpdateDatum(Timestamp metadataUpdateDatum) {
		this.metadataUpdateDatum = metadataUpdateDatum;
	}

	/**
	 * @return the verversen
	 */
	public Boolean getVerversen() {
		return verversen;
	}

	/**
	 * @param verversen
	 *            the verversen to set
	 */
	public void setVerversen(Boolean verversen) {
		this.verversen = verversen;
	}

	/**
	 * @return the wfsUrl
	 */
	public String getWfsUrl() {
		return wfsUrl;
	}

	/**
	 * @param wfsUrl
	 *            the wfsUrl to set
	 */
	public void setWfsUrl(String wfsUrl) {
		this.wfsUrl = wfsUrl;
	}

	/**
	 * @return the metadataUrl
	 */
	public String getMetadataUrl() {
		return metadataUrl;
	}

	/**
	 * @param metadataUrl the metadataUrl to set
	 */
	public void setMetadataUrl(String metadataUrl) {
		this.metadataUrl = metadataUrl;
	}

	/**
	 * @return the datasetUrl
	 */
	public String getDatasetUrl() {
		return datasetUrl;
	}

	/**
	 * @param datasetUrl
	 *            the datasetUrl to set
	 */
	public void setDatasetUrl(String datasetUrl) {
		this.datasetUrl = datasetUrl;
	}

	private Map<String,Object> readParameters() {
		if (parameters == null || parameters.isEmpty()) {
			return new HashMap<>();
		}
		ObjectMapper mapper = new ObjectMapper();
		ByteArrayInputStream bis = new ByteArrayInputStream(parameters.getBytes());
		try {
			Map<String, Object> params = mapper.readValue(bis, new TypeReference<Map<String, Object>>() {});
			bis.close();
			return params;

		} catch (IOException e) {
			throw new RuntimeException("Error reading job parameters from string field.", e);
		}
	}

	private void writeParameters(Map<String, Object> params) {
		ObjectMapper mapper = new ObjectMapper();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			mapper.writeValue(bos, params);
			bos.flush();
			parameters = bos.toString();
			bos.close();
		} catch (IOException e) {
			throw new RuntimeException("Error writing job parameters to string field.", e);
		}
	}

	protected void setParameter(String key, Object value) {
		Map<String, Object> params = readParameters();
		params.put(key, value);
		writeParameters(params);
	}

	protected Object getParameter(String key) {
		return readParameters().get(key);
	}

	public Integer getFeatureCount() {
		return featureCount;
	}

	public void setFeatureCount(Integer featureCount) {
		this.featureCount = featureCount;
	}

	public Integer getGeometryErrorCount() {
		return geometryErrorCount;
	}

	public void setGeometryErrorCount(Integer geometryErrorCount) {
		this.geometryErrorCount = geometryErrorCount;
	}
	
	@Override
	public String toString() {
		return "## Job (id: " + getId () + ", creatie: " + getCreateTime () + ", status: " + getStatus ()
				+ ", verversen: " + verversen + ", jobType: " + JobTypeIntrospector.getJobTypeName (this) + ", uuid: " + uuid + ", type: " + datasettype + ", bronhouder: " + bronhouder  + ")";
	}

	public boolean isForceExecution () {
		return forceExecution ;
	}

	public void setForceExecution (final boolean forceExecution) {
		this.forceExecution = forceExecution;
	}

	public Integer getMaxFeatures () {
		return maxFeatures;
	}

	public void setMaxFeatures (final Integer maxFeatures) {
		this.maxFeatures = maxFeatures;
	}

	public boolean isIgnoreInvalidMapping() {
		return ignoreInvalidMapping;
	}

	public void setIgnoreInvalidMapping(boolean ignoreInvalidMapping) {
		this.ignoreInvalidMapping = ignoreInvalidMapping;
	}

}
