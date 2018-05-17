package de.papke.cloud.portal.pojo;

public class Relation {

	private String name;
	private String type;
	
	@Override
	public String toString() {
		return "name=" + name + ", type=" + type;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
}