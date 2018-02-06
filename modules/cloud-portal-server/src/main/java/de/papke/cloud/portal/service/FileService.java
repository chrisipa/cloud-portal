package de.papke.cloud.portal.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileService {

	private static final Logger LOG = LoggerFactory.getLogger(FileService.class);
	
	@Autowired
	private ResourceService resourceService;
	
	public File copyResourceToFilesystem(String resourcePath) {
		return copyResourceToFilesystem(resourcePath, null);
	}
	
    public File copyResourceToFilesystem(String resourcePath, String targetPath) {

        File tmpFile = null;

        try {
        	
            File resource = resourceService.getClasspathResource(resourcePath);
            String resourceName = resource.getName();
            
            if (resource.isFile()) {
            	
            	if (targetPath != null) {
            		tmpFile = new File(targetPath);
            	}
            	else {
            		String prefix = FilenameUtils.removeExtension(resourceName);
            		String suffix = FilenameUtils.getExtension(resourceName);
            		tmpFile = File.createTempFile(prefix, suffix);	
            	}
            	
            	FileUtils.copyFile(resource, tmpFile);
            }
            else {
            	if (targetPath != null) {
            		tmpFile = new File(targetPath);
            	}
            	else {
            		String prefix = resourceName;
            		Path tmpPath = Files.createTempDirectory(prefix);
            		tmpFile = new File(tmpPath.toUri());
            	}
            	
            	FileUtils.copyDirectory(resource, tmpFile);
            }
            
            // set file executable
            makeExecutable(tmpFile, true);
        }
        catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        return tmpFile;
    }
    
    public void copyFolder(File sourceFolder, File targetFolder) {
    	
    	try {
    		FileUtils.copyDirectory(sourceFolder, targetFolder);
    		makeExecutable(targetFolder, true);
    	}
    	catch(Exception e) {
    		LOG.error(e.getMessage(), e);
    	}
    }
    
    public void makeExecutable(File file) {
    	makeExecutable(file, false);
    }
    
    public void makeExecutable(File file, boolean recursive) {
    	
    	file.setExecutable(true); // NOSONAR
    	
    	if (file.isDirectory() &&  recursive) {
    		for (File child : file.listFiles()) {
    			makeExecutable(child, recursive);
    		}
    	}
    }
}
