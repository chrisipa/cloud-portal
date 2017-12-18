package de.papke.cloud.portal.service;

import java.util.ArrayList;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.papke.cloud.portal.pojo.User;

@Service
public class SessionUserService {
	
	private static final String SESSION_ATTRIBUTE_USER = "user";
	private static final User DUMMY_USER = new User("anonymous", "Anonymous", "User", "dummy@dummy.com", new ArrayList<>(), false);
	
	@Autowired
	private HttpSession session;

	public User getUser() {
		
		User user = (User) session.getAttribute(SESSION_ATTRIBUTE_USER);
		
		if (user == null) {
			user = DUMMY_USER; 
		}
		
		return user;
	}
	
	public boolean isAdmin() {
		return getUser().getIsAdmin();
	}
	
	public void setUser(User user) {
		session.setAttribute(SESSION_ATTRIBUTE_USER, user);
	}
}
