package de.papke.cloud.portal.controller;

import java.io.File;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import de.papke.cloud.portal.constants.Constants;
import de.papke.cloud.portal.model.VirtualMachineModel;
import de.papke.cloud.portal.pojo.Credentials;
import de.papke.cloud.portal.pojo.ProvisionLog;
import de.papke.cloud.portal.pojo.Variable;
import de.papke.cloud.portal.pojo.VariableGroup;
import de.papke.cloud.portal.service.CredentialsService;
import de.papke.cloud.portal.service.KeyPairService;
import de.papke.cloud.portal.service.ProvisionLogService;
import de.papke.cloud.portal.service.SessionUserService;
import de.papke.cloud.portal.service.TerraformService;
import de.papke.cloud.portal.service.VirtualMachineService;

/**
 * Created by chris on 16.10.17.
 */
@Controller
public class VirtualMachineController extends ApplicationController {

	private static final Logger LOG = LoggerFactory.getLogger(VirtualMachineController.class);

	private static final String PREFIX = "/vm";
	private static final String MODEL_VAR_NAME = "virtualMachine";

	@Autowired
	private CredentialsService credentialsService;

	@Autowired
	private VirtualMachineService virtualMachineService;

	@Autowired
	private TerraformService terraformService;

	@Autowired
	private ProvisionLogService provisionLogService;
	
	@Autowired
	private KeyPairService keyPairService;
	
	@Autowired
	private SessionUserService sessionUserService;

	/**
	 * Method for returning the model and view for the create vm page.
	 *
	 * @param model
	 * @return
	 */
	@GetMapping(path = PREFIX + "/list/form/{provider}")
	public String list(Map<String, Object> model, @PathVariable String provider) {

		// fill model
		fillModel(model, provider);

		// return view name
		return "vm-list-form";
	}

	/**
	 * Method for returning the model and view for the create vm page.
	 *
	 * @param model
	 * @return
	 */
	@GetMapping(path = PREFIX + "/create/form/{provider}")
	public String create(Map<String, Object> model, @PathVariable String provider) {

		// fill model
		fillModel(model, provider);

		// return view name
		return "vm-create-form";
	}

	/**
	 * Method for returning the model and view for the provision vm page.
	 *
	 * @param model
	 * @return
	 */
	@PostMapping(path = PREFIX + "/create/action/{action}", produces="text/plain")
	@ResponseBody
	public void provision(
			@PathVariable String action,
			@RequestParam String provider,
			@RequestParam Map<String, Object> variableMap,
			HttpServletRequest request,
			HttpServletResponse response) {

		List<File> tempFileList = new ArrayList<>();

		try {

			// get multipart request
			StandardMultipartHttpServletRequest multipartHttpServletRequest = (StandardMultipartHttpServletRequest) request;

			// get file map from request
			Map<String, MultipartFile> fileMap = multipartHttpServletRequest.getFileMap();

			// iterate over file map
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
			
			// fill up optional values
			File privateKeyFile = null;
			List<Variable> optionalVariablesList = terraformService.getOptionalVariables(provider);
			for (Variable variable : optionalVariablesList) {
				
				String variableName = variable.getName();
				Object variableValue = variableMap.get(variableName); 
				
				if (variableValue == null) {
					
					String variableType = variable.getType();
					
					if (variableType.equals("file")) {
						
						if (variableName.contains("key")) {
						
							List<File> keyFileList = keyPairService.createKeyPair();
							
							for (File keyFile : keyFileList) {
								
								String keyFilePath = keyFile.getAbsolutePath();
								if (keyFilePath.endsWith(Constants.KEY_FILE_SUFFIX)) {
									variableMap.put("public_key_file", keyFile.getAbsolutePath());
								}
								else {
									variableMap.put("private_key_file", keyFile.getAbsolutePath());
									privateKeyFile = keyFile;
								}
								
								tempFileList.add(keyFile);
							}
						}
						else if (variableName.contains("script")) {
							
							StringBuilder scriptPath = new StringBuilder("script/default.");
							
							String imageName = ((String) variableMap.get("image_name")).toLowerCase();
							if (imageName.contains("windows")) {
								scriptPath.append("ps1");
							}
							else if (imageName.contains("linux")) {
								scriptPath.append("sh");
							}
							
							URL scriptUrl = getClass().getClassLoader().getResource(scriptPath.toString());
							File scriptFile = new File(scriptUrl.toURI());
							variableMap.put("script_file", scriptFile.getAbsolutePath());
						}
					}
				}
			}

			// get credentials
			Credentials credentials = credentialsService.getCredentials(provider);
			if (credentials != null) {

				// get response output stream
				OutputStream outputStream = response.getOutputStream();

				// provision VM
				virtualMachineService.provision(action, credentials, variableMap, privateKeyFile, outputStream);
			}
			else {
				response.getWriter().println(String.format("No credentials found for cloud provider '%s'. Please contact your administrator.", provider));
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

	@GetMapping(path = PREFIX + "/delete/action/{provider}/{id}")
	public void deprovision(Map<String, Object> model,
			@PathVariable String provider,
			@PathVariable String id,
			HttpServletResponse response) {

		try {

			// get credentials
			Credentials credentials = credentialsService.getCredentials(provider);
			if (credentials != null) {

				// get provision log entry
				ProvisionLog provisionLog = provisionLogService.get(id);
				if (provisionLog != null) {
					
					// get group
					String group = provisionLog.getGroup();
					
					// check if user is allowed to deprovision the vm
					if (sessionUserService.getUser().getGroups().contains(group)) {
					
						// get response output stream
						OutputStream outputStream = response.getOutputStream();
						
						// provision VM
						virtualMachineService.deprovision(provisionLog, credentials, outputStream);
					}
					else {
						response.getWriter().println(String.format("You are not allowed to deprovision the entry with the id '%s'", id));
					}
				}
				else {
					response.getWriter().println(String.format("No provision log entry found for id '%s'.", id));
				}
			}
			else {
				response.getWriter().println(String.format("No credentials found for cloud provider '%s'. Please contact your administrator.", provider));
			}

		}
		catch(Exception e) {
			LOG.error(e.getMessage(), e);
		}

		// fill model
		fillModel(model, provider);
	} 

	private static File writeMultipartFile(MultipartFile multipartFile) {

		File file = null;

		try {
			if (!multipartFile.isEmpty()) {

				String multipartFileName = multipartFile.getOriginalFilename();
				String prefix = FilenameUtils.getBaseName(multipartFileName);
				String suffix = FilenameUtils.getExtension(multipartFileName);
				file = File.createTempFile(prefix, suffix);

				FileUtils.writeByteArrayToFile(file, multipartFile.getBytes());
			}
		}
		catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return file;
	}

	private void fillModel(Map<String, Object> model, String provider) {

		fillModel(model);

		// get cloud provider defaults map
		Map<String, List<VariableGroup>> cloudProviderDefaultsMap = terraformService.getProviderDefaults();

		// create virtual machine model
		VirtualMachineModel virtualMachineModel = new VirtualMachineModel();

		// set cloud provider
		virtualMachineModel.setProvider(provider);

		// set cloud provider defaults
		virtualMachineModel.setProviderDefaultsList(cloudProviderDefaultsMap.get(provider));

		// set provision log list
		virtualMachineModel.setProvisionLogList(provisionLogService.getList(provider));

		model.put(MODEL_VAR_NAME, virtualMachineModel);
	}	
}