package nl.ipo.cds.domain;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import nl.idgis.commons.jobexecutor.AbstractJob;
import nl.idgis.commons.jobexecutor.JobLogger.LogLevel;

@Entity
@SequenceGenerator(name="ORDER_SEQ",allocationSize=1,initialValue=1,sequenceName="joblog_sequence")
@Table (name = "joblog")
public class JobLog implements Identity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="ORDER_SEQ")
	private Long id;
	
	@ManyToOne
	private AbstractJob job;
	
	@Column
	private String key;
	
	@Column(columnDefinition="text")
	private String message;
	
	@Column
	private Timestamp time;

	@Column
	private LogLevel logLevel;

	@Column
	private Double x;

	@Column
	private Double y;
	
	@Column
	private String gmlId;
	
	@Column
	private String context;
	
	public JobLog() {
		super();
	}

	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public AbstractJob getJob() {
		return job;
	}

	public void setJob(AbstractJob job) {
		this.job = job;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Timestamp getTime() {
		return time;
	}

	public void setTime(Timestamp time) {
		this.time = time;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public LogLevel getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	public Double getX() {
		return x;
	}

	public void setX(Double x) {
		this.x = x;
	}

	public Double getY() {
		return y;
	}

	public void setY(Double y) {
		this.y = y;
	}

	public String getGmlId() {
		return gmlId;
	}

	public void setGmlId(String gmlId) {
		this.gmlId = gmlId;
	}
	
	public void setContext (final String context) {
		this.context = context;
	}
	
	public String getContext () {
		return context;
	}
}
