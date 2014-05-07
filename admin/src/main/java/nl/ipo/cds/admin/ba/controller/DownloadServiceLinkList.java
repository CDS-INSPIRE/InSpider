/**
 * 
 */
package nl.ipo.cds.admin.ba.controller;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rob
 *
 */
public class DownloadServiceLinkList {
	private List<DownloadServiceLink> downloadLinkList;
	
	public DownloadServiceLinkList(){
		downloadLinkList = new ArrayList<DownloadServiceLink>();
	}
	
	public void addLink(DownloadServiceLink downloadServiceLink){
		downloadLinkList.add(downloadServiceLink);
	}

	public List<DownloadServiceLink> getDownloadLinkList() {
		return downloadLinkList;
	}

	public void setDownloadLinkList(List<DownloadServiceLink> downloadLinkList) {
		this.downloadLinkList = downloadLinkList;
	}
}
