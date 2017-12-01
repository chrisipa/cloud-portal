package de.papke.cloud.portal.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import de.papke.cloud.portal.model.ApplicationModel;
import de.papke.cloud.portal.service.MenuService;
import de.papke.cloud.portal.service.TerraformService;
import de.papke.cloud.portal.service.UserService;
import de.papke.cloud.portal.terraform.Variable;

@Controller
public class ApplicationController {
	
	private static final String MODEL_VAR_NAME = "application";
	protected static final String REDIRECT_PREFIX = "redirect:";

	@Value("${application.title}")
	private String applicationTitle;
	
	@Autowired
	private UserService userService;

	@Autowired
	private TerraformService terraformService;
	
	@Autowired
	private MenuService menuService;
	
	protected void fillModel(Map<String, Object> model) {
		model.put(MODEL_VAR_NAME, getApplicationModel());
	}
	
	protected ApplicationModel getApplicationModel() {
		
		// create application object
		ApplicationModel applicationModel = new ApplicationModel();
		
		// set application title
		applicationModel.setApplicationTitle(applicationTitle);
		
		// set username
		applicationModel.setUser(userService.getUser());

		// get cloud provider defaults map
		Map<String, Map<String, List<Variable>>> cloudProviderDefaultsMap = terraformService.getProviderDefaultsMap();

		// set cloud providers
		List<String> cloudProviderList = new ArrayList<>();
		cloudProviderList.addAll(cloudProviderDefaultsMap.keySet());
		applicationModel.setCloudProviderList(cloudProviderList);
		
		// set menu
		applicationModel.setMenu(menuService.getMenu());
		
		return applicationModel;
	}
}
