package de.papke.cloud.portal.pojo;

import java.util.Date;
import java.util.Map;

import org.springframework.data.annotation.Id;

public class ProvisionLog {

	@Id
	private String id;
	private Date date;
	private String username;
	private String action;
	private String provider;
	private Boolean success;
	private Map<String, Object> variableMap;
	private byte[] result;
	
	public ProvisionLog() {}

	public ProvisionLog(Date date, String username, String action, String provider, Boolean success, Map<String, Object> variableMap, byte[] result) {
		super();
		this.date = date;
		this.username = username;
		this.action = action;
		this.provider = provider;
		this.success = success;
		this.variableMap = variableMap;
		this.result = result;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}
	
	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}
	
	public Map<String, Object> getVariableMap() {
		return variableMap;
	}

	public void setVariableMap(Map<String, Object> variableMap) {
		this.variableMap = variableMap;
	}

	public byte[] getResult() {
		return result;
	}

	public void setResult(byte[] result) {
		this.result = result;
	}
}