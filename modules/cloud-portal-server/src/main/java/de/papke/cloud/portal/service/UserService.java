package de.papke.cloud.portal.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.papke.cloud.portal.pojo.User;

@Service
public class UserService {

	private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

	@Value("${application.admin.group}")
	private String adminGroup;	

	@Autowired
	private DirectoryService directoryService;

	public User getUser(String username) {

		User user = null;

		try {

			Boolean isAdmin = false;

			List<String> groupList = directoryService.getGroupList(username);
			for (String group : groupList) {
				if (group.equals(adminGroup)) {
					isAdmin = true;
				}
			}

			user = new User(username, username, groupList, isAdmin);

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
