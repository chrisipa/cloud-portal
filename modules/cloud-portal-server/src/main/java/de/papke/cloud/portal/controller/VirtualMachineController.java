package de.papke.cloud.portal.controller;

import java.io.File;
import java.io.OutputStream;
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

import de.papke.cloud.portal.model.VirtualMachineModel;
import de.papke.cloud.portal.pojo.Credentials;
import de.papke.cloud.portal.pojo.ProvisionLog;
import de.papke.cloud.portal.pojo.User;
import de.papke.cloud.portal.service.CredentialsService;
import de.papke.cloud.portal.service.ProvisionLogService;
import de.papke.cloud.portal.service.TerraformService;
import de.papke.cloud.portal.service.UserService;
import de.papke.cloud.portal.service.VirtualMachineService;
import de.papke.cloud.portal.terraform.Variable;

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
	private UserService userService;

	/**
	 * Method for returning the model and view for the create vm page.
	 *
	 * @param model
	 * @return
	 */
	@GetMapping(path = PREFIX + "/list/form/{cloudProvider}")
	public String vmList(Map<String, Object> model, @PathVariable String cloudProvider) {

		// fill model
		fillModel(model, cloudProvider);

		// return view name
		return "vm-list-form";
	}

	/**
	 * Method for returning the model and view for the create vm page.
	 *
	 * @param model
	 * @return
	 */
	@GetMapping(path = PREFIX + "/create/form/{cloudProvider}")
	public String vmCreate(Map<String, Object> model, @PathVariable String cloudProvider) {

		// fill model
		fillModel(model, cloudProvider);

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
			@RequestParam String cloudProvider,
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

			// get credentials
			Credentials credentials = credentialsService.getCredentials(cloudProvider);
			if (credentials != null) {

				// get response output stream
				OutputStream outputStream = response.getOutputStream();

				// provision VM
				virtualMachineService.provision(action, credentials, variableMap, outputStream);
			}
			else {
				response.getWriter().println(String.format("No credentials found for cloud provider '%s'. Please contact your administrator.", cloudProvider));
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

	@GetMapping(path = PREFIX + "/delete/action/{cloudProvider}/{id}")
	public void deprovision(Map<String, Object> model,
			@PathVariable String cloudProvider,
			@PathVariable String id,
			HttpServletResponse response) {

		try {

			// get credentials
			Credentials credentials = credentialsService.getCredentials(cloudProvider);
			if (credentials != null) {

				// get username
				User user = userService.getUser();
				String username = user.getUsername();
				
				// get provision log entry
				ProvisionLog provisionLog = provisionLogService.get(username, id);
				if (provisionLog != null) {
					
					// get response output stream
					OutputStream outputStream = response.getOutputStream();
					
					// provision VM
					virtualMachineService.deprovision(provisionLog, credentials, outputStream);
				}
				else {
					response.getWriter().println(String.format("No provision log entry found for username '%s' and id '%s'.", username, id));
				}
			}
			else {
				response.getWriter().println(String.format("No credentials found for cloud provider '%s'. Please contact your administrator.", cloudProvider));
			}

		}
		catch(Exception e) {
			LOG.error(e.getMessage(), e);
		}

		// fill model
		fillModel(model, cloudProvider);
	} 

	private File writeMultipartFile(MultipartFile multipartFile) {

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

	private void fillModel(Map<String, Object> model, String cloudProvider) {

		fillModel(model);

		// get cloud provider defaults map
		Map<String, Map<String, List<Variable>>> cloudProviderDefaultsMap = terraformService.getProviderDefaultsMap();

		// create virtual machine model
		VirtualMachineModel virtualMachineModel = new VirtualMachineModel();

		// set cloud provider
		virtualMachineModel.setCloudProvider(cloudProvider);

		// set cloud provider defaults
		virtualMachineModel.setCloudProviderDefaultsMap(cloudProviderDefaultsMap.get(cloudProvider));

		// set provision log list
		virtualMachineModel.setProvisionLogList(provisionLogService.getList(cloudProvider));

		model.put(MODEL_VAR_NAME, virtualMachineModel);
	}	
}