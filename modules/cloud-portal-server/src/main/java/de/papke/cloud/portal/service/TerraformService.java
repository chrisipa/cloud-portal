package de.papke.cloud.portal.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import de.papke.cloud.portal.constants.Constants;
import de.papke.cloud.portal.pojo.CommandResult;
import de.papke.cloud.portal.pojo.Credentials;
import de.papke.cloud.portal.pojo.Variable;
import de.papke.cloud.portal.pojo.VariableConfig;
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

	private static final String VARIABLE_IDENTIFIER = "variable";

	@Autowired
	private CommandExecutorService commandExecutorService;

	@Autowired
	private ResourceService resourceService;

	@Value("${terraform.path}")
	private String terraformPath;

	private Map<String, List<VariableGroup>> providerDefaults = new HashMap<>();

	@PostConstruct
	public void init() {

		try {

			Constructor constructor = new Constructor(VariableConfig.class);

			TypeDescription variableConfigTypeDescription = new TypeDescription(VariableConfig.class);
			variableConfigTypeDescription.putListPropertyType("variableGroups", VariableGroup.class);
			constructor.addTypeDescription(variableConfigTypeDescription);

			TypeDescription variableGroupTypeDescription = new TypeDescription(VariableGroup.class);
			variableGroupTypeDescription.putListPropertyType("variables", Variable.class);
			constructor.addTypeDescription(variableGroupTypeDescription);

			Yaml yaml = new Yaml(constructor);

			File terraformFolder = resourceService.getClasspathResource("terraform");
			if (!terraformFolder.isFile()) {
				File[] providerFolderArray = terraformFolder.listFiles();
				for (File providerFolder : providerFolderArray) {
					File variableFile = new File(new URI(providerFolder.toURI() + "/variables.yml"));
					if (variableFile.exists()) {
						VariableConfig variableConfig = yaml.loadAs(new FileInputStream(variableFile), VariableConfig.class);
						List<VariableGroup> variableGroupList = variableConfig.getVariableGroups();
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

			// generate variable file
			generateVariablesFile(credentials.getProvider(), tmpFolder);

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

	private void generateVariablesFile(String provider, File tmpFolder) throws IOException {

		File variablesFile = new File(tmpFolder.getAbsolutePath() + File.separator + "variables.tf");
		StringBuilder variablesBuilder = new StringBuilder();

		List<VariableGroup> variableGroupList = providerDefaults.get(provider);
		for (VariableGroup variableGroup : variableGroupList) {
			for (Variable variable : variableGroup.getVariables()) {
				variablesBuilder
				.append(VARIABLE_IDENTIFIER)
				.append(Constants.CHAR_WHITESPACE)
				.append(Constants.CHAR_DOUBLE_QUOTE)
				.append(variable.getName())
				.append(Constants.CHAR_DOUBLE_QUOTE)
				.append(Constants.CHAR_WHITESPACE)
				.append(Constants.CHAR_BRACE_OPEN)
				.append(Constants.CHAR_BRACE_CLOSE)
				.append(Constants.CHAR_NEW_LINE);
			}
		}

		FileUtils.writeStringToFile(variablesFile, variablesBuilder.toString(), StandardCharsets.UTF_8, false);
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
		executionMap.putAll(variableMap);
		executionMap.putAll(credentials.getSecretMap());

		return executionMap;
	}

	public Map<String, List<VariableGroup>> getProviderDefaults() {
		return providerDefaults;
	}

	public Map<String, List<VariableGroup>> getVisibleProviderDefaults() {

		Map<String, List<VariableGroup>> visibleProviderDefaults = new HashMap<>();

		for (Entry<String, List<VariableGroup>> entry : providerDefaults.entrySet()) {
			List<VariableGroup> visibleVariableGroupList = new ArrayList<>();
			for (VariableGroup variableGroup : entry.getValue()) {
				if (!variableGroup.isHidden()) {
					visibleVariableGroupList.add(variableGroup);
				}
			}
			visibleProviderDefaults.put(entry.getKey(), visibleVariableGroupList);
		}

		return visibleProviderDefaults;
	}

	public List<Variable> getVisibleVariables(String provider) {
		
		List<Variable> variables = new ArrayList<>();
		
		for (VariableGroup variableGroup : providerDefaults.get(provider)) {
			if (!variableGroup.isHidden()) {
				for (Variable variable : variableGroup.getVariables()) {
					variables.add(variable);
				}
			}
		}
		
		return variables;
	}
}
