package de.papke.cloud.portal.pojo;

import org.springframework.data.annotation.Id;

public class Credentials {

    @Id
    private String id;
    private String group;
    private String provider;
    private String username;
    private String password;

    public Credentials() {}
    
    public Credentials(String group, String provider, String username, String password) {
		super();
		this.group = group;
		this.provider = provider;
		this.username = username;
		this.password = password;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getGroup() {
		return group;
	}
	
	public void setGroup(String group) {
		this.group = group;
	}
	
	public String getProvider() {
		return provider;
	}
	
	public void setProvider(String provider) {
		this.provider = provider;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}