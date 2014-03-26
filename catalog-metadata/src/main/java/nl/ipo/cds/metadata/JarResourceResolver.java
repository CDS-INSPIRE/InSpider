package nl.ipo.cds.metadata;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

class JarResourceResolver implements LSResourceResolver {
	
	private final String basePath;
	private final Map<String, String> localPaths = new HashMap<String, String>();
	
	JarResourceResolver(final String basePath) {
		this.basePath = basePath;
	}
	
	void addPath(final String remotePath, final String localPath) {
		localPaths.put(remotePath, basePath + "/" + localPath);
	}

	@Override
	public LSInput resolveResource(final String type, final String namespaceURI, final String publicId, final String systemId, final String baseURI) { 
		InputStream inputStream = null;
		
		for(final Map.Entry<String, String> pathMapping : localPaths.entrySet()) {
			final String remotePath = pathMapping.getKey();
			
			if(baseURI.startsWith(remotePath)) {
				final String localPath = pathMapping.getValue();
				final String filePart = baseURI.substring(remotePath.length());				
				final String resourcePath = localPath + filePart;
				
				inputStream = getClass().getResourceAsStream(resourcePath);
			}
		}
		
		if(inputStream == null) {
			throw new IllegalStateException("XML Schema resource missing: " + baseURI);
		}
		
		final Input input = new Input();
		input.setPublicId(publicId);
		input.setSystemId(systemId);
		input.setBaseURI(baseURI);
		
		return input;
	}
}
