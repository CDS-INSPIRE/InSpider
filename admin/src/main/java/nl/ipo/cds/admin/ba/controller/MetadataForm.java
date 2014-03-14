package nl.ipo.cds.admin.ba.controller;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import nl.ipo.cds.domain.MetadataDocumentType;

public class MetadataForm {
	
	public static interface Add {}
	public static interface Modify {}
	
	@NotNull(groups=Modify.class)
	private Long id;

	@NotNull(groups={Add.class, Modify.class})
	private Long themeId;
	
	@NotNull(groups={Add.class, Modify.class})
	private MetadataDocumentType documentType;
	
	@Pattern(groups=Add.class, regexp=".*\\.xml")	
	private String documentName;
	
	private String documentContent;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public Long getThemeId() {
		return themeId;
	}

	public void setThemeId(Long themeId) {
		this.themeId = themeId;
	}

	public MetadataDocumentType getDocumentType() {
		return documentType;
	}

	public void setDocumentType(MetadataDocumentType documentType) {
		this.documentType = documentType;
	}

	public String getDocumentContent() {
		return documentContent;
	}

	public void setDocumentContent(String documentContent) {
		this.documentContent = documentContent;
	}

	public String getDocumentName() {
		return documentName;
	}

	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}

	@Override
	public String toString() {
		return "MetadataForm [id=" + id + ", theme=" + themeId
				+ ", documentType=" + documentType + ", documentName="
				+ documentName + ", documentContent=" + documentContent + "]";
	}	
}
