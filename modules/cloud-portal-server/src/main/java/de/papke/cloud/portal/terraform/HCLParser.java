package de.papke.cloud.portal.terraform;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

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
		
		return variableList;
	}
}
