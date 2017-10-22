package de.papke.cloud.portal.credentials;

import org.springframework.data.annotation.Id;

public class Credentials {

    @Id
    private String id;
    private String username;
    private String password;
    private String provider;
    private String group;

    public Credentials() {}
    
    public Credentials(String username, String password, String provider, String group) {
		super();
		this.username = username;
		this.password = password;
		this.provider = provider;
		this.group = group;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}
}