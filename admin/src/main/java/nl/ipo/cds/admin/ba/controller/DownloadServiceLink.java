/**
 * 
 */
package nl.ipo.cds.admin.ba.controller;

/**
 * @author Rob
 *
 */
public class DownloadServiceLink {
	private String text;
	private String ref;
	
	public DownloadServiceLink(String text, String ref){
		this.text = text;
		this.ref = ref;
	}
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getRef() {
		return ref;
	}
	public void setRef(String ref) {
		this.ref = ref;
	}
}
