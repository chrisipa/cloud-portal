package de.papke.cloud.portal.model;

import java.util.List;

import de.papke.cloud.portal.pojo.Menu;
import de.papke.cloud.portal.pojo.User;

public class ApplicationModel {
	
    private String applicationTitle;
    private User user;
	private List<String> cloudProviderList;
	private Menu menu;
	private Boolean devMode;

	public String getApplicationTitle() {
		return applicationTitle;
	}
	
	public void setApplicationTitle(String applicationTitle) {
		this.applicationTitle = applicationTitle;
	}
	
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public List<String> getCloudProviderList() {
		return cloudProviderList;
	}
	
	public void setCloudProviderList(List<String> cloudProviderList) {
		this.cloudProviderList = cloudProviderList;
	}

	public Menu getMenu() {
		return menu;
	}

	public void setMenu(Menu menu) {
		this.menu = menu;
	}

	public Boolean getDevMode() {
		return devMode;
	}

	public void setDevMode(Boolean devMode) {
		this.devMode = devMode;
	}	
}
