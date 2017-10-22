package de.papke.cloud.portal.credentials;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CredentialsService {
	
	@Autowired
	private CredentialsRepository credentialsRepository;
	
	public Credentials getCredentials(String provider) {
		
		for (GrantedAuthority grantedAuthority : SecurityContextHolder.getContext().getAuthentication().getAuthorities()) {
			
			String groupName = grantedAuthority.toString();
			Credentials credentials = credentialsRepository.findByGroup(groupName);
			
			if (credentials != null) {
				return credentials;
			}
		}
		
		return null;
	}
}
