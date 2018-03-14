package de.papke.cloud.portal.model;

import java.util.List;

import de.papke.cloud.portal.pojo.ProvisionLog;
import de.papke.cloud.portal.pojo.Variable;
import de.papke.cloud.portal.pojo.VariableGroup;

public class VirtualMachineModel {

	private String provider;
    private List<VariableGroup> providerDefaultsList;
    private List<ProvisionLog> provisionLogList;
    
	public String getProvider() {
		return provider;
	}
	
	public void setProvider(String provider) {
		this.provider = provider;
	}
	
	public List<VariableGroup> getProviderDefaultsList() {
		return providerDefaultsList;
	}
	
	public void setProviderDefaultsList(List<VariableGroup> providerDefaultsList) {
		this.providerDefaultsList = providerDefaultsList;
	}

	public List<ProvisionLog> getProvisionLogList() {
		return provisionLogList;
	}

	public void setProvisionLogList(List<ProvisionLog> provisionLogList) {
		this.provisionLogList = provisionLogList;
	}
	
	public boolean isSecret(String variableName) {
		
		for (VariableGroup variableGroup : providerDefaultsList) {
			for (Variable variable : variableGroup.getVariables()) {
				if (variableName.equals(variable.getName()) && variable.getType().equals(Variable.TYPE_SECRET)) {
					return true;
				}
			}
		}
		
		return false;
	}
}