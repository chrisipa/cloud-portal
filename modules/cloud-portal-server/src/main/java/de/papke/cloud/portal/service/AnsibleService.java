package de.papke.cloud.portal.service;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.exec.CommandLine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.papke.cloud.portal.constants.Constants;
import de.papke.cloud.portal.pojo.UseCase;

@Service
public class AnsibleService extends ProvisionerService {
	
	private static final String ANSIBLE_SSH_USER = "ansible_ssh_user";
	private static final String ANSIBLE_SSH_PASS = "ansible_ssh_pass";
	private static final String ESXI_HOSTNAME = "esxi_hostname";
	private static final String ESXI_USERNAME = "esxi_username";
	private static final String ESXI_PASSWORD = "esxi_password"; // NOSONAR
	private static final String FLAG_EXTRA_VARS = "--extra-vars";
	private static final String FLAG_INVENTORY = "-i";
	private static final String FLAG_DRY_RUN = "--check";
	private static final String OUTPUT_MSG = "msg";
	private static final String SUFFIX_YML = ".yml";

	public static final String PREFIX = "ansible";

	@Value("${ansible.path}")
	private String ansiblePath;
	
	@Override
	protected String getPrefix() {
		return PREFIX;
	}
	
	@Override
	protected String getBinaryPath() {
		return ansiblePath;
	}

	@Override
	protected void prepare(UseCase useCase, File tmpFolder) throws IOException {
		// do nothing
	}

	@Override
	protected Pattern getParsingPattern() {
		
		StringBuilder patternBuilder = new StringBuilder()
				.append(Constants.CHAR_DOUBLE_QUOTE)
				.append(OUTPUT_MSG)
				.append(Constants.CHAR_DOUBLE_QUOTE)
				.append(Constants.CHAR_DOUBLE_DOT)
				.append(Constants.CHAR_WHITESPACE)
				.append(Constants.CHAR_DOUBLE_QUOTE)
				.append(Constants.CHAR_PARENTHESES_OPEN)
				.append(Constants.CHAR_DOT)
				.append(Constants.CHAR_STAR)
				.append(Constants.CHAR_PARENTHESES_CLOSE)
				.append(Constants.CHAR_WHITESPACE)
				.append(Constants.CHAR_EQUAL)
				.append(Constants.CHAR_WHITESPACE)
				.append(Constants.CHAR_PARENTHESES_OPEN)
				.append(Constants.CHAR_DOT)
				.append(Constants.CHAR_STAR)
				.append(Constants.CHAR_PARENTHESES_CLOSE)
				.append(Constants.CHAR_DOUBLE_QUOTE);
		
		return Pattern.compile(patternBuilder.toString());
	}
	
	@Override
	protected CommandLine buildActionCommand(String ansiblePath, String action, Map<String, Object> variableMap) {

		CommandLine actionCommand = new CommandLine(ansiblePath);

		if (action.equals(Constants.ACTION_PLAN)) {
			actionCommand.addArgument(FLAG_DRY_RUN);
		}
		
		actionCommand.addArgument(FLAG_INVENTORY);
		actionCommand.addArgument(variableMap.get(ESXI_HOSTNAME) + Constants.CHAR_COMMA);
		
		actionCommand.addArgument(FLAG_EXTRA_VARS);
		actionCommand.addArgument(ANSIBLE_SSH_USER + Constants.CHAR_EQUAL + variableMap.get(ESXI_USERNAME));
		
		actionCommand.addArgument(FLAG_EXTRA_VARS);
		actionCommand.addArgument(ANSIBLE_SSH_PASS + Constants.CHAR_EQUAL + variableMap.get(ESXI_PASSWORD));
		
		for (Entry<String, Object> variableEntry : variableMap.entrySet()) {

			String variableName = variableEntry.getKey();
			String variableValue = (String) variableEntry.getValue();
			String variableString = variableName + Constants.CHAR_EQUAL + Constants.CHAR_SINGLE_QUOTE + (variableValue.equals("on") ? "true" : variableValue) + Constants.CHAR_SINGLE_QUOTE;

			actionCommand.addArgument(FLAG_EXTRA_VARS);
			actionCommand.addArgument(variableString, false);
		}
		
		actionCommand.addArgument(action + SUFFIX_YML);

		return actionCommand;
	}	
}
