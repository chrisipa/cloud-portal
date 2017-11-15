package de.papke.cloud.portal.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.papke.cloud.portal.dao.ProvisionLogDao;
import de.papke.cloud.portal.pojo.ProvisionLog;
import de.papke.cloud.portal.pojo.User;
import de.papke.cloud.portal.util.ZipUtil;

@Service
public class ProvisionLogService {

	private static final Logger LOG = LoggerFactory.getLogger(ProvisionLogService.class);

	private static final String TMP_FILE_PREFIX = "provision-log";
	private static final String TMP_FILE_SUFFIX = ".zip";

	@Autowired
	private UserService userService;

	@Autowired
	private ProvisionLogDao provisionLogDao;

	public List<ProvisionLog> getList(String provider) {

		List<ProvisionLog> provisionLogList = new ArrayList<>();

		User user = userService.getUser();
		if (user != null) {
			String username = user.getUsername();
			provisionLogList = provisionLogDao.findByUsernameAndProvider(username, provider);
		}

		return provisionLogList;
	}

	public ProvisionLog get(String id) {
		return provisionLogDao.findById(id);
	}

	public ProvisionLog create(String state, String provider, Boolean success, Map<String, Object> variableMap, File tmpFolder) {

		ProvisionLog provisionLog = null;
		File zipFile = null;

		try {

			// get username;
			User user = userService.getUser();
			String username = user.getUsername();

			// zip temp folder
			zipFile = File.createTempFile(TMP_FILE_PREFIX, TMP_FILE_SUFFIX);
			ZipUtil.zip(tmpFolder, zipFile);
			byte[] data = IOUtils.toByteArray(new FileInputStream(zipFile));

			// remove credentials from variable map
			Map<String, Object> variableMapWithoutCredentials = removeCredentialsFromMap(variableMap);

			// create provision log
			provisionLog = provisionLogDao.save(new ProvisionLog(new Date(), username, state, provider, success, variableMapWithoutCredentials, data));
		}
		catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		finally {
			if (zipFile != null) {
				zipFile.delete();
			}
		}

		return provisionLog;
	}

	public ProvisionLog update(ProvisionLog provisionLog) {
		
		// set date
		provisionLog.setDate(new Date());
		
		// remove credentials from variable map
		Map<String, Object> variableMapWithoutCredentials = removeCredentialsFromMap(provisionLog.getVariableMap());
		provisionLog.setVariableMap(variableMapWithoutCredentials);
		
		return provisionLogDao.save(provisionLog);
	}

	public void delete(String id) {
		provisionLogDao.delete(id);
	}

	private Map<String, Object> removeCredentialsFromMap(Map<String, Object> variableMap) {

		Map<String, Object> variableMapWithoutCredentials = new HashMap<>();
		for (String key : variableMap.keySet()) {
			if (!key.startsWith("credentials-")) {
				Object value = variableMap.get(key);
				variableMapWithoutCredentials.put(key, value);
			}
		}

		return variableMapWithoutCredentials;
	}
}
