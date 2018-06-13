package de.papke.cloud.portal.service;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import de.papke.cloud.portal.constants.Constants;

@Service
public class ScriptingConsoleService {
	
	private static final Logger LOG = LoggerFactory.getLogger(ScriptingConsoleService.class);
	
	private static final String FILE_EXAMPLE = "example.groovy";
	
	private String exampleFileString;
	private Map<String, Object> variableMap;
	private List<String> variableList;
	
	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private GroovyService groovyService;

	@Autowired
	private ResourceService resourceService;
	
	@PostConstruct
	private void init() {
		
		try {
		
			// get example file as string
	    	File exampleFile = resourceService.getClasspathResource(Constants.FOLDER_CONSOLE  + File.separator + FILE_EXAMPLE);
	    	exampleFileString = FileUtils.readFileToString(exampleFile, StandardCharsets.UTF_8);
	    	
	    	// create map for groovy script with predefined variables
	        variableMap = new HashMap<>();
	    	for (String beanName : applicationContext.getBeanDefinitionNames()) {
    			Object bean = applicationContext.getBean(beanName);
    			if (bean.getClass().getName().startsWith(this.getClass().getPackage().getName())) {
    				variableMap.put(beanName, bean);
    			}
	    	}
	    	
	    	// get list from map
	    	variableList = new ArrayList<>(variableMap.keySet());
			Collections.sort(variableList);
		}
		catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
	public void execute(String script, PrintWriter out) {
		
        // execute groovy script with groovy service
        groovyService.execute(script, variableMap, out);
	}
	
	public String getExampleFileString() {
		return exampleFileString;
	}
	
	public List<String> getVariableList() {
		return variableList;
	}
}
