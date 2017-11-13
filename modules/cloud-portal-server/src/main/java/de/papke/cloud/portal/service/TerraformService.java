package de.papke.cloud.portal.service;

import java.io.File;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.papke.cloud.portal.pojo.CommandResult;
import de.papke.cloud.portal.pojo.User;
import de.papke.cloud.portal.terraform.HCLParser;
import de.papke.cloud.portal.terraform.Variable;

/**
 * Created by chris on 16.10.17.
 */
@Service
public class TerraformService {


	private static final Logger LOG = LoggerFactory.getLogger(TerraformService.class);

	private static final String CHAR_EQUAL = "=";
	private static final String CHAR_WHITESPACE = " ";
	private static final String CHAR_QUOTE = "\"";
	private static final String CHAR_NEW_LINE = "\n";
	
	private static final String FLAG_NO_COLOR = "-no-color";
	private static final String FLAG_VAR = "-var";
	
	private static final String ACTION_INIT = "init";
	private static final String ACTION_APPLY = "apply";
	
	private static final String PATTERN_OUTPUTS = "Outputs:";
	private static final String PATTERN_EMPTY_LINE = "(?m)^\\s";
	
	private static final String ATTACHMENT_SUFFIX = ".txt";
	private static final String ATTACHMENT_PREFIX = "log";

	@Autowired
	private CommandExecutorService commandExecutorService;

	@Autowired
	private FileService fileService;

	@Autowired
	private MailService mailService;
	
	@Autowired
	private UserService userService;

	@Value("${terraform.path}")
	private String terraformPath;

	@Value("${terraform.mail.success.subject}")
	private String mailSuccessSubject;

	@Value("${terraform.mail.success.template}")
	private String mailSuccessTemplate;

	@Value("${terraform.mail.error.subject}")
	private String mailErrorSubject;

	@Value("${terraform.mail.error.template}")
	private String mailErrorTemplate;

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

	public void provisionVM(String action, String provider, Map<String, Object> variableMap, OutputStream outputStream) {

		File tmpFolder = null;

		try {

			if (StringUtils.isNotEmpty(provider)) {

				// print waiting message
				outputStream.write("TERRAFORM IS WORKING ON YOUR ACTION. YOU WILL GET AN EMAIL WITH THE RESULTS. PLEASE BE PATIENT!!!\n".getBytes());
				outputStream.flush();				
				
				// copy terraform resources to filesystem
				tmpFolder = fileService.copyResourceToFilesystem("terraform/" + provider);

				// execute init command
				String initCommand = buildInitCommand(terraformPath);
				commandExecutorService.execute(initCommand, tmpFolder, outputStream);

				// get action to execute
				if (StringUtils.isNotEmpty(action)) {
					
					// build the command string
					String commandString = buildActionCommand(terraformPath, action, variableMap);

					// execute action command
					CommandResult commandResult = commandExecutorService.execute(commandString, tmpFolder, outputStream);

					// if terraform action is apply
					if (action.equals(ACTION_APPLY)) {

						// send mail
						File attachment = null;
						
						try {
							
							// get variables from output
							Map<String, String> mailVariableMap = parseOutput(commandResult.getOutput());
							mailVariableMap.put("provider", provider);
							
							// write command output to attachment file
							String output = commandResult.getOutput();
							if (StringUtils.isNotEmpty(output)) {
								attachment = File.createTempFile(ATTACHMENT_PREFIX, ATTACHMENT_SUFFIX);
								FileUtils.writeStringToFile(attachment, commandResult.getOutput(), StandardCharsets.UTF_8);
							}
							
							// get mail address
							User user = userService.getUser();
							String email = user.getEmail(); 
							
							// send mail
							if (StringUtils.isNotEmpty(email)) {
								if (commandResult.isSuccess()) {
									mailService.send(email, mailSuccessSubject, mailSuccessTemplate, attachment, mailVariableMap);
								}
								else {
									mailService.send(email, mailErrorSubject, mailErrorTemplate, attachment, mailVariableMap);
								}
							}
						}
						catch (Exception e) {
							LOG.error(e.getMessage(), e);
						}
						finally {
							if (attachment != null) {
								attachment.delete();
							}
						}
					}
				}
			}
		}
		catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		finally {
			if (tmpFolder != null) {
				try {
					FileUtils.deleteDirectory(tmpFolder);
				}
				catch (Exception e) {
					LOG.error(e.getMessage());
				}
			}
		}
	}

	private Map<String, String> parseOutput(String output) {

		Map<String, String> variableMap = new HashMap<>();

		if (StringUtils.isNotEmpty(output)) {
			String[] outputArray = output.split(PATTERN_OUTPUTS);
			if (outputArray.length == 2) {
				String outputVariablePart = outputArray[1].replaceAll(PATTERN_EMPTY_LINE, StringUtils.EMPTY);
				for (String line : outputVariablePart.split(CHAR_NEW_LINE)) {
					String[] variablePart = line.split(CHAR_EQUAL);
					if (variablePart.length == 2) {
						String key = variablePart[0].trim();
						String value = variablePart[1].trim();
						variableMap.put(key, value);
					}
				}
			}
		}

		return variableMap;
	}

	private String buildInitCommand(String terraformPath) {
		return terraformPath + CHAR_WHITESPACE + ACTION_INIT + CHAR_WHITESPACE + FLAG_NO_COLOR;
	}

	private String buildActionCommand(String terraformPath, String action, Map<String, Object> variableMap) {

		StringBuffer commandStringBuffer = new StringBuffer();
		commandStringBuffer.append(terraformPath);
		commandStringBuffer.append(CHAR_WHITESPACE);
		commandStringBuffer.append(action);
		commandStringBuffer.append(CHAR_WHITESPACE);
		commandStringBuffer.append(FLAG_NO_COLOR);


		for (String variableName : variableMap.keySet()) {

			String variableValue = (String) variableMap.get(variableName);

			commandStringBuffer.append(CHAR_WHITESPACE);
			commandStringBuffer.append(FLAG_VAR);
			commandStringBuffer.append(CHAR_WHITESPACE);
			commandStringBuffer.append(CHAR_QUOTE);
			commandStringBuffer.append(variableName);
			commandStringBuffer.append(CHAR_EQUAL);
			commandStringBuffer.append(variableValue.equals("on") ? "true" : variableValue);
			commandStringBuffer.append(CHAR_QUOTE);
		}

		return commandStringBuffer.toString();
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
