package de.papke.cloud.portal.pojo;

import java.util.Date;
import java.util.Map;

import org.springframework.data.annotation.Id;

public class ProvisionLog {

	@Id
	private String id;
	private Date date;
	private Date expirationDate;
	private String username;
	private String command;
	private String provider;
	private Boolean success;
	private Map<String, Object> variableMap;
	private byte[] privateKey;
	private byte[] result;
	
	public ProvisionLog() {}
	
	public ProvisionLog(Date date, Date expirationDate, String username, String command, String provider, Boolean success, Map<String, Object> variableMap, byte[] privateKey, byte[] result) {
		super();
		this.date = date;
		this.expirationDate = expirationDate;
		this.username = username;
		this.command = command;
		this.provider = provider;
		this.success = success;
		this.variableMap = variableMap;
		this.privateKey = privateKey;
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
	
	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
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

	public byte[] getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(byte[] privateKey) {
		this.privateKey = privateKey;
	}

	public byte[] getResult() {
		return result;
	}

	public void setResult(byte[] result) {
		this.result = result;
	}
}