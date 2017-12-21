package de.papke.cloud.portal.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController extends ApplicationController {
	
	private static final String PREFIX = "/";
	
	@GetMapping(path = PREFIX)
	public String index(Map<String, Object> model) {

		// fill model
		fillModel(model);

		// return view name
		return "index";
	}
}
