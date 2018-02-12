package de.papke.cloud.portal.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import de.papke.cloud.portal.model.ApplicationModel;
import de.papke.cloud.portal.model.DashboardModel;
import de.papke.cloud.portal.pojo.ProvisionLog;
import de.papke.cloud.portal.service.ProvisionLogService;

@Controller
public class DashboardController extends ApplicationController {
	
	private static final String PREFIX = "/";
	private static final String DASHBOARD_MODEL_VAR_NAME = "dashboard";
	private static final String VAR_IMAGE_NAME = "image_name";
	
	@Autowired
	private ProvisionLogService provisionLogService;
	
	@GetMapping(path = PREFIX)
	public String index(Map<String, Object> model) {

		// fill model
		fillModel(model);

		// return view name
		return "index";
	}
	
	@Override
	protected void fillModel(Map<String, Object> model) {
		
		// fill model by parent class
		super.fillModel(model);

		// initialize statistic maps
		Map<Long, Integer> provisioningHistoryMap = new HashMap<>();
		Map<String, Integer> provisioningCommandMap = new HashMap<>();
		Map<String, Integer> cloudProviderUsageMap = new HashMap<>();
		Map<String, Integer> operatingSystemUsageMap = new HashMap<>();

		// get application model
		ApplicationModel applicationModel = (ApplicationModel) model.get(APPLICATION_MODEL_VAR_NAME);
		
		// iterate over cloud provider list
		for (String provider : applicationModel.getCloudProviderList()) {

			// get provisioning log list by provider
			List<ProvisionLog> provisionLogList = provisionLogService.getList(provider);

			// add cloud provider usage to map
			addCloudProviderUsageToMap(provider, cloudProviderUsageMap, provisionLogList);
			
			// iterate over provisioning log list
			for (ProvisionLog provisionLog : provisionLogList) {
				
				// add statistics to maps
				addProvisioningHistoryToMap(provisionLog, provisioningHistoryMap);
				addProvisioningCommandToMap(provisionLog, provisioningCommandMap);
				addOperatingSystemUsageToMap(provisionLog, operatingSystemUsageMap);
			}
		}
		
		// create dashboard model
		DashboardModel dashboardModel = new DashboardModel();
		dashboardModel.setProvisioningHistory(provisioningHistoryMap);
		dashboardModel.setProvisioningCommand(provisioningCommandMap);
		dashboardModel.setCloudProviderUsage(cloudProviderUsageMap);
		dashboardModel.setOperatingSystemUsage(operatingSystemUsageMap);
		
		// add dashboard model to request
		model.put(DASHBOARD_MODEL_VAR_NAME, dashboardModel);
	}

	private Map<String, Integer> addCloudProviderUsageToMap(String provider, Map<String, Integer> cloudProviderUsageMap, List<ProvisionLog> provisionLogList) {

		cloudProviderUsageMap.put(provider, provisionLogList.size());
		
		return cloudProviderUsageMap;
	}
	
	private Map<Long, Integer> addProvisioningHistoryToMap(ProvisionLog provisionLog, Map<Long, Integer> provisioningHistoryMap) {

		Date date = provisionLog.getDate();
		Date calculatedDate = DateUtils.truncate(date, Calendar.DAY_OF_MONTH); // NOSONAR
		long timeInMillis = calculatedDate.getTime();
		
		Integer counter = provisioningHistoryMap.get(timeInMillis);
		if (counter == null) {
			counter = 0;
		}
		counter = counter + 1;
		
		provisioningHistoryMap.put(timeInMillis, counter);
		
		return provisioningHistoryMap;
	}
	
	private Map<String, Integer> addProvisioningCommandToMap(ProvisionLog provisionLog, Map<String, Integer> provisioningCommandMap) {
		
		String command = provisionLog.getCommand();
		
		Integer counter = provisioningCommandMap.get(command);
		if (counter == null) {
			counter = 0;
		}
		counter = counter + 1;
		
		provisioningCommandMap.put(command, counter);
		
		return provisioningCommandMap;
	}
	
	private Map<String, Integer> addOperatingSystemUsageToMap(ProvisionLog provisionLog, Map<String, Integer> operatingSystemUsageMap) {
		
		String imageName = (String) provisionLog.getVariableMap().get(VAR_IMAGE_NAME);
		if (StringUtils.isNotEmpty(imageName)) {
			
			Integer counter = operatingSystemUsageMap.get(imageName);
			if (counter == null) {
				counter = 0;
			}
			counter = counter + 1;

			operatingSystemUsageMap.put(imageName, counter);
		}
		
		return operatingSystemUsageMap;
	}
}
