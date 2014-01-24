package nl.ipo.cds.admin.ba.controller;

public class AWstats{
	public String url;
	public String name;
	
	public AWstats (String url, String name){
		this.url  = url;
		this.name = name;
	}
	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	public String toString(){
		return name + " = " + url;
	}
}
