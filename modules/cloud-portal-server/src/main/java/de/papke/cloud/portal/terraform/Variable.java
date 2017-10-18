package de.papke.cloud.portal.terraform;

public class Variable {
	
	private String name = "";
	private String description = "";
	private String defaultValue = "";
	
	public Variable() {}

	@Override
	public String toString() {
		return "name=" + name + ", description=" + description + ", defaultValue=" + defaultValue;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
}
