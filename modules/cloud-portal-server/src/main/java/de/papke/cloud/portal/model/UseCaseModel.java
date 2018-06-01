package de.papke.cloud.portal.model;

import java.util.List;

import de.papke.cloud.portal.constants.Constants;
import de.papke.cloud.portal.pojo.ProvisionLog;
import de.papke.cloud.portal.pojo.Variable;
import de.papke.cloud.portal.pojo.VariableGroup;

public class UseCaseModel {

	private String id;
    private List<VariableGroup> variableGroups;
    private List<ProvisionLog> provisionLogs;
    
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public List<VariableGroup> getVariableGroups() {
		return variableGroups;
	}
	
	public void getVariableGroups(List<VariableGroup> variableGroups) {
		this.variableGroups = variableGroups;
	}

	public List<ProvisionLog> getProvisionLogs() {
		return provisionLogs;
	}

	public void setProvisionLogs(List<ProvisionLog> provisionLogs) {
		this.provisionLogs = provisionLogs;
	}
	
	public boolean isSecret(String variableName) {
		
		for (VariableGroup variableGroup : variableGroups) {
			for (Variable variable : variableGroup.getVariables()) {
				if (variableName.equals(variable.getName()) && variable.getType().equals(Constants.VAR_TYPE_SECRET)) {
					return true;
				}
			}
		}
		
		return false;
	}
}