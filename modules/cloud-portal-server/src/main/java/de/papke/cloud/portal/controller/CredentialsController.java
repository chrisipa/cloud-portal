package de.papke.cloud.portal.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.papke.cloud.portal.constants.AwsConstants;
import de.papke.cloud.portal.constants.AzureConstants;
import de.papke.cloud.portal.constants.VSphereConstants;
import de.papke.cloud.portal.model.CredentialsModel;
import de.papke.cloud.portal.service.CredentialsService;
import de.papke.cloud.portal.service.SessionUserService;

@Controller
public class CredentialsController extends ApplicationController {

	private static final String PREFIX = "/credentials";
	private static final String MODEL_VAR_NAME = "credentials";
	private static final String LIST_PATH_PREFIX = PREFIX + "/list/form";
	private static final String LIST_VIEW_PREFIX = "credentials-list-form-";

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
			String group = variableMap.get("group");

			// get secret map from variable map
			Map<String, String> secretMap = new HashMap<>();
			if (provider.equals(AzureConstants.PROVIDER)) {
				secretMap.put(AzureConstants.SUBSCRIPTION_ID, variableMap.get(AzureConstants.SUBSCRIPTION_ID));
				secretMap.put(AzureConstants.TENANT_ID, variableMap.get(AzureConstants.TENANT_ID));
				secretMap.put(AzureConstants.CLIENT_ID, variableMap.get(AzureConstants.CLIENT_ID));
				secretMap.put(AzureConstants.CLIENT_SECRET, variableMap.get(AzureConstants.CLIENT_SECRET));
			}
			else if (provider.equals(AwsConstants.PROVIDER)) {
				secretMap.put(AwsConstants.ACCESS_KEY, variableMap.get(AwsConstants.ACCESS_KEY));
				secretMap.put(AwsConstants.SECRET_KEY, variableMap.get(AwsConstants.SECRET_KEY));
			}
			else if (provider.equals(VSphereConstants.PROVIDER)) {
				secretMap.put(VSphereConstants.VCENTER_HOSTNAME, variableMap.get(VSphereConstants.VCENTER_HOSTNAME));
				secretMap.put(VSphereConstants.VCENTER_USERNAME, variableMap.get(VSphereConstants.VCENTER_USERNAME));
				secretMap.put(VSphereConstants.VCENTER_PASSWORD, variableMap.get(VSphereConstants.VCENTER_PASSWORD));
			}

			// create credentials
			credentialsService.create(group, provider, secretMap);

			// fill model
			fillModel(model, provider);
		}

		// return to list view
		return REDIRECT_PREFIX + LIST_PATH_PREFIX + "/" + provider;
	}

	/**
	 * Method for returning the model and view for the credentials create form page.
	 *
	 * @param model
	 * @return
	 */
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

	/**
	 * Method for returning the model and view for the credentials list page.
	 *
	 * @param model
	 * @return
	 */
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
