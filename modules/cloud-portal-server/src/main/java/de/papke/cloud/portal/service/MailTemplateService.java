package de.papke.cloud.portal.service;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MailTemplateService {
	
	private static final Logger LOG = LoggerFactory.getLogger(MailTemplateService.class);

	public static final String VM_PROVISION_SUCCESS = "vm-provision-success";
	public static final String VM_PROVISION_ERROR = "vm-provision-error";
	public static final String VM_DEPROVISION_SUCCESS = "vm-deprovision-success";
	public static final String VM_DEPROVISION_ERROR = "vm-deprovision-error";

	private static final String TEMPLATE_FOLDER_NAME = "mail";
	
	private Map<String, String> mailTemplateMap = new HashMap<>();

	@PostConstruct
	public void init() {
		
		try {
			
			URL url = getClass().getClassLoader().getResource(TEMPLATE_FOLDER_NAME);
			File mailFolder = new File(url.toURI());
			if (!mailFolder.isFile()) {
				File[] mailFolderArray = mailFolder.listFiles();
				for (File mailTemplateFile : mailFolderArray) {
					String mailTemplateName = FilenameUtils.getBaseName(mailTemplateFile.getName());
					String mailTemplatePath = TEMPLATE_FOLDER_NAME + File.separator + mailTemplateFile.getName(); 
					mailTemplateMap.put(mailTemplateName, mailTemplatePath);
				}
			}
		}
		catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
	public String getMailTemplatePath(String mailTemplateName) {
		return mailTemplateMap.get(mailTemplateName);
	}
}
