package com.oneops.api.resource;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.oneops.api.APIClient;
import com.oneops.api.OOInstance;
import com.oneops.api.ResourceObject;
import com.oneops.api.exception.OneOpsClientAPIException;
import com.oneops.api.resource.model.CiResource;
import com.oneops.api.util.IConstants;
import com.oneops.api.util.JsonUtil;

public class Monitor extends APIClient {

	private String transitionMonitorUri;
	private String environmentName;
	
	public Monitor(OOInstance instance, String assemblyName, String environment, String platform, String component) throws OneOpsClientAPIException {
		super(instance);
		if(assemblyName == null || assemblyName.length() == 0) {
			String msg = "Missing assembly name";
			throw new OneOpsClientAPIException(msg);
		}
		if(environment == null || environment.length() == 0) {
			String msg = "Missing environment name";
			throw new OneOpsClientAPIException(msg);
		}
		if(platform == null || platform.length() == 0) {
			String msg = "Missing platform name";
			throw new OneOpsClientAPIException(msg);
		}
		if(component == null || component.length() == 0) {
			String msg = "Missing component name";
			throw new OneOpsClientAPIException(msg);
		}
		
		this.environmentName = environment;
		transitionMonitorUri = IConstants.ASSEMBLY_URI + assemblyName + IConstants.TRANSITION_URI +  IConstants.ENVIRONMENT_URI
				 + environmentName 
				+ IConstants.PLATFORM_URI + platform
				+ IConstants.COMPONENT_URI + component + IConstants.MONITORS_URI;
	}
	
	/**
	 * Fetches specific monitor details
	 * 
	 * @param monitorName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public CiResource getMonitor(String monitorName) throws OneOpsClientAPIException {
		if(monitorName == null || monitorName.length() == 0) {
			String msg = "Missing monitor name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.get(transitionMonitorUri + monitorName);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(CiResource.class);
			} else {
				String msg = String.format("Failed to get environment with name %s due to %s", environmentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to get environment with name %s due to null response", environmentName);
		throw new OneOpsClientAPIException(msg);
	}
	
	
	
	/**
	 * Update monitor
	 * 
	 * @param monitorName
	 * @param cmdOptions
	 * @param duration
	 * @param sampleInterval
	 * @param thresholds
	 * @param heartbeat
	 * @param enable
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public CiResource updateMonitor(String monitorName, String cmdOptions, Integer duration, Integer sampleInterval, JSONObject thresholds, boolean heartbeat, boolean enable) throws OneOpsClientAPIException {
		if(monitorName == null || monitorName.length() == 0) {
			String msg = "Missing monitor name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.get(transitionMonitorUri + monitorName);
		
		ResourceObject ro = new ResourceObject();
		Map<String, String> attributes = new HashMap<String ,String>();
		JSONObject monitor = JsonUtil.createJsonObject(response.getBody().asString());
		if(monitor != null && monitor.has("ciAttributes")) {
			JSONObject attrs = monitor.getJSONObject("ciAttributes");
			 for (Object key : attrs.keySet()) {
		        //based on you key types
		        String keyStr = (String)key;
		        String keyvalue = String.valueOf(attrs.get(keyStr));

		        attributes.put(keyStr, keyvalue);
		        
		        if(cmdOptions != null && attributes.containsKey("cmd_options")) {
					attributes.put("cmd_options", String.valueOf(cmdOptions));
				}
				
				if(duration != null && attributes.containsKey("duration")) {
					attributes.put("duration", String.valueOf(duration));
				}
				
				if(sampleInterval != null && attributes.containsKey("sample_interval")) {
					attributes.put("sample_interval", String.valueOf(sampleInterval));
				}
				
				if(thresholds != null && attributes.containsKey("thresholds")) {
					attributes.put("thresholds", thresholds.toString());
				}
				
		    }
			attributes.put("heartbeat", String.valueOf(heartbeat));
			attributes.put("enable", String.valueOf(enable));
		}
		ro.setAttributes(attributes);
		JSONObject jsonObject = JsonUtil.createJsonObject(ro , "cms_dj_ci");
		response = request.body(jsonObject.toString()).put(transitionMonitorUri + monitorName);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(CiResource.class);
			} else {
				String msg = String.format("Failed to monitor definition for monitor %s due to %s", monitorName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		}		
		
		String msg = String.format("Failed to update monitor definition for %s due to null response", monitorName);
		throw new OneOpsClientAPIException(msg);
	}
	
	
	public JSONObject getThresholdJson(String name, String state, String metric, String bucket, String stat, JSONObject trigger, JSONObject reset) {
		JSONObject thresholdDef = new JSONObject();
		thresholdDef.put("bucket", bucket);
		thresholdDef.put("metric", metric);
		thresholdDef.put("state", state);
		thresholdDef.put("stat", stat);
		thresholdDef.put("trigger", trigger);
		thresholdDef.put("reset", reset);
		
		JSONObject threshold = new JSONObject();
		threshold.put(name, thresholdDef);
		
		return threshold;
	}
	
	public JSONObject createThresholdCondition(String operator, int value, int duration, int numOfOccurances) {
		JSONObject thresholdCondition = new JSONObject();
		thresholdCondition.put("operator", operator);
		thresholdCondition.put("value", value);
		thresholdCondition.put("duration", duration);
		thresholdCondition.put("numocc", numOfOccurances);
		
		return thresholdCondition;
	}
	
	public JSONObject addMetrics(String metricName, String unit, String dstype, String display, String description){
		JSONObject metricinfo = new JSONObject();
		metricinfo.put("unit", unit);
		metricinfo.put("dstype", dstype);
		metricinfo.put("display", display);
		metricinfo.put("display_group", "");
		metricinfo.put("description", description);
		return metricinfo;
	}
	
	//list thresholds
	//add threshold
	//update threshold
	//delete threshold
}
