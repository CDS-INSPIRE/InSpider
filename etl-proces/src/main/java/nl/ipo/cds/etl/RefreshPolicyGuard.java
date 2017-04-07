package nl.ipo.cds.etl;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.inject.Inject;

import static nl.ipo.cds.domain.RefreshPolicy.*;
import nl.ipo.cds.dao.ManagerDao;
import nl.ipo.cds.domain.Dataset;
import nl.ipo.cds.domain.DatasetType;
import nl.ipo.cds.domain.EtlJob;





//import nl.idgis.commons.jobexecutor.Job;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;


public class RefreshPolicyGuard {
	
	
	private static final Log technicalLog = LogFactory.getLog(RefreshPolicyGuard.class);
	
	private ManagerDao managerDao;
	
	public RefreshPolicyGuard() {
		super();
	}

	public RefreshPolicyGuard(ManagerDao managerDao) {
		super();
		this.managerDao = managerDao;
	}

	/**
	 * Checks whether a data refresh is allowed for the given job. 
	 * 
	 * @param current
	 *         current ETL job, must not be <code>null</code>
	 * @param lastSuccess
	 *         last successful ETL job, must not be <code>null</code>
	 * @return
	 *         <code>true</code>, if refresh is allowed, <code>false</code> otherwise
	 */
	boolean isRefreshAllowed (final EtlJob current, final EtlJob lastSuccess)  {
		
		technicalLog.debug("lastSuccessfulJob: " + lastSuccess);
		if (current.isForceExecution()) {
			technicalLog.debug("refresh ok: execution has been forced interactively");
			return true;
		} else if (lastSuccess == null) {
			technicalLog.debug("refresh ok: no prior successful execution");
			return true;
		}
		
		//DatasetType datasetType = current.getDatasetType();
		//W1502 019
		technicalLog.debug("managerDao +++++++++++++++++++++++ " + this.managerDao);
		Dataset dataset = this.managerDao.getDatasetBy(current.getBronhouder(), current.getDatasetType(), current.getUuid());
		
		boolean isRefreshAllowed = false;		
		switch (dataset.getRefreshPolicy()){
		case IF_MODIFIED_METADATA:
		case IF_MODIFIED_HTTP_HEADER:			
			isRefreshAllowed = hasMetadataUpdateDatumChanged (current, lastSuccess);
			if (isRefreshAllowed) {
				technicalLog.debug("refresh ok: last modification info changed");
			} else {
				technicalLog.debug("refresh veto: last modification info unchanged");
			}
			break;
		case ONCE_A_DAY:
			isRefreshAllowed = lastExecutionOnAnotherDay (current, lastSuccess);
			if (isRefreshAllowed) {
				technicalLog.debug("refresh ok: based on date (once a day)");
			} else {
				technicalLog.debug("refresh veto: based on date (once a day)");				
			}			
			break;
		default:
			throw new RuntimeException("Internal error: Unhandled case '" + dataset.getRefreshPolicy() + "'");	
		}
		return isRefreshAllowed;
	}

	private boolean hasMetadataUpdateDatumChanged(final EtlJob current, final EtlJob lastSuccess) {
		final Timestamp lastSuccessMetadataUpdate = lastSuccess.getMetadataUpdateDatum();
		final Timestamp currentMetadataUpdateDatum = current.getMetadataUpdateDatum();		
		technicalLog.debug("lastSuccessfulJob.metadataUpdateDatum: " + lastSuccessMetadataUpdate);
		technicalLog.debug("job.metadataUpdateDatum: " + currentMetadataUpdateDatum);
		return lastSuccessMetadataUpdate == null || !lastSuccessMetadataUpdate.equals(currentMetadataUpdateDatum);
	}

	private boolean lastExecutionOnAnotherDay(final EtlJob current, final EtlJob lastSuccess) {
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		final String lastSuccessStartDate = dateFormat.format(lastSuccess.getStartTime());
		final String currentStartDate = dateFormat.format(current.getStartTime());
		return !lastSuccessStartDate.equals(currentStartDate);
	}

}
