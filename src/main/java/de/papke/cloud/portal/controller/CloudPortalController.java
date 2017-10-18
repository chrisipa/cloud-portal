package de.papke.cloud.portal.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.papke.cloud.portal.cloud.CloudProviderService;
import de.papke.cloud.portal.model.Data;

/**
 * Created by chris on 16.10.17.
 */
@Controller
public class CloudPortalController {

	@Value("${application.title}")
	private String applicationTitle;

	@Autowired
	private CloudProviderService cloudProviderService;

	/**
	 * Method for returning the model and view for the index page.
	 *
	 * @param model
	 * @return
	 */
	@GetMapping(path = "/")
	public String index(Map<String, Object> model) throws IOException {

		// put data object into model
		model.put("self", getData());

		// return view name
		return "index";
	}

	/**
	 * Method for returning the model and view for the login page.
	 *
	 * @param model
	 * @return
	 */
	@GetMapping(path = "/login")
	public String login(Map<String, Object> model) {

		// put data object into model
		model.put("self", getData());

		// return view name
		return "login";
	}

	/**
	 * Method for returning the model and view for the create vm page.
	 *
	 * @param model
	 * @return
	 */
	@GetMapping(path = "/vm/create")
	public String vmCreate(Map<String, Object> model) throws IOException {

		// put data object into model
		model.put("self", getData());

		// return view name
		return "vm-create";
	}

	/**
	 * Method for returning the model and view for the provision vm page.
	 *
	 * @param model
	 * @return
	 */
	@PostMapping(path = "/vm/provision", produces="text/plain")
	@ResponseBody
	public void vmProvision( @RequestParam Map<String, Object> variableMap, HttpServletResponse response) throws IOException {

		// get response output stream
		OutputStream outputStream = response.getOutputStream();

		// provision VM
		cloudProviderService.provisionVM(variableMap, outputStream);
	}    

	private Data getData() {

		// create data object
		Data data = new Data();

		// set application title
		data.setApplicationTitle(applicationTitle);

		// set cloud providers
		data.setCloudProviderList(cloudProviderService.getProviderList());

		// get username
		data.setUsername((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

		return data;
	}
}
