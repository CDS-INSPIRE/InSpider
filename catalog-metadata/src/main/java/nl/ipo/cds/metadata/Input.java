package nl.ipo.cds.metadata;

import java.io.InputStream;
import java.io.Reader;

import org.w3c.dom.ls.LSInput;

class Input implements LSInput {
	
	private String baseURI;
	private InputStream byteStream;
	private boolean certifiedText;
	private Reader characterStream;
	private String encoding;
	private String publicId;
	private String stringData;
	private String systemId;

	@Override
	public String getBaseURI() {
		return baseURI;
	}

	@Override
	public InputStream getByteStream() {
		return byteStream;
	}

	@Override
	public boolean getCertifiedText() {
		return certifiedText;
	}

	@Override
	public Reader getCharacterStream() {		
		return characterStream;
	}

	@Override
	public String getEncoding() { 
		return encoding;
	}

	@Override
	public String getPublicId() {
		return publicId;
	}

	@Override
	public String getStringData() {
		return stringData;
	}

	@Override
	public String getSystemId() { 
		return systemId;
	}

	@Override
	public void setBaseURI(String baseURI) {
		this.baseURI = baseURI;
	}

	@Override
	public void setByteStream(InputStream byteStream) {
		this.byteStream = byteStream;		
	}

	@Override
	public void setCertifiedText(boolean certifiedText) {
		this.certifiedText = certifiedText;		
	}

	@Override
	public void setCharacterStream(Reader characterStream) {
		this.characterStream = characterStream;		
	}

	@Override
	public void setEncoding(String encoding) {
		this.encoding = encoding;		
	}

	@Override
	public void setPublicId(String publicId) {
		this.publicId = publicId;		
	}

	@Override
	public void setStringData(String stringData) {
		this.stringData = stringData;		
	}

	@Override
	public void setSystemId(String systemId) {
		this.systemId = systemId;		
	}
}
