package de.papke.cloud.portal.pojo;

import java.util.List;

public class UseCase {
	
	private String id;
	private String provider;
	private String provisioner;
	private List<VariableGroup> variableGroups; 
	private String resourceFolderPath; 

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getProvisioner() {
		return provisioner;
	}

	public void setProvisioner(String provisioner) {
		this.provisioner = provisioner;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<VariableGroup> getVariableGroups() {
		return variableGroups;
	}

	public void setVariableGroups(List<VariableGroup> variableGroups) {
		this.variableGroups = variableGroups;
	}

	public String getResourceFolderPath() {
		return resourceFolderPath;
	}

	public void setResourceFolderPath(String resourceFolderPath) {
		this.resourceFolderPath = resourceFolderPath;
	}
}