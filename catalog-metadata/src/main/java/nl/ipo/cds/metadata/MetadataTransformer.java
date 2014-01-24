package nl.ipo.cds.metadata;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.MetadataDocumentType;
import nl.ipo.cds.etl.Transformer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MetadataTransformer implements Transformer {
	
	private static final Log logger = LogFactory.getLog(MetadataTransformer.class);
	
	private final MetadataManager metadataManager;
	private final ManagerDao managerDao;
	
	public MetadataTransformer(final MetadataManager metadataManager, final ManagerDao managerDao) {
		this.metadataManager = metadataManager;
		this.managerDao = managerDao;
	}

	@Override
	public void transform(List<String> themeNames) throws Exception {
		logger.debug("modifying metadata documents");
		
		final List<Object[]> docs = managerDao.getChangedMetadataDocuments();
		logger.debug("metadata documents to change: " + docs.size());
		for(final Object[] doc : docs) {
			final String documentName = (String)doc[0];
			final MetadataDocumentType documentType = MetadataDocumentType.valueOf((String)doc[1]);
			final Timestamp updateDatum = (Timestamp)doc[2];
			
			final Calendar calendar = Calendar.getInstance();
	        calendar.setTimeInMillis(updateDatum.getTime());
			
			final String dateTime = DatatypeConverter.printDateTime(calendar);
			
			logger.debug("updating: " + documentName + " " + documentType + " " + updateDatum);
			try {
				metadataManager.updateMetadata(documentName, documentType, dateTime);
			} catch(Exception e) {
				logger.error("failed to update metadata document: " + documentName, e);
			}
		}
	}
}
