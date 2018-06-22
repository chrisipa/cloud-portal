package de.papke.cloud.portal.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import de.papke.cloud.portal.constants.Constants;
import de.papke.cloud.portal.model.ScriptingConsoleModel;
import de.papke.cloud.portal.service.ScriptingConsoleService;
import de.papke.cloud.portal.service.SessionUserService;

/**
 * Controller class for scripting console.
 */
@Controller
public class ScriptingConsoleController extends ApplicationController {
	
	private static final String MODEL_VAR_NAME = "console";
	private static final String VIEW_NAME = "console";
	private static final String PREFIX = "/console";
	private static final String SESSION_VAR_LAST_SCRIPT = "lastScript";

    @Autowired
    private HttpSession session;

    @Autowired
    private SessionUserService sessionUserService;
    
    @Autowired
    private ScriptingConsoleService scriptingConsoleService;
    
    /**
     * Method for returning the model and view for the index page.
     *
     * @param model
     * @return
     */
    @GetMapping(path = PREFIX)
    public String console(Map<String, Object> model) {

		// fill model
		fillModel(model);
		
		// check if user is allowed to see console
		if (sessionUserService.isAllowed()) {
			return VIEW_NAME;
		}
		else {
			return VIEW_NOT_ALLOWED;
		}
    }

	/**
     * Method for executing a groovy script and returning the model and view for the output page.
     *
     * @param script
     * @param file
     * @param model
     * @return
     * @throws IOException
     */
    @PostMapping(path = PREFIX + "/execute", produces = Constants.RESPONSE_CONTENT_TYPE_TEXT_PLAIN)
    public void execute(@RequestParam("script") String script, @RequestParam("file") MultipartFile file, Map<String, Object> model, HttpServletResponse response) throws IOException {
    	
    	// set response content type
    	response.setContentType(Constants.RESPONSE_CONTENT_TYPE_TEXT_PLAIN);
    	
		// check if user is allowed to execute script
		if (sessionUserService.isAllowed()) {
    	
	        // use script string as default
	        String scriptToExecute = script;
	
	        // use multipart file as script if present
	        if (file != null && file.getSize() > 0) {
	            ByteArrayInputStream fileInputStream = new ByteArrayInputStream(file.getBytes());
	            scriptToExecute = IOUtils.toString(fileInputStream, StandardCharsets.UTF_8);
	            fileInputStream.close();
	        }
	        
	        // set script as session attribute
	        session.setAttribute(SESSION_VAR_LAST_SCRIPT, scriptToExecute);
	        
	        // execute script
	        scriptingConsoleService.execute(scriptToExecute, response.getWriter());
		}
		else {
			fail("You are not allowed to execute a script", response);
		}
    }
        

    /**
     * Method for getting the last script from session.
     *
     * Return an example list if no script has been executed before.
     *
     * @return
     */
    private String getLastScript() {

        StringBuilder lastScriptBuilder = new StringBuilder();

        String lastScript = (String) session.getAttribute(SESSION_VAR_LAST_SCRIPT);
        if (StringUtils.isEmpty(lastScript)) {
            lastScriptBuilder.append(scriptingConsoleService.getExampleFileString());
        }
        else {
            lastScriptBuilder.append(lastScript);
        }

        return lastScriptBuilder.toString();
    }
    
	private ScriptingConsoleModel getScriptingConsoleModel() {

		ScriptingConsoleModel scriptingConsoleModel = new ScriptingConsoleModel();
		scriptingConsoleModel.setLastScript(getLastScript());
		scriptingConsoleModel.setVariableList(scriptingConsoleService.getVariableList());

		return scriptingConsoleModel;
	}
	
	@Override
	protected void fillModel(Map<String, Object> model) {
    	super.fillModel(model);
    	model.put(MODEL_VAR_NAME, getScriptingConsoleModel());
	}
}