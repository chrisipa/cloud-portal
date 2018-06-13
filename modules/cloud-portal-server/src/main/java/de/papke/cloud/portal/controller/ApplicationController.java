package de.papke.cloud.portal.controller;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import de.papke.cloud.portal.model.ApplicationModel;
import de.papke.cloud.portal.service.MenuService;
import de.papke.cloud.portal.service.SessionUserService;
import de.papke.cloud.portal.service.UseCaseService;

@Controller
public class ApplicationController {
	
	protected static final String APPLICATION_MODEL_VAR_NAME = "application";
	protected static final String REDIRECT_PREFIX = "redirect:";
	protected static final String VIEW_NOT_ALLOWED = "not-allowed";

	@Value("${application.title}")
	private String applicationTitle;
	
	@Value("${application.dev.mode}")
	private boolean devMode;
	
	@Autowired
	private SessionUserService sessionUserService;

	@Autowired
	private UseCaseService useCaseService;
	
	@Autowired
	private MenuService menuService;
	
	protected void fillModel(Map<String, Object> model) {
		model.put(APPLICATION_MODEL_VAR_NAME, getApplicationModel());
	}
	
	protected ApplicationModel getApplicationModel() {
		
		// create application object
		ApplicationModel applicationModel = new ApplicationModel();
		
		// set application title
		applicationModel.setApplicationTitle(applicationTitle);
		
		// set dev mode
		applicationModel.setDevMode(devMode);
		
		// set username
		applicationModel.setUser(sessionUserService.getUser());

		// set cloud providers
		applicationModel.setCloudProviderList(useCaseService.getProviders());
		
		// set menu
		applicationModel.setMenu(menuService.getMenu());
		
		return applicationModel;
	}
	
	protected void fail(String message, HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		response.getWriter().println(message);
	}
	
	protected void success(String message, HttpServletResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println(message);
	}
}
