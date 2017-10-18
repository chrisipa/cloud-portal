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
import org.apache.commons.lang3.text.StrSubstitutor;
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
@SuppressWarnings("deprecation")
@Service
public class TerraformService {

	private static final Logger LOG = LoggerFactory.getLogger(TerraformService.class);
	
	@Autowired
	private ProcessExecutorService processExecutorService;

	@Autowired
	private FileService fileService;
	
	@Value("${terraform.init.command}")
	private String initCommand;
	
	@Value("${terraform.execute.command}")
	private String executeCommand;	

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

	public void provisionVM(Map<String, Object> variableMap, OutputStream outputStream) {
		
		File tmpFolder = null;
		
		try {

			String provider = (String) variableMap.get("provider");
	
			if (StringUtils.isNotEmpty(provider)) {
	
				// copy terraform resources to filesystem
				tmpFolder = fileService.copyResourceToFilesystem("terraform/" + provider);
				
				// execute init command
				processExecutorService.execute(initCommand, tmpFolder, outputStream);
	
				// build the command string
				StrSubstitutor stringSubstitutor = new StrSubstitutor(variableMap);
				String executeCommandReplace = stringSubstitutor.replace(executeCommand);
	
				// execute action command
				processExecutorService.execute(executeCommandReplace, tmpFolder, outputStream);
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
