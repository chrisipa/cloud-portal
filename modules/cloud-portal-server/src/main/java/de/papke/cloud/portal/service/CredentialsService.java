package de.papke.cloud.portal.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import de.papke.cloud.portal.dao.CredentialsDao;
import de.papke.cloud.portal.pojo.Credentials;

@Service
public class CredentialsService {
	
	@Autowired
	private EncryptionService encryptionService;
	
	@Autowired
	private CredentialsDao credentialsDao;
	
	public List<Credentials> getCredentialList() {
		
		List<Credentials> credentialsList = credentialsDao.findAll();
		
		for (Credentials credentials : credentialsList) {
			String encryptedPassword = credentials.getPassword();
			String decryptedPassword = encryptionService.decrypt(encryptedPassword);
			credentials.setPassword(decryptedPassword);
		}
		
		return credentialsList;
	}
	
	public Credentials getCredentials(String provider) {
		
		for (GrantedAuthority grantedAuthority : SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
			
			String groupName = grantedAuthority.toString();
			Credentials credentials = credentialsDao.findByGroupAndProvider(groupName, provider);
			
			if (credentials != null) {
				String encryptedPassword = credentials.getPassword();
				String decryptedPassword = encryptionService.decrypt(encryptedPassword);
				credentials.setPassword(decryptedPassword);
				return credentials;
			}
		}
		
		return null;
	}
	
	public Credentials create(String group, String provider, String username, String password) {
		
		String encryptedPassword = encryptionService.encrypt(password);
		Credentials credentials = new Credentials(group, provider, username, encryptedPassword);
		return credentialsDao.save(credentials);
	}
	
	public void delete(String id) {
		credentialsDao.delete(id);
	}
}
