package de.papke.cloud.portal.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.papke.cloud.portal.model.CredentialsModel;
import de.papke.cloud.portal.service.CredentialsService;

@Controller
public class CredentialsController extends ApplicationController {
	
	private static final String MODEL_VAR_NAME = "credentials";

	private static final String PREFIX = "/credentials";
	
	@Autowired
	private CredentialsService credentialsService;

	@PostMapping(path = PREFIX + "/create/action")
	public String createAction(Map<String, Object> model,
			@RequestParam String group,
			@RequestParam String provider,
			@RequestParam String username,
			@RequestParam String password) throws IOException {

		// create credentials
		credentialsService.create(group, provider, username, password);
		
		// fill model
		fillModel(model);
		
		// return to list view
		return "credentials-list";
	}
	
	/**
	 * Method for returning the model and view for the credentials create form page.
	 *
	 * @param model
	 * @return
	 */
	@GetMapping(value = PREFIX + "/create/form")
	public String createForm(Map<String, Object> model) throws IOException {
		
		// fill model
		fillModel(model);
		
		// return view name
		return "credentials-create-form";
	}
	
	/**
	 * Method for returning the model and view for the credentials list page.
	 *
	 * @param model
	 * @return
	 */
	@GetMapping(value = PREFIX + "/list")
	public String list(Map<String, Object> model) throws IOException {

		// fill model
		fillModel(model);
		
		// return view name
		return "credentials-list";
	}	
	
	@GetMapping(path = PREFIX + "/delete/action/{id}")
	public String deleteAction(Map<String, Object> model,
			@PathVariable String id) throws IOException {

		// delete credentials
		credentialsService.delete(id);
		
		// fill model
		fillModel(model);
		
		// return to list view
		return "credentials-list";
	}	
	
	private CredentialsModel getCredentialsModel() {
		
		CredentialsModel credentialsModel = new CredentialsModel();
		credentialsModel.setCredentialsList(credentialsService.getCredentialList());
		
		return credentialsModel;
	}
	
	@Override
	protected void fillModel(Map<String, Object> model) {
		super.fillModel(model);
		model.put(MODEL_VAR_NAME, getCredentialsModel());
	}	
}
