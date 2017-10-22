package de.papke.cloud.portal.model;

import java.util.List;
import java.util.Map;

import de.papke.cloud.portal.terraform.Variable;

/**
 * Created by chris on 16.10.17.
 */
public class Data {

    private String applicationTitle;
    private String username;
    private List<String> groupList; 
    private Boolean isAdmin;
    private String cloudProvider;
    private List<String> cloudProviderList;
    private Map<String, List<Variable>> cloudProviderDefaultsMap;

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

    public List<String> getGroupList() {
    	return groupList;
    }
    
    public void setGroupList(List<String> groupList) {
    	this.groupList = groupList;
    }

	public Boolean getIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(Boolean isAdmin) {
		this.isAdmin = isAdmin;
	}    
    
    public String getCloudProvider() {
    	return cloudProvider;
    }
    
    public void setCloudProvider(String cloudProvider) {
    	this.cloudProvider = cloudProvider;
    }
    
    public List<String> getCloudProviderList() {
        return cloudProviderList;
    }

    public void setCloudProviderList(List<String> cloudProviderList) {
        this.cloudProviderList = cloudProviderList;
    }

	public Map<String, List<Variable>> getCloudProviderDefaultsMap() {
		return cloudProviderDefaultsMap;
	}

	public void setCloudProviderDefaultsMap(Map<String, List<Variable>> cloudProviderDefaultsMap) {
		this.cloudProviderDefaultsMap = cloudProviderDefaultsMap;
	}
}
