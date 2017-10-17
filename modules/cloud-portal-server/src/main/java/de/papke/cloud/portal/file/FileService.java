package de.papke.cloud.portal.file;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FileService {
	
	private static final Logger LOG = LoggerFactory.getLogger(FileService.class);

    public File copyResourceToFilesystem(String resourcePath) {

        File tmpFile = null;

        try {
        	
        	URL url = getClass().getClassLoader().getResource(resourcePath);
            File resource = new File(url.toURI());
            String resourceName = resource.getName();
            
            String prefix = null;
            String suffix = null;
            
            if (resourceName.contains(".")) {
            	prefix = FilenameUtils.getPrefix(resourceName);
            	suffix = FilenameUtils.getExtension(resourceName);
            }
            else {
            	prefix = resourceName;
            }
            
        	
            if (resource.isFile()) {
            	tmpFile = File.createTempFile(prefix, suffix);	
            	FileUtils.copyFile(resource, tmpFile);
            }
            else {
            	Path tmpPath = Files.createTempDirectory(prefix);
            	tmpFile = new File(tmpPath.toUri());
            	FileUtils.copyDirectory(resource, tmpFile);
            }
        }
        catch (Exception e) {
            LOG.error(e.getMessage());
        }

        return tmpFile;
    }

}
