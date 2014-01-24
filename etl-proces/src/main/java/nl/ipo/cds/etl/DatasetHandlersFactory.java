package nl.ipo.cds.etl;

import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.EtlJob;

public interface DatasetHandlersFactory<T extends PersistableFeature> {

	boolean isJobSupported (EtlJob job);

	DatasetHandlers<T> createDatasetHandlers(EtlJob job, ManagerDao managerDao);
}
