package de.papke.cloud.portal.pojo;

import java.util.List;

public class VariableGroup {

	private String title;
	private List<Variable> variables;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public List<Variable> getVariables() {
		return variables;
	}
	public void setVariables(List<Variable> variables) {
		this.variables = variables;
	}
}
