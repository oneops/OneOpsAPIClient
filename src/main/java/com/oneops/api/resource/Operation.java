package com.oneops.api.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.oneops.api.APIClient;
import com.oneops.api.OOInstance;
import com.oneops.api.ResourceObject;
import com.oneops.api.exception.OneOpsClientAPIException;
import com.oneops.api.resource.model.CiResource;
import com.oneops.api.resource.model.Procedure;
import com.oneops.api.util.IConstants;
import com.oneops.api.util.JsonUtil;

public class Operation extends APIClient {
	
	private String operationURI;
	private OOInstance instance;
	private String assemblyName;
	private String environmentName;
	
	public Operation(OOInstance instance, String assemblyName, String environmentName) throws OneOpsClientAPIException {
		super(instance);
		if(assemblyName == null || assemblyName.length() == 0) {
			String msg = "Missing assembly name";
			throw new OneOpsClientAPIException(msg);
		}
		
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name";
			throw new OneOpsClientAPIException(msg);
		}
		
		this.assemblyName = assemblyName;
		this.environmentName = environmentName;
		this.instance = instance;
		operationURI = IConstants.ASSEMBLY_URI + assemblyName + IConstants.OPERATION_URI  + IConstants.ENVIRONMENT_URI + environmentName;
	}

	/**
	 * Lists all instances for a given assembly, environment, platform and component
	 * 
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public List<CiResource> listInstances(String platformName, String componentName) throws OneOpsClientAPIException {
		if(platformName == null || platformName.length() == 0) {
			String msg = "Missing platform name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		if(componentName == null || componentName.length() == 0) {
			String msg = "Missing component name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.queryParam("instances_state", "all").get(operationURI 
				+ IConstants.PLATFORM_URI + platformName 
				+ IConstants.COMPONENT_URI + componentName 
				+ IConstants.INSTANCES_URI);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<CiResource>>(){});
				} else {
				String msg = String.format("Failed to get instances due to %s", response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = "Failed to get instances due to null response";
		throw new OneOpsClientAPIException(msg);
	}

	
	/**
	 * Mark all instances for replacement for a given platform and component
	 * 
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public Boolean markInstancesForReplacement(String platformName, String componentName) throws OneOpsClientAPIException {
		List<CiResource> instances = listInstances(platformName, componentName);
		List<Long> instanceIds = Lists.newArrayList();
		for (CiResource ciResource : instances) {
			instanceIds.add(ciResource.getCiId());
		}
		return markInstancesForReplacement(platformName, componentName, instanceIds);
	}
	
	/**
	 * Mark an instance for replacement for a given platform and component
	 * 
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public Boolean markInstanceForReplacement(String platformName, String componentName, Long instanceId) throws OneOpsClientAPIException {
		List<Long> instanceIds = Lists.newArrayList();
		instanceIds.add(instanceId);
		return markInstancesForReplacement(platformName, componentName, instanceIds);
	}
	
	/**
	 * Mark an instance for replacement for a given platform and component
	 * 
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	Boolean markInstancesForReplacement(String platformName, String componentName, List<Long> instanceIds) throws OneOpsClientAPIException {
		RequestSpecification request = createRequest();
		JSONObject jo = new JSONObject();
		jo.put("ids", instanceIds);
		jo.put("state", "replace");
		String uri = IConstants.ASSEMBLY_URI + assemblyName + IConstants.OPERATION_URI +  IConstants.INSTANCES_URI + "state" ;
		
		Response response = request.body(jo.toString()).put(uri);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(Boolean.class);
			} else {
				String msg = String.format("Failed to set replace marker on instance(s) due to %s", response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = "Failed to set replace marker on instance(s) due to null response";
		throw new OneOpsClientAPIException(msg);
	}
	
	public JsonPath getLogData(String procedureId, List<String> actionIds) throws OneOpsClientAPIException {
		RequestSpecification request = createRequest();
		String uri = IConstants.OPERATION_URI + IConstants.PROCEDURES_URI + "log_data" ;
		request.queryParam("procedure_id", procedureId);
		
		if(actionIds != null) {
			for(String actionId : actionIds){
				request.queryParam("action_ids", actionId);
			}
		}
		
		Response response = request.get(uri);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to set replace marker on instance(s) due to %s", response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = "Failed to set replace marker on instance(s) due to null response";
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Lists all procedures for a given platform 
	 * 
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public List<CiResource> listProcedures(String platformName) throws OneOpsClientAPIException {
		if(platformName == null || platformName.length() == 0) {
			String msg = "Missing platform name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		RequestSpecification request = createRequest();
		Response response = request.get(operationURI 
				+ IConstants.PLATFORM_URI + platformName 
				+ IConstants.PROCEDURES_URI);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<CiResource>>(){});
				} else {
				String msg = String.format("Failed to get procedures due to %s", response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = "Failed to get procedures due to null response";
		throw new OneOpsClientAPIException(msg);
	}
	
	
	/**
	 * Get procedure Id for a given platform, procedure name 
	 * 
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public Long getProcedureId(String platformName, String procedureName) throws OneOpsClientAPIException {
		if(platformName == null || platformName.length() == 0) {
			String msg = "Missing platform name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		if(procedureName == null || procedureName.length() == 0) {
			String msg = "Missing procedure name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		List<CiResource> procs = listProcedures(platformName);
		if(procs != null) {
			for (CiResource procedure : procs) {
				if(procedureName.equals(procedure.getCiName())) {
					return procedure.getCiId();
				}
			}
		}
		
		String msg = String.format("Failed to get procedure with the given name %s ", procedureName);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Lists all actions for a given platform, component
	 * 
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath listActions(String platformName, String componentName) throws OneOpsClientAPIException {
		if(platformName == null || platformName.length() == 0) {
			String msg = "Missing platform name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		if(componentName == null || componentName.length() == 0) {
			String msg = "Missing component name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.get(operationURI 
				+ IConstants.PLATFORM_URI + platformName 
				+ IConstants.COMPONENT_URI + componentName
				+ IConstants.ACTIONS_URI);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to get actions due to %s", response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = "Failed to get actions due to null response";
		throw new OneOpsClientAPIException(msg);
	}
	
	
	/**
	 * Execute procedure for a given platform
	 * 
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public Procedure executeProcedure(String platformName, String procedureName, String arglist) throws OneOpsClientAPIException {
		if(platformName == null || platformName.length() == 0) {
			String msg = "Missing platform name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		if(procedureName == null || procedureName.length() == 0) {
			String msg = "Missing procedure name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		ResourceObject ro = new ResourceObject();
		Map<String ,String> properties= new HashMap<String ,String>();
		
		Transition transition = new Transition(instance, this.assemblyName);
		CiResource platform = transition.getPlatform(this.environmentName, platformName);
		Long platformId = platform.getCiId();
		properties.put("procedureState", "active");
		properties.put("arglist", arglist);
		properties.put("definition",null);
		properties.put("ciId", "" +  platformId);
		properties.put("procedureCiId", "" + getProcedureId(platformName, procedureName));
		ro.setProperties(properties);
		
		JSONObject jsonObject = JsonUtil.createJsonObject(ro , "cms_procedure");
		Response response = request.body(jsonObject.toString()).post(IConstants.OPERATION_URI +  IConstants.PROCEDURES_URI);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(Procedure.class);
			} else {
				String msg = String.format("Failed to execute procedures due to %s", response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = "Failed to execute procedures due to null response";
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Get procedure status for a given Id 
	 * 
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public Procedure getProcedureStatus(Long procedureId) throws OneOpsClientAPIException {
		if(procedureId == null) {
			String msg = "Missing procedure Id to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.get(IConstants.OPERATION_URI +  IConstants.PROCEDURES_URI + procedureId);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(Procedure.class);
			} else {
				String msg = String.format("Failed to get procedure status due to %s", response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		
		String msg = String.format("Failed to get procedure status with the given Id " + procedureId);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Get procedure status for a given Id 
	 * 
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public Procedure cancelProcedure(Long procedureId) throws OneOpsClientAPIException {
		if(procedureId == null) {
			String msg = "Missing procedure Id to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		ResourceObject ro = new ResourceObject();
		Map<String ,String> properties= new HashMap<String ,String>();
		
		properties.put("procedureState", "canceled");
		properties.put("definition", null);
		properties.put("actions", null);
		properties.put("procedureCiId", null);
		properties.put("procedureId", null);
		ro.setProperties(properties);
		
		JSONObject jsonObject = JsonUtil.createJsonObject(ro , "cms_procedure");
		Response response = request.body(jsonObject.toString()).put(IConstants.OPERATION_URI +  IConstants.PROCEDURES_URI + procedureId);
		
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(Procedure.class);
			} else {
				String msg = String.format("Failed to cancel procedure due to %s", response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		
		String msg = String.format("Failed to cancel procedure with the given Id %s due to null response", procedureId);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Execute procedure for a given platform
	 * 
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public Procedure executeAction(String platformName, String componentName, String actionName, List<Long> instanceList, String arglist, int rollingPercent) throws OneOpsClientAPIException {
		if(platformName == null || platformName.length() == 0) {
			String msg = "Missing platform name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		if(componentName == null || componentName.length() == 0) {
			String msg = "Missing component name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		if(actionName == null || actionName.length() == 0) {
			String msg = "Missing action name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		if(instanceList == null || instanceList.size() == 0) {
			String msg = "Missing instances list to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		ResourceObject ro = new ResourceObject();
		Map<String ,String> properties= new HashMap<String ,String>();
		
		Transition transition = new Transition(instance, this.assemblyName);
		CiResource component = transition.getPlatformComponent(this.environmentName, platformName, componentName);
		Long componentId = component.getCiId();
		properties.put("procedureState", "active");
		properties.put("arglist", arglist);
		
		properties.put("ciId", "" +  componentId);
		properties.put("force", "true");
		properties.put("procedureCiId", "0");
		
		Map<String ,Object> flow = Maps.newHashMap();
		flow.put("targetIds", instanceList);
		flow.put("relationName", "base.RealizedAs");
		flow.put("direction", "from");
		
		Map<String ,Object> action = Maps.newHashMap();
		action.put("isInheritable", null);
		action.put("actionName", "base.RealizedAs");
		action.put("inherited", null);
		action.put("isCritical", "true");
		action.put("stepNumber", "1");
		action.put("extraInfo", null);
		action.put("actionName", actionName);
		
		List<Map<String ,Object>> actions = Lists.newArrayList();
		actions.add(action);
		flow.put("actions", actions);
		
		Map<String ,Object> definition = new HashMap<String ,Object>();
		List<Map<String ,Object>> flows = Lists.newArrayList();
		flows.add(flow);
		definition.put("flow", flows);
		definition.put("name", actionName);
		
		properties.put("definition", definition.toString());
		ro.setProperties(properties);
		
		JSONObject jsonObject = JsonUtil.createJsonObject(ro , "cms_procedure");
		Response response = request.body(jsonObject.toString()).post(IConstants.OPERATION_URI +  IConstants.PROCEDURES_URI);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(Procedure.class);
			} else {
				String msg = String.format("Failed to execute procedures due to %s", response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = "Failed to execute procedures due to null response";
		throw new OneOpsClientAPIException(msg);
	}
	
	public CiResource updatePlatformAutoHealingStatus(String environmentName, String platformName, String healingOption, boolean isEnabled) throws OneOpsClientAPIException {
		if (environmentName == null || environmentName.length() == 0) {
			String msg = String.format("Missing environment name to be updated");
			throw new OneOpsClientAPIException(msg);
		}

		if (platformName == null || platformName.length() == 0) {
			String msg = String.format("Missing platform name to be updated");
			throw new OneOpsClientAPIException(msg);
		}

		if (healingOption == null || healingOption.length() == 0) {
			String msg = String.format("No healing options available to be updated");
			throw new OneOpsClientAPIException(msg);
		}

		RequestSpecification request = createRequest();
		
		String enabled = "disable";
		if(isEnabled) {
			enabled = "enable";
		}
		
		Response response = request.body("").queryParam("status", enabled)
				.put(operationURI + IConstants.PLATFORM_URI + platformName + "/" + healingOption);
		if (response != null) {
			if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(CiResource.class);
			} else {
				String msg = String.format("Failed to update platforms due to %s", response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		}
		String msg = String.format("Failed to update platforms due to null response");
		throw new OneOpsClientAPIException(msg);
	}
	
	public CiResource updatePlatformAutoReplaceConfig(String environmentName, String platformName, int repairCount, int repairTime) throws OneOpsClientAPIException {
		if (environmentName == null || environmentName.length() == 0) {
			String msg = String.format("Missing environment name to be updated");
			throw new OneOpsClientAPIException(msg);
		}

		if (platformName == null || platformName.length() == 0) {
			String msg = String.format("Missing platform name to be updated");
			throw new OneOpsClientAPIException(msg);
		}

		RequestSpecification request = createRequest();
		JSONObject jo = new JSONObject();
		jo.put("replace_after_minutes", String.valueOf(repairTime));
		jo.put("replace_after_repairs", String.valueOf(repairCount));
		
		Response response = request.body(jo.toString())
				.put(operationURI + IConstants.PLATFORM_URI + platformName + "/autoreplace");
		if (response != null) {
			if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(CiResource.class);
			} else {
				String msg = String.format("Failed to update platforms due to %s", response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		}
		String msg = String.format("Failed to update platforms due to null response");
		throw new OneOpsClientAPIException(msg);
	}
}
