package nl.ipo.cds.etl.theme.schema;

import static nl.ipo.cds.etl.process.HarvesterMessageKey.METADATA_FEATURETYPE_HTTP_ERROR;
import static nl.ipo.cds.etl.process.HarvesterMessageKey.METADATA_FEATURETYPE_INVALID;

import java.io.IOException;
import java.io.InputStream;

import nl.ipo.cds.etl.featuretype.GMLFeatureTypeParser;
import nl.ipo.cds.etl.featuretype.ParseSchemaException;
import nl.ipo.cds.etl.process.DatasetMetadata;
import nl.ipo.cds.etl.process.HarvesterException;
import nl.ipo.cds.utils.HttpUtils;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.deegree.feature.types.AppSchema;

public class WfsSchemaHarvester implements SchemaHarvester {

	@Override
	public AppSchema parseApplicationSchema (final DatasetMetadata metadata) throws HarvesterException {
		final String url = metadata.getSchemaUrl ();
		try {
			final DefaultHttpClient httpClient = HttpUtils.createHttpClient ();
			final HttpGet httpRequest = new HttpGet (url);
			final HttpResponse httpResponse = httpClient.execute (httpRequest);
			final StatusLine statusLine = httpResponse.getStatusLine ();
			if (statusLine.getStatusCode () != 200) {
				throw new HarvesterException (METADATA_FEATURETYPE_HTTP_ERROR, url);
			}
			final HttpEntity entity = httpResponse.getEntity ();
			final InputStream inputStream = entity.getContent ();
			final Header contentTypeHeader = entity.getContentType ();
			final String encoding = contentTypeHeader != null ? getCharsetFromContentType (contentTypeHeader.getValue (), "UTF-8") : "UTF-8";			
			try {
				return parseApplicationSchema (metadata, inputStream, encoding);
			} finally {
				inputStream.close ();
			}
		} catch (IOException e) {
			throw new HarvesterException (e, METADATA_FEATURETYPE_HTTP_ERROR, url);
		}
	}

	private String getCharsetFromContentType (final String contentType, final String defaultCharset) {
		final String[] parts = contentType.split (";");
		for (final String part: parts) {
			if (!part.toLowerCase ().startsWith ("charset=")) {
				continue;
			}		
			return part.substring (8);
		}		
		return defaultCharset;
	}
	
	private AppSchema parseApplicationSchema (final DatasetMetadata metadata, final InputStream stream, final String encoding) throws HarvesterException {
		final GMLFeatureTypeParser featureTypeParser = new GMLFeatureTypeParser (null);
		final String featureTypeQName = metadata.getFeatureTypeName ();
		final String featureTypeName = featureTypeQName.substring (featureTypeQName.indexOf (':') + 1); 
		try {
			return featureTypeParser.parseApplicationSchema (stream, encoding);
		} catch (ParseSchemaException e) {
			throw new HarvesterException (e, METADATA_FEATURETYPE_INVALID, metadata.getSchemaUrl (), featureTypeName, e.getMessage());
		}
	}	
	
}
