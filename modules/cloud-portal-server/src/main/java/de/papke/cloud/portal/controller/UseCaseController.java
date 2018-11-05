package de.papke.cloud.portal.controller;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import de.papke.cloud.portal.constants.Constants;
import de.papke.cloud.portal.model.UseCaseModel;
import de.papke.cloud.portal.pojo.Credentials;
import de.papke.cloud.portal.pojo.ProvisionLog;
import de.papke.cloud.portal.pojo.Relation;
import de.papke.cloud.portal.pojo.UseCase;
import de.papke.cloud.portal.pojo.User;
import de.papke.cloud.portal.pojo.Variable;
import de.papke.cloud.portal.service.CredentialsService;
import de.papke.cloud.portal.service.KeyPairService;
import de.papke.cloud.portal.service.ProvisionLogService;
import de.papke.cloud.portal.service.SessionUserService;
import de.papke.cloud.portal.service.UseCaseService;

@Controller
public class UseCaseController extends ApplicationController {

	private static final Logger LOG = LoggerFactory.getLogger(UseCaseController.class);

	private static final String PREFIX = "/usecase";
	private static final String MODEL_VAR_NAME = "useCase";
	private static final String VAR_TYPE_FILE = "file";
	private static final String VAR_NAME_SCRIPT_FILE = "script_file";
	private static final String VAR_NAME_ANSIBLE_REQUIREMENTS_FILE = "ansible_requirements_file";
	private static final String VAR_NAME_ANSIBLE_PLAYBOOK_FILE = "ansible_playbook_file";
	private static final String VAR_NAME_PRIVATE_KEY_FILE = "private_key_file";
	private static final String VAR_NAME_PUBLIC_KEY_FILE = "public_key_file";
	private static final String VAR_NAME_RANDOM_ID = "random_id";
	private static final String VAR_NAME_PART_KEY = "key";
	private static final String VAR_NAME_CREATION_DATE = "creation_date";
	private static final String VAR_NAME_OWNER = "owner";
	private static final String VAR_NAME_GROUP = "group";
	private static final String VAR_NAME_APPLICATION_URL = "application_url";
	private static final String EMPTY_SCRIPT_NAME = "empty";
	
	@Value("${APPLICATION_DATE_FORMAT}")
	private SimpleDateFormat dateFormat;
	
	@Value("${APPLICATION_URL}")
	private String applicationUrl;

	@Autowired
	private CredentialsService credentialsService;

	@Autowired
	private ProvisionLogService provisionLogService;
	
	@Autowired
	private KeyPairService keyPairService;
	
	@Autowired
	private SessionUserService sessionUserService;
	
	@Autowired
	private UseCaseService useCaseService;

	@GetMapping(path = PREFIX + "/list/form/{id}")
	public String list(Map<String, Object> model, @PathVariable String id) {

		// fill model
		fillModel(model, id);

		// return view name
		return "usecase-list-form";
	}

	@GetMapping(path = PREFIX + "/create/form/{id}")
	public String create(Map<String, Object> model, @PathVariable String id) {
		
		// fill model
		fillModel(model, id);
		
		// return view name
		return "usecase-create-form";
	}
	
	@GetMapping(path = PREFIX + "/variables/{id}")
	public void variables(HttpServletResponse response, @PathVariable String id) {

		try {
			StringBuilder usageBuilder = new StringBuilder();
			
			List<Variable> variables = useCaseService.getVisibleVariables(id);
			for (Variable variable : variables) {
				usageBuilder.append(renderVariable(variable));
				usageBuilder.append(Constants.CHAR_NEW_LINE);
			}
			
			response.getWriter().println(usageBuilder);
		}
		catch(Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	@PostMapping(path = PREFIX + "/create/action/{action}")
	@ResponseBody
	public void provision(
			@PathVariable String action,
			@RequestParam String id,
			@RequestParam Map<String, Object> variableMap,
			HttpServletRequest request,
			HttpServletResponse response) {

		List<File> tempFileList = new ArrayList<>();

		try {
			
			// set response content type
			response.setContentType(Constants.RESPONSE_CONTENT_TYPE_TEXT_PLAIN);

			// get use case
			UseCase useCase = useCaseService.getUseCaseById(id); 
			
			// get provider
			String provider = useCase.getProvider();
			
			// get credentials
			Credentials credentials = credentialsService.getCredentials(provider);
			if (credentials != null) {
			
				// iterate over file map
				writeFilesAndAddToMap(request, variableMap, tempFileList);
				
				// get variables
				List<Variable> variables = useCaseService.getVisibleVariables(id);
				
				// validate parameters
				List<Variable> errorList = validateValues(variables, variableMap);
				if (errorList.isEmpty()) {
					
					// extend with sytem generated data
					extendWithUserData(credentials, variableMap);
					extendWithCreationDate(variableMap);
					extendWithRandomId(variableMap);
					extendWithApplicationUrl(variableMap);
					
					// extend variables map with default values
					File privateKeyFile = extendWithDefaultValues(variables, variableMap, tempFileList);
	
					// get response output stream
					OutputStream outputStream = response.getOutputStream();
	
					// provision VM
					useCaseService.provision(useCase, action, credentials, variableMap, privateKeyFile, outputStream);
				}
				else {
					fail(renderVariableErrorMessage(errorList), response);
				}
			}
			else {
				fail(String.format("No credentials found for cloud provider '%s'. Please contact your administrator.", provider), response);
			}
		}
		catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		finally {

			// remove temp files
			for (File tempFile : tempFileList) {
				FileUtils.deleteQuietly(tempFile);
			}
		}
	}
	
	@GetMapping(path = PREFIX + "/unlink/action/{useCaseId}/{provisionLogId}")
	public void unlink(Map<String, Object> model,
			@PathVariable String useCaseId,
			@PathVariable String provisionLogId,
			HttpServletResponse response) {
		
		try {
			
			// set response content type
			response.setContentType(Constants.RESPONSE_CONTENT_TYPE_TEXT_PLAIN);
			
			// get current user
			User user = sessionUserService.getUser();

			// check if user is allowed to deprovision the vm
			if (user.isAdmin()) {			
			
				// get provision log entry
				ProvisionLog provisionLog = provisionLogService.get(provisionLogId);
				if (provisionLog != null) {
					
					// delete provision log entry
					provisionLogService.delete(provisionLogId);
					
					// print success message
					success(String.format("Provision log entry with id '%s' was unlinked successfully.", provisionLogId), response);
				}
				else {
					fail(String.format("No provision log entry found for id '%s'.", provisionLogId), response);
				}
			}
			else {
				fail(String.format("You are not allowed to unlink the entry with the id '%s'", provisionLogId), response);
			}
		}
		catch(Exception e) {
			LOG.error(e.getMessage(), e);
		}
		
		// fill model
		fillModel(model, useCaseId);
	} 

	@GetMapping(path = PREFIX + "/destroy/action/{useCaseId}/{provisionLogId}")
	public void destroy(Map<String, Object> model,
			@PathVariable String useCaseId,
			@PathVariable String provisionLogId,
			HttpServletResponse response) {

		try {
			
			// set response content type
			response.setContentType(Constants.RESPONSE_CONTENT_TYPE_TEXT_PLAIN);

			// get use case
			UseCase useCase = useCaseService.getUseCaseById(useCaseId);
			
			// get provider
			String provider = useCase.getProvider();

			// get credentials
			Credentials credentials = credentialsService.getCredentials(provider);
			if (credentials != null) {

				// get provision log entry
				ProvisionLog provisionLog = provisionLogService.get(provisionLogId);
				if (provisionLog != null) {
					
					// check if user is allowed to deprovision the vm
					if (sessionUserService.isAllowed(provisionLog)) {
					
						// get response output stream
						OutputStream outputStream = response.getOutputStream();
						
						// provision VM
						useCaseService.deprovision(provisionLog, credentials, outputStream);
					}
					else {
						fail(String.format("You are not allowed to deprovision the entry with the id '%s'", provisionLogId), response);
					}
				}
				else {
					fail(String.format("No provision log entry found for id '%s'.", provisionLogId), response);
				}
			}
			else {
				fail(String.format("No credentials found for cloud provider '%s'. Please contact your administrator.", provider), response);
			}

		}
		catch(Exception e) {
			LOG.error(e.getMessage(), e);
		}

		// fill model
		fillModel(model, useCaseId);
	} 
	
	private static File writeMultipartFile(MultipartFile multipartFile) {

		File file = null;

		try {
			if (!multipartFile.isEmpty()) {

				String multipartFileName = multipartFile.getOriginalFilename();
				String prefix = FilenameUtils.getBaseName(multipartFileName);
				String suffix = Constants.CHAR_DOT + FilenameUtils.getExtension(multipartFileName);
				file = File.createTempFile(prefix, suffix);

				FileUtils.writeByteArrayToFile(file, multipartFile.getBytes());
			}
		}
		catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return file;
	}	
	
	private void writeFilesAndAddToMap(HttpServletRequest request, Map<String, Object> variableMap, List<File> tempFileList) {
		
		// get multipart request
		StandardMultipartHttpServletRequest multipartHttpServletRequest = (StandardMultipartHttpServletRequest) request;

		// get file map from request
		Map<String, MultipartFile> fileMap = multipartHttpServletRequest.getFileMap();
		
		for (Entry<String, MultipartFile> fileMapEntry : fileMap.entrySet()) {

			// write file uploads to disk
			File file = writeMultipartFile(fileMapEntry.getValue());
			if (file != null) {
				
				// add to temp file list
				tempFileList.add(file);

				// add file paths to variable map
				variableMap.put(fileMapEntry.getKey(), file.getAbsolutePath());
			}
		}
	}	
	
	private void extendWithUserData(Credentials credentials, Map<String, Object> variableMap) {
		variableMap.put(VAR_NAME_OWNER, sessionUserService.getUser().getEmail());
		variableMap.put(VAR_NAME_GROUP, credentials.getGroup());
	}	
	
	private void extendWithCreationDate(Map<String, Object> variableMap) {
		variableMap.put(VAR_NAME_CREATION_DATE, dateFormat.format(new Date()));
	}
	
	private void extendWithRandomId(Map<String, Object> variableMap) {
		variableMap.put(VAR_NAME_RANDOM_ID, RandomStringUtils.randomAlphanumeric(12).toLowerCase());
	}
	
	private void extendWithApplicationUrl(Map<String, Object> variableMap) {
		variableMap.put(VAR_NAME_APPLICATION_URL, applicationUrl);
	}	

	private File extendWithDefaultValues(List<Variable> variables, Map<String, Object> variableMap, List<File> tempFileList) throws IOException {
		
		File privateKeyFile = null;
		
		for (Variable variable : variables) {
			
			String variableName = variable.getName();
			
			if (!variableMap.containsKey(variableName)) {
				
				Object variableValue = null;
				
				if (!variable.getType().equals(VAR_TYPE_FILE)) {
					List<String> defaultsList = variable.getDefaults();
					if (!defaultsList.isEmpty()) {
						variableValue = defaultsList.get(variable.getIndex()); 
						variableMap.put(variableName, variableValue);
					}
				}
				else {
					if (variableName.contains(VAR_NAME_PART_KEY)) {
						privateKeyFile = generateKeyPair(variableMap, tempFileList);
					}
					else if (variableName.equals(VAR_NAME_SCRIPT_FILE)) {
						addEmptyScriptFile(variableMap, tempFileList, VAR_NAME_SCRIPT_FILE);
					}
					else if (variableName.equals(VAR_NAME_ANSIBLE_REQUIREMENTS_FILE)) {
						addEmptyScriptFile(variableMap, tempFileList, VAR_NAME_ANSIBLE_REQUIREMENTS_FILE);
					}
					else if (variableName.equals(VAR_NAME_ANSIBLE_PLAYBOOK_FILE)) {
						addEmptyScriptFile(variableMap, tempFileList, VAR_NAME_ANSIBLE_PLAYBOOK_FILE);
					}
				}
			}
		}
		
		return privateKeyFile;
	}

	private void addEmptyScriptFile(Map<String, Object> variableMap, List<File> tempFileList, String variableName) throws IOException {
		File emptyScriptFile = File.createTempFile(EMPTY_SCRIPT_NAME, Constants.CHAR_EMPTY);
		variableMap.put(variableName, emptyScriptFile.getAbsolutePath());
		tempFileList.add(emptyScriptFile);
	}
	
	private File generateKeyPair(Map<String, Object> variableMap, List<File> tempFileList) {
		
		File privateKeyFile = null;
		
		List<File> keyFileList = keyPairService.createKeyPair();
		for (File keyFile : keyFileList) {
			
			String keyFilePath = keyFile.getAbsolutePath();
			if (keyFilePath.endsWith(Constants.KEY_FILE_PUBLIC_SUFFIX)) {
				variableMap.put(VAR_NAME_PUBLIC_KEY_FILE, keyFile.getAbsolutePath());
			}
			else {
				variableMap.put(VAR_NAME_PRIVATE_KEY_FILE, keyFile.getAbsolutePath());
				privateKeyFile = keyFile;
			}
			
			tempFileList.add(keyFile);
		}
		
		return privateKeyFile;
	}	
	
	private List<Variable> validateValues(List<Variable> variables, Map<String, Object> variableMap) {
		
		List<Variable> errorList = new ArrayList<>();
		
		for (Variable variable : variables) {

			boolean errorFound = false;
			
			String variableName = variable.getName();
			if (variableMap.containsKey(variableName)) {
				String pattern = variable.getPattern();
				if (StringUtils.isNotEmpty(pattern)) {
					String variableValue = String.valueOf(variableMap.get(variableName));
					if (!Pattern.matches(pattern, variableValue)) {
						errorFound = true;
					}
				}
			}
			else if (variable.isRequired() && variable.getDefaults().isEmpty()) {
				errorFound = true;
			}
			
			List<Relation> relations = variable.getRelations();
			for (Relation relation : relations) {
				
				String relationName = relation.getName();
				String relationType = relation.getType();
				Object relationValue = variableMap.get(relationName);
				Object variableValue = variableMap.get(variableName);
				
				if (relationType.equals("equals") && !variableValue.equals(relationValue)) {
					errorFound = true;
				}
			}
			
			if (errorFound) {
				errorList.add(variable);
			}
		}
		
		return errorList;
	}
	
	private String renderVariableErrorMessage(List<Variable> errorList) throws IllegalAccessException {
		
		StringBuilder errorMessage = new StringBuilder();
		errorMessage.append("Parameter validation failed:");
		errorMessage.append(Constants.CHAR_NEW_LINE);
		errorMessage.append(Constants.CHAR_NEW_LINE);
		
		for (int i = 0; i < errorList.size(); i++) {
			
			errorMessage.append(renderVariable(errorList.get(i)));
			
			if (i < errorList.size() - 1) {
				errorMessage.append(Constants.CHAR_NEW_LINE);
				errorMessage.append(Constants.CHAR_NEW_LINE);
			}
		}
		
		return errorMessage.toString();
	}
	
	private String renderVariable(Variable variable) throws IllegalAccessException {
		
		StringBuilder variableBuilder = new StringBuilder();
		
		String variableName = variable.getName();
		variableBuilder.append(variableName);
		variableBuilder.append(Constants.CHAR_NEW_LINE);
		
		for (int i = 0; i < variableName.length(); i++) {
			variableBuilder.append(Constants.CHAR_DASH);
		}
		
		variableBuilder.append(Constants.CHAR_NEW_LINE);
		
		for (Field field : Variable.class.getDeclaredFields()) {
			field.setAccessible(true);
			variableBuilder.append(field.getName());
			variableBuilder.append(Constants.CHAR_DOUBLE_DOT);
			variableBuilder.append(Constants.CHAR_WHITESPACE);
			variableBuilder.append(field.get(variable));
			variableBuilder.append(Constants.CHAR_NEW_LINE);
		}
		
		return variableBuilder.toString();
	}	

	private void fillModel(Map<String, Object> model, String id) {

		fillModel(model);

		// create use case model
		UseCaseModel useCaseModel = new UseCaseModel();

		// set id
		useCaseModel.setId(id);

		// set variable groups
		useCaseModel.getVariableGroups(useCaseService.getVisibleVariableGroups(id));

		// set provision log list
		useCaseModel.setProvisionLogs(provisionLogService.getListByUseCaseId(id));

		model.put(MODEL_VAR_NAME, useCaseModel);
	}	
}