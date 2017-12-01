package de.papke.cloud.portal.terraform;

public class Variable {
	
	private String title = "";
	private String name = "";
	private String description = "";
	private String defaultValue = "";
	
	@Override
	public String toString() {
		return "title=" + title + ", name=" + name + ", description=" + description + ", defaultValue=" + defaultValue;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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
