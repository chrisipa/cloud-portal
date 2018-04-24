package de.papke.cloud.portal.pojo;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String username;
	private String givenName;
	private String surName;
	private String email;
	private List<String> groups;
	private boolean admin;
	
	public User() {}
	
	public User(String username, String givenName, String surName, String email, List<String> groups, boolean admin) {
		super();
		this.givenName = givenName;
		this.surName = surName;
		this.username = username;
		this.email = email;
		this.groups = groups;
		this.admin = admin;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getSurName() {
		return surName;
	}

	public void setSurName(String surName) {
		this.surName = surName;
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

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
}