package de.papke.cloud.portal.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import de.papke.cloud.portal.model.ApplicationModel;
import de.papke.cloud.portal.service.TerraformService;
import de.papke.cloud.portal.terraform.Variable;

@Controller
public class ApplicationController {
	
	private static final String MODEL_VAR_NAME = "application";

	@Value("${application.title}")
	private String applicationTitle;
	
	@Value("${application.admin.group}")
	private String adminGroup;	
	
	@Autowired
	private TerraformService terraformService;
	
	protected void fillModel(Map<String, Object> model) {
		model.put(MODEL_VAR_NAME, getApplicationModel());
	}
	
	protected ApplicationModel getApplicationModel() {
		
		// create application object
		ApplicationModel applicationModel = new ApplicationModel();
		
		// set application title
		applicationModel.setApplicationTitle(applicationTitle);
		
		// set username
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		applicationModel.setUsername((String) authentication.getPrincipal());

		// set default for is admin flag
		applicationModel.setIsAdmin(false);
		
		// set groups
		List<String> groupList = new ArrayList<>();
		for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
			
			String groupName = grantedAuthority.toString();
			groupList.add(groupName);
			
			// set is admin flag
			if (groupName.equals(adminGroup)) {
				applicationModel.setIsAdmin(true);
			}
		}
		applicationModel.setGroupList(groupList);
		
		// get cloud provider defaults map
		Map<String, Map<String, List<Variable>>> cloudProviderDefaultsMap = terraformService.getProviderDefaultsMap();

		// set cloud providers
		List<String> cloudProviderList = new ArrayList<String>();
		cloudProviderList.addAll(cloudProviderDefaultsMap.keySet());
		applicationModel.setCloudProviderList(cloudProviderList);			
		
		return applicationModel;
	}
}
