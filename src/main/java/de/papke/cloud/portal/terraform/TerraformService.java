package de.papke.cloud.portal.terraform;

import java.io.File;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.papke.cloud.portal.file.FileService;
import de.papke.cloud.portal.process.ProcessExecutorService;

/**
 * Created by chris on 16.10.17.
 */
@Service
public class TerraformService {

	private static final Logger LOG = LoggerFactory.getLogger(TerraformService.class);
	
	@Autowired
	private ProcessExecutorService processExecutorService;

	@Autowired
	private FileService fileService;
	
	@Value("${terraform.path}")
	private String terraformPath;
	
	private Map<String, List<Variable>> providerDefaultsMap = new HashMap<>();

	@PostConstruct
	public void init() {

		try {
			URL url = getClass().getClassLoader().getResource("terraform");
			File terraformFolder = new File(url.toURI());
			if (!terraformFolder.isFile()) {
				File[] providerFolderArray = terraformFolder.listFiles();
				for (File providerFolder : providerFolderArray) {
					File variableFile = new File(new URI(providerFolder.toURI() + "/vars.tf"));
					List<Variable> variableList = getProviderDefaults(variableFile);
					providerDefaultsMap.put(providerFolder.getName(), variableList);
				}
			}
		}
		catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}

	public void provisionVM(String provider, Map<String, Object> variableMap, OutputStream outputStream) {
		
		File tmpFolder = null;
		
		try {

			if (StringUtils.isNotEmpty(provider)) {
	
				// copy terraform resources to filesystem
				tmpFolder = fileService.copyResourceToFilesystem("terraform/" + provider);
				
				// execute init command
				String initCommand = buildInitCommand(terraformPath, "init");
				processExecutorService.execute(initCommand, tmpFolder, outputStream);
	
				// get action to execute
				String action = (String) variableMap.get("action");
				if (StringUtils.isNotEmpty(action)) {
					
					// remove action from map
					variableMap.remove("action");
					
					// build the command string
					String commandString = buildActionCommand(terraformPath, action, variableMap);
					
					// execute action command
					processExecutorService.execute(commandString, tmpFolder, outputStream);
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
	
	private String buildInitCommand(String terraformPath, String action) {
		return terraformPath + " " + action;
	}
	
	private String buildActionCommand(String terraformPath, String action, Map<String, Object> variableMap) {
		
		StringBuffer commandStringBuffer = new StringBuffer();
		commandStringBuffer.append(terraformPath);
		commandStringBuffer.append(" ");
		commandStringBuffer.append(action);
				
		for (String variableName : variableMap.keySet()) {
			
			String variableValue = (String) variableMap.get(variableName);
			
			commandStringBuffer.append(" ");
			commandStringBuffer.append("-var \"");
			commandStringBuffer.append(variableName);
			commandStringBuffer.append("=");
			commandStringBuffer.append(variableValue.equals("on") ? "true" : variableValue);
			commandStringBuffer.append("\"");
		}
		
		return commandStringBuffer.toString();
	}
	
	public List<Variable> getProviderDefaults(File providerDefaultsFile) {
		
		List<Variable> variableList = new ArrayList<>();
		
		try {
			HCLParser hclParser = new HCLParser(providerDefaultsFile);
			variableList = hclParser.parse();
		}
		catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		
		return variableList;
	}

	public Map<String, List<Variable>> getProviderDefaultsMap() {
		return providerDefaultsMap;
	}
}
