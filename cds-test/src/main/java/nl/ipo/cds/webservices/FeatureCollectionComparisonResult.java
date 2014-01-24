package nl.ipo.cds.webservices;

import java.util.ArrayList;
import java.util.List;

public class FeatureCollectionComparisonResult {
	private List<String> messages;
	private boolean success;
	private long featureCount;

	public FeatureCollectionComparisonResult() {
		super();
		this.messages = new ArrayList<String>();
	}

	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public long getFeatureCount() {
		return featureCount;
	}
	public void setFeatureCount(long featureCount) {
		this.featureCount = featureCount;
	}

	public List<String> getMessages() {
		return messages;
	}

	public void setMessages(List<String> messages) {
		this.messages = messages;
	}
	
	public void addMessage(String message){
		this.messages.add(message);
	}
}
