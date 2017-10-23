package de.papke.cloud.portal.model;

import java.util.List;

public class ApplicationModel {
	
    private String applicationTitle;
    private String username;
    private Boolean isAdmin;
	private List<String> groupList;
	private List<String> cloudProviderList;

	public String getApplicationTitle() {
		return applicationTitle;
	}
	
	public void setApplicationTitle(String applicationTitle) {
		this.applicationTitle = applicationTitle;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public Boolean getIsAdmin() {
		return isAdmin;
	}
	
	public void setIsAdmin(Boolean isAdmin) {
		this.isAdmin = isAdmin;
	}
	
	public List<String> getGroupList() {
		return groupList;
	}

	public void setGroupList(List<String> groupList) {
		this.groupList = groupList;
	}	
	
	public List<String> getCloudProviderList() {
		return cloudProviderList;
	}
	
	public void setCloudProviderList(List<String> cloudProviderList) {
		this.cloudProviderList = cloudProviderList;
	}	
}
