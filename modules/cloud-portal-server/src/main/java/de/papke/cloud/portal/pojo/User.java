package de.papke.cloud.portal.pojo;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String username;
	private String email;
	private List<String> groups;
	private Boolean isAdmin;
	
	public User() {}
	
	public User(String username, String email, List<String> groups, Boolean isAdmin) {
		super();
		this.username = username;
		this.email = email;
		this.groups = groups;
		this.isAdmin = isAdmin;
	}

	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public List<String> getGroups() {
		return groups;
	}
	public void setGroups(List<String> groups) {
		this.groups = groups;
	}

	public Boolean getIsAdmin() {
		return isAdmin;
	}

	public void setIsAdmin(Boolean isAdmin) {
		this.isAdmin = isAdmin;
	}
}