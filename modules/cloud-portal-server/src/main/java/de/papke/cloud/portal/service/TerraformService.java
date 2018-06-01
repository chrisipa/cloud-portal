package de.papke.cloud.portal.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
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

import de.papke.cloud.portal.constants.Constants;
import de.papke.cloud.portal.pojo.CommandResult;
import de.papke.cloud.portal.pojo.Credentials;
import de.papke.cloud.portal.pojo.UseCase;
import de.papke.cloud.portal.pojo.Variable;
import de.papke.cloud.portal.pojo.VariableGroup;

@Service
public class TerraformService {

	private static final Logger LOG = LoggerFactory.getLogger(TerraformService.class);

	private static final String TEXT_INTRODUCTION = "TERRAFORM IS WORKING ON YOUR ACTION. PLEASE BE PATIENT!!!\n\n";
	private static final String FLAG_NO_COLOR = "-no-color";
	private static final String FLAG_VAR = "-var";
	private static final String FLAG_FORCE = "-force";
	private static final String VARIABLE_IDENTIFIER = "variable";
	private static final String FILE_VARIABLES_TF = "variables.tf";
	private static final String FOLDER_INIT = "init";
	private static final String FOLDER_PLUGINS = ".terraform";
	
	private static final Integer NUMBER_OF_RETRIES = 60;

	@Autowired
	private CommandExecutorService commandExecutorService;

	@Autowired
	private FileService fileService;
	
	@Value("${terraform.path}")
	private String terraformPath;

	private File pluginSourceFolder;

	@PostConstruct
	public void init() {
		retryDownloadProviderPluginFiles();
	}

	private void retryDownloadProviderPluginFiles() {
		
		for (int i = 0; i < NUMBER_OF_RETRIES; i++) {
			try {
				downloadProviderPluginFiles();
				break;
			}
			catch (IllegalStateException e) {	
				LOG.error(e.getMessage());
			}
		}
	}

	private void downloadProviderPluginFiles() {
	
		// copy init folder to temp
		String resourcePath = Constants.FOLDER_PROVISIONER + File.separator + Constants.FOLDER_TERRAFORM + File.separator + FOLDER_INIT;
		String targetPath = System.getProperty("java.io.tmpdir") + File.separator + Constants.FOLDER_TERRAFORM + File.separator + FOLDER_INIT;
		File initFolder = fileService.copyResourceToFilesystem(resourcePath, targetPath);

		// execute init command
		CommandLine initCommand = buildInitCommand(terraformPath);
		CommandResult commandResult = commandExecutorService.execute(initCommand, initFolder, new ByteArrayOutputStream());
		
		// quit program if command was not successful
		if (!commandResult.isSuccess()) {
			throw new IllegalStateException("System was not able to download terraform providers. Quitting ...");
		}
		
		// get plugin folder path
		pluginSourceFolder = new File(targetPath + File.separator + FOLDER_PLUGINS);
	}

	public CommandResult execute(UseCase useCase, String action, Credentials credentials, Map<String, Object> variableMap, OutputStream outputStream, File tmpFolder) {

		CommandResult commandResult = null;

		try {

			// print waiting message
			outputStream.write(TEXT_INTRODUCTION.getBytes());
			outputStream.flush();

			// generate variable file
			generateVariablesFile(useCase, tmpFolder);

			// copy provider plugins to temp folder
			File pluginTargetFolder = new File(tmpFolder.getAbsolutePath() + File.separator + FOLDER_PLUGINS);
			fileService.copyFolder(pluginSourceFolder, pluginTargetFolder);

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

	private void generateVariablesFile(UseCase useCase, File tmpFolder) throws IOException {

		File variablesFile = new File(tmpFolder.getAbsolutePath() + File.separator + FILE_VARIABLES_TF);
		StringBuilder variablesBuilder = new StringBuilder();

		for (VariableGroup variableGroup : useCase.getVariableGroups()) {
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
}
