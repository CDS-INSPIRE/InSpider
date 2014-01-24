/**
 * 
 */
package nl.ipo.cds.domain;

import static javax.persistence.EnumType.STRING;
import static nl.ipo.cds.domain.RefreshPolicy.IF_MODIFIED_METADATA;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * DatasetType wordt ingevuld aangeleverd met de zes protected site typen.<br>
 * Later kan uitgebreid worden met andere (ook niet Inspire) datasets.<br>
 * <em>Stamtabel<em>.
 * 
 * @author Rob
 * 
 */
@Entity
//@Table(name="datasettype", schema="manager")
public class DatasetType implements Identity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@ManyToOne
	private Thema thema;

	@Column(unique=true, nullable=false)
	private String naam;

	@Enumerated(STRING)
	@Column(nullable=false, columnDefinition="text default 'IF_MODIFIED_METADATA'")
	private RefreshPolicy refreshPolicy = IF_MODIFIED_METADATA;

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
	 * @return the thema
	 */
	public Thema getThema() {
		return thema;
	}

	/**
	 * @param thema
	 *            the thema to set
	 */
	public void setThema(Thema thema) {
		this.thema = thema;
	}

	/**
	 * @return the naam
	 */
	public String getNaam() {
		return naam;
	}

	/**
	 * @param naam
	 *            the naam to set
	 */
	public void setNaam(String naam) {
		this.naam = naam;
	}

	/**
	 * @return the refresh policy, never <code>null</code>
	 */
	public RefreshPolicy getRefreshPolicy() {
		return refreshPolicy;
	}

	/**
	 * @param refreshPolicy the refresh policy to set, must not be <code>null</code> 
	 */
	public void setRefreshPolicy(RefreshPolicy refreshPolicy) {
		this.refreshPolicy = refreshPolicy;
	}

	@Override
	public String toString(){
		return "## DatasetType (id: " + id + ", naam: " + naam + ", thema: " + thema + ", refreshPolicy: " + refreshPolicy;		
	}

}
