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
	private CredentialsDao credentialsRepository;
	
	public List<Credentials> getCredentialList() {
		return credentialsRepository.findAll();
	}
	
	public Credentials getCredentials(String provider) {
		
		for (GrantedAuthority grantedAuthority : SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
			
			String groupName = grantedAuthority.toString();
			Credentials credentials = credentialsRepository.findByGroupAndProvider(groupName, provider);
			
			if (credentials != null) {
				return credentials;
			}
		}
		
		return null;
	}
	
	public Credentials create(String group, String provider, String username, String password) {
		Credentials credentials = new Credentials(group, provider, username, password);
		return credentialsRepository.save(credentials);
	}
}
