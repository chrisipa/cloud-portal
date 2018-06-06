package de.papke.cloud.portal.service;

import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.lang.StringUtils;

import de.papke.cloud.portal.pojo.CommandResult;
import de.papke.cloud.portal.pojo.Credentials;
import de.papke.cloud.portal.pojo.UseCase;

public abstract class ProvisionerService {
	
	private static final String INTRODUCTION_SUFFIX = " IS WORKING ON YOUR ACTION. PLEASE BE PATIENT!!!\n\n";
	
	public abstract CommandResult execute(UseCase useCase, String action, Credentials credentials, Map<String, Object> variableMap, OutputStream outputStream, File tmpFolder);

	protected abstract String getPrefix();
	protected abstract CommandLine buildActionCommand(String ansiblePath, String action, Map<String, Object> variableMap);
	protected abstract Pattern getParsingPattern();

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
