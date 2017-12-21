package de.papke.cloud.portal.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.papke.cloud.portal.model.CredentialsModel;
import de.papke.cloud.portal.service.CredentialsService;
import de.papke.cloud.portal.service.SessionUserService;

@Controller
public class CredentialsController extends ApplicationController {

	private static final String PREFIX = "/credentials";
	private static final String MODEL_VAR_NAME = "credentials";
	private static final String LIST_PATH_PREFIX = PREFIX + "/list/form";
	private static final String LIST_VIEW_PREFIX = "credentials-list-form-";
	private static final String ATTRIBUTE_GROUP = "group";

	@Autowired
	private CredentialsService credentialsService;

	@Autowired
	private SessionUserService sessionUserService;

	@PostMapping(path = PREFIX + "/create/action/{provider}")
	public String createAction(Map<String, Object> model,
			@PathVariable String provider,
			@RequestParam Map<String, String> variableMap) {

		if (sessionUserService.isAdmin()) {

			// get group
			String group = variableMap.get(ATTRIBUTE_GROUP);

			// remove group from map
			variableMap.remove(ATTRIBUTE_GROUP);
			
			// create credentials
			credentialsService.create(group, provider, variableMap);

			// fill model
			fillModel(model, provider);
		}

		// return to list view
		return REDIRECT_PREFIX + LIST_PATH_PREFIX + "/" + provider;
	}

	@GetMapping(value = PREFIX + "/create/form/{provider}")
	public String createForm(Map<String, Object> model,
			@PathVariable String provider) {

		if (sessionUserService.isAdmin()) {
			
			// fill model
			fillModel(model, provider);
		}

		// return view name
		return "credentials-create-form-" + provider;
	}

	@GetMapping(value = LIST_PATH_PREFIX + "/{provider}")
	public String list(Map<String, Object> model,
			@PathVariable String provider) {

		if (sessionUserService.isAdmin()) {
		
			// fill model
			fillModel(model, provider);
		}

		// return view name
		return LIST_VIEW_PREFIX + provider;
	}	

	@GetMapping(path = PREFIX + "/delete/action/{provider}/{id}")
	public String deleteAction(Map<String, Object> model,
			@PathVariable String provider,
			@PathVariable String id) {

		if (sessionUserService.isAdmin()) {
		
			// delete credentials
			credentialsService.delete(id);
	
			// fill model
			fillModel(model, provider);
		}

		// return to list view
		return REDIRECT_PREFIX + LIST_PATH_PREFIX + "/" + provider;
	}	

	private CredentialsModel getCredentialsModel(String provider) {

		CredentialsModel credentialsModel = new CredentialsModel();
		credentialsModel.setCredentialsList(credentialsService.getCredentialList(provider));

		return credentialsModel;
	}

	protected void fillModel(Map<String, Object> model, String provider) {
		super.fillModel(model);
		model.put(MODEL_VAR_NAME, getCredentialsModel(provider));
	}	
}