package de.papke.cloud.portal.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import de.papke.cloud.portal.constants.Constants;
import de.papke.cloud.portal.pojo.CommandResult;
import de.papke.cloud.portal.pojo.Credentials;
import de.papke.cloud.portal.pojo.ProvisionLog;
import de.papke.cloud.portal.pojo.UseCase;
import de.papke.cloud.portal.pojo.User;
import de.papke.cloud.portal.pojo.Variable;
import de.papke.cloud.portal.pojo.VariableConfig;
import de.papke.cloud.portal.pojo.VariableGroup;
import de.papke.cloud.portal.util.ZipUtil;

@Service
public class UseCaseService {

	private static final Logger LOG = LoggerFactory.getLogger(UseCaseService.class);

	private Map<String, UseCase> useCaseMap = new HashMap<>(); 
	private Set<String> providers = new HashSet<>(); 
	
	private static final String FILE_VARIABLES_YML = "variables.yml";
	private static final String PROPERTY_VARIABLES = "variables";
	private static final String PROPERTY_VARIABLE_GROUPS = "variableGroups";	
	private static final String PATTERN_FILE = "_file";
	private static final String SUFFIX_ATTACHMENT = ".txt";
	private static final String SUFFIX_ZIP = ".zip";
	private static final String PREFIX_VM = "vm";
	private static final String PREFIX_ATTACHMENT = "log";
	private static final String PART_ERROR = "error";
	private static final String PART_SUCCESS = "success";
	private static final String VARIABLE_SUR_NAME = "surName";
	private static final String VARIABLE_GIVEN_NAME = "givenName";
	private static final String VARIABLE_PROVISIONING_ID = "provisioning_id";
	private static final String VARIABLE_PROVIDER = "provider";
	
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
	
	@Autowired
	private ResourceService resourceService;	

	@PostConstruct
	public void init() {
		getUseCasesFromFileSystem();
	}

	private void getUseCasesFromFileSystem() {

		try {
			
			Constructor constructor = new Constructor(VariableConfig.class);

			TypeDescription variableConfigTypeDescription = new TypeDescription(VariableConfig.class);
			variableConfigTypeDescription.putListPropertyType(PROPERTY_VARIABLE_GROUPS, VariableGroup.class);
			constructor.addTypeDescription(variableConfigTypeDescription);

			TypeDescription variableGroupTypeDescription = new TypeDescription(VariableGroup.class);
			variableGroupTypeDescription.putListPropertyType(PROPERTY_VARIABLES, Variable.class);
			constructor.addTypeDescription(variableGroupTypeDescription);

			Yaml yaml = new Yaml(constructor);

			File useCaseRootFolder = resourceService.getClasspathResource(Constants.FOLDER_USE_CASE);
			
			for (File useCaseFolder : useCaseRootFolder.listFiles()) {
				for (File providerFolder : useCaseFolder.listFiles()) {
					for (File provisionerFolder : providerFolder.listFiles()) {
						
						StringBuilder useCaseIdBuilder = new StringBuilder();
						useCaseIdBuilder.append(useCaseFolder.getName());
						useCaseIdBuilder.append(Constants.CHAR_DASH);
						useCaseIdBuilder.append(providerFolder.getName());
						useCaseIdBuilder.append(Constants.CHAR_DASH);
						useCaseIdBuilder.append(provisionerFolder.getName());
						
						String id = useCaseIdBuilder.toString();
						String provider = providerFolder.getName();
						String provisioner = provisionerFolder.getName();

						List<VariableGroup> variableGroups = new ArrayList<>();
						File variableFile = new File(new URI(provisionerFolder.toURI() + File.separator + FILE_VARIABLES_YML));
						if (variableFile.exists()) {
							VariableConfig variableConfig = yaml.loadAs(new FileInputStream(variableFile), VariableConfig.class);
							variableGroups = variableConfig.getVariableGroups();
						}
						
						StringBuilder resourcePathBuilder = new StringBuilder();
						resourcePathBuilder.append(useCaseRootFolder.getName());
						resourcePathBuilder.append(File.separator);
						resourcePathBuilder.append(useCaseFolder.getName());
						resourcePathBuilder.append(File.separator);
						resourcePathBuilder.append(providerFolder.getName());
						resourcePathBuilder.append(File.separator);
						resourcePathBuilder.append(provisionerFolder.getName());
						
						UseCase useCase = new UseCase();
						useCase.setId(id);
						useCase.setProvider(provider);
						useCase.setProvisioner(provisioner);
						useCase.setVariableGroups(variableGroups);
						useCase.setResourceFolderPath(resourcePathBuilder.toString());
						useCaseMap.put(id, useCase);
						
						providers.add(provider);
					}
				}
			}
		}
		catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
	@Scheduled(cron = "${application.expiration.cron.expression}")
	private void schedule() {
		
		List<ProvisionLog> provisionLogList = provisionLogService.getExpired();
		
		for (ProvisionLog provisionLog : provisionLogList) {

			String username = provisionLog.getUsername();
			User user = userService.getUser(username);
			String useCaseId = provisionLog.getUseCaseId();
			UseCase useCase = getUseCaseById(useCaseId); 
			String provider = useCase.getProvider();
			Credentials credentials = credentialsService.getCredentials(user, provider);
			
			if (user != null && credentials != null) {
				OutputStream outputStream = new ByteArrayOutputStream();
				deprovision(user, provisionLog, credentials, outputStream);
			}
		}
	}

	public void provision(UseCase useCase, String action, Credentials credentials, Map<String, Object> variableMap, File privateKeyFile, OutputStream outputStream) {

		File tmpFolder = null;
		File attachment = null;

		try {

			// get provisioner
			ProvisionerService provisioner = getProvisioner(useCase);
			if (provisioner != null) {
				
				// get group from credentials
				String group = credentials.getGroup();
				
				// copy provisioner resources to filesystem
				tmpFolder = fileService.copyResourceToFilesystem(useCase.getResourceFolderPath());				
				
				// provision use case
				CommandResult commandResult = provisioner.execute(useCase, action, credentials, variableMap, outputStream, tmpFolder);
	
				if (commandResult != null && action.equals(Constants.ACTION_APPLY)) {
					
					// get success flag
					boolean success = commandResult.isSuccess();
					
					// get variables from output
					Map<String, Object> commandVariableMap = provisioner.getCommandVariableMap(commandResult);
					
					// add command output variables to map
					variableMap.putAll(commandVariableMap);
	
					// create provision log
					ProvisionLog provisionLog = provisionLogService.create(action, useCase, group, success, variableMap, privateKeyFile, tmpFolder);
					
					// add provisioning id
					String provisioningId = provisionLog.getId();
					String provisioningIdVariableString = VARIABLE_PROVISIONING_ID + Constants.CHAR_WHITESPACE + Constants.CHAR_EQUAL + Constants.CHAR_WHITESPACE + provisioningId;
					outputStream.write((provisioningIdVariableString).getBytes());
					variableMap.put(VARIABLE_PROVISIONING_ID, provisioningId);
					
					// add provider
					String provider = useCase.getProvider();
					variableMap.put(VARIABLE_PROVIDER, provider);
	
					// get attachment for mail
					attachment = getAttachment(commandResult);
		
					// send mail
					sendMail(action, success, variableMap, attachment);
				}
			}
			else {
				LOG.error("No provisioner found for use case '{}'", useCase);
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

			// get use case
			UseCase useCase = getUseCaseById(provisionLog.getUseCaseId());
			
			// get provisioner
			ProvisionerService provisioner = getProvisioner(useCase);
			if (provisioner != null) {
				
				// get variable map
				Map<String, Object> variableMap = provisionLog.getVariableMap(); 
	
				// get resource folder
				tmpFolder = getTmpFolder();
				zipFile = File.createTempFile(this.getClass().getSimpleName(), SUFFIX_ZIP);
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

				// destroy vm with provisioner
				CommandResult commandResult = provisioner.execute(useCase, action, credentials, variableMap, outputStream, resourceFolder);
				
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
				
				// add provisioning id
				variableMap.put(VARIABLE_PROVISIONING_ID, provisionLog.getId());
				
				// add provider
				String provider = useCase.getProvider();
				variableMap.put(VARIABLE_PROVIDER, provider);			
				
				// send mail
				sendMail(user, action, success, variableMap, attachment);
			}
			else {
				LOG.error("No provisioner found for use case '{}'", useCase);
			}
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
		variableMap.put(VARIABLE_GIVEN_NAME, user.getGivenName());
		variableMap.put(VARIABLE_SUR_NAME, user.getSurName());

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
		return PREFIX_VM + Constants.CHAR_DASH + action + Constants.CHAR_DASH + (success ? PART_SUCCESS : PART_ERROR); 
	}

	private File getAttachment(CommandResult commandResult) throws IOException {
		
		File attachment = null;
		
		// write command output to attachment file
		if (commandResult != null) {
			String output = commandResult.getOutput();
			if (StringUtils.isNotEmpty(output)) {
				attachment = File.createTempFile(PREFIX_ATTACHMENT, SUFFIX_ATTACHMENT);
				FileUtils.writeStringToFile(attachment, commandResult.getOutput(), StandardCharsets.UTF_8);
			}
		}
		
		return attachment;
	}	
	
	private ProvisionerService getProvisioner(UseCase useCase) {

		String provisioner = useCase.getProvisioner();
		
		ProvisionerService configManagementService = null;
		if (provisioner.equals(TerraformService.PREFIX)) {
			configManagementService = terraformService;
		}
		
		return configManagementService;
	}
	
	private List<VariableGroup> getVariableGroups(String id) {
		UseCase useCase = useCaseMap.get(id);
		return useCase.getVariableGroups();
	}
	
	public List<VariableGroup> getVisibleVariableGroups(String id) {
		
		List<VariableGroup> visibleVariableGroups = new ArrayList<>();

		for (VariableGroup variableGroup : getVariableGroups(id)) {
			if (!variableGroup.isHidden()) {
				visibleVariableGroups.add(variableGroup);
			}
		}
		
		return visibleVariableGroups;
	}	
	
	public List<Variable> getVisibleVariables(String id) {

		List<Variable> variables = new ArrayList<>();

		for (VariableGroup variableGroup : useCaseMap.get(id).getVariableGroups()) {
			if (!variableGroup.isHidden()) {
				for (Variable variable : variableGroup.getVariables()) {
					variables.add(variable);
				}
			}
		}

		return variables;
	}	
	
	public List<String> getProviders() {
		return new ArrayList<>(providers);
	}
	
	public UseCase getUseCaseById(String id) {
		return useCaseMap.get(id);
	}
}