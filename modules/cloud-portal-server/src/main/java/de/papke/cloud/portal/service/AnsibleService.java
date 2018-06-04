package de.papke.cloud.portal.service;

import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.exec.CommandLine;
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

@Service
public class AnsibleService {
	
	private static final Logger LOG = LoggerFactory.getLogger(AnsibleService.class);
	
	private static final String TEXT_INTRODUCTION = "ANSIBLE IS WORKING ON YOUR ACTION. PLEASE BE PATIENT!!!\n\n";
	private static final String FLAG_DRY_RUN = "--check";

	@Autowired
	private CommandExecutorService commandExecutorService;	
	
	@Value("${ansible.path}")
	private String ansiblePath;
	
	public CommandResult execute(UseCase useCase, String action, Credentials credentials, Map<String, Object> variableMap, OutputStream outputStream, File tmpFolder) {

		CommandResult commandResult = null;

		try {

			// print waiting message
			outputStream.write(TEXT_INTRODUCTION.getBytes());
			outputStream.flush();

			// get action to execute
			if (StringUtils.isNotEmpty(action)) {

				// get execution map
				Map<String, Object> executionMap = getExecutionMap(credentials, variableMap);

				// build the command string
				CommandLine actionCommand = buildActionCommand(ansiblePath, action, executionMap);

				// execute action command
				commandResult = commandExecutorService.execute(actionCommand, tmpFolder, outputStream);
			}
		}
		catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return commandResult;
	}
	
	private CommandLine buildActionCommand(String ansiblePath, String action, Map<String, Object> variableMap) {

		CommandLine actionCommand = new CommandLine(ansiblePath);

		if (action.equals(Constants.ACTION_PLAN)) {
			actionCommand.addArgument(FLAG_DRY_RUN);
		}
		
		actionCommand.addArgument("-i");
		actionCommand.addArgument(variableMap.get("esxi_hostname") + ",");
		
		actionCommand.addArgument("-e");
		actionCommand.addArgument("deprecation_warnings=False");
		
		actionCommand.addArgument("-e");
		actionCommand.addArgument("ansible_ssh_user=root");
		
		actionCommand.addArgument("-e");
		actionCommand.addArgument("ansible_ssh_pass=" + variableMap.get("esxi_password"));
		
		for (Entry<String, Object> variableEntry : variableMap.entrySet()) {

			String variableName = variableEntry.getKey();
			String variableValue = (String) variableEntry.getValue();
			String variableString = variableName + Constants.CHAR_EQUAL + Constants.CHAR_SINGLE_QUOTE + (variableValue.equals("on") ? "true" : variableValue) + Constants.CHAR_SINGLE_QUOTE;

			actionCommand.addArgument("--extra-vars");
			actionCommand.addArgument(variableString, false);
		}
		
		actionCommand.addArgument("playbook.yml");

		return actionCommand;
	}	
	
	private Map<String, Object> getExecutionMap(Credentials credentials, Map<String, Object> variableMap) {

		Map<String, Object> executionMap = new HashMap<>();
		executionMap.putAll(variableMap);
		executionMap.putAll(credentials.getSecretMap());

		return executionMap;
	}
}
