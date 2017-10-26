package de.papke.cloud.portal.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	public List<Credentials> getCredentialList(String provider) {
		
		List<Credentials> credentialsList = credentialsDao.findByProvider(provider);
		
		for (Credentials credentials : credentialsList) {
			Map<String, String> secretMap = credentials.getSecretMap();
			Map<String, String> decryptedSecretMap = decryptSecretMap(secretMap);
			credentials.setSecretMap(decryptedSecretMap);
		}
		
		return credentialsList;
	}
	
	public Credentials getCredentials(String provider) {
		
		for (GrantedAuthority grantedAuthority : SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
			
			String groupName = grantedAuthority.toString();
			Credentials credentials = credentialsDao.findByGroupAndProvider(groupName, provider);
			
			if (credentials != null) {
				
				Map<String, String> secretMap = credentials.getSecretMap();
				Map<String, String> decryptedSecretMap = decryptSecretMap(secretMap);
				credentials.setSecretMap(decryptedSecretMap);
				
				return credentials;
			}
		}
		
		return null;
	}
	
	public Credentials create(String group, String provider, Map<String, String> secretMap) {
		
		Map<String, String> encryptedSecretMap = encryptSecretMap(secretMap);
		Credentials credentials = new Credentials(group, provider, encryptedSecretMap);
		
		return credentialsDao.save(credentials);
	}
	
	public void delete(String id) {
		credentialsDao.delete(id);
	}
	
	private Map<String, String> encryptSecretMap(Map<String, String> secretMap) {
		
		Map<String, String> encryptedSecretMap = new HashMap<>();
		
		if (secretMap != null) {
			for (String key : secretMap.keySet()) {
				String value = secretMap.get(key);					
				encryptedSecretMap.put(key, encryptionService.encrypt(value));
			}
		}
		
		return encryptedSecretMap;
	}
	
	private Map<String, String> decryptSecretMap(Map<String, String> secretMap) {
		
		Map<String, String> decryptedSecretMap = new HashMap<>();
		
		if (secretMap != null) {
			for (String key : secretMap.keySet()) {
				String value = secretMap.get(key);					
				decryptedSecretMap.put(key, encryptionService.decrypt(value));
			}
		}
		
		return decryptedSecretMap;
	}
}
