package de.papke.cloud.portal.pojo;

import java.util.ArrayList;
import java.util.List;

public class Variable {
	
	private String title = "";
	private String name = "";
	private String description = "";
	private List<String> defaults = new ArrayList<>();
	private boolean required = false;
	private String type = "";
	private int index = 0;
	private String pattern = "";
	private String url = "";
	private List<Relation> relations = new ArrayList<>();
	
	@Override
	public String toString() {
		return "title=" + title + ", name=" + name + ", description=" + description + ", defaults=" + defaults + ", required=" + required + ", type=" + type + ", index=" + index + ", pattern" + pattern + ", " + url;
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

	public List<String> getDefaults() {
		return defaults;
	}

	public void setDefaults(List<String> defaults) {
		this.defaults = defaults;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<Relation> getRelations() {
		return relations;
	}

	public void setRelations(List<Relation> relations) {
		this.relations = relations;
	}
}
