package de.papke.cloud.portal.service;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import de.papke.cloud.portal.constants.Constants;
import de.papke.cloud.portal.pojo.Variable;
import de.papke.cloud.portal.pojo.VariableConfig;
import de.papke.cloud.portal.pojo.VariableGroup;

@Service
public class VariableService {
	
	private static final Logger LOG = LoggerFactory.getLogger(VariableService.class);
	
	private static final String FILE_VARIABLES_YML = "variables.yml";
	private static final String FOLDER_VM = "vm";
	private static final String PROPERTY_VARIABLES = "variables";
	private static final String PROPERTY_VARIABLE_GROUPS = "variableGroups";
	
	private Map<String, List<VariableGroup>> providerDefaults = new HashMap<>();
	
	@Autowired
	private ResourceService resourceService;
	
	@PostConstruct
	public void init() {
		retrieveProviderDefaults();
	}
	
	private void retrieveProviderDefaults() {

		try {

			Constructor constructor = new Constructor(VariableConfig.class);

			TypeDescription variableConfigTypeDescription = new TypeDescription(VariableConfig.class);
			variableConfigTypeDescription.putListPropertyType(PROPERTY_VARIABLE_GROUPS, VariableGroup.class);
			constructor.addTypeDescription(variableConfigTypeDescription);

			TypeDescription variableGroupTypeDescription = new TypeDescription(VariableGroup.class);
			variableGroupTypeDescription.putListPropertyType(PROPERTY_VARIABLES, Variable.class);
			constructor.addTypeDescription(variableGroupTypeDescription);

			Yaml yaml = new Yaml(constructor);

			File terraformFolder = resourceService.getClasspathResource(Constants.FOLDER_PROVISIONER + File.separator + Constants.FOLDER_TERRAFORM + File.separator + FOLDER_VM);
			if (!terraformFolder.isFile()) {
				File[] providerFolderArray = terraformFolder.listFiles();
				for (File providerFolder : providerFolderArray) {
					File variableFile = new File(new URI(providerFolder.toURI() + File.separator + FILE_VARIABLES_YML));
					if (variableFile.exists()) {
						VariableConfig variableConfig = yaml.loadAs(new FileInputStream(variableFile), VariableConfig.class);
						List<VariableGroup> variableGroupList = variableConfig.getVariableGroups();
						getProviderDefaults().put(providerFolder.getName(), variableGroupList);
					}

				}
			}
		}
		catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
	}

	public Map<String, List<VariableGroup>> getVisibleProviderDefaults() {

		Map<String, List<VariableGroup>> visibleProviderDefaults = new HashMap<>();

		for (Entry<String, List<VariableGroup>> entry : getProviderDefaults().entrySet()) {
			List<VariableGroup> visibleVariableGroupList = new ArrayList<>();
			for (VariableGroup variableGroup : entry.getValue()) {
				if (!variableGroup.isHidden()) {
					visibleVariableGroupList.add(variableGroup);
				}
			}
			visibleProviderDefaults.put(entry.getKey(), visibleVariableGroupList);
		}

		return visibleProviderDefaults;
	}

	public List<Variable> getVisibleVariables(String provider) {

		List<Variable> variables = new ArrayList<>();

		for (VariableGroup variableGroup : getProviderDefaults().get(provider)) {
			if (!variableGroup.isHidden()) {
				for (Variable variable : variableGroup.getVariables()) {
					variables.add(variable);
				}
			}
		}

		return variables;
	}

	public Map<String, List<VariableGroup>> getProviderDefaults() {
		return providerDefaults;
	}
}
