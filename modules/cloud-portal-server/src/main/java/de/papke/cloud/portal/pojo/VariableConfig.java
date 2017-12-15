package de.papke.cloud.portal.pojo;

import java.util.List;

public class VariableConfig {
	
	private List<VariableGroup> variableGroups;
	
	public VariableConfig() {}

	public VariableConfig(List<VariableGroup> variableGroups) {
		super();
		this.variableGroups = variableGroups;
	}

	public List<VariableGroup> getVariableGroups() {
		return variableGroups;
	}

	public void setVariableGroups(List<VariableGroup> variableGroups) {
		this.variableGroups = variableGroups;
	}
}
