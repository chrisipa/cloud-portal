package de.papke.cloud.portal.service;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

@Service
public class ResourceService {

	private static final Logger LOG = LoggerFactory.getLogger(ResourceService.class);

	private static final String PREFIX_CLASSPATH = "classpath:/";

	@Autowired
	private ResourceLoader resourceLoader;
	
	public File getClasspathResource(String path) {
		
		File file = null;
		
		try {
			Resource resource = resourceLoader.getResource(PREFIX_CLASSPATH + path);
			file = new File(resource.getURI());
		}
		catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		
		return file;
	}
}
