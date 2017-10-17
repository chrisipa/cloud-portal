package de.papke.cloud.portal.cloud;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
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
import de.papke.cloud.portal.velocity.VelocityService;

/**
 * Created by chris on 16.10.17.
 */
@Service
public class CloudProviderService {

	private static final Logger LOG = LoggerFactory.getLogger(CloudProviderService.class);

	@Value("${terraform.command}")
	private String terraformCommand;

	@Autowired
	private ProcessExecutorService processExecutorService;

	@Autowired
	private FileService fileService;

	private List<String> providerList = new ArrayList<>();

	@PostConstruct
	public void init() {

		try {
			URL url = getClass().getClassLoader().getResource("terraform");
			File terraformFolder = new File(url.toURI());
			if (!terraformFolder.isFile()) {
				File[] providerFolderArray = terraformFolder.listFiles();
				for (File providerFolder : providerFolderArray) {
					providerList.add(providerFolder.getName());
				}
			}
		}
		catch (Exception e) {
			LOG.error(e.getMessage());
		}
	}

	@SuppressWarnings("deprecation")
	public String provisionVM(Map<String, Object> variableMap) {
		
		String output = null;
		File tmpFolder = null;
		
		try {

			String provider = (String) variableMap.get("provider");
	
			if (StringUtils.isNotEmpty(provider)) {
	
				// copy terraform resources to filesystem
				tmpFolder = fileService.copyResourceToFilesystem("terraform/" + provider);
	
				// build the command string
				StrSubstitutor stringSubstitutor = new StrSubstitutor(variableMap);
				String command = stringSubstitutor.replace(terraformCommand);
	
				// execute command
				output = processExecutorService.execute(command, tmpFolder);
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

		return output;
	}

	public List<String> getProviderList() {
		return providerList;
	}
}
