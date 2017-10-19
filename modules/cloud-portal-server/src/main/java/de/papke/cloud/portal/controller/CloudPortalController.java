package de.papke.cloud.portal.controller;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import de.papke.cloud.portal.model.Data;
import de.papke.cloud.portal.terraform.TerraformService;
import de.papke.cloud.portal.terraform.Variable;

/**
 * Created by chris on 16.10.17.
 */
@Controller
public class CloudPortalController {

	private static final Logger LOG = LoggerFactory.getLogger(CloudPortalController.class);

	@Value("${application.title}")
	private String applicationTitle;

	@Autowired
	private TerraformService terraformService;

	/**
	 * Method for returning the model and view for the index page.
	 *
	 * @param model
	 * @return
	 */
	@GetMapping(path = "/")
	public String index(Map<String, Object> model) throws IOException {

		// put data object into model
		model.put("self", getData(null));

		// return view name
		return "index";
	}

	/**
	 * Method for returning the model and view for the login page.
	 *
	 * @param model
	 * @return
	 */
	@GetMapping(path = "/login")
	public String login(Map<String, Object> model) {

		// put data object into model
		model.put("self", getData(null));

		// return view name
		return "login";
	}

	/**
	 * Method for returning the model and view for the user profile page.
	 *
	 * @param model
	 * @return
	 */
	@GetMapping(path = "/user/profile")
	public String vmCreate(Map<String, Object> model) throws IOException {

		// put data object into model
		model.put("self", getData(null));

		// return view name
		return "user-profile";
	}	
	
	/**
	 * Method for returning the model and view for the create vm page.
	 *
	 * @param model
	 * @return
	 */
	@GetMapping(path = "/vm/create/{cloudProvider}")
	public String vmCreate(Map<String, Object> model, @PathVariable String cloudProvider) throws IOException {

		// put data object into model
		model.put("self", getData(cloudProvider));

		// return view name
		return "vm-create";
	}

	/**
	 * Method for returning the model and view for the provision vm page.
	 *
	 * @param model
	 * @return
	 */
	@PostMapping(path = "/vm/provision/{cloudProvider}", produces="text/plain")
	@ResponseBody
	public void vmProvision(
			@PathVariable String cloudProvider,
			@RequestParam Map<String, Object> variableMap,
			@RequestParam("bootstrap-public-key-file") MultipartFile sshPublicKeyFileUpload,
			@RequestParam("bootstrap-private-key-file") MultipartFile sshPrivateKeyFileUpload,
			@RequestParam("bootstrap-script-file") MultipartFile bootstrapScriptFileUpload,
			HttpServletResponse response) {
		
		File sshPublicKeyFile = null;
		File sshPrivateKeyFile = null;
		File bootstrapScriptFile = null;

		try {

			// write file uploads to disk
			sshPublicKeyFile = writeMultipartFile(sshPublicKeyFileUpload);
			sshPrivateKeyFile = writeMultipartFile(sshPrivateKeyFileUpload);
			bootstrapScriptFile = writeMultipartFile(bootstrapScriptFileUpload);

			// add file paths to variable map
			variableMap.put("bootstrap-public-key-file", sshPublicKeyFile.getAbsolutePath());
			variableMap.put("bootstrap-private-key-file", sshPrivateKeyFile.getAbsolutePath());
			variableMap.put("bootstrap-script-file", bootstrapScriptFile.getAbsolutePath());

			// get response output stream
			OutputStream outputStream = response.getOutputStream();

			// provision VM
			terraformService.provisionVM(cloudProvider, variableMap, outputStream);
		}
		catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		finally {
			
			// remove all uploaded files
			if (sshPublicKeyFile != null) {
				sshPublicKeyFile.delete();
			}
			if (sshPrivateKeyFile != null) {
				sshPrivateKeyFile.delete();
			}
			if (bootstrapScriptFile != null) {
				bootstrapScriptFile.delete();
			}
		}
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

	private Data getData(String cloudProvider) {

		// create data object
		Data data = new Data();

		// set application title
		data.setApplicationTitle(applicationTitle);
		
		// set provider
		data.setCloudProvider(cloudProvider);
		
		// set cloud provider defaults map
		Map<String, Map<String, List<Variable>>> cloudProviderDefaultsMap = terraformService.getProviderDefaultsMap();

		// set cloud providers
		List<String> cloudProviderList = new ArrayList<String>();
		cloudProviderList.addAll(cloudProviderDefaultsMap.keySet());
		data.setCloudProviderList(cloudProviderList);
		
		// set cloud provider defaults
		if (StringUtils.isNotEmpty(cloudProvider)) {
			data.setCloudProviderDefaultsMap(cloudProviderDefaultsMap.get(cloudProvider));
		}

		// set username
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		data.setUsername((String) authentication.getPrincipal());

		// set groups
		List<String> groupList = new ArrayList<>();
		for (GrantedAuthority grantedAuthority : authentication.getAuthorities()) {
			groupList.add(grantedAuthority.toString());
		}
		data.setGroupList(groupList);
		
		return data;
	}
}
