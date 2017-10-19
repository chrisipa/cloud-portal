package de.papke.cloud.portal.terraform;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class HCLParser {
	
	private File file;
	
	public HCLParser(File file) {
		this.file = file;
	}
	
	public Map<String, List<Variable>> parse() throws IOException {
		
		Map<String, List<Variable>> variableGroupMap = new LinkedHashMap<>();
		List<Variable> variableList = null;
		Variable variable = null;
		
		if (file.exists()) {
			for (String line : FileUtils.readLines(file, StandardCharsets.UTF_8)) {
				if (!line.startsWith("#")) {
					
					String variableName = null;
					
					if (line.startsWith("variable")) {
						
						if (variable != null) {
							
							String prefix = getPrefix(variable.getName());
							
							if (StringUtils.isNotEmpty(prefix)) {
								
								variableList = variableGroupMap.get(prefix);
								if (variableList == null) {
									variableList = new ArrayList<>();
								}
								
								variableList.add(variable);
								variableGroupMap.put(prefix, variableList);
							}
						}
						
						variable = new Variable();
						
						String[] variableArray = line.split("\"");
						variableName = variableArray[1];
						variable.setName(variableName);
						variable.setTitle(getVariableTitle(variableName));
					}
					
					if (line.contains("=")) {
						
						String[] keyValueArray = line.trim().split("=");
						
						if (keyValueArray.length == 2) {
							
							String key = keyValueArray[0].trim();
							String value = keyValueArray[1].trim().replace("\"", "");
							
							if (key.equals("description")) {
								variable.setDescription(value);
							}
							
							if (key.equals("default")) {
								variable.setDefaultValue(value);
							}
						}
					}
				}
			}
		}
		
		String prefix = getPrefix(variable.getName());
		
		if (StringUtils.isNotEmpty(prefix)) {
			
			variableList = variableGroupMap.get(prefix);
			if (variableList == null) {
				variableList = new ArrayList<>();
			}
			
			variableList.add(variable);
			variableGroupMap.put(prefix, variableList);
		}
		
		return variableGroupMap;
	}
	
	private String getPrefix(String variableName) {
		
		String prefix = "";
		
		if (StringUtils.isNotEmpty(variableName)) {
			String[] variableNameArray = variableName.split("-");
			if (variableNameArray.length >= 3) {
				prefix = variableNameArray[0];
			}
		}
		
		return prefix;
	}
	
	private String getVariableTitle(String variableName) {
		
		String title = "";
		
		if (StringUtils.isNotEmpty(variableName)) {
			String[] variableNameArray = variableName.split("-");
			if (variableNameArray.length >= 3) {
				for (int i = 1; i < variableNameArray.length - 1; i++) {
					title += StringUtils.capitalize(variableNameArray[i]);
					if (i < variableNameArray.length - 2) {
						title += " ";
					}
				}
			}
		}
		
		return title;
	}
}
