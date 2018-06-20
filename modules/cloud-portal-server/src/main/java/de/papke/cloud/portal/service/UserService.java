package de.papke.cloud.portal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.papke.cloud.portal.pojo.User;

@Service
public class UserService {

	private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

	@Value("${APPLICATION_ADMIN_GROUP}")
	private String adminGroup;	

	@Autowired
	private DirectoryService directoryService;

	public User getUser(String username) {

		User user = null;

		try {

			user = directoryService.getUser(username);
			
			if (user.getGroups() != null) {
				for (String group : user.getGroups()) {
					if (group.equals(adminGroup)) {
						user.setAdmin(true);
						break;
					}
				}
			}
		}
		catch(Exception e) {
			LOG.error(e.getMessage(), e);
		}
		
		return user;
	}
	
	public boolean authenticate(String username, String password) {
		return directoryService.authenticate(username, password);
	}	
}
