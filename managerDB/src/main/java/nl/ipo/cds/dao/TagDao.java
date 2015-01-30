/**
 * 
 */
package nl.ipo.cds.dao;


/**
 * @author annes
 *
 * DAO for methods that concern the tag functionality
 *
 */
public interface TagDao {

	Boolean doesTagExist(String tag, String schemaName, String tableName);
	
	Boolean doesTagJobWithIdExist(String tag);
	
}
