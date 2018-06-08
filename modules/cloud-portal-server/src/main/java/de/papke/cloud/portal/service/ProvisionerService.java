package de.papke.cloud.portal.service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.papke.cloud.portal.pojo.CommandResult;
import de.papke.cloud.portal.pojo.Credentials;
import de.papke.cloud.portal.pojo.UseCase;

@Service
public abstract class ProvisionerService {
	
	private static final Logger LOG = LoggerFactory.getLogger(ProvisionerService.class);
	
	private static final String INTRODUCTION_SUFFIX = " IS WORKING ON YOUR ACTION. PLEASE BE PATIENT!!!\n\n";
	
	protected abstract String getPrefix();
	protected abstract String getBinaryPath();
	protected abstract CommandLine buildActionCommand(String ansiblePath, String action, Map<String, Object> variableMap);
	protected abstract Pattern getParsingPattern();
	protected abstract void prepare(UseCase useCase, String action, Credentials credentials, Map<String, Object> variableMap, OutputStream outputStream, File tmpFolder) throws IOException;
	protected abstract void cleanup(UseCase useCase, String action, Credentials credentials, Map<String, Object> variableMap, OutputStream outputStream, File tmpFolder) throws IOException;
	
	@Autowired
	private CommandExecutorService commandExecutorService;
	
	public CommandResult execute(UseCase useCase, String action, Credentials credentials, Map<String, Object> variableMap, OutputStream outputStream, File tmpFolder) {

		CommandResult commandResult = null;

		try {

			// print waiting message
			outputStream.write(getIntroductionText().getBytes());
			outputStream.flush();

			// get action to execute
			if (StringUtils.isNotEmpty(action)) {
				
				// get execution map
				Map<String, Object> executionMap = getExecutionMap(credentials, variableMap);

				// build the command string
				CommandLine actionCommand = buildActionCommand(getBinaryPath(), action, executionMap);
				
				// prepare execution
				prepare(useCase, action, credentials, variableMap, outputStream, tmpFolder);

				// execute action command
				commandResult = commandExecutorService.execute(actionCommand, tmpFolder, outputStream);
				
				// cleanup execution
				cleanup(useCase, action, credentials, variableMap, outputStream, tmpFolder);
			}
		}
		catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return commandResult;
	}

	public Map<String, Object> getCommandVariableMap(CommandResult commandResult) {

		Map<String, Object> variableMap = new HashMap<>();
		
		if (commandResult != null) {
			
			String output = commandResult.getOutput();
	
			if (StringUtils.isNotEmpty(output)) {
					
				Pattern pattern = getParsingPattern();
				Matcher matcher = pattern.matcher(output);
				
				while (matcher.find()) {
					String key = matcher.group(1);
					String value = matcher.group(2);
					variableMap.put(key, value);
				}
			}
		}

		return variableMap;
	}
	
	protected Map<String, Object> getExecutionMap(Credentials credentials, Map<String, Object> variableMap) {

		Map<String, Object> executionMap = new HashMap<>();
		executionMap.putAll(variableMap);
		executionMap.putAll(credentials.getSecretMap());

		return executionMap;
	}
	
	protected String getIntroductionText() {
		return getPrefix().toUpperCase() + INTRODUCTION_SUFFIX;
	}
}
