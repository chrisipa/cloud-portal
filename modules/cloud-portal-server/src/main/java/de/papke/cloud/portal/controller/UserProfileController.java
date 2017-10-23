package de.papke.cloud.portal.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserProfileController extends ApplicationController {
	
	private static final String PREFIX = "/user";
	
	/**
	 * Method for returning the model and view for the user profile page.
	 *
	 * @param model
	 * @return
	 */
	@GetMapping(path = PREFIX + "/profile")
	public String userProfile(Map<String, Object> model) throws IOException {
		
		// fill
		fillModel(model);
		
		// return view name
		return "user-profile";
	}
}
