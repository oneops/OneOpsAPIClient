package com.oneops.api.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Uninterruptibles;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.oneops.api.APIClient;
import com.oneops.api.OOInstance;
import com.oneops.api.ResourceObject;
import com.oneops.api.exception.OneOpsClientAPIException;
import com.oneops.api.resource.model.RedundancyConfig;
import com.oneops.api.util.IConstants;
import com.oneops.api.util.JsonUtil;

public class Transition extends APIClient {

//	static String RESOURCE_URI = "/transition/environments/";
	private String transitionEnvUri;
	private OOInstance instance;
	private String assemblyName;
	
	public Transition(OOInstance instance, String assemblyName) throws OneOpsClientAPIException {
		super(instance);
		if(assemblyName == null || assemblyName.length() == 0) {
			String msg = "Missing assembly name";
			throw new OneOpsClientAPIException(msg);
		}
		this.assemblyName = assemblyName;
		this.instance = instance;
		transitionEnvUri = IConstants.ASSEMBLY_URI + assemblyName + IConstants.TRANSITION_URI + IConstants.ENVIRONMENT_URI;
	}
	
	/**
	 * Fetches specific environment details
	 * 
	 * @param environmentName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath getEnvironment(String environmentName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri + environmentName);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to get environment with name %s due to %s", environmentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to get environment with name %s due to null response", environmentName);
		throw new OneOpsClientAPIException(msg);
	}
	
	
	/**
	 * Lists all environments for a given assembly
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath listEnvironments() throws OneOpsClientAPIException {
		
		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to list environments due to %s", response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = "Failed to list environments due to null response";
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Creates environment within the given assembly
	 * 
	 * @param environmentName {mandatory}
	 * @param envprofile if exists
	 * @param availability {mandatory}
	 * @param platformAvailability
	 * @param cloudMap {mandatory}
	 * @param debugFlag {mandatory}
	 * @param gdnsFlag {mandatory}
	 * @param description
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath createEnvironment(String environmentName, String envprofile, String availability, 
			Map<String, String> platformAvailability , Map<String, String> cloudMap, boolean debugFlag, boolean gdnsFlag, String description) throws OneOpsClientAPIException {
		
		ResourceObject ro = new ResourceObject();
		Map<String ,String> attributes = new HashMap<String ,String>();
		Map<String ,String> properties= new HashMap<String ,String>();
		
		if(environmentName != null && environmentName.length() > 0) {
			properties.put("ciName", environmentName);
		} else {
			String msg = "Missing environment name to create environment";
			throw new OneOpsClientAPIException(msg);
		}
		properties.put("nsPath", instance.getOrgname() + "/" + assemblyName);
		if(availability != null && availability.length() > 0) {
			attributes.put("availability", availability);
		} else {
			String msg = "Missing availability to create environment";
			throw new OneOpsClientAPIException(msg);
		}
		
		ro.setProperties(properties);
		attributes.put("profile", envprofile);
		attributes.put("description", description);
		attributes.put("debug", String.valueOf(debugFlag));
		attributes.put("global_dns", String.valueOf(gdnsFlag));
		ro.setAttributes(attributes);
		
		RequestSpecification request = createRequest();
		JSONObject jsonObject = JsonUtil.createJsonObject(ro , "cms_ci");
		
		if(platformAvailability == null || platformAvailability.size() == 0) {
			Design design = new Design(instance, assemblyName);
			JsonPath platforms = design.listPlatforms();
			if(platforms != null) {
				List<Integer> ciIds = platforms.getList("ciId");
				platformAvailability = new HashMap<String, String>();
				for (Integer platform : ciIds) {
					platformAvailability.put(platform + "", availability);
				}
			}
		}
		jsonObject.put("platform_availability", platformAvailability);
		
		if(cloudMap == null || cloudMap.size() == 0) {
			String msg = "Missing clouds map to create environment";
			throw new OneOpsClientAPIException(msg);
		}
		jsonObject.put("clouds", cloudMap);
		
		Response response = request.body(jsonObject.toString()).post(transitionEnvUri);
		
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to create environment with name %s due to %s", environmentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to create environment with name %s due to null response", environmentName);
		throw new OneOpsClientAPIException(msg);
	}
	

	/**
	 * Commits environment open releases
	 * 
	 * @param environmentName {mandatory}
	 * @param excludePlatforms
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath commitEnvironment(String environmentName, List<Integer> excludePlatforms, String comment) throws OneOpsClientAPIException {
		
		RequestSpecification request = createRequest();
		JSONObject jo = new JSONObject();
		if(excludePlatforms != null && excludePlatforms.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < excludePlatforms.size(); i++) {
				sb.append(excludePlatforms.get(i));
				if(i < (excludePlatforms.size() - 1)){
					sb.append(",");
				}
			}
			jo.put("exclude_platforms", sb.toString());
		}
		if(comment != null)
			jo.put("desc", comment);
		Response response = request.body(jo.toString()).post(transitionEnvUri + environmentName + "/commit");
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				
				response = request.get(transitionEnvUri + environmentName);
				String envState = response.getBody().jsonPath().get("ciState");
				//wait for deployment plan to generate
				do {
					Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
					response = request.get(transitionEnvUri + environmentName);
					if(response == null) {
						String msg = String.format("Failed to commit environment due to null response");
						throw new OneOpsClientAPIException(msg);
					}
					envState = response.getBody().jsonPath().get("ciState");
				} while(response != null && "locked".equalsIgnoreCase(envState));
				
				return response.getBody().jsonPath();
				
			} else {
				String msg = String.format("Failed to commit environment due to %s",  response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = "Failed to commit environment due to null response";
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Deploy an already generated deployment plan
	 * 
	 * @param environmentName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath deploy(String environmentName, String comments) throws OneOpsClientAPIException {
		
		RequestSpecification request = createRequest();
		
		Response response = request.get(transitionEnvUri + environmentName + IConstants.RELEASES_URI + "bom");
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				Integer releaseId = response.getBody().jsonPath().get("releaseId");
				String nsPath = response.getBody().jsonPath().get("nsPath");
				if(releaseId != null && nsPath!= null) {
					Map<String ,String> properties= new HashMap<String ,String>();
					properties.put("nsPath", nsPath);
					properties.put("releaseId", releaseId + "");
					if(comments != null) {
						properties.put("comments", comments);
					}
					ResourceObject ro = new ResourceObject();
					ro.setProperties(properties);
					JSONObject jsonObject = JsonUtil.createJsonObject(ro , "cms_deployment");
					response = request.body(jsonObject.toString()).post(transitionEnvUri + environmentName + "/deployments/");
					if(response == null) {
						String msg = String.format("Failed to start deployment for environment %s due to null response" , environmentName);
						throw new OneOpsClientAPIException(msg);
					}
					if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
						return response.getBody().jsonPath();
					} else {
						String msg = String.format("Failed to start deployment for environment %s due to null response" , environmentName);
						throw new OneOpsClientAPIException(msg);
					}
				} else {
					String msg = String.format("Failed to find release id to be deployed");
					throw new OneOpsClientAPIException(msg);
				}
				
			} else {
				String msg = String.format("Failed to start deployment for environment %s due to %s", environmentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to get latest deployment details for environment %s due to null response" , environmentName);
		throw new OneOpsClientAPIException(msg);
	}
	
	
	/**
	 * Fetches deployment status for the given assembly/environment
	 * 
	 * @param environmentName
	 * @param deploymentId
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath getDeploymentStatus(String environmentName, String deploymentId) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		if(deploymentId == null || deploymentId.length() == 0) {
			String msg = "Missing deployment to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri + environmentName + IConstants.DEPLOYMENTS_URI + deploymentId + "/status");
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to get deployment status for environment %s with deployment Id %s due to %s", environmentName, deploymentId, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to get deployment status for environment %s with deployment Id %s due to null response", environmentName, deploymentId);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Fetches latest deployment for the given assembly/environment
	 * 
	 * @param environmentName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath getLatestDeployment(String environmentName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri + environmentName + IConstants.DEPLOYMENTS_URI + "latest" );
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to get latest deployment for environment %s due to %s", environmentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to get latest deployment for environment %s due to null response", environmentName);
		throw new OneOpsClientAPIException(msg);
	}
	
	
	/**
	 * Discard already generated deployment plan
	 * 
	 * @param environmentName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath discardDeploymentPlan(String environmentName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		
		JsonPath bomRelease = getBomRelease(environmentName);
		if(bomRelease != null) {
			long releaseId = bomRelease.getLong("releaseId");
			Response response = request.body("").post(transitionEnvUri + environmentName + IConstants.RELEASES_URI + releaseId + "/discard" );
			if(response != null) {
				if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
					return response.getBody().jsonPath();
				} else {
					String msg = String.format("Failed to discard deployment plan for environment %s due to %s", environmentName, response.getStatusLine());
					throw new OneOpsClientAPIException(msg);
				}
			} 
		} else {
			String msg = String.format("Failed to discard deployment plan for environment %s due to no open bom", environmentName);
			throw new OneOpsClientAPIException(msg);
		}
		
		String msg = String.format("Failed to discard deployment plan for environment %s due to null response", environmentName);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Discard uncommitted changes for the given {environmentName}
	 * 
	 * @param environmentName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath discardOpenRelease(String environmentName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.body("").post(transitionEnvUri + environmentName + "/discard" );
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to discard changes for environment %s due to %s", environmentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		
		String msg = String.format("Failed to discard changes for environment %s due to null response", environmentName);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Disable all platforms for the given assembly/environment
	 * 
	 * @param environmentName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath disableAllPlatforms(String environmentName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to disable all platforms";
			throw new OneOpsClientAPIException(msg);
		}
		
		JsonPath ps = listPlatforms(environmentName);
		List<String> platformIds = ps.getList("ciId");
		
		RequestSpecification request = createRequest();
		Response response = request.queryParam("platformCiIds[]", platformIds).put(transitionEnvUri + environmentName + "/disable" );
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to disable platforms for environment %s due to %s", environmentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to disable platforms for environment %s due to null response", environmentName);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Fetches latest release for the given assembly/environment
	 * 
	 * @param environmentName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath getLatestRelease(String environmentName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri + environmentName + IConstants.RELEASES_URI + "latest" );
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to get latest releases for environment %s due to %s", environmentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to get latest releases for environment %s due to null response", environmentName);
		throw new OneOpsClientAPIException(msg);
	}
	
	
	/**
	 * Fetches bom release for the given assembly/environment
	 * 
	 * @param environmentName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath getBomRelease(String environmentName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri + environmentName + IConstants.RELEASES_URI + "bom" );
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("No bom releases found for environment %s ", environmentName);
				System.out.println(msg);
				return null;
			}
		} 
		String msg = String.format("Failed to get bom releases for environment %s due to null response", environmentName);
		throw new OneOpsClientAPIException(msg);
	}
	
	
	/**
	 * Cancels a failed/paused deployment
	 * 
	 * @param environmentName
	 * @param deploymentId
	 * @param releaseId
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath cancelDeployment(String environmentName, String deploymentId, String releaseId) throws OneOpsClientAPIException {
		return updateDeploymentStatus(environmentName, deploymentId, releaseId, "canceled");
	}
	
	
	public JsonPath getDeployment(String environmentName, String deploymentId) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		if(deploymentId == null || deploymentId.length() == 0) {
			String msg = "Missing deployment Id to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri + environmentName + IConstants.DEPLOYMENTS_URI + deploymentId );
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to get deployment details for environment %s for id %s due to %s", environmentName, deploymentId, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to get deployment details for environment %s for id %s due to null response", environmentName, deploymentId);
		throw new OneOpsClientAPIException(msg);
	}
	
	public JsonPath getDeploymentRfcLog(String environmentName, String deploymentId, String rfcId) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		if(deploymentId == null || deploymentId.length() == 0) {
			String msg = "Missing deployment Id to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		if(rfcId == null || rfcId.length() == 0) {
			String msg = "Missing rfc Id to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.queryParam("rfcId", rfcId).get(transitionEnvUri + environmentName + IConstants.DEPLOYMENTS_URI + deploymentId + "/log_data");
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to get deployment logs for environment %s, deployment id %s and rfcId %s due to %s", environmentName, deploymentId, rfcId, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to get deployment logs for environment %s, deployment id %s and rfcId %s due to null response", environmentName, deploymentId, rfcId);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Approve a deployment
	 * 
	 * @param environmentName
	 * @param deploymentId
	 * @param releaseId
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath approveDeployment(String environmentName, String deploymentId, String releaseId) throws OneOpsClientAPIException {
		return updateDeploymentStatus(environmentName, deploymentId, releaseId, "active");
	}
	
	/**
	 * Retry a deployment
	 * 
	 * @param environmentName
	 * @param deploymentId
	 * @param releaseId
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath retryDeployment(String environmentName, String deploymentId, String releaseId) throws OneOpsClientAPIException {
		return updateDeploymentStatus(environmentName, deploymentId, releaseId, "active");
	}
	
	/**
	 * Update deployment state
	 * 
	 * @param environmentName
	 * @param deploymentId
	 * @param releaseId
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	private JsonPath updateDeploymentStatus(String environmentName, String deploymentId, String releaseId, String newstate) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		if(deploymentId == null || deploymentId.length() == 0) {
			String msg = "Missing deployment to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		
		Map<String ,String> properties= new HashMap<String ,String>();
		properties.put("deploymentState", newstate);
		properties.put("releaseId", releaseId );
		ResourceObject ro = new ResourceObject();
		ro.setProperties(properties);
		JSONObject jsonObject = JsonUtil.createJsonObject(ro , "cms_deployment");
		
		Response response = request.body(jsonObject.toString()).put(transitionEnvUri + environmentName + IConstants.DEPLOYMENTS_URI + deploymentId);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to update deployment state to %s for environment %s with deployment Id %s due to %s", newstate, environmentName, deploymentId, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to update deployment state to %s for environment %s with deployment Id %s due to null response", newstate, environmentName, deploymentId);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Deletes the given environment
	 * 
	 * @param environmentName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath deleteEnvironment(String environmentName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to delete";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.delete(transitionEnvUri + environmentName);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to delete environment with name %s due to %s", environmentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to delete environment with name %s due to null response", environmentName);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * List platforms for a given assembly/environment
	 * 
	 * @param environmentName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath listPlatforms(String environmentName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name list platforms";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri + environmentName + IConstants.PLATFORM_URI);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to get list of environment platforms due to %s", response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = "Failed to get list of environment platforms due to null response";
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Get platform details for a given assembly/environment
	 * 
	 * @param environmentName
	 * @param platformName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath getPlatform(String environmentName, String platformName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to get details";
			throw new OneOpsClientAPIException(msg);
		}
		if(platformName == null || platformName.length() == 0) {
			String msg = "Missing platform name to get details";
			throw new OneOpsClientAPIException(msg);
		}
		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri + environmentName + IConstants.PLATFORM_URI + platformName);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to get environment platform details due to %s", response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = "Failed to get environment platform details due to null response";
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * List platform components for a given assembly/environment/platform
	 * 
	 * @param environmentName
	 * @param platformName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath listPlatformComponents(String environmentName, String platformName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to list enviornment platform components";
			throw new OneOpsClientAPIException(msg);
		}
		if(platformName == null || platformName.length() == 0) {
			String msg = "Missing platform name to list enviornment platform components";
			throw new OneOpsClientAPIException(msg);
		}
		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri + environmentName + IConstants.PLATFORM_URI + platformName + IConstants.COMPONENT_URI);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to get list of environment platforms components due to %s", response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = "Failed to get list of environment platforms components due to null response";
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Get platform component details for a given assembly/environment/platform
	 * 
	 * @param environmentName
	 * @param platformName
	 * @param componentName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath getPlatformComponent(String environmentName, String platformName, String componentName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to get enviornment platform component details";
			throw new OneOpsClientAPIException(msg);
		}
		if(platformName == null || platformName.length() == 0) {
			String msg = "Missing platform name to get enviornment platform component details";
			throw new OneOpsClientAPIException(msg);
		}
		if(componentName == null || componentName.length() == 0) {
			String msg = "Missing component name to get enviornment platform component details";
			throw new OneOpsClientAPIException(msg);
		}
		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri + environmentName + IConstants.PLATFORM_URI + platformName + IConstants.COMPONENT_URI + componentName);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to get enviornment platform component details due to %s", response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = "Failed to get enviornment platform component details due to null response";
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Update component attributes for a given assembly/environment/platform/component
	 * 
	 * @param environmentName
	 * @param platformName
	 * @param componentName
	 * @param attributes
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath updatePlatformComponent(String environmentName, String platformName, String componentName, Map<String, String> attributes) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to update component attributes";
			throw new OneOpsClientAPIException(msg);
		}
		if(platformName == null || platformName.length() == 0) {
			String msg = "Missing platform name to update component attributes";
			throw new OneOpsClientAPIException(msg);
		}
		if(componentName == null || componentName.length() == 0) {
			String msg = "Missing component name to update component attributes";
			throw new OneOpsClientAPIException(msg);
		}
		if(attributes == null || attributes.size() == 0) {
			String msg = "Missing attributes list to be updated";
			throw new OneOpsClientAPIException(msg);
		}
		
		JsonPath componentDetails = getPlatformComponent(environmentName, platformName, componentName);
		if(componentDetails != null) {
			ResourceObject ro = new ResourceObject();
			
			String ciId = componentDetails.getString("ciId");
			RequestSpecification request = createRequest();
			Map<String, String> attr = componentDetails.getMap("ciAttributes");
			if(attr == null) {
				attr = new HashMap<String, String> ();
			}
			attr.putAll(attributes);
			ro.setAttributes(attr);
			Map<String, String> ownerProps =  componentDetails.getMap("ciAttrProps.owner");
			if(ownerProps == null) {
				ownerProps = Maps.newHashMap();
			}
			for(Entry<String, String> entry :  attributes.entrySet()) {
				ownerProps.put(entry.getKey(), "manifest");
			}
			ro.setOwnerProps(ownerProps);

			
			JSONObject jsonObject = JsonUtil.createJsonObject(ro , "cms_dj_ci");
 			Response response = request.body(jsonObject.toString()).put(transitionEnvUri + environmentName + IConstants.PLATFORM_URI + platformName + IConstants.COMPONENT_URI + ciId);
			if(response != null) {
				if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
					return response.getBody().jsonPath();
				} else {
					String msg = String.format("Failed to get update component %s due to %s", componentName, response.getStatusLine());
					throw new OneOpsClientAPIException(msg);
				}
			} 
		}
		String msg = String.format("Failed to get update component %s due to null response", componentName);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * touch component
	 * 
	 * @param environmentName
	 * @param platformName
	 * @param componentName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath touchPlatformComponent(String environmentName, String platformName, String componentName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to update component attributes";
			throw new OneOpsClientAPIException(msg);
		}
		if(platformName == null || platformName.length() == 0) {
			String msg = "Missing platform name to update component attributes";
			throw new OneOpsClientAPIException(msg);
		}
		if(componentName == null || componentName.length() == 0) {
			String msg = "Missing component name to update component attributes";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		JSONObject jo = new JSONObject();
		
		Response response = request.body(jo.toString()).post(transitionEnvUri + environmentName + IConstants.PLATFORM_URI + platformName + IConstants.COMPONENT_URI + componentName + "/touch");
		
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to touch component %s due to %s", componentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to get touch component %s due to null response", componentName);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Pull latest design commits
	 * 
	 * @param environmentName {mandatory}
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath pullDesin(String environmentName) throws OneOpsClientAPIException {
		
		RequestSpecification request = createRequest();
		JSONObject jo = new JSONObject();
		
		Response response = request.body(jo.toString()).post(transitionEnvUri + environmentName + "/pull");
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to pull design for environment %s due to %s", environmentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to pull design for environment %s due to null response", environmentName);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Use this pull design when new platform(s) or newer version of platform(s) is being added to the environment
	 *  
	 * @param environmentName
	 * @param platformAvailability
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath pullNewPlatform(String environmentName, Map<String, String> platformAvailability) throws OneOpsClientAPIException {
		
		RequestSpecification request = createRequest();
		JSONObject jo = new JSONObject();
		
		if(platformAvailability == null || platformAvailability.size() == 0) {
			Design design = new Design(instance, assemblyName);
			JsonPath platforms = design.listPlatforms();
			
			JsonPath env = getEnvironment(environmentName);
			String availability = "single";
			if(env != null) {
				availability = env.getString("ciAttributes.availability");
			}
			if(platforms != null) {
				List<Integer> ciIds = platforms.getList("ciId");
				platformAvailability = new HashMap<String, String>();
				for (Integer platform : ciIds) {
					platformAvailability.put(platform + "", availability);
				}
			}
		}
		jo.put("platform_availability", platformAvailability);
		
		Response response = request.body(jo.toString()).post(transitionEnvUri + environmentName + "/pull");
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to pull design for environment %s due to %s", environmentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to pull design for environment %s due to null response", environmentName);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * List local variables for a given assembly/environment/platform
	 * 
	 * @param environmentName
	 * @param platformName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath listPlatformVariables(String environmentName, String platformName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to list enviornment platform variables";
			throw new OneOpsClientAPIException(msg);
		}
		if(platformName == null || platformName.length() == 0) {
			String msg = "Missing platform name to list enviornment platform variables";
			throw new OneOpsClientAPIException(msg);
		}
		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri + environmentName + IConstants.PLATFORM_URI + platformName + IConstants.VARIABLES_URI);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to get list of environment platforms variables due to %s", response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = "Failed to get list of environment platforms variables due to null response";
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Update platform local variables for a given assembly/environment/platform
	 * 
	 * @param environmentName
	 * @param platformName
	 * @param variables
	 * @param isSecure
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	 
	public Boolean updatePlatformVariable(String environmentName, String platformName, Map<String, String> variables, boolean isSecure) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to update variables";
			throw new OneOpsClientAPIException(msg);
		}
		if(platformName == null || platformName.length() == 0) {
			String msg = "Missing platform name to update variables";
			throw new OneOpsClientAPIException(msg);
		}
		if(variables == null || variables.size() == 0) {
			String msg = "Missing variables list to be updated";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		boolean success = false;
		for(Entry<String, String> entry : variables.entrySet()) {
			ResourceObject ro = new ResourceObject();
			Map<String ,String> attributes = new HashMap<String ,String>();
			
			String uri = transitionEnvUri + environmentName + IConstants.PLATFORM_URI + platformName + IConstants.VARIABLES_URI + entry.getKey();
			Response response = request.get(uri);
			if(response != null) {
				JSONObject var = JsonUtil.createJsonObject(response.getBody().asString());
				if(var != null && var.has("ciAttributes")) {
					JSONObject attrs = var.getJSONObject("ciAttributes");
					 for (Object key : attrs.keySet()) {
				        //based on you key types
				        String keyStr = (String)key;
				        String keyvalue = String.valueOf(attrs.get(keyStr));

				        attributes.put(keyStr, keyvalue);
				    }
					if(isSecure) {
						attributes.put("secure", "true");
						attributes.put("encrypted_value", entry.getValue());
					} else {
						attributes.put("secure", "false");
						attributes.put("value", entry.getValue());
					}
					ro.setAttributes(attributes);
					
					JSONObject jsonObject = JsonUtil.createJsonObject(ro , "cms_dj_ci");
					if(response != null ) {
						response = request.body(jsonObject.toString()).put(uri);
						if(response != null) {
							if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
								success = true;
							} else {
								String msg = String.format("Failed to get update variables %s due to %s", entry.getKey(), response.getStatusLine());
								throw new OneOpsClientAPIException(msg);
							}
						} 
					}
				} else {
					success = true;
				}
			} else {
				String msg = String.format("Failed to get update variables %s due to null response", variables);
				throw new OneOpsClientAPIException(msg);
			}
			
		}
		
		return success;
	}
	
	

	/**
	 * List global variables for a given assembly/environment
	 * 
	 * @param environmentName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath listGlobalVariables(String environmentName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to list enviornment variables";
			throw new OneOpsClientAPIException(msg);
		}
		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri + environmentName + IConstants.VARIABLES_URI);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to get list of environment variables due to %s", response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = "Failed to get list of environment variables due to null response";
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Update global variables for a given assembly/environment
	 * 
	 * @param environmentName
	 * @param variables
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public Boolean updateGlobalVariable(String environmentName, Map<String, String> variables, boolean isSecure) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to update component attributes";
			throw new OneOpsClientAPIException(msg);
		}
		
		if(variables == null || variables.size() == 0) {
			String msg = "Missing variables list to be updated";
			throw new OneOpsClientAPIException(msg);
		}
		boolean success = false;
		RequestSpecification request = createRequest();
		for(Entry<String, String> entry : variables.entrySet()) {
			ResourceObject ro = new ResourceObject();
			Map<String ,String> attributes = new HashMap<String ,String>();
			
			String uri = transitionEnvUri + environmentName + IConstants.VARIABLES_URI + entry.getKey();
			Response response = request.get(uri);
			if(response != null) {
				JSONObject var = JsonUtil.createJsonObject(response.getBody().asString());
				if(var != null && var.has("ciAttributes")) {
					JSONObject attrs = var.getJSONObject("ciAttributes");
					 for (Object key : attrs.keySet()) {
				        //based on you key types
				        String keyStr = (String)key;
				        String keyvalue = String.valueOf(attrs.get(keyStr));
				        attributes.put(keyStr, keyvalue);
				    }
					if(isSecure) {
						attributes.put("secure", "true");
						attributes.put("encrypted_value", entry.getValue());
					} else {
						attributes.put("secure", "false");
						attributes.put("value", entry.getValue());
					}
						
					ro.setAttributes(attributes);
					
					JSONObject jsonObject = JsonUtil.createJsonObject(ro , "cms_dj_ci");
					response = request.body(jsonObject.toString()).put(uri);
					if(response != null) {
						if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
							success = true;
						} else {
							String msg = String.format("Failed to get update variables %s due to %s", entry.getKey(), response.getStatusLine());
							throw new OneOpsClientAPIException(msg);
						}
					} 
				} else {
					success = true;
				}
			} else {
				String msg = String.format("Failed to get update variables %s due to null response", variables);
				throw new OneOpsClientAPIException(msg);
			}
			
		}
		
		
		return success;
	}
	
	/**
	 * Mark the input {#platformIdList} platforms for delete
	 * 
	 * @param environmentName
	 * @param platformIdList
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath updateDisableEnvironment(String environmentName, List<String> platformIdList) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to disable platforms";
			throw new OneOpsClientAPIException(msg);
		}
		
		if(platformIdList == null || platformIdList.size() == 0) {
			String msg = "Missing platforms list to be disabled";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("platformCiIds", platformIdList);
		
		Response response = request.body(jsonObject.toString()).put(transitionEnvUri + environmentName + "/disable");
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to disable platforms for environment with name %s due to %s", environmentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to disable platforms for environment with name %s due to null response", environmentName);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Update redundancy configuration for a given platform
	 * 
	 * @param environmentName
	 * @param platformName
	 * @param config update any 
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath updatePlatformRedundancyConfig(String environmentName, String platformName, RedundancyConfig config) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to be updated";
			throw new OneOpsClientAPIException(msg);
		}
		
		if(platformName == null || platformName.length() == 0) {
			String msg = "Missing platform name to be updated";
			throw new OneOpsClientAPIException(msg);
		}
		
		if(config == null ) {
			String msg = "Missing redundancy config to be updated";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		
		JSONObject redundant = new JSONObject();
		redundant.put("max", config.getMax());
		redundant.put("pct_dpmt", config.getPercentDeploy());
		redundant.put("step_down", config.getStepDown());
		redundant.put("flex", "true");
		redundant.put("converge", "false");
		redundant.put("min", config.getMin());
		redundant.put("current", config.getCurrent());
		redundant.put("step_up", config.getStepUp());
		JSONObject rconfig = new JSONObject();
		rconfig.put("relationAttributes", redundant);
		
		redundant = new JSONObject();
		redundant.put("max", "manifest");
		redundant.put("pct_dpmt", "manifest");
		redundant.put("step_down", "manifest");
		redundant.put("flex", "manifest");
		redundant.put("converge", "manifest");
		redundant.put("min", "manifest");
		redundant.put("current", "manifest");
		redundant.put("step_up", "manifest");
		JSONObject owner = new JSONObject();
		rconfig.put("owner", owner);
		
		rconfig.put("relationAttrProps", owner);
		
		JsonPath computeDetails = getPlatformComponent(environmentName, platformName, "compute");
		int computeId = computeDetails.getInt("ciId");
		
		JSONObject jo = new JSONObject();
		jo.put(String.valueOf(computeId), rconfig);
		
		
		JSONObject dependsOn = new JSONObject();
		dependsOn.put("depends_on", jo);
		
		System.out.println(dependsOn);
		Response response = request.body(dependsOn.toString()).put(transitionEnvUri + environmentName + IConstants.PLATFORM_URI + platformName);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to update platforms redundancy for environment with name %s due to %s", environmentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to update platforms redundancy for environment with name %s due to null response", environmentName);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * List relays
	 * 
	 * @param environmentName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath listRelays(String environmentName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to get relays";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri + environmentName + "/relays/");
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to list relay due to %s", response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = "Failed to list relay due to null response";
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Get relay details
	 * 
	 * @param environmentName
	 * @param relayName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	 
	public JsonPath getRelay(String environmentName, String relayName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to get relay details";
			throw new OneOpsClientAPIException(msg);
		}
		
		if(relayName == null || relayName.length() == 0) {
			String msg = "Missing relay name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri + environmentName + "/relays/" + relayName);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to get relay with name %s due to %s", relayName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to get relay with name %s due to null response", relayName);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Add new Relay
	 * 
	 * @param relayName
	 * @param severity
	 * @param emails
	 * @param source
	 * @param nsPaths
	 * @param regex
	 * @param correlation
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath addRelay(String environmentName, String relayName, String severity, String emails, String source, String nsPaths, String regex, boolean correlation) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to create relay";
			throw new OneOpsClientAPIException(msg);
		}
		
		ResourceObject ro = new ResourceObject();
		Map<String ,String> attributes = Maps.newHashMap();
		Map<String ,String> properties= Maps.newHashMap();
		
		if(relayName == null || relayName.length() == 0) {
			String msg = "Missing relay name to create one";
			throw new OneOpsClientAPIException(msg);
		} 
		if(emails == null || emails.length() == 0) {
			String msg = "Missing emails addresses to create relay";
			throw new OneOpsClientAPIException(msg);
		} 
		if(severity == null || severity.length() == 0) {
			String msg = "Missing severity to create relay";
			throw new OneOpsClientAPIException(msg);
		} 
		if(source == null || source.length() == 0) {
			String msg = "Missing source to create relay";
			throw new OneOpsClientAPIException(msg);
		} 
		
		properties.put("ciName", relayName);
		
		RequestSpecification request = createRequest();
		ro.setProperties(properties);
		attributes.put("enabled", "true");
		attributes.put("emails", emails);
		if(!Strings.isNullOrEmpty(severity))
			attributes.put("severity", severity);
		if(!Strings.isNullOrEmpty(source))
			attributes.put("source", source);
		if(!Strings.isNullOrEmpty(nsPaths))
			attributes.put("ns_paths", nsPaths);
		if(!Strings.isNullOrEmpty(regex))
			attributes.put("text_regex", regex);
		
		attributes.put("correlation", String.valueOf(correlation));
		ro.setAttributes(attributes);
		
		JSONObject jsonObject = JsonUtil.createJsonObject(ro , "cms_ci");
		Response response = request.body(jsonObject.toString()).post(transitionEnvUri + environmentName + "/relays/");
		
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to create relay with name %s due to %s", relayName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to create relay with name %s due to null response", relayName);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Update relay
	 * 
	 * @param environmentName
	 * @param relayName
	 * @param severity
	 * @param emails
	 * @param source
	 * @param nsPaths
	 * @param regex
	 * @param correlation
	 * @param enable
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath updateRelay(String environmentName, String relayName, String severity, String emails, String source, String nsPaths, String regex, boolean correlation, boolean enable) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to create relay";
			throw new OneOpsClientAPIException(msg);
		}
		
		ResourceObject ro = new ResourceObject();
		Map<String ,String> attributes = Maps.newHashMap();
		
		if(relayName == null || relayName.length() == 0) {
			String msg = "Missing relay name to update";
			throw new OneOpsClientAPIException(msg);
		} 
		
		RequestSpecification request = createRequest();
		
		Response relayResponse = request.get(transitionEnvUri + environmentName + "/relays/" + relayName);
		if(relayResponse != null) {
			JsonPath newVarJsonPath = relayResponse.getBody().jsonPath();
			if(newVarJsonPath != null) {
				attributes = newVarJsonPath.getMap("ciAttributes");
				if(attributes == null) {
					attributes = Maps.newHashMap();
				} else {
					attributes.put("enabled", String.valueOf(enable));
					if(!Strings.isNullOrEmpty(emails))
						attributes.put("emails", emails);
					if(!Strings.isNullOrEmpty(severity))
						attributes.put("severity", severity);
					if(!Strings.isNullOrEmpty(source))
						attributes.put("source", source);
					if(!Strings.isNullOrEmpty(nsPaths))
						attributes.put("ns_paths", nsPaths);
					if(!Strings.isNullOrEmpty(regex))
						attributes.put("text_regex", regex);
				}
			}
		}
		ro.setAttributes(attributes);
		
		JSONObject jsonObject = JsonUtil.createJsonObject(ro , "cms_ci");
		
		Response response = request.body(jsonObject.toString()).put(transitionEnvUri + environmentName + "/relays/" + relayName);
		
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to update relay with name %s due to %s", relayName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to update relay with name %s due to null response", relayName);
		throw new OneOpsClientAPIException(msg);
	}
}
