/**
 * 
 */
package nl.ipo.cds.dao.metadata.impl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import nl.ipo.cds.dao.metadata.MetadataDao;
import nl.ipo.cds.domain.metadata.Service;

import org.springframework.transaction.annotation.Transactional;

/**
 * @author eshuism
 * 13 jan 2012
 */
public class MetadataDaoImpl implements MetadataDao {

	public EntityManager entityManager;

	@PersistenceContext(unitName = "cds-metadata")
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	/* (non-Javadoc)
	 * @see nl.ipo.cds.dao.metadata.MetadataDao#create(nl.ipo.cds.domain.metadata.Service)
	 */
	@Override
	@Transactional(value="transactionManagerMetadata")
	public void create(Service service) {
		this.entityManager.persist(service);
	}

	/* (non-Javadoc)
	 * @see nl.ipo.cds.dao.metadata.MetadataDao#getService(java.lang.Long)
	 */
	@Override
	public Service getService(Long id) {
		return this.entityManager.find(Service.class, id);
	}

	/* (non-Javadoc)
	 * @see nl.ipo.cds.dao.metadata.MetadataDao#findService(java.lang.String)
	 */
	@Override
	public Service findService(String name) {
		Query serviceQuery = null;
		serviceQuery = entityManager.createQuery("from Service as service where service.name = ?1")
				.setParameter(1, name);
		return (Service)serviceQuery.getSingleResult();
	}

	/* (non-Javadoc)
	 * @see nl.ipo.cds.dao.metadata.MetadataDao#getAllServices()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Service> getAllServices() {
		Query serviceQuery = null;
		serviceQuery = entityManager.createQuery("from Service as service order by service.name");
		return serviceQuery.getResultList();
	}

	/* (non-Javadoc)
	 * @see nl.ipo.cds.dao.metadata.MetadataDao#update(nl.ipo.cds.domain.metadata.Service)
	 */
	@Override
	@Transactional(value="transactionManagerMetadata")
	public Service update(Service service) {
		return this.entityManager.merge(service);
	}

}
