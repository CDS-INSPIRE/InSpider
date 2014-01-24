package nl.ipo.cds.admin.ba.attributemapping;

import static nl.ipo.cds.etl.process.HarvesterMessageKey.METADATA;
import static nl.ipo.cds.etl.process.HarvesterMessageKey.METADATA_FEATURETYPE_INVALID;
import static nl.ipo.cds.etl.process.HarvesterMessageKey.METADATA_FEATURETYPE_NOT_FOUND;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import nl.ipo.cds.domain.Dataset;
import nl.ipo.cds.domain.FeatureType;
import nl.ipo.cds.etl.featuretype.FeatureTypeNotFoundException;
import nl.ipo.cds.etl.featuretype.GMLFeatureTypeParser;
import nl.ipo.cds.etl.process.DatasetMetadata;
import nl.ipo.cds.etl.process.HarvesterException;
import nl.ipo.cds.etl.process.HarvesterFactory;
import nl.ipo.cds.etl.process.MetadataHarvester;
import nl.ipo.cds.etl.theme.ThemeConfig;
import nl.ipo.cds.etl.theme.ThemeDiscoverer;
import nl.ipo.cds.etl.theme.schema.SchemaHarvester;

import org.deegree.commons.xml.XMLProcessingException;
import org.deegree.feature.types.AppSchema;

public class FeatureTypeCache {

	private final HarvesterFactory harvesterFactory;

	private final ThemeDiscoverer themeDiscoverer;
	
	public FeatureTypeCache (final HarvesterFactory harvesterFactory, ThemeDiscoverer themeDiscoverer) {
		this.harvesterFactory = harvesterFactory;
		this.themeDiscoverer = themeDiscoverer;
	}
	
	private ConcurrentHashMap<String, CacheEntry> featureTypeCache = new ConcurrentHashMap<String, CacheEntry> ();
	
	private static class CacheEntry {
		public final Future<FeatureType> future;
		public final long timestamp;
		
		public CacheEntry (final Future<FeatureType> future, final long timestamp) {
			this.future = future;
			this.timestamp = timestamp;
		}
	}
	
	public FeatureType getFeatureType (final Dataset dataset) throws HarvesterException {
		final String uuid = dataset.getUuid ();
		
		// Evict old entries from the cache:
		final long evictTimestamp = new Date ().getTime () - 10 * 60 * 1000;
		final List<Map.Entry<String, CacheEntry>> entries = new ArrayList<Map.Entry<String,CacheEntry>> (featureTypeCache.entrySet ());
		Collections.sort (entries, new Comparator<Map.Entry<String, CacheEntry>> () {
			@Override
			public int compare (final Entry<String, CacheEntry> o1, final Entry<String, CacheEntry> o2) {
				return (int)Math.signum (o1.getValue ().timestamp - o2.getValue ().timestamp);
			}
		});
		for (int i = entries.size () - 1; i >= 0; -- i) {
			if (entries.get (i).getValue ().timestamp < evictTimestamp) {
				featureTypeCache.remove (entries.get (i).getKey ());
				entries.remove (i);
			}
		}
		while (entries.size () > 10) {
			featureTypeCache.remove (entries.get (0).getKey ());
			entries.remove (0);
		}
		
		ThemeConfig<?> themeConfig = themeDiscoverer.getThemeConfiguration(dataset.getDatasetType().getThema().getNaam());
		final SchemaHarvester schemaHarvester = themeConfig.getSchemaHarvester();
		
		// Create a task that will load the feature type if it wasn't previously cached:
		final FutureTask<FeatureType> getFeatureTypeTask = new FutureTask<FeatureType> (new Callable<FeatureType> () {
			@Override
			public FeatureType call () throws Exception {
				String schemaUrl = null;
				String featureTypeName = null;
				try {
					MetadataHarvester harvester = harvesterFactory.createMetadataHarvester ();
					DatasetMetadata metadata = harvester.parseMetadata (uuid);
					if (metadata == null) {
						throw new HarvesterException (METADATA, schemaUrl, featureTypeName); 
					}				
					AppSchema appSchema = schemaHarvester.parseApplicationSchema(metadata);
					String ftNameFromMetadata = metadata.getFeatureTypeName();
					if (ftNameFromMetadata != null) {
						ftNameFromMetadata = ftNameFromMetadata.substring(ftNameFromMetadata.indexOf(':') + 1);
					}
					String ftLocalName = determineLocalFtName (appSchema, ftNameFromMetadata);
					return new GMLFeatureTypeParser().parseSchema(appSchema, ftLocalName);
				} catch (FeatureTypeNotFoundException e) {
					throw new HarvesterException (e, METADATA_FEATURETYPE_NOT_FOUND, schemaUrl, featureTypeName);
				} catch (XMLProcessingException e) {
					throw new HarvesterException (e, METADATA_FEATURETYPE_INVALID, schemaUrl, featureTypeName, e.getMessage());
				}	
			}

		});
		
		// Try to insert the task in the cache map. If it succeeds, run the task. Otherwise wait for the previous task
		// to complete at this point:
		final CacheEntry previousTask = featureTypeCache.putIfAbsent (uuid, new CacheEntry (getFeatureTypeTask, new Date ().getTime ()));
		try {
			if (previousTask != null) {
				return previousTask.future.get ();
			} else {
				getFeatureTypeTask.run ();
				return getFeatureTypeTask.get ();
			}
		} catch (ExecutionException e) {
			if (e.getCause () instanceof HarvesterException) {
				final HarvesterException harvesterException = (HarvesterException)e.getCause ();
				throw harvesterException;
			} else {
				throw new RuntimeException (e);
			}
		} catch (InterruptedException e) {
			throw new RuntimeException (e);
		}
	}
	
	private String determineLocalFtName(AppSchema appSchema, String ftNameFromMetadata) throws FeatureTypeNotFoundException {
		List<org.deegree.feature.types.FeatureType> fts = appSchema.getFeatureTypes(null, false, false);
		if (fts.size() == 1) {
			return fts.get (0).getName().getLocalPart();
		}
		for (org.deegree.feature.types.FeatureType ft : fts ) {
			if (ft.getName().getLocalPart().equals(ftNameFromMetadata)) {
				return ft.getName().getLocalPart();
			}
		}
		throw new FeatureTypeNotFoundException(ftNameFromMetadata, appSchema);
	}	
}
