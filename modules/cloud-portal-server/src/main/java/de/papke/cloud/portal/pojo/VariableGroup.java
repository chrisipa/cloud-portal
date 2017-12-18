package de.papke.cloud.portal.pojo;

import java.util.List;

public class VariableGroup {

	private String title;
	private boolean hidden = false;
	private List<Variable> variables;
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public boolean isHidden() {
		return hidden;
	}
	
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	
	public List<Variable> getVariables() {
		return variables;
	}
	
	public void setVariables(List<Variable> variables) {
		this.variables = variables;
	}
}
