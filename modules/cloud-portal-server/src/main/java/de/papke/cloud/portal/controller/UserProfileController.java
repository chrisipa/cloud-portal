package de.papke.cloud.portal.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserProfileController extends ApplicationController {
	
	private static final String PREFIX = "/user";
	
	@GetMapping(path = PREFIX + "/profile")
	public String userProfile(Map<String, Object> model) {
		
		// fill
		fillModel(model);
		
		// return view name
		return "user-profile";
	}
}
