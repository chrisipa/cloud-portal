package de.papke.cloud.portal.terraform;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class HCLParser {
	
	private File file;
	
	public HCLParser(File file) {
		this.file = file;
	}
	
	public List<Variable> parse() throws IOException {
		
		List<Variable> variableList = new ArrayList<>();
		Variable variable = null;
		
		if (file.exists()) {
			for (String line : FileUtils.readLines(file, StandardCharsets.UTF_8)) {
				if (!line.startsWith("#")) {
					
					String variableName = null;
					
					if (line.startsWith("variable")) {
						
						if (variable != null) {
							variableList.add(variable);
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
		
		variableList.add(variable);
		
		return variableList;
	}
	
	private String getVariableTitle(String variableName) {
		
		String title = "";
		
		String[] variableNameArray = variableName.split("-");
		if (variableNameArray.length >= 3) {
			for (int i = 1; i < variableNameArray.length - 1; i++) {
				title += StringUtils.capitalize(variableNameArray[i]);
				if (i < variableNameArray.length - 2) {
					title += " ";
				}
			}
		}
		
		return title;
	}
}
