package de.papke.cloud.portal.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import de.papke.cloud.portal.constants.AwsConstants;
import de.papke.cloud.portal.constants.AzureConstants;
import de.papke.cloud.portal.constants.Constants;
import de.papke.cloud.portal.constants.VSphereConstants;
import de.papke.cloud.portal.pojo.CommandResult;
import de.papke.cloud.portal.pojo.Credentials;
import de.papke.cloud.portal.pojo.VariableGroup;

/**
 * Created by chris on 16.10.17.
 */
@Service
public class TerraformService {

	private static final Logger LOG = LoggerFactory.getLogger(TerraformService.class);

	private static final String TEXT_INTRODUCTION = "TERRAFORM IS WORKING ON YOUR ACTION. YOU WILL GET AN EMAIL WITH THE RESULTS. PLEASE BE PATIENT!!!\n";

	private static final String FLAG_NO_COLOR = "-no-color";
	private static final String FLAG_VAR = "-var";
	private static final String FLAG_FORCE = "-force";

	@Autowired
	private CommandExecutorService commandExecutorService;

	@Value("${terraform.path}")
	private String terraformPath;

	private Map<String, List<VariableGroup>> providerDefaults = new HashMap<>();

	@SuppressWarnings("unchecked")
	@PostConstruct
	public void init() {

		try {
			Yaml yaml = new Yaml();
			URL url = getClass().getClassLoader().getResource("terraform");
			File terraformFolder = new File(url.toURI());
			if (!terraformFolder.isFile()) {
				File[] providerFolderArray = terraformFolder.listFiles();
				for (File providerFolder : providerFolderArray) {
					File variableFile = new File(new URI(providerFolder.toURI() + "/gui.yml"));
					if (variableFile.exists()) {
						List<VariableGroup> variableGroupList = yaml.loadAs(new FileInputStream(variableFile), List.class);
						providerDefaults.put(providerFolder.getName(), variableGroupList);
					}

				}
			}
		}
		catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public CommandResult execute(String action, Credentials credentials, Map<String, Object> variableMap, OutputStream outputStream, File tmpFolder) {

		CommandResult commandResult = null;

		try {

			// print waiting message
			outputStream.write(TEXT_INTRODUCTION.getBytes());
			outputStream.flush();

			// execute init command
			CommandLine initCommand = buildInitCommand(terraformPath);
			commandExecutorService.execute(initCommand, tmpFolder, outputStream);

			// get action to execute
			if (StringUtils.isNotEmpty(action)) {

				// get execution map
				Map<String, Object> executionMap = getExecutionMap(credentials, variableMap);

				// build the command string
				CommandLine actionCommand = buildActionCommand(terraformPath, action, executionMap);

				// execute action command
				commandResult = commandExecutorService.execute(actionCommand, tmpFolder, outputStream);
			}
		}
		catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return commandResult;
	}

	private CommandLine buildInitCommand(String terraformPath) {
		
		CommandLine initCommand = new CommandLine(terraformPath);
		initCommand.addArgument(Constants.ACTION_INIT);
		initCommand.addArgument(FLAG_NO_COLOR);
		
		return initCommand;
	}

	private CommandLine buildActionCommand(String terraformPath, String action, Map<String, Object> variableMap) {
		
		CommandLine actionCommand = new CommandLine(terraformPath);
		actionCommand.addArgument(action);
		
		if (action.equals(Constants.ACTION_DESTROY)) {
			actionCommand.addArgument(FLAG_FORCE);
		}

		actionCommand.addArgument(FLAG_NO_COLOR);

		for (Entry<String, Object> variableEntry : variableMap.entrySet()) {

			String variableName = variableEntry.getKey();
			String variableValue = (String) variableEntry.getValue();
			String variableString = variableName + Constants.CHAR_EQUAL + (variableValue.equals("on") ? "true" : variableValue);

			actionCommand.addArgument(FLAG_VAR);
			actionCommand.addArgument(variableString, false);
		}

		return actionCommand;
	}

	private Map<String, Object> getExecutionMap(Credentials credentials, Map<String, Object> variableMap) {

		Map<String, Object> executionMap = new HashMap<>();

		String provider = credentials.getProvider();
		if (provider.equals(AzureConstants.PROVIDER)) {
			executionMap.put("subscription_id", credentials.getSecretMap().get(AzureConstants.SUBSCRIPTION_ID));
			executionMap.put("tenant_id", credentials.getSecretMap().get(AzureConstants.TENANT_ID));
			executionMap.put("client_id", credentials.getSecretMap().get(AzureConstants.CLIENT_ID));
			executionMap.put("client_secret", credentials.getSecretMap().get(AzureConstants.CLIENT_SECRET));
		}
		else if (provider.equals(AwsConstants.PROVIDER)) {
			executionMap.put("access_key", credentials.getSecretMap().get(AwsConstants.ACCESS_KEY));
			executionMap.put("secret_key", credentials.getSecretMap().get(AwsConstants.SECRET_KEY));
		}
		else if (provider.equals(VSphereConstants.PROVIDER)) {
			executionMap.put("vcenter_hostname", credentials.getSecretMap().get(VSphereConstants.VCENTER_HOSTNAME));
			executionMap.put("vcenter_image_folder", credentials.getSecretMap().get(VSphereConstants.VCENTER_IMAGE_FOLDER));
			executionMap.put("vcenter_target_folder", credentials.getSecretMap().get(VSphereConstants.VCENTER_TARGET_FOLDER));
			executionMap.put("vcenter_username", credentials.getSecretMap().get(VSphereConstants.VCENTER_USERNAME));
			executionMap.put("vcenter_password", credentials.getSecretMap().get(VSphereConstants.VCENTER_PASSWORD));
		}

		executionMap.putAll(variableMap);

		return executionMap;
	}

	public Map<String, List<VariableGroup>> getProviderDefaults() {
		return providerDefaults;
	}
}
