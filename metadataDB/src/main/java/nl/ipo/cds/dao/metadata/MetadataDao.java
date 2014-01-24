/**
 * 
 */
package nl.ipo.cds.dao.metadata;

import java.util.List;

import nl.ipo.cds.domain.metadata.Service;


/**
 * @author eshuism
 * 13 jan 2012
 */
public interface MetadataDao {

	public void create(Service service);
	
	public Service getService(Long id);

	public Service findService(String name);

	public List<Service> getAllServices();

	public Service update(Service service);
}
