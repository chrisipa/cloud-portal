package de.papke.cloud.portal.model;

import java.util.Map;

public class DashboardModel {

	private Map<Long, Integer> provisioningHistory;
	private Map<String, Integer> provisioningCommand;
	private Map<String, Integer> cloudProviderUsage;
	private Map<String, Integer> operatingSystemUsage;

	public Map<Long, Integer> getProvisioningHistory() {
		return provisioningHistory;
	}
	
	public void setProvisioningHistory(Map<Long, Integer> provisioningHistory) {
		this.provisioningHistory = provisioningHistory;
	}
	
	public Map<String, Integer> getProvisioningCommand() {
		return provisioningCommand;
	}
	
	public void setProvisioningCommand(Map<String, Integer> provisioningCommand) {
		this.provisioningCommand = provisioningCommand;
	}
	
	public Map<String, Integer> getCloudProviderUsage() {
		return cloudProviderUsage;
	}

	public void setCloudProviderUsage(Map<String, Integer> cloudProviderUsage) {
		this.cloudProviderUsage = cloudProviderUsage;
	}

	public Map<String, Integer> getOperatingSystemUsage() {
		return operatingSystemUsage;
	}

	public void setOperatingSystemUsage(Map<String, Integer> operatingSystemUsage) {
		this.operatingSystemUsage = operatingSystemUsage;
	}
}
