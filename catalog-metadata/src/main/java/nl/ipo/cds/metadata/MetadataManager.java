package nl.ipo.cds.metadata;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import nl.ipo.cds.domain.MetadataDocumentType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

public class MetadataManager {
	
	private static final Log logger = LogFactory.getLog(MetadataManager.class);

	protected final File metadataFolder;
	
	public MetadataManager(final File metadataFolder) throws IOException {
		final String metadataFolderPath = metadataFolder.getCanonicalPath();
		
		logger.debug("Constructing MetadataManager with metadata folder: " + metadataFolderPath);
		
		if(!metadataFolder.exists()) {
			throw new IllegalArgumentException("Metadata folder doesn't exist: " + metadataFolderPath);
		}
		
		if(!metadataFolder.isDirectory()) {
			throw new IllegalArgumentException("Metadata folder parameter should refer to a directory: " + metadataFolderPath);
		}
		
		this.metadataFolder = metadataFolder;
	}

	protected File getDocumentFile(final String documentName) {
		return new File(metadataFolder, documentName);	
	}
	
	public synchronized void updateMetadata(final String documentName, final MetadataDocumentType documentType, final String dateTime) throws IOException {
		logger.debug("Updating metadata document: '" + documentName + "' dateTime: " + dateTime);
		
		final File f = getDocumentFile(documentName);
		if(!f.exists()) {
			throw new IllegalArgumentException("Metadata document doesn't exists.");
		}
		
		try {		
			XMLRewriter rewriter = createRewriter(new FileInputStream(f));
			
			updateMetadata(documentType, dateTime, rewriter);
			
			FileOutputStream outputStream = new FileOutputStream(f);
			rewriter.write(outputStream);
			outputStream.close();
		} catch(Exception e) {
			throw new IOException("Couldn't update metadata document");
		}
	}

	private void updateMetadata(final MetadataDocumentType documentType,
			final String dateTime, XMLRewriter rewriter)
			throws XPathExpressionException {
		if(documentType == MetadataDocumentType.SERVICE) {
			updateServiceMetadata(rewriter, dateTime);
		} else if(documentType == MetadataDocumentType.DATASET){				
			updateDatasetMetadata(rewriter, dateTime);
		} else {
			throw new IllegalArgumentException("Unknown type: " + documentType);
		}
	}	
	
	public synchronized byte[] retrieveDocument(final String documentName) throws IOException {
		final File f = getDocumentFile(documentName);
		if(f.exists()) {
			final FileInputStream fis = new FileInputStream(f);			
			return IOUtils.toByteArray(fis);
		} else {
			throw new IllegalArgumentException("Document doesn't exists: " + documentName);
		}
	}
	
	public synchronized boolean documentExists(final String documentName) {
		final File f = getDocumentFile(documentName);
		return f.exists();
	}
	
	public boolean validateDocument(final byte[] bytes, final MetadataDocumentType documentType) {
		try {
			updateMetadata(documentType, "", createRewriter(new ByteArrayInputStream(bytes)));
		} catch(Exception e) {
			logger.debug("Not valid", e);
			
			return false;
		}
		
		return true;
	}

	public synchronized void storeDocument(final String documentName, final byte[] bytes) throws ParserConfigurationException, SAXException, IOException, TransformerException {
		final File f = getDocumentFile(documentName);
		final FileOutputStream fos = new FileOutputStream(f);
		fos.write(bytes);
		fos.close();
	}
	
	public synchronized void deleteDocument(final String documentName) {
		final File f = new File(metadataFolder, documentName);
		if(f.exists()) {
			f.delete();
		} else {
			throw new IllegalArgumentException("Document doesn't exists: " + documentName);
		}
	}
	
	public Set<String> listDocuments() {
		final Set<String> documents = new HashSet<String>();
		
		for(final String fileName : metadataFolder.list()) {
			if(fileName.toLowerCase().endsWith(".xml")) {
				documents.add(fileName);
			}
		}
		
		return documents;
	}
	
	protected XMLRewriter createRewriter(final InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
		XMLRewriter rewriter = new XMLRewriter(inputStream);
		rewriter.addNamespace("gmd", "http://www.isotc211.org/2005/gmd");
		rewriter.addNamespace("gco", "http://www.isotc211.org/2005/gco");
		rewriter.addNamespace("srv", "http://www.isotc211.org/2005/srv");
		rewriter.addNamespace("gml", "http://www.opengis.net/gml");
		return rewriter;
	}
	
	protected void updateServiceMetadata(final XMLRewriter rewriter, final String dateTime) throws XPathExpressionException {
		rewriter.modify("/gmd:MD_Metadata/gmd:identificationInfo/srv:SV_ServiceIdentification/srv:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:begin/gml:TimeInstant/gml:timePosition", dateTime); 
	}
	
	protected void updateDatasetMetadata(final XMLRewriter rewriter, final String dateTime) throws XPathExpressionException {		
		rewriter.modify("/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:date/gmd:CI_Date[gmd:dateType/gmd:CI_DateTypeCode/@codeListValue = 'revision']/gmd:date/gco:DateTime", dateTime);
		rewriter.modify("/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:extent/gmd:EX_Extent/gmd:temporalElement/gmd:EX_TemporalExtent/gmd:extent/gml:TimePeriod/gml:begin/gml:TimeInstant/gml:timePosition", dateTime);
	}
}
