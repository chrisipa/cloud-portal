package de.papke.cloud.portal.model;

import java.util.List;

import de.papke.cloud.portal.pojo.Credentials;

public class CredentialsModel {
	
	private List<Credentials> credentialsList;

	public List<Credentials> getCredentialsList() {
		return credentialsList;
	}

	public void setCredentialsList(List<Credentials> credentialsList) {
		this.credentialsList = credentialsList;
	}
}