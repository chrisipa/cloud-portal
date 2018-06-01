package de.papke.cloud.portal.service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.papke.cloud.portal.constants.Constants;
import de.papke.cloud.portal.pojo.UseCase;

@Service
public class UseCaseService {
	
	private static final Logger LOG = LoggerFactory.getLogger(UseCaseService.class);
	
	private Map<String, UseCase> useCaseMap = new HashMap<>(); 
	
	@Autowired
	private ResourceService resourceService;	
	
	@PostConstruct
	public void init() {
		getUseCasesFromFileSystem();
	}

	private void getUseCasesFromFileSystem() {

		try {
			
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
						
						UseCase useCase = new UseCase();
						useCase.setId(id);
						useCase.setProvider(provider);
						useCase.setProvisioner(provisioner);
						useCaseMap.put(id, useCase);
					}
				}
			}
		}
		catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}
}
