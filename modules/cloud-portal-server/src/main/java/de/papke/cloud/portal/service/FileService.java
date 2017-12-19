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
	
	@Autowired
	private ResourceService resourceService;
	
	private static final Logger LOG = LoggerFactory.getLogger(FileService.class);

    public File copyResourceToFilesystem(String resourcePath) {

        File tmpFile = null;

        try {
        	
            File resource = resourceService.getClasspathResource(resourcePath);
            String resourceName = resource.getName();
            
            if (resource.isFile()) {
            	String prefix = FilenameUtils.removeExtension(resourceName);
            	String suffix = FilenameUtils.getExtension(resourceName);
            	tmpFile = File.createTempFile(prefix, suffix);	
            	FileUtils.copyFile(resource, tmpFile);
            }
            else {
            	String prefix = resourceName;
            	Path tmpPath = Files.createTempDirectory(prefix);
            	tmpFile = new File(tmpPath.toUri());
            	FileUtils.copyDirectory(resource, tmpFile);
            }
            
            // set file executable
            tmpFile.setExecutable(true); // NOSONAR
        }
        catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        return tmpFile;
    }

}
