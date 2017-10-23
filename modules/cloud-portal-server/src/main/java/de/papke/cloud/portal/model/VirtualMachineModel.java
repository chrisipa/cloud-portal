package de.papke.cloud.portal.model;

import java.util.List;
import java.util.Map;

import de.papke.cloud.portal.terraform.Variable;

public class VirtualMachineModel {

	private String cloudProvider;
    private Map<String, List<Variable>> cloudProviderDefaultsMap;
    
	public String getCloudProvider() {
		return cloudProvider;
	}
	
	public void setCloudProvider(String cloudProvider) {
		this.cloudProvider = cloudProvider;
	}
	
	public Map<String, List<Variable>> getCloudProviderDefaultsMap() {
		return cloudProviderDefaultsMap;
	}
	
	public void setCloudProviderDefaultsMap(Map<String, List<Variable>> cloudProviderDefaultsMap) {
		this.cloudProviderDefaultsMap = cloudProviderDefaultsMap;
	}
}
