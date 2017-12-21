package de.papke.cloud.portal.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.papke.cloud.portal.constants.Constants;
import de.papke.cloud.portal.pojo.ProvisionLog;
import de.papke.cloud.portal.service.ProvisionLogService;

@Controller
public class ProvisionLogController {
	
	private static final String PRIVATE_KEY_MIME_TYPE = "application/x-pem-file";
	private static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
	private static final String PREFIX = "/provision-log";
	
	@Autowired
	private ProvisionLogService provisionLogService;
	
    @RequestMapping(value = PREFIX + "/private-key/{id}", method = RequestMethod.GET)
    public void downloadPrivateKey(HttpServletResponse response, @PathVariable("id") String id) throws IOException {

    	ProvisionLog provisionLog = provisionLogService.get(id);
    	
    	if (provisionLog != null) {
    		byte[] privateKey = provisionLog.getPrivateKey();
    		response.setContentType(PRIVATE_KEY_MIME_TYPE);
    		response.setHeader(HEADER_CONTENT_DISPOSITION, String.format("inline; filename=\"%s\"", Constants.KEY_FILE_PREFIX));
    		response.setContentLength(privateKey.length);
    		IOUtils.copy(new ByteArrayInputStream(privateKey), response.getOutputStream());
    	}
    	else {
    		response.sendError(HttpServletResponse.SC_NOT_FOUND);
    	}
    }

}
