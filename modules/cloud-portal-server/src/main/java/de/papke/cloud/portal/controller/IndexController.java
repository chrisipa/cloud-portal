package de.papke.cloud.portal.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController extends ApplicationController {
	
	private static final String PREFIX = "/";
	
	/**
	 * Method for returning the model and view for the index page.
	 *
	 * @param model
	 * @return
	 */
	@GetMapping(path = PREFIX)
	public String index(Map<String, Object> model) throws IOException {

		// fill model
		fillModel(model);

		// return view name
		return "index";
	}
}
