package gr.cti.securedistro;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class BlockedWebsite {
	 
	private final StringProperty siteUrlProperty;
	
	public BlockedWebsite(String url){
		this.siteUrlProperty = new SimpleStringProperty(url);
	}
	
	public BlockedWebsite(){
		this(null);
	}
	
	public String getSiteUrl(){
		return siteUrlProperty.get();
	}
	
	public void setSiteUrl(String newUrl) {
		this.siteUrlProperty.set(newUrl);
	}
	
	public StringProperty siteUrlProperty(){
		return this.siteUrlProperty;
	}
}
