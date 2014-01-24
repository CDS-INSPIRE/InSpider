package nl.ipo.cds.etl.theme.protectedSite;

import java.sql.Date;
import java.util.Arrays;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import nl.ipo.cds.etl.PersistableFeature;
import nl.ipo.cds.etl.db.annotation.Column;
import nl.ipo.cds.etl.db.annotation.Table;
import nl.ipo.cds.etl.theme.annotation.MappableAttribute;
import nl.ipo.cds.etl.xml.bind.GmlElement;

import org.deegree.geometry.Geometry;

@Table(name="protected_site", schema="bron")
@XmlRootElement(name="protectedSite")
public class ProtectedSite extends PersistableFeature {
	
	@Column
	@GmlElement
	private Geometry geometry;
	
	@Column(name="legal_foundation_date")
	@XmlElement
	private Date legalFoundationDate;
	
	@Column(name="legal_foundation_document")
	@XmlElement
	private String legalFoundationDocument;
	
	@Column(name="inspire_id")	
	@XmlElement
	private String inspireID;
	
	@Column(name="site_name")	
	@XmlElement
	private String siteName;
	
	@Column(name="site_designation")
	@XmlElement
	private String[] siteDesignation;
	
	@Column(name="site_protection_classification")
	@XmlElement
	private String[] siteProtectionClassification;

	@MappableAttribute
	public Geometry getGeometry() {
		return geometry;
	}

	@MappableAttribute
	public Date getLegalFoundationDate() {
		return legalFoundationDate == null ? null : new Date (legalFoundationDate.getTime ());
	}

	@MappableAttribute
	public String getLegalFoundationDocument() {
		return legalFoundationDocument;
	}

	@MappableAttribute
	public String getInspireID() {
		return inspireID;
	}

	@MappableAttribute
	public String getSiteName() {
		return siteName;
	}

	@MappableAttribute
	public String[] getSiteDesignation() {
		if (siteDesignation == null) {
			return null;
		}
		
		return Arrays.copyOf (siteDesignation, siteDesignation.length);
	}

	@MappableAttribute
	public String[] getSiteProtectionClassification() {
		if (siteProtectionClassification == null) {
			return null;
		}
		
		return Arrays.copyOf (siteProtectionClassification, siteProtectionClassification.length);
	}

	@MappableAttribute
	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	@MappableAttribute
	public void setLegalFoundationDate(Date legalFoundationDate) {
		this.legalFoundationDate = legalFoundationDate == null ? null : new Date (legalFoundationDate.getTime ());
	}

	@MappableAttribute
	public void setLegalFoundationDocument(String legalFoundationDocument) {
		this.legalFoundationDocument = legalFoundationDocument;
	}
	
	@MappableAttribute
	public void setInspireID(String inspireID) {
		this.inspireID = inspireID;
	}

	@MappableAttribute
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	@MappableAttribute
	public void setSiteDesignation(String[] siteDesignation) {
		if (siteDesignation == null) {
			this.siteDesignation = null;
		} else {
			this.siteDesignation = Arrays.copyOf (siteDesignation, siteDesignation.length);
		}
	}

	@MappableAttribute
	public void setSiteProtectionClassification(String[] siteProtectionClassification) {
		if (siteProtectionClassification == null) {
			this.siteProtectionClassification = null;
		} else {
			this.siteProtectionClassification = Arrays.copyOf (siteProtectionClassification, siteProtectionClassification.length);
		}
	}
}
