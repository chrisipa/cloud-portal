package de.papke.cloud.portal.model;

import java.util.List;

import de.papke.cloud.portal.pojo.ProvisionLog;
import de.papke.cloud.portal.pojo.VariableGroup;

public class VirtualMachineModel {

	private String cloudProvider;
    private List<VariableGroup> cloudProviderDefaultsList;
    private List<ProvisionLog> provisionLogList;
    
	public String getCloudProvider() {
		return cloudProvider;
	}
	
	public void setCloudProvider(String cloudProvider) {
		this.cloudProvider = cloudProvider;
	}
	
	public List<VariableGroup> getCloudProviderDefaultsList() {
		return cloudProviderDefaultsList;
	}
	
	public void setCloudProviderDefaultsList(List<VariableGroup> cloudProviderDefaultsList) {
		this.cloudProviderDefaultsList = cloudProviderDefaultsList;
	}

	public List<ProvisionLog> getProvisionLogList() {
		return provisionLogList;
	}

	public void setProvisionLogList(List<ProvisionLog> provisionLogList) {
		this.provisionLogList = provisionLogList;
	}
}