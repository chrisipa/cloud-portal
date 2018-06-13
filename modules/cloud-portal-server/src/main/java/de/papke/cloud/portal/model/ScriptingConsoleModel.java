package de.papke.cloud.portal.model;

import java.util.List;

public class ScriptingConsoleModel {
	
	private String lastScript;
	private List<String> variableList;

	public String getLastScript() {
		return lastScript;
	}

	public void setLastScript(String lastScript) {
		this.lastScript = lastScript;
	}

	public List<String> getVariableList() {
		return variableList;
	}

	public void setVariableList(List<String> variableList) {
		this.variableList = variableList;
	}
}
