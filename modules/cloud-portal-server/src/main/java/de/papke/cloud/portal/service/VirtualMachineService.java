package de.papke.cloud.portal.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import de.papke.cloud.portal.constants.Constants;
import de.papke.cloud.portal.pojo.CommandResult;
import de.papke.cloud.portal.pojo.Credentials;
import de.papke.cloud.portal.pojo.ProvisionLog;
import de.papke.cloud.portal.pojo.User;
import de.papke.cloud.portal.util.ZipUtil;

@Service
public class VirtualMachineService {

	private static final Logger LOG = LoggerFactory.getLogger(VirtualMachineService.class);

	private static final String MAIL_TEMPLATE_PREFIX = "vm";
	private static final String PATTERN_FILE = "_file";
	private static final String PATTERN_OUTPUTS = "Outputs:";
	private static final String PATTERN_EMPTY_LINE = "(?m)^\\s";
	private static final String ATTACHMENT_SUFFIX = ".txt";
	private static final String ATTACHMENT_PREFIX = "log";

	@Autowired
	private ProvisionLogService provisionLogService;

	@Autowired
	private SessionUserService sessionUserService;

	@Autowired
	private TerraformService terraformService;

	@Autowired
	private MailService mailService;
	
	@Autowired
	private MailTemplateService mailTemplateService;

	@Autowired
	private FileService fileService;
	
	@Autowired
	private CredentialsService credentialsService;
	
	@Autowired
	private UserService userService;
	
	@Scheduled(cron = "${application.expiration.cron.expression}")
	public void schedule() {
		List<ProvisionLog> provisionLogList = provisionLogService.getExpired();
		for (ProvisionLog provisionLog : provisionLogList) {
			String username = provisionLog.getUsername();
			User user = userService.getUser(username);
			String provider = provisionLog.getProvider();
			Credentials credentials = credentialsService.getCredentials(user, provider);
			OutputStream outputStream = new ByteArrayOutputStream();
			deprovision(user, provisionLog, credentials, outputStream);
		}
	}

	public void provision(String action, Credentials credentials, Map<String, Object> variableMap, File privateKeyFile, OutputStream outputStream) {

		File tmpFolder = null;
		File attachment = null;

		try {

			// get provider from credentials
			String provider = credentials.getProvider();
			
			// get group from credentials
			String group = credentials.getGroup();
			
			// copy terraform resources to filesystem
			tmpFolder = fileService.copyResourceToFilesystem("terraform/" + provider);

			// use terraform to provision vms
			CommandResult commandResult = terraformService.execute(action, credentials, variableMap, outputStream, tmpFolder);

			if (action.equals(Constants.ACTION_APPLY)) {
				
				// get success flag
				boolean success = commandResult.isSuccess();
				
				// get variables from output
				Map<String, Object> commandVariableMap = getCommandVariableMap(commandResult);
				
				// add command output variables to map
				variableMap.putAll(commandVariableMap);

				// create provision log
				ProvisionLog provisionLog = provisionLogService.create(action, provider, group, success, variableMap, privateKeyFile, tmpFolder);
				
				// output provision log id
				outputStream.write(("provisioning_id = " + provisionLog.getId()).getBytes());

				// get attachment for mail
				attachment = getAttachment(commandResult);
	
				// send mail
				sendMail(action, success, variableMap, attachment);
			}
		}
		catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		finally {
			try {
				FileUtils.deleteQuietly(tmpFolder);
				FileUtils.deleteQuietly(attachment);
			}
			catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	public void deprovision(ProvisionLog provisionLog, Credentials credentials, OutputStream outputStream) {
		deprovision(sessionUserService.getUser(), provisionLog, credentials, outputStream);
	}
	
	public void deprovision(User user, ProvisionLog provisionLog, Credentials credentials, OutputStream outputStream) {
		
		File attachment = null;
		File zipFile = null; 
		File tmpFolder = null;
		List<File> dummmyFileList = new ArrayList<>();

		try {

			// get variable map
			Map<String, Object> variableMap = provisionLog.getVariableMap(); 

			// get resource folder
			tmpFolder = getTmpFolder();
			zipFile = File.createTempFile("test", ".zip");
			IOUtils.write(provisionLog.getResult(), new FileOutputStream(zipFile));
			ZipUtil.unzip(zipFile, tmpFolder);
			File resourceFolder = tmpFolder.listFiles()[0];
			
			// create dummy files
			for (Entry<String, Object> variableMapEntry : variableMap.entrySet()) {
				if (variableMapEntry.getKey().endsWith(PATTERN_FILE)) {
					String filePath = (String) variableMapEntry.getValue();
					File dummyFile = new File(filePath);
					dummyFile.createNewFile(); // NOSONAR
					dummmyFileList.add(dummyFile);
				}
			}

			// specify action
			String action = Constants.ACTION_DESTROY;
			
			// destroy vm with terraform
			CommandResult commandResult = terraformService.execute(action, credentials, variableMap, outputStream, resourceFolder);
			
			// command was successful?
			if (commandResult.isSuccess()) {
				
				// get username
				String username = user.getUsername();
				
				// update provision log entry
				provisionLog.setCommand(action);
				provisionLog.setUsername(username);
				provisionLogService.update(provisionLog);
			}
			
			// get attachment for mail
			attachment = getAttachment(commandResult);
		
			// get success flag
			boolean success = commandResult.isSuccess();
			
			// send mail
			sendMail(user, action, success, variableMap, attachment);
		}
		catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		finally {
			FileUtils.deleteQuietly(attachment);
			FileUtils.deleteQuietly(zipFile);
			FileUtils.deleteQuietly(tmpFolder);
			for (File dummyFile : dummmyFileList) {
				FileUtils.deleteQuietly(dummyFile);
			}
		}
	}
	
	private static final File getTmpFolder() {

		File tmpFolder = new File(Constants.TMP_FOLDER_PREFIX + System.nanoTime());
		tmpFolder.mkdirs();

		return tmpFolder;
	}
	
	private File sendMail(String action, boolean success, Map<String, Object> variableMap, File attachment) {
		return sendMail(sessionUserService.getUser(), action, success, variableMap, attachment);
	}
	
	private File sendMail(User user, String action, boolean success, Map<String, Object> variableMap, File attachment) {
		
		// get mail address
		String email = user.getEmail();
		
		// add additional user properties to map
		variableMap.put("givenName", user.getGivenName());
		variableMap.put("surName", user.getSurName());

		// send mail
		if (StringUtils.isNotEmpty(email)) {
			String mailTemplateName = getMailTemplateName(action, success);
			String mailSubject = getMailSubject(mailTemplateName);
			String mailTemplatePath = mailTemplateService.getMailTemplatePath(mailTemplateName);
			mailService.send(email, mailSubject, mailTemplatePath, attachment, variableMap);
		}
		
		return attachment;
	}	
	
	private String getMailSubject(String mailTemplateName) {
		
		StringBuilder mailSubjectBuilder = new StringBuilder();
		
		String[] mailTemplateNameArray = mailTemplateName.split(Constants.CHAR_DASH);
		for (int i = 0; i < mailTemplateNameArray.length; i++) {
			mailSubjectBuilder.append(WordUtils.capitalize(mailTemplateNameArray[i]));
			if (i < mailTemplateNameArray.length - 1) {
				mailSubjectBuilder.append(Constants.CHAR_WHITESPACE);
			}
		}
		
		return mailSubjectBuilder.toString();
	}
	
	private String getMailTemplateName(String action, boolean success) {
		return MAIL_TEMPLATE_PREFIX + Constants.CHAR_DASH + action + Constants.CHAR_DASH + (success ? "success" : "error"); 
	}

	private Map<String, Object> getCommandVariableMap(CommandResult commandResult) {

		Map<String, Object> variableMap = new HashMap<>();
		
		if (commandResult != null) {
		
			String output = commandResult.getOutput();
	
			if (StringUtils.isNotEmpty(output)) {
				String[] outputArray = output.split(PATTERN_OUTPUTS);
				if (outputArray.length == 2) {
					String outputVariablePart = outputArray[1].replaceAll(PATTERN_EMPTY_LINE, StringUtils.EMPTY);
					for (String line : outputVariablePart.split(Constants.CHAR_NEW_LINE)) {
						String[] variablePart = line.split(Constants.CHAR_EQUAL);
						if (variablePart.length == 2) {
							String key = variablePart[0].trim();
							String value = variablePart[1].trim();
							variableMap.put(key, value);
						}
					}
				}
			}
		}

		return variableMap;
	}
	
	private File getAttachment(CommandResult commandResult) throws IOException {
		
		File attachment = null;
		
		// write command output to attachment file
		if (commandResult != null) {
			String output = commandResult.getOutput();
			if (StringUtils.isNotEmpty(output)) {
				attachment = File.createTempFile(ATTACHMENT_PREFIX, ATTACHMENT_SUFFIX);
				FileUtils.writeStringToFile(attachment, commandResult.getOutput(), StandardCharsets.UTF_8);
			}
		}
		
		return attachment;
	}	
}