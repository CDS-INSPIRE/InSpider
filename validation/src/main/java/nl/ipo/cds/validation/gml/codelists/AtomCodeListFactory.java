package nl.ipo.cds.validation.gml.codelists;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public class AtomCodeListFactory extends CachingCodeListFactory {

	private final static String ATOM_CONTENT_TYPE = "application/atom+xml";

	private final HttpClient httpClient;
	private final Map<String, String> codeSpaceUrlMap;
	private final String language;

	public AtomCodeListFactory() {
		this("en", Collections.<String, String> emptyMap());
	}

	public AtomCodeListFactory(final String language) {
		this(language, Collections.<String, String> emptyMap());
	}

	public AtomCodeListFactory(final Map<String, String> codeSpaceUrlMap) {
		this("en", codeSpaceUrlMap);
	}

	public AtomCodeListFactory(final String language, final Map<String, String> codeSpaceUrlMap) {
		this.language = language;
		this.codeSpaceUrlMap = new HashMap<String, String>(codeSpaceUrlMap);
		this.httpClient = new HttpClient();
	}

	@Override
	protected CodeList doGetCodeList(final String codeSpace) throws CodeListException {
		final String url;

		if (codeSpaceUrlMap.get(codeSpace) != null && !codeSpaceUrlMap.get(codeSpace).trim().isEmpty()) {
			url = codeSpaceUrlMap.get(codeSpace);
		} else {
			url = codeSpace;
		}

		// Fetch the codeList:
		final GetMethod get = new GetMethod(url);

		get.addRequestHeader("Accept", ATOM_CONTENT_TYPE);
		get.addRequestHeader("Accept-Language", language);

		try {
			httpClient.executeMethod(get);

			final SyndFeedInput input = new SyndFeedInput();
			final SyndFeed feed = input.build(new XmlReader(get.getResponseBodyAsStream()));

			return new AtomCodeList(codeSpace, feed);
		} catch (HttpException e) {
			throw new CodeListException(codeSpace, url, String.format("Unable to fetch code list: %s", url), e);
		} catch (IOException e) {
			throw new CodeListException(codeSpace, url, String.format("Unable to read code list: %s", url), e);
		} catch (IllegalArgumentException e) {
			throw new CodeListException(codeSpace, url, e);
		} catch (FeedException e) {
			throw new CodeListException(codeSpace, url, String.format("Invalid code list feed: %s", url), e);
		} finally {
			get.releaseConnection();
		}
	}

	private static class AtomCodeList implements CodeList {

		private final String codeSpace;
		private final Set<String> values = new HashSet<>();

		public AtomCodeList(final String codeSpace, final SyndFeed feed) {
			this.codeSpace = codeSpace;

			@SuppressWarnings("unchecked")
			final List<SyndEntry> entries = (List<SyndEntry>) feed.getEntries();

			for (final SyndEntry entry : entries) {
				if (entry.getUri().startsWith(feed.getUri())) {
					String id = entry.getUri().substring(feed.getUri().length());
					if (id.startsWith("/")) {
						id = id.substring(1);
					}
					values.add(id);
				} else {
					values.add(entry.getTitle());
				}
			}
		}

		@Override
		public String getCodeSpace() {
			return codeSpace;
		}

		@Override
		public Set<String> getCodes() {
			return Collections.unmodifiableSet(values);
		}

		@Override
		public boolean hasCode(final String code) {
			return values.contains(code);
		}
	}
}
