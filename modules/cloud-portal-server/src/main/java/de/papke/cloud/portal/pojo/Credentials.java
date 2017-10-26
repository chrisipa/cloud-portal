package de.papke.cloud.portal.pojo;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;

public class Credentials {

    @Id
    private String id;
    private String group;
    private String provider;
    private Map<String, String> secretMap = new HashMap<>();

    public Credentials() {}
    
	public Credentials(String group, String provider, Map<String, String> secretMap) {
		this.group = group;
		this.provider = provider;
		this.secretMap = secretMap;
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

	public Map<String, String> getSecretMap() {
		return secretMap;
	}

	public void setSecretMap(Map<String, String> secretMap) {
		this.secretMap = secretMap;
	}
}