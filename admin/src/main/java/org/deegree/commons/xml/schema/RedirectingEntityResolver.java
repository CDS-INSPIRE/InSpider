package org.deegree.commons.xml.schema;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import nl.ipo.cds.utils.UrlUtils;

import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Overrides the standard deegree-provided class to work around SNI-related problems when fetching GML application schemas:
 * 
 * http://stackoverflow.com/questions/7615645/ssl-handshake-alert-unrecognized-
 * name-error-since-upgrade-to-java-1-7-0
 */
public class RedirectingEntityResolver implements XMLEntityResolver {

	private static final Logger LOG = LoggerFactory
			.getLogger(RedirectingEntityResolver.class);

	private static final String SCHEMAS_OPENGIS_NET_URL = "http://schemas.opengis.net/";

	private static final String ROOT = "/META-INF/SCHEMAS_OPENGIS_NET/";

	private static final URL baseURL;

	static {
		baseURL = RedirectingEntityResolver.class.getResource(ROOT);
		if (baseURL == null) {
			LOG.warn("'"
					+ ROOT
					+ "' could not be found on the classpath. Schema references to 'http://schemas.opengis.net' will not be redirected, but fetched from their original location.  ");
		}
	}

	/**
	 * Redirects the given entity URL, returning a local URL if available.
	 * 
	 * @param systemId
	 *            entity URL, must not be <code>null</code>
	 * @return redirected URL, identical to input if it cannot be redirected,
	 *         never <code>null</code>
	 */
	public String redirect(String systemId) {
		if (systemId.startsWith(SCHEMAS_OPENGIS_NET_URL)) {
			String localPart = systemId.substring(SCHEMAS_OPENGIS_NET_URL
					.length());
			URL u = RedirectingEntityResolver.class.getResource(ROOT
					+ localPart);
			if (u != null) {
				LOG.debug("Local hit: " + systemId);
				return u.toString();
			}
		} else if (systemId.equals("http://www.w3.org/2001/xml.xsd")) {
			// workaround for schemas that include the xml base schema...
			return RedirectingEntityResolver.class.getResource("/w3c/xml.xsd")
					.toString();
		} else if (systemId.equals("http://www.w3.org/1999/xlink.xsd")) {
			// workaround for schemas that include the xlink schema...
			return RedirectingEntityResolver.class
					.getResource("/w3c/xlink.xsd").toString();
		}
		return systemId;
	}

	@Override
	public XMLInputSource resolveEntity(XMLResourceIdentifier identifier)
			throws XNIException, IOException {

		String systemId = identifier.getExpandedSystemId();
		String redirectedSystemId = systemId != null ? redirect(systemId)
				: null;
		LOG.debug("'" + systemId + "' -> '" + redirectedSystemId + "'");
		if (redirectedSystemId != null
				&& redirectedSystemId.startsWith("https:")) {
			InputStream is = UrlUtils.open(new URL(redirectedSystemId));
			return new XMLInputSource(null, systemId, redirectedSystemId, is,
					null);
		}
		return new XMLInputSource(null, systemId, redirectedSystemId);
	}

}
