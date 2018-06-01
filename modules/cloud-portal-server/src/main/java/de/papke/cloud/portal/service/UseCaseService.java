package de.papke.cloud.portal.service;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import de.papke.cloud.portal.constants.Constants;
import de.papke.cloud.portal.pojo.UseCase;
import de.papke.cloud.portal.pojo.Variable;
import de.papke.cloud.portal.pojo.VariableConfig;
import de.papke.cloud.portal.pojo.VariableGroup;

@Service
public class UseCaseService {
	
	private static final Logger LOG = LoggerFactory.getLogger(UseCaseService.class);
	
	private Map<String, UseCase> useCaseMap = new HashMap<>(); 
	private Set<String> providers = new HashSet<>(); 
	
	private static final String FILE_VARIABLES_YML = "variables.yml";
	private static final String PROPERTY_VARIABLES = "variables";
	private static final String PROPERTY_VARIABLE_GROUPS = "variableGroups";
	
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
	
	public UseCase getUseCaseById(String id) {
		return useCaseMap.get(id);
	}
	
	public List<VariableGroup> getVariableGroups(String id) {
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
}
