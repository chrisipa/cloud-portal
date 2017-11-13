package de.papke.cloud.portal.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
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

import de.papke.cloud.portal.constants.AwsConstants;
import de.papke.cloud.portal.constants.AzureConstants;
import de.papke.cloud.portal.constants.VSphereConstants;
import de.papke.cloud.portal.model.VirtualMachineModel;
import de.papke.cloud.portal.pojo.CommandResult;
import de.papke.cloud.portal.pojo.Credentials;
import de.papke.cloud.portal.pojo.ProvisionLog;
import de.papke.cloud.portal.service.CredentialsService;
import de.papke.cloud.portal.service.ProvisionLogService;
import de.papke.cloud.portal.service.TerraformService;
import de.papke.cloud.portal.service.ZipService;
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
	private TerraformService terraformService;

	@Autowired
	private CredentialsService credentialsService;

	@Autowired
	private ProvisionLogService provisionLogService;

	@Autowired
	private ZipService zipService;

	/**
	 * Method for returning the model and view for the create vm page.
	 *
	 * @param model
	 * @return
	 */
	@GetMapping(path = PREFIX + "/list/form/{cloudProvider}")
	public String vmList(Map<String, Object> model, @PathVariable String cloudProvider) throws IOException {

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
	public String vmCreate(Map<String, Object> model, @PathVariable String cloudProvider) throws IOException {

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
	public void vmProvision(
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
			for (String fileName : fileMap.keySet()) {

				// write file uploads to disk
				File file = writeMultipartFile(fileMap.get(fileName));

				// add to temp file list
				tempFileList.add(file);

				// add file paths to variable map
				variableMap.put(fileName, file.getAbsolutePath());
			}

			// get credentials
			Credentials credentials = credentialsService.getCredentials(cloudProvider);
			if (credentials != null) {

				// add credentials to map
				addCredentialsToMap(cloudProvider, credentials, variableMap);

				// get response output stream
				OutputStream outputStream = response.getOutputStream();

				// provision VM
				terraformService.provisionVM(action, cloudProvider, variableMap, outputStream);
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
				if (tempFile != null) {
					tempFile.delete();
				}
			}
		}
	}    

	@GetMapping(path = PREFIX + "/delete/action/{cloudProvider}/{id}")
	public String vmDeprovision(Map<String, Object> model,
			@PathVariable String cloudProvider,
			@PathVariable String id) {

		File tmpFile = null; 
		File tmpFolder = null;

		try {

			// get provision log
			ProvisionLog provisionLog = provisionLogService.get(id);

			// get variable map
			Map<String, Object> variableMap = provisionLog.getVariableMap(); 

			// add credentials to map
			Credentials credentials = credentialsService.getCredentials(cloudProvider);
			if (credentials != null) {
				addCredentialsToMap(cloudProvider, credentials, variableMap);
			}

			// get resource folder
			tmpFolder = getTmpFolder();
			tmpFile = File.createTempFile("test", ".zip");
			IOUtils.write(provisionLog.getResult(), new FileOutputStream(tmpFile));
			zipService.unzip(tmpFile, tmpFolder);
			File resourceFolder = tmpFolder.listFiles()[0];

			// destroy vm with terraform
			CommandResult commandResult = terraformService.provisionVM("destroy -force", cloudProvider, variableMap, resourceFolder);
			
			// remove provision log entry
			if (commandResult.isSuccess()) {
				provisionLogService.delete(id);
			}
			

		}
		catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		finally {
			if (tmpFile != null) {
				tmpFile.delete();
			}
			if (tmpFolder != null) {
				try {
					FileUtils.deleteDirectory(tmpFolder);
				}
				catch (Exception e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}

		// fill model
		fillModel(model, cloudProvider);

		// return to list view
		return "vm-list-form";
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

	private void addCredentialsToMap(String cloudProvider, Credentials credentials, Map<String, Object> variableMap) {

		if (cloudProvider.equals(AzureConstants.PROVIDER)) {
			variableMap.put("credentials-subscription-id-string", credentials.getSecretMap().get(AzureConstants.SUBSCRIPTION_ID));
			variableMap.put("credentials-tenant-id-string", credentials.getSecretMap().get(AzureConstants.TENANT_ID));
			variableMap.put("credentials-client-id-string", credentials.getSecretMap().get(AzureConstants.CLIENT_ID));
			variableMap.put("credentials-client-secret-string", credentials.getSecretMap().get(AzureConstants.CLIENT_SECRET));
		}
		else if (cloudProvider.equals(AwsConstants.PROVIDER)) {
			variableMap.put("credentials-access-key-string", credentials.getSecretMap().get(AwsConstants.ACCESS_KEY));
			variableMap.put("credentials-secret-key-string", credentials.getSecretMap().get(AwsConstants.SECRET_KEY));
		}
		else if (cloudProvider.equals(VSphereConstants.PROVIDER)) {
			variableMap.put("credentials-vcenter-hostname-string", credentials.getSecretMap().get(VSphereConstants.VCENTER_HOSTNAME));
			variableMap.put("credentials-vcenter-username-string", credentials.getSecretMap().get(VSphereConstants.VCENTER_USERNAME));
			variableMap.put("credentials-vcenter-password-string", credentials.getSecretMap().get(VSphereConstants.VCENTER_PASSWORD));
		}
	}

	private static final File getTmpFolder() {

		File tmpFolder = new File(System.getProperty("java.io.tmpdir") + "/tmp" + System.nanoTime());
		tmpFolder.mkdirs();

		return tmpFolder;
	}
}
