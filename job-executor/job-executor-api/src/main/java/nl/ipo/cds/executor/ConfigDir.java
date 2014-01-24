package nl.ipo.cds.executor;

public class ConfigDir {

	private String path;

	public ConfigDir () {
	}
	
	public ConfigDir (final String path) {
		this.path = path;
	}
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
