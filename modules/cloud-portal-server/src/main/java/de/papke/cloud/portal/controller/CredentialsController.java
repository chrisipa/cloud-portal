package de.papke.cloud.portal.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.papke.cloud.portal.constants.AzureConstants;
import de.papke.cloud.portal.model.CredentialsModel;
import de.papke.cloud.portal.service.CredentialsService;

@Controller
public class CredentialsController extends ApplicationController {
	
	private static final String PREFIX = "/credentials";
	private static final String MODEL_VAR_NAME = "credentials";
	private static final String LIST_VIEW_PREFIX = "credentials-list-";
	
	@Autowired
	private CredentialsService credentialsService;

	@PostMapping(path = PREFIX + "/create/action/{provider}")
	public String createAction(Map<String, Object> model,
			@PathVariable String provider,
			@RequestParam Map<String, String> variableMap) throws IOException {

		// get group
		String group = variableMap.get("group");
		
		// get secret map from variable map
		Map<String, String> secretMap = new HashMap<>();
		if (provider.equals(AzureConstants.PROVIDER)) {
			secretMap.put(AzureConstants.SUBSCRIPTION_ID, variableMap.get(AzureConstants.SUBSCRIPTION_ID));
			secretMap.put(AzureConstants.TENANT_ID, variableMap.get(AzureConstants.TENANT_ID));
			secretMap.put(AzureConstants.CLIENT_ID, variableMap.get(AzureConstants.CLIENT_ID));
			secretMap.put(AzureConstants.CLIENT_SECRET, variableMap.get(AzureConstants.CLIENT_SECRET));
		}
		
		// create credentials
		credentialsService.create(group, provider, secretMap);
		
		// fill model
		fillModel(model, provider);
		
		// return to list view
		return LIST_VIEW_PREFIX + provider;
	}
	
	/**
	 * Method for returning the model and view for the credentials create form page.
	 *
	 * @param model
	 * @return
	 */
	@GetMapping(value = PREFIX + "/create/form/{provider}")
	public String createForm(Map<String, Object> model,
			@PathVariable String provider) throws IOException {
		
		// fill model
		fillModel(model, provider);
		
		// return view name
		return "credentials-create-form-" + provider;
	}
	
	/**
	 * Method for returning the model and view for the credentials list page.
	 *
	 * @param model
	 * @return
	 */
	@GetMapping(value = PREFIX + "/list/{provider}")
	public String list(Map<String, Object> model,
			@PathVariable String provider) throws IOException {

		// fill model
		fillModel(model, provider);
		
		// return view name
		return LIST_VIEW_PREFIX + provider;
	}	
	
	@GetMapping(path = PREFIX + "/delete/action/{provider}/{id}")
	public String deleteAction(Map<String, Object> model,
			@PathVariable String provider,
			@PathVariable String id) throws IOException {

		// delete credentials
		credentialsService.delete(id);
		
		// fill model
		fillModel(model, provider);
		
		// return to list view
		return LIST_VIEW_PREFIX + provider;
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
