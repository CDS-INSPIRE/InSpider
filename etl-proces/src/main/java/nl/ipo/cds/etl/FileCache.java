/**
 * 
 */
package nl.ipo.cds.etl;

import java.net.URL;

import javax.xml.stream.XMLStreamReader;

import nl.ipo.cds.domain.EtlJob;

/**
 * FileCache.
 * @author Rob
 *
 */
public interface FileCache {

	/**
	 * Store the content of an OMElement to file cache.
	 * @param job
	 * @param xmlStream
	 */
	public URL storeToCache(EtlJob job, XMLStreamReader xmlStream);

	/**
	 * Remove the file belonging to job, that was previously stored.
	 * @param job 
	 * @return true if it was successfully removed from cache.
	 */
	public boolean removeFromCache(EtlJob job);
	
	/**
	 * Remove from cache all directories and files from a certain point e.g. /cacheroot/bronhouder/uuid/*.*<br/>
	 * Note: this acts as rm -rf  
	 * @param job
	 * @return true if the tree was successfully removed from cache.
	 */
	public boolean removeFromCacheRecursive(EtlJob job);
	
	public String getFilename(EtlJob job) ;
	public String getFiledir(EtlJob job);
}
