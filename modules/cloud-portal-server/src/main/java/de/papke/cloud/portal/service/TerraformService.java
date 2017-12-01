package de.papke.cloud.portal.service;

import java.io.File;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.papke.cloud.portal.Constants;
import de.papke.cloud.portal.constants.AwsConstants;
import de.papke.cloud.portal.constants.AzureConstants;
import de.papke.cloud.portal.constants.VSphereConstants;
import de.papke.cloud.portal.pojo.CommandResult;
import de.papke.cloud.portal.pojo.Credentials;
import de.papke.cloud.portal.terraform.HCLParser;
import de.papke.cloud.portal.terraform.Variable;

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

	private Map<String, Map<String, List<Variable>>> providerDefaultsMap = new HashMap<>();

	@PostConstruct
	public void init() {

		try {
			URL url = getClass().getClassLoader().getResource("terraform");
			File terraformFolder = new File(url.toURI());
			if (!terraformFolder.isFile()) {
				File[] providerFolderArray = terraformFolder.listFiles();
				for (File providerFolder : providerFolderArray) {
					File variableFile = new File(new URI(providerFolder.toURI() + "/vars.tf"));
					Map<String, List<Variable>> variableMap = getProviderDefaults(variableFile);
					providerDefaultsMap.put(providerFolder.getName(), variableMap);
				}
			}
		}
		catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}

	public CommandResult execute(String action, Credentials credentials, Map<String, Object> variableMap, OutputStream outputStream, File tmpFolder) {

		CommandResult commandResult = null;

		try {

			// print waiting message
			outputStream.write(TEXT_INTRODUCTION.getBytes());
			outputStream.flush();
			
			// execute init command
			String initCommand = buildInitCommand(terraformPath);
			commandExecutorService.execute(initCommand, tmpFolder, outputStream);

			// get action to execute
			if (StringUtils.isNotEmpty(action)) {

				// get execution map
				Map<String, Object> executionMap = getExecutionMap(credentials, variableMap);
				
				// build the command string
				String commandString = buildActionCommand(terraformPath, action, executionMap);

				// execute action command
				commandResult = commandExecutorService.execute(commandString, tmpFolder, outputStream);
			}
		}
		catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return commandResult;
	}

	private String buildInitCommand(String terraformPath) {
		return terraformPath + Constants.CHAR_WHITESPACE + Constants.ACTION_INIT + Constants.CHAR_WHITESPACE + FLAG_NO_COLOR;
	}

	private String buildActionCommand(String terraformPath, String action, Map<String, Object> variableMap) {

		StringBuilder commandStringBuilder = new StringBuilder();
		commandStringBuilder.append(terraformPath);
		commandStringBuilder.append(Constants.CHAR_WHITESPACE);
		commandStringBuilder.append(action);
		
		if (action.equals(Constants.ACTION_DESTROY)) {
			commandStringBuilder.append(Constants.CHAR_WHITESPACE);
			commandStringBuilder.append(FLAG_FORCE);
		}
		
		commandStringBuilder.append(Constants.CHAR_WHITESPACE);
		commandStringBuilder.append(FLAG_NO_COLOR);


		for (String variableName : variableMap.keySet()) {

			String variableValue = (String) variableMap.get(variableName);

			commandStringBuilder.append(Constants.CHAR_WHITESPACE);
			commandStringBuilder.append(FLAG_VAR);
			commandStringBuilder.append(Constants.CHAR_WHITESPACE);
			commandStringBuilder.append(Constants.CHAR_QUOTE);
			commandStringBuilder.append(variableName);
			commandStringBuilder.append(Constants.CHAR_EQUAL);
			commandStringBuilder.append(variableValue.equals("on") ? "true" : variableValue);
			commandStringBuilder.append(Constants.CHAR_QUOTE);
		}

		return commandStringBuilder.toString();
	}
	
	private Map<String, Object> getExecutionMap(Credentials credentials, Map<String, Object> variableMap) {
		
		Map<String, Object> executionMap = new HashMap<>();

		String cloudProvider = credentials.getProvider();
		if (cloudProvider.equals(AzureConstants.PROVIDER)) {
			executionMap.put("credentials-subscription-id-string", credentials.getSecretMap().get(AzureConstants.SUBSCRIPTION_ID));
			executionMap.put("credentials-tenant-id-string", credentials.getSecretMap().get(AzureConstants.TENANT_ID));
			executionMap.put("credentials-client-id-string", credentials.getSecretMap().get(AzureConstants.CLIENT_ID));
			executionMap.put("credentials-client-secret-string", credentials.getSecretMap().get(AzureConstants.CLIENT_SECRET));
		}
		else if (cloudProvider.equals(AwsConstants.PROVIDER)) {
			executionMap.put("credentials-access-key-string", credentials.getSecretMap().get(AwsConstants.ACCESS_KEY));
			executionMap.put("credentials-secret-key-string", credentials.getSecretMap().get(AwsConstants.SECRET_KEY));
		}
		else if (cloudProvider.equals(VSphereConstants.PROVIDER)) {
			executionMap.put("credentials-vcenter-hostname-string", credentials.getSecretMap().get(VSphereConstants.VCENTER_HOSTNAME));
			executionMap.put("credentials-vcenter-username-string", credentials.getSecretMap().get(VSphereConstants.VCENTER_USERNAME));
			executionMap.put("credentials-vcenter-password-string", credentials.getSecretMap().get(VSphereConstants.VCENTER_PASSWORD));
		}
		
		executionMap.putAll(variableMap);
		
		return executionMap;
	}

	public Map<String, List<Variable>> getProviderDefaults(File providerDefaultsFile) {

		Map<String, List<Variable>> variableMap = new HashMap<>();

		try {
			HCLParser hclParser = new HCLParser(providerDefaultsFile);
			variableMap = hclParser.parse();
			variableMap.remove("credentials");
		}
		catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return variableMap;
	}

	public Map<String, Map<String, List<Variable>>> getProviderDefaultsMap() {
		return providerDefaultsMap;
	}
}
