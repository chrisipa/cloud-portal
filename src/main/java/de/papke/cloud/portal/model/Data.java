package de.papke.cloud.portal.model;

import java.util.List;
import de.papke.cloud.portal.terraform.Variable;

/**
 * Created by chris on 16.10.17.
 */
public class Data {

    private String applicationTitle;
    private String username;
    private List<String> groupList; 
    private String cloudProvider;
    private List<String> cloudProviderList;
    private List<Variable> cloudProviderDefaultsList;

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

	public List<Variable> getCloudProviderDefaultsList() {
		return cloudProviderDefaultsList;
	}

	public void setCloudProviderDefaultsList(List<Variable> cloudProviderDefaultsList) {
		this.cloudProviderDefaultsList = cloudProviderDefaultsList;
	}
}
