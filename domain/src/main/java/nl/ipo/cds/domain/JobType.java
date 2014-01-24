/**
 * 
 */
package nl.ipo.cds.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * Jobs zijn van een bepaald type. <br>
 * Een job_type heeft impliciet de informatie in zich welke job_fasen doorlopen
 * moeten worden.<br>
 * M.a.w. de programmatuur weet welke fasen horen bij een bepaald job_type.<br
 * . <em>Stamtabel<em>.
 * 
 * @author Rob
 * 
 */
@Entity
@Table(name="jobtype")
public class JobType implements Identity {	

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column(unique=true, nullable=false)	
	private String naam;

	@NotNull
	private Integer prioriteit;

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
	 * @return the naam
	 */
	public String getNaam() {
		return naam;
	}

	/**
	 * @param naam the naam to set
	 */
	public void setNaam(String naam) {
		this.naam = naam;
	}

	/**
	 * @return the prioriteit
	 */
	public Integer getPrioriteit() {
		return prioriteit;
	}

	/**
	 * @param prioriteit
	 *            the prioriteit to set
	 */
	public void setPrioriteit(Integer prioriteit) {
		this.prioriteit = prioriteit;
	}

	public String toString(){
		return ReflectionToStringBuilder.toString(this);
//		return "## JobType (id: " + id + ", naam: " + naam + ", prioriteit: " + prioriteit + ")";		
	}

}
