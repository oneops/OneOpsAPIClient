package com.oneops.api.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Uninterruptibles;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.oneops.api.APIClient;
import com.oneops.api.OOInstance;
import com.oneops.api.ResourceObject;
import com.oneops.api.exception.OneOpsClientAPIException;
import com.oneops.api.resource.model.*;
import com.oneops.api.util.IConstants;
import com.oneops.api.util.JsonUtil;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class Transition extends APIClient {
	
	private static final Logger LOG = LoggerFactory.getLogger(Transition.class);
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
	public CiResource getEnvironment(String environmentName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri + environmentName);
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
	 * Lists all environments for a given assembly
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public List<CiResource> listEnvironments() throws OneOpsClientAPIException {
		
		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<CiResource>>(){});
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
	 * @param attributes {mandatory}
	 * @param platformAvailability
	 * @param cloudMap {mandatory}
	 * @param description
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public CiResource createEnvironment(String environmentName, String envprofile, Map<String ,String> attributes, 
			Map<String, String> platformAvailability , Map<String, Map<String, String>> cloudMap, String description) throws OneOpsClientAPIException {
		
		ResourceObject ro = new ResourceObject();
		Map<String ,String> properties= new HashMap<String ,String>();
		
		if(environmentName != null && environmentName.length() > 0) {
			properties.put("ciName", environmentName);
		} else {
			String msg = "Missing environment name to create environment";
			throw new OneOpsClientAPIException(msg);
		}
		if(attributes == null) {
			String msg = "Missing availability in attributes map to create environment";
			throw new OneOpsClientAPIException(msg);
		}
		
		properties.put("nsPath", instance.getOrgname() + "/" + assemblyName);
		
		String availability = null;
		if(attributes.containsKey("availability")) {
			availability = attributes.get("availability");
		} else {
			String msg = "Missing availability in attributes map to create environment";
			throw new OneOpsClientAPIException(msg);
		}
		
		if(attributes.containsKey("global_dns")) {
			attributes.put("global_dns", String.valueOf(attributes.get("global_dns")));
		}
		
		ro.setProperties(properties);
		attributes.put("profile", envprofile);
		attributes.put("description", description);
		String subdomain = environmentName + "." + assemblyName + "." + instance.getOrgname();
		attributes.put("subdomain", subdomain);
		ro.setAttributes(attributes);
		
		RequestSpecification request = createRequest();
		JSONObject jsonObject = JsonUtil.createJsonObject(ro , "cms_ci");
		if(platformAvailability == null || platformAvailability.size() == 0) {
			Design design = new Design(instance, assemblyName);
			List<CiResource> platforms = design.listPlatforms();
			if(platforms != null) {
				platformAvailability = new HashMap<String, String>();
				for (CiResource platform : platforms) {
					platformAvailability.put(platform.getCiId() + "", availability);
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
				return response.getBody().as(CiResource.class);
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
	public Release commitEnvironment(String environmentName, List<Long> excludePlatforms, String comment) throws OneOpsClientAPIException {
		
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
				String envState = response.getBody().jsonPath().getString("ciState");
				//wait for deployment plan to generate
				do {
					Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
					response = request.get(transitionEnvUri + environmentName);
					if(response == null) {
						String msg = String.format("Failed to commit environment due to null response");
						throw new OneOpsClientAPIException(msg);
					}
					envState = response.getBody().jsonPath().getString("ciState");
				} while(response != null && "locked".equalsIgnoreCase(envState));
				
				String comments = response.getBody().jsonPath().getString("comments");
				if(comments != null && comments.startsWith("ERROR:")) {
					String msg = String.format("Failed to commit environment due to %s",  comments);
					throw new OneOpsClientAPIException(msg);
				}
				
				return response.getBody().as(Release.class);
				
			} else {
				String msg = String.format("Failed to commit environment %s. %s.", environmentName, getErrorMessageFromResponse(response));
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to commit environment %s due to null response", environmentName);
		throw new OneOpsClientAPIException(msg);
	}

	/**
	 * Commits environment and generate in-memory deployment plan
	 *
	 * @param environmentName {mandatory}
	 * @param excludePlatforms
	 * @param includeClouds
	 * @param ignoreCloudDeploymentOrder
	 * @param comment
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public DeploymentBom commitEnvironment(String environmentName, List<Long> excludePlatforms, List<Long> includeClouds,
									 boolean ignoreCloudDeploymentOrder, String comment) throws OneOpsClientAPIException {

		RequestSpecification request = createRequest();
		StringBuilder queryString = new StringBuilder();
		queryString.append(IConstants.DEFAULT_COMMIT_QUERY_STRING);

		appendListToQueryString(excludePlatforms, queryString, "exclude_platforms");

		appendListToQueryString(includeClouds, queryString, "include_clouds");

		queryString.append("&").append("ignore_cloud_dpmt_order=").append(ignoreCloudDeploymentOrder);

		if(comment != null) {
			queryString.append("&").append("desc=").append(comment);
		}

		String url = transitionEnvUri + environmentName + "/deployments/bom?" + queryString.toString();
		Response response = request.get(url);

		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				DeploymentBom body = response.getBody().as(DeploymentBom.class);

				if (body.getErrors() != null) {
					StringBuilder sb = new StringBuilder();
					for (String error : body.getErrors()) {
						sb.append(error).append(" ");
					}
					sb = sb.deleteCharAt(sb.length() - 1);
					String msg = String.format("Failed to commit environment %s due to the following errors %s",
							environmentName, sb.toString());
					throw new OneOpsClientAPIException(msg);
				}

				Capacity capacity = body.getCapacity();
				if (!"ok".equals(capacity.getReservationCheck())) {
					String msg = String.format("Environment %s committed successfully." +
									"But reservation check failed: %s. Cannot proceed with deployment",
							environmentName, capacity.getReservationCheck());
					throw new OneOpsClientAPIException(msg);
				}

				return body;

			} else {
				String msg = String.format("Failed to commit environment %s. %s.", environmentName, getErrorMessageFromResponse(response));
				throw new OneOpsClientAPIException(msg);
			}
		}
		String msg = String.format("Failed to commit environment %s due to null response", environmentName);
		throw new OneOpsClientAPIException(msg);
	}

	/**
	 * Generate a customized deployment plan and start the deployment.
	 *
	 * 1. The first Deployment API call generates a deployment plan, starts the deployment and returns
	 * the deployment plan information.
	 * 2. When CI state becomes "default", the deployment has been initiated/failed.
	 * Check the environment API till CI state changes from "locked" to "default"
	 * 3. Check the message of the latest deployment and if that matches the comments provided to the method call.
	 * 4. If the comments match return the latest deployment object
	 * 5. If it does not match, that means the deployment was not successfully initiated. 
	 * 		a. Check for `comments` of the environment to see if there was any ERROR.
	 * 		Throw `OneOpsClientAPIException` with the message.
	 * 	 	b. If there are no errors in `comments` of the environment, throw `OneOpsClientAPIException`
	 * 	  	with DEFAULT_ERROR_MESSAGE.
	 *
	 * @param environmentName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public Deployment deploy(String environmentName, List<Long> excludePlatforms, List<Long> includeComponents, List<Long> includeClouds,
							 boolean ignoreCloudDeploymentOrder, String comments) throws OneOpsClientAPIException {

		RequestSpecification request = createRequest();
		Map<String, String> properties = new HashMap<>();

		addStringToPropertyMap(excludePlatforms, properties, "exclude_platforms");
		addStringToPropertyMap(includeComponents, properties, "components");
		addStringToPropertyMap(includeClouds, properties, "include_clouds");

		properties.put("ignore_cloud_dpmt_order", String.valueOf(ignoreCloudDeploymentOrder));

		ResourceObject ro = new ResourceObject();
		ro.setProperties(properties);

		JSONObject jsonObject = JsonUtil.createJsonObject(ro, null);

		if (comments == null)
			comments = String.format("Deployment for %s environment identified by: %s",
					environmentName, UUID.randomUUID().toString());

		Map<String, String> cmsDeployment = new HashMap<>();
		cmsDeployment.put("comments", comments);
		jsonObject.put("cms_deployment", cmsDeployment);

		// Start a new deployment
		CiResource envInfo = doDeploy(environmentName, transitionEnvUri, request, jsonObject);

		while (!IConstants.DEFAULT_CI_STATE.equalsIgnoreCase(envInfo.getCiState())) {
			LOG.info("Deployment initiation in progress ...");
			try {
				Thread.sleep(5000);
			} catch(InterruptedException ie) {
				LOG.error("Error waiting for deployment to start: {}", ie.getMessage());
			}
			envInfo = getEnvironment(environmentName);
		}

		Deployment latestDeployment = getLatestDeployment(environmentName);

		if (!comments.equalsIgnoreCase(latestDeployment.getComments())) {
			String envComments = envInfo.getComments();

			if (!envComments.startsWith(IConstants.DEFAULT_ERROR_COMMENT_PREFIX))
				envComments = String.format(IConstants.DEFAULT_ERROR_MESSAGE, environmentName);
			throw new OneOpsClientAPIException(envComments);
		}

		return latestDeployment;
	}

	/**
	 * Deploy an already generated deployment plan
	 * 
	 * @param environmentName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public Deployment deploy(String environmentName, String comments) throws OneOpsClientAPIException {
		
		RequestSpecification request = createRequest();
		
		 Release bomRelease = getBomRelease(environmentName);
		 Long releaseId = bomRelease.getReleaseId();
		 String nsPath = bomRelease.getNsPath();
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
			Response response = request.body(jsonObject.toString()).post(transitionEnvUri + environmentName + "/deployments/");
			if(response == null) {
				String msg = String.format("Failed to start deployment for environment %s due to null response" , environmentName);
				throw new OneOpsClientAPIException(msg);
			}
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(Deployment.class);
			} else {
				String msg = String.format("Failed to start deployment for environment %s. %s" , environmentName, getErrorMessageFromResponse(response));
				throw new OneOpsClientAPIException(msg);
			}
		} else {
			String msg = String.format("Failed to find release id to be deployed for environment %s", environmentName);
			throw new OneOpsClientAPIException(msg);
		}
	}

	/**
	 * Fetches deployment status for the given assembly/environment
	 * 
	 * @param environmentName
	 * @param deploymentId
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public Deployment getDeploymentStatus(String environmentName, Long deploymentId) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		if(deploymentId == null) {
			String msg = "Missing deployment to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri + environmentName + IConstants.DEPLOYMENTS_URI + deploymentId + "/status");
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(Deployment.class);
			} else {
				String msg = String.format("Failed to get deployment status for environment %s with deployment Id %s. %s",
						environmentName, deploymentId, getErrorMessageFromResponse(response));
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
	public Deployment getLatestDeployment(String environmentName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri + environmentName + IConstants.DEPLOYMENTS_URI + "latest" );
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(Deployment.class);
			} else {
				String msg = String.format("Failed to get latest deployment for environment %s. %s", environmentName, getErrorMessageFromResponse(response));
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
	public Release discardDeploymentPlan(String environmentName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		
		Release bomRelease = getBomRelease(environmentName);
		if(bomRelease != null) {
			long releaseId = bomRelease.getReleaseId();
			Response response = request.body("").post(transitionEnvUri + environmentName + IConstants.RELEASES_URI + releaseId + "/discard" );
			if(response != null) {
				if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
					return response.getBody().as(Release.class);
				} else {
					String msg = String.format("Failed to discard deployment plan for environment %s. %s", environmentName, getErrorMessageFromResponse(response));
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
	public Release discardOpenRelease(String environmentName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.body("").post(transitionEnvUri + environmentName + "/discard" );
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(Release.class);
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
	public CiResource disableAllPlatforms(String environmentName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to disable all platforms";
			throw new OneOpsClientAPIException(msg);
		}
		
		List<CiResource> ps = listPlatforms(environmentName);
		List<Long> platformIds = Lists.newArrayList();
		for (CiResource ciResource : ps) {
			platformIds.add(ciResource.getCiId());
		}
		
		RequestSpecification request = createRequest();
		Response response = request.queryParam("platformCiIds[]", platformIds).put(transitionEnvUri + environmentName + "/disable" );
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(CiResource.class);
			} else {
				String msg = String.format("Failed to disable platforms for environment %s due to %s", environmentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to disable platforms for environment %s due to null response", environmentName);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Disable selected list of platforms for the given assembly/environment
	 * 
	 * @param environmentName
	 * @param platformNames
	 * @param status disable/enable
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public CiResource updatePlatformStatus(String environmentName, List<String> platformNames, String status) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to disable all platforms";
			throw new OneOpsClientAPIException(msg);
		}
		if(status == null || status.length() == 0) {
			String msg = "Missing status(disable/enable) for platform to be updated";
			throw new OneOpsClientAPIException(msg);
		}
		if(platformNames == null || platformNames.size() == 0) {
			String msg = "Missing platform name list to be updated";
			throw new OneOpsClientAPIException(msg);
		}
		
		List<CiResource> ps = listPlatforms(environmentName);
		List<Long> platformIds = Lists.newArrayList();
		for (CiResource ciResource : ps) {
			if(platformNames.contains(ciResource.getCiName())) {
				platformIds.add(ciResource.getCiId());
			}
		}
		
		RequestSpecification request = createRequest();
		Response response = request.queryParam("platformCiIds[]", platformIds).put(transitionEnvUri + environmentName + "/" + status );
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(CiResource.class);
			} else {
				String msg = String.format("Failed to update platforms %s status to %s for environment %s due to %s", platformNames, status, environmentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to update platforms %s status to %s for environment %s due to null response", platformNames, status, environmentName);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Fetches latest release for the given assembly/environment
	 * 
	 * @param environmentName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public Release getLatestRelease(String environmentName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri + environmentName + IConstants.RELEASES_URI + "latest" );
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(Release.class);
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
	public Release getBomRelease(String environmentName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri + environmentName + IConstants.RELEASES_URI + "bom" );
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(Release.class);
			} else {
				String msg = String.format("No bom-releases or updates found for environment %s. Please try to touch a component and commit the env before deploying.", environmentName);
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to get bom releases for environment %s due to null response", environmentName);
		throw new OneOpsClientAPIException(msg);
	}

	/**
	 * Restore a specific release
	 *
	 * @param environmentName
	 * @apram releaseId
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public Release restoreRelease(String environmentName, Long releaseId) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}

		if(releaseId == null) {
			String msg = "Missing releaseId to fetch the details";
			throw new OneOpsClientAPIException(msg);
		}

		Release latestRelease = getLatestRelease(environmentName);
		String releaseState = latestRelease.getReleaseState();
		if("open".equals(releaseState.toLowerCase())) {
			String msg = String.format("Failed to restore release, release -> %s is in open state", latestRelease.getReleaseId());
			throw new OneOpsClientAPIException(msg);
		}

		RequestSpecification request = createRequest();
		Response response = request.post(transitionEnvUri + environmentName + IConstants.RELEASES_URI + releaseId +"/restore" );
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				Map<String, Object> map = (Map<String, Object>)response.getBody().as(Map.class);
				Object o = map.get("release");

				return JsonUtil.convert(o, new TypeReference<Release>(){});

			} else {
				String msg = String.format("Failed to restore release for environment %s due to %s", environmentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		}
		String msg = String.format("Failed to restore releases for environment %s due to null response", environmentName);
		throw new OneOpsClientAPIException(msg);
	}

	/**
	 * Fetch all release for given environment
	 *
	 * @param environmentName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public List<Release> listReleases(String environmentName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}

		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri + environmentName + "/timeline" );
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				List<Release> releases = new ArrayList<>();
				List<Object> timeline = (List<Object>)response.getBody().as(List.class);
				for(Object o : timeline) {
					if(o instanceof Map && ((Map) o).get("releaseName") != null) {
						Release release = JsonUtil.convert(o, new TypeReference<Release>(){});
						releases.add(release);
					}
				}

				return releases;

			} else {
				String msg = String.format("Failed to restore release for environment %s due to %s", environmentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		}
		String msg = String.format("Failed to restore releases for environment %s due to null response", environmentName);
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
	public Deployment cancelDeployment(String environmentName, Long deploymentId, Long releaseId) throws OneOpsClientAPIException {
		return updateDeploymentStatus(environmentName, deploymentId, releaseId, "canceled");
	}

	/**
	 * Pause an active deployment
	 *
	 * @param environmentName
	 * @param deploymentId
	 * @param releaseId
	 * @return @{@link Deployment}
	 * @throws OneOpsClientAPIException
	 */
	public Deployment pauseDeployment(String environmentName, Long deploymentId, Long releaseId) throws OneOpsClientAPIException {
		return updateDeploymentStatus(environmentName, deploymentId, releaseId, "paused");
	}
	
	
	public DeploymentRFC getDeployment(String environmentName, Long deploymentId) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		if(deploymentId == null) {
			String msg = "Missing deployment Id to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri + environmentName + IConstants.DEPLOYMENTS_URI + deploymentId );
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(DeploymentRFC.class);
			} else {
				String msg = String.format("Failed to get deployment details for environment %s for id %s due to %s", environmentName, deploymentId, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to get deployment details for environment %s for id %s due to null response", environmentName, deploymentId);
		throw new OneOpsClientAPIException(msg);
	}
	
	public Log getDeploymentRfcLog(String environmentName, Long deploymentId, Long rfcId) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		if(deploymentId == null) {
			String msg = "Missing deployment Id to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		if(rfcId == null ) {
			String msg = "Missing rfc Id to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.queryParam("rfcId", rfcId).get(transitionEnvUri + environmentName + IConstants.DEPLOYMENTS_URI + deploymentId + "/log_data");
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				List<Log> logs = JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<Log>>(){});
				if(logs != null && logs.size() > 0) {
					return logs.get(0);
				} else {
					String msg = String.format("Failed to get deployment logs for environment %s, deployment id %s and rfcId %s due to %s", environmentName, deploymentId, rfcId, response.getStatusLine());
					throw new OneOpsClientAPIException(msg);
				}
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
	public Deployment approveDeployment(String environmentName, Long deploymentId, Long releaseId) throws OneOpsClientAPIException {
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
	public Deployment retryDeployment(String environmentName, Long deploymentId, Long releaseId) throws OneOpsClientAPIException {
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
	private Deployment updateDeploymentStatus(String environmentName, Long deploymentId, Long releaseId, String newstate) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		if(deploymentId == null ) {
			String msg = "Missing deployment to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		
		Map<String ,String> properties= new HashMap<String ,String>();
		properties.put("deploymentState", newstate);
		properties.put("releaseId", String.valueOf(releaseId));
		ResourceObject ro = new ResourceObject();
		ro.setProperties(properties);
		JSONObject jsonObject = JsonUtil.createJsonObject(ro , "cms_deployment");
		
		Response response = request.body(jsonObject.toString()).put(transitionEnvUri + environmentName + IConstants.DEPLOYMENTS_URI + deploymentId);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(Deployment.class);
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
	public CiResource deleteEnvironment(String environmentName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to delete";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.delete(transitionEnvUri + environmentName);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(CiResource.class);
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
	public List<CiResource> listPlatforms(String environmentName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name list platforms";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri + environmentName + IConstants.PLATFORM_URI);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<CiResource>>(){});
			} else {
				String msg = String.format("Failed to get list of platforms for environemnt %s due to %s", environmentName,response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to get list of platforms for environemnt %s due to null response", environmentName);
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
	public CiResource getPlatform(String environmentName, String platformName) throws OneOpsClientAPIException {
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
				return response.getBody().as(CiResource.class);
			} else {
				String msg = String.format("Failed to get platform %s details for environment %s due to %s", platformName, environmentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to get platform %s details for environment %s due to null response", platformName, environmentName);
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
	public List<CiResource> listPlatformComponents(String environmentName, String platformName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to list environment platform components";
			throw new OneOpsClientAPIException(msg);
		}
		if(platformName == null || platformName.length() == 0) {
			String msg = "Missing platform name to list environment platform components";
			throw new OneOpsClientAPIException(msg);
		}
		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri + environmentName + IConstants.PLATFORM_URI + platformName + IConstants.COMPONENT_URI);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<CiResource>>(){});
			} else {
				String msg = String.format("Failed to list components for platform %s environment %s due to %s", platformName, environmentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to list components for platform %s environment %s due to null response", platformName, environmentName);
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
	public CiResource getPlatformComponent(String environmentName, String platformName, String componentName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to get environment platform component details";
			throw new OneOpsClientAPIException(msg);
		}
		if(platformName == null || platformName.length() == 0) {
			String msg = "Missing platform name to get environment platform component details";
			throw new OneOpsClientAPIException(msg);
		}
		if(componentName == null || componentName.length() == 0) {
			String msg = "Missing component name to get environment platform component details";
			throw new OneOpsClientAPIException(msg);
		}
		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri + environmentName + IConstants.PLATFORM_URI + platformName + IConstants.COMPONENT_URI + componentName);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(CiResource.class);
			} else {
				String msg = String.format("Failed to get environment %s platform %s component %s details due to %s", environmentName, platformName, componentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to get environment %s platform %s component %s details due to null response", environmentName, platformName, componentName);
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
	public CiResource updatePlatformComponent(String environmentName, String platformName, String componentName, Map<String, String> attributes) throws OneOpsClientAPIException {
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
		
		CiResource componentDetails = getPlatformComponent(environmentName, platformName, componentName);
		if(componentDetails != null) {
			ResourceObject ro = new ResourceObject();
			
			Long ciId = componentDetails.getCiId();
			
			Map<String, String> attr = Maps.newHashMap();
			//Add ciAttributes to be updated
			if(attributes != null && attributes.size() > 0)
				attr.putAll(attributes);
			
			Map<String, String> ownerProps = Maps.newHashMap();
			//Add existing attrProps to retain locking of attributes 
			AttrProps attrProps = componentDetails.getAttrProps();
			if(attrProps != null && attrProps.getAdditionalProperties() != null && attrProps.getAdditionalProperties().size() > 0 && attrProps.getAdditionalProperties().get("owner") != null) {
				@SuppressWarnings("unchecked")
				Map<String, String> ownersMap = (Map<String, String>) attrProps.getAdditionalProperties().get("owner");
				for(Entry<String, String> entry : ownersMap.entrySet()) {
					ownerProps.put(entry.getKey(), entry.getValue());
				}
			}
			
			//Add updated attributes to attrProps to lock them
			for(Entry<String, String> entry :  attributes.entrySet()) {
				ownerProps.put(entry.getKey(), "manifest");
			}
			
			ro.setAttributes(attr);
			ro.setOwnerProps(ownerProps);
			
			RequestSpecification request = createRequest();
			JSONObject jsonObject = JsonUtil.createJsonObject(ro , "cms_dj_ci");
 			Response response = request.body(jsonObject.toString()).put(transitionEnvUri + environmentName + IConstants.PLATFORM_URI + platformName + IConstants.COMPONENT_URI + ciId);
			if(response != null) {
				if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
					return response.getBody().as(CiResource.class);
				} else {
					String msg = String.format("Failed to get update component %s for platform %s environment %s due to %s", componentName, platformName, environmentName, response.getStatusLine());
					throw new OneOpsClientAPIException(msg);
				}
			} 
		}
		String msg = String.format("Failed to get update component %s for platform %s environment %s due to null response", componentName, platformName, environmentName);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Fetch attachment details for an environment/platform/component
	 * 
	 * @param environmentName
	 * @param platformName
	 * @param componentName
	 * @param attachmentName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public CiResource getPlatformComponentAttachment(String environmentName, String platformName, String componentName, String attachmentName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to get platform component attachment details";
			throw new OneOpsClientAPIException(msg);
		}
		if(platformName == null || platformName.length() == 0) {
			String msg = "Missing platform name to get platform component attachment details";
			throw new OneOpsClientAPIException(msg);
		}
		if(componentName == null || componentName.length() == 0) {
			String msg = "Missing component name to get platform component attachment details";
			throw new OneOpsClientAPIException(msg);
		}
		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri + environmentName + IConstants.PLATFORM_URI + platformName + IConstants.COMPONENT_URI + componentName + IConstants.ATTACHMENTS_URI + attachmentName);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(CiResource.class);
			} else {
				String msg = String.format("Failed to get attachment %s details for component %s platform %s environment %s due to %s", attachmentName, componentName, platformName, environmentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to get attachment %s details for component %s platform %s environment %s due to null response", attachmentName, componentName, platformName, environmentName);
		throw new OneOpsClientAPIException(msg);
	}
	
	
	/**
	 * Update Attachment
	 * 
	 *  Sample request for new attachment attributes
	 * 	attributes.put("content", "content");
		attributes.put("source", "source");
		attributes.put("path", "/tmp/my.sh");
		attributes.put("exec_cmd", "exec_cmd");
		attributes.put("run_on", "after-add,after-replace,after-update");
		attributes.put("run_on_action", "[\"after-restart\"]");
		
	 * @param environmentName
	 * @param platformName
	 * @param componentName
	 * @param attachmentName
	 * @param attributes
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public CiResource updatePlatformComponentAttachment(String environmentName, String platformName, String componentName, String attachmentName, Map<String, String> attributes) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to update attachment attributes";
			throw new OneOpsClientAPIException(msg);
		}
		if(platformName == null || platformName.length() == 0) {
			String msg = "Missing platform name to update attachment attributes";
			throw new OneOpsClientAPIException(msg);
		}
		if(componentName == null || componentName.length() == 0) {
			String msg = "Missing component name to update attachment attributes";
			throw new OneOpsClientAPIException(msg);
		}
		if(attachmentName == null || attachmentName.length() == 0) {
			String msg = "Missing attachment name to update attachment attributes";
			throw new OneOpsClientAPIException(msg);
		}
		if(attributes == null || attributes.size() == 0) {
			//nothing to be updated
			return null;
		}
		
		CiResource attachmentDetails = getPlatformComponentAttachment(environmentName, platformName, componentName, attachmentName);
		if(attachmentDetails != null) {
			ResourceObject ro = new ResourceObject();
			
			Long ciId = attachmentDetails.getCiId();
			RequestSpecification request = createRequest();
			
			//Add existing ciAttributes 
			CiAttributes ciAttributes = attachmentDetails.getCiAttributes();
			Map<String, String> attr = Maps.newHashMap();
			if(ciAttributes != null && ciAttributes.getAdditionalProperties() != null && ciAttributes.getAdditionalProperties().size() > 0) {
				for(Entry<String, Object> entry : ciAttributes.getAdditionalProperties().entrySet()) {
					attr.put(entry.getKey(), String.valueOf(entry.getValue()));
				}
			}
			
			//Add ciAttributes to be updated
			if(attributes != null && attributes.size() > 0)
				attr.putAll(attributes);
			
			Map<String, String> ownerProps = Maps.newHashMap();
			//Add existing attrProps to retain locking of attributes 
			AttrProps attrProps = attachmentDetails.getAttrProps();
			if(attrProps != null && attrProps.getAdditionalProperties() != null && attrProps.getAdditionalProperties().size() > 0 && attrProps.getAdditionalProperties().get("owner") != null) {
				@SuppressWarnings("unchecked")
				Map<String, String> ownersMap = (Map<String, String>) attrProps.getAdditionalProperties().get("owner");
				for(Entry<String, String> entry : ownersMap.entrySet()) {
					ownerProps.put(entry.getKey(), entry.getValue());
				}
			}
			
			//Add updated attributes to attrProps to lock them
			for(Entry<String, String> entry :  attributes.entrySet()) {
				ownerProps.put(entry.getKey(), "manifest");
			}
			
			ro.setOwnerProps(ownerProps);
			ro.setAttributes(attr);
			JSONObject jsonObject = JsonUtil.createJsonObject(ro , "cms_dj_ci");
 			Response response = request.body(jsonObject.toString()).put(transitionEnvUri + environmentName + IConstants.PLATFORM_URI + platformName 
 					+ IConstants.COMPONENT_URI + componentName + IConstants.ATTACHMENTS_URI + ciId);
			if(response != null) {
				if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
					return response.getBody().as(CiResource.class);
				} else {
					String msg = String.format("Failed to get update attachment %s on component %s platform %s environemnt %s due to %s", attachmentName, componentName, platformName, environmentName, response.getStatusLine());
					throw new OneOpsClientAPIException(msg);
				}
			} 
		}
		String msg = String.format("Failed to get update attachment %s on component %s platform %s environemnt %s due to null response", attachmentName, componentName, platformName, environmentName);
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
	public CiResource touchPlatformComponent(String environmentName, String platformName, String componentName) throws OneOpsClientAPIException {
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
				return response.getBody().as(CiResource.class);
			} else {
				String msg = String.format("Failed to touch component %s for platform %s environment %s due to %s", componentName, platformName, environmentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to touch component %s for platform %s environment %s due to null response", componentName, platformName, environmentName);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Pull latest design commits
	 * 
	 * @param environmentName {mandatory}
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public CiResource pullDesign(String environmentName) throws OneOpsClientAPIException {
		
		RequestSpecification request = createRequest();
		JSONObject jo = new JSONObject();
		
		Response response = request.body(jo.toString()).post(transitionEnvUri + environmentName + "/pull");
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				CiResource env = response.getBody().as(CiResource.class);
				if(env == null || (env.getComments() != null && env.getComments().startsWith("ERROR:"))) {
					String msg = String.format("Failed to pull design for environment %s due to %s", environmentName, env.getComments());
					throw new OneOpsClientAPIException(msg);
				} else 
					return env;
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
	public CiResource pullNewPlatform(String environmentName, Map<String, String> platformAvailability) throws OneOpsClientAPIException {
		
		RequestSpecification request = createRequest();
		JSONObject jo = new JSONObject();
		
		if(platformAvailability == null || platformAvailability.size() == 0) {
			Design design = new Design(instance, assemblyName);
			List<CiResource> platforms = design.listPlatforms();
			
			CiResource env = getEnvironment(environmentName);
			String availability = "single";
			if(env != null && env.getCiAttributes() != null) {
				CiAttributes attr = env.getCiAttributes();//.availability");
				if(attr.getAdditionalProperties() != null && attr.getAdditionalProperties().containsKey("availability")) {
					availability = String.valueOf(attr.getAdditionalProperties().get("availability"));
				}
			}
			if(platforms != null) {
				platformAvailability = new HashMap<String, String>();
				for (CiResource platform : platforms) {
					platformAvailability.put(platform.getCiId() + "", availability);
				}
			}
		}
		jo.put("platform_availability", platformAvailability);
		
		Response response = request.body(jo.toString()).post(transitionEnvUri + environmentName + "/pull");
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				CiResource env = response.getBody().as(CiResource.class);
				if(env == null || (env.getComments() != null && env.getComments().startsWith("ERROR:"))) {
					String msg = String.format("Failed to pull design for environment %s due to %s", environmentName, env.getComments());
					throw new OneOpsClientAPIException(msg);
				} else 
					return env;
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
	public List<CiResource> listPlatformVariables(String environmentName, String platformName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to list environment platform variables";
			throw new OneOpsClientAPIException(msg);
		}
		if(platformName == null || platformName.length() == 0) {
			String msg = "Missing platform name to list environment platform variables";
			throw new OneOpsClientAPIException(msg);
		}
		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri + environmentName + IConstants.PLATFORM_URI + platformName + IConstants.VARIABLES_URI);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<CiResource>>(){});
			} else {
				String msg = String.format("Failed to list local variables for platform %s environment %s due to %s", platformName, environmentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to list local variables for platform %s environment %s due to null response", platformName, environmentName);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Update platform local variables for a given assembly/environment/platform
	 * 
	 * @param environmentName
	 * @param platformName
	 * @param variableName
	 * @param variableValue
	 * @param isSecure
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	 
	public Boolean updatePlatformVariable(String environmentName, String platformName, String variableName, String variableValue, boolean isSecure) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to update variables";
			throw new OneOpsClientAPIException(msg);
		}
		if(platformName == null || platformName.length() == 0) {
			String msg = "Missing platform name to update variables";
			throw new OneOpsClientAPIException(msg);
		}
		if(variableName == null ) {
			String msg = "Missing variable name to be added";
			throw new OneOpsClientAPIException(msg);
		}
		if(variableValue == null ) {
			String msg = "Missing variable value to be added";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		boolean success = false;
			ResourceObject ro = new ResourceObject();
			Map<String ,String> attributes = new HashMap<String ,String>();
			Map<String, String> ownerProps = Maps.newHashMap();
			
			String uri = transitionEnvUri + environmentName + IConstants.PLATFORM_URI + platformName + IConstants.VARIABLES_URI + variableName;
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
						attributes.put("encrypted_value", variableValue);
						ownerProps.put("secure", "manifest");
						ownerProps.put("encrypted_value", "manifest");
					} else {
						attributes.put("secure", "false");
						attributes.put("value", variableValue);
						ownerProps.put("value", "manifest");
					}
					
					ro.setOwnerProps(ownerProps);
					ro.setAttributes(attributes);
					
					JSONObject jsonObject = JsonUtil.createJsonObject(ro , "cms_dj_ci");
					if(response != null ) {
						response = request.body(jsonObject.toString()).put(uri);
						if(response != null) {
							if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
								success = true;
							} else {
								String msg = String.format("Failed to update local variable %s with value %s for platform %s environment %s due to %s", variableName, variableValue, platformName, environmentName, response.getStatusLine());
								throw new OneOpsClientAPIException(msg);
							}
						} 
					}
				} else {
					success = true;
				}
			} else {
				String msg = String.format("Failed to update local variable %s with value %s for platform %s environment %s due to null response", variableName, variableValue, platformName, environmentName);
				throw new OneOpsClientAPIException(msg);
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
	public List<CiResource> listGlobalVariables(String environmentName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to list environment variables";
			throw new OneOpsClientAPIException(msg);
		}
		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri + environmentName + IConstants.VARIABLES_URI);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<CiResource>>(){});
			} else {
				String msg = String.format("Failed to list global variables of environment %s due to %s", environmentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to list global variables of environment %s due to null response", environmentName);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Update global variables for a given assembly/environment
	 * 
	 * @param environmentName
	 * @param variableName
	 * @param variableValue
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public Boolean updateGlobalVariable(String environmentName, String variableName, String variableValue, boolean isSecure) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to update global variable";
			throw new OneOpsClientAPIException(msg);
		}
		if(variableName == null ) {
			String msg = "Missing variable name to be updated";
			throw new OneOpsClientAPIException(msg);
		}
		if(variableValue == null ) {
			String msg = "Missing variable value to be updated";
			throw new OneOpsClientAPIException(msg);
		}
		boolean success = false;
		RequestSpecification request = createRequest();
			ResourceObject ro = new ResourceObject();
			Map<String ,String> attributes = new HashMap<String ,String>();
			Map<String, String> ownerProps = Maps.newHashMap();
			
			String uri = transitionEnvUri + environmentName + IConstants.VARIABLES_URI + variableName;
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
						attributes.put("encrypted_value", variableValue);
						ownerProps.put("secure", "manifest");
						ownerProps.put("encrypted_value", "manifest");
					} else {
						attributes.put("secure", "false");
						attributes.put("value", variableValue);
						ownerProps.put("value", "manifest");
					}
						
					ro.setOwnerProps(ownerProps);
					ro.setAttributes(attributes);
					
					JSONObject jsonObject = JsonUtil.createJsonObject(ro , "cms_dj_ci");
					response = request.body(jsonObject.toString()).put(uri);
					if(response != null) {
						if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
							success = true;
						} else {
							String msg = String.format("Failed to update global variable %s with value %s of environment %s due to %s", variableName, variableValue, environmentName, response.getStatusLine());
							throw new OneOpsClientAPIException(msg);
						}
					} 
				} else {
					success = true;
				}
			} else {
				String msg = String.format("Failed to update global variable %s with value %s of environment %s due to null response", variableName, variableValue, environmentName);
				throw new OneOpsClientAPIException(msg);
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
	public CiResource updateDisableEnvironment(String environmentName, List<String> platformIdList) throws OneOpsClientAPIException {
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
				return response.getBody().as(CiResource.class);
			} else {
				String msg = String.format("Failed to disable platforms for environment with name %s due to %s", environmentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to disable platforms for environment with name %s due to null response", environmentName);
		throw new OneOpsClientAPIException(msg);
	}

    /**
     * Get redundancy configuration for a given platform
     *
     * @param environmentName
     * @param platformName
     * @return
     * @throws OneOpsClientAPIException
     */
	public CiResource getPlatformRedundancyConfig(String environmentName, String platformName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to get redundancy configuration";
			throw new OneOpsClientAPIException(msg);
		}

		if(platformName == null || platformName.length() == 0) {
			String msg = "Missing platform name to get redundancy configuration";
			throw new OneOpsClientAPIException(msg);
		}

		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri + environmentName + IConstants.PLATFORM_URI + platformName + "/edit");
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(CiResource.class);
			} else {
				String msg = String.format("Failed to get redundancy configuration for platform %s due to %s", platformName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		}

		String msg = String.format("Failed to get redundancy configuration for platform %s due to null response", platformName);
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
	public Boolean updatePlatformRedundancyConfig(String environmentName, String platformName, String componentName, RedundancyConfig config) throws OneOpsClientAPIException {
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
		owner.put("owner", redundant);
		
		rconfig.put("relationAttrProps", owner);
		if(componentName == null || componentName.isEmpty()) {
			componentName = "compute";
		} 
		CiResource componentDetails = getPlatformComponent(environmentName, platformName, componentName);
		JSONObject jo = new JSONObject();
		jo.put(String.valueOf(componentDetails.getCiId()), rconfig);
		
		JSONObject dependsOn = new JSONObject();
		dependsOn.put("depends_on", jo);
		
		Response response = request.body(dependsOn.toString()).put(transitionEnvUri + environmentName + IConstants.PLATFORM_URI + platformName);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return true;
			} else {
				String msg = String.format("Failed to update platforms %s redundancy for environment %s due to %s", platformName, environmentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to update platforms %s redundancy for environment %s due to null response", platformName, environmentName);
		throw new OneOpsClientAPIException(msg);
	}
	
	public CiResource updatePlatformCloudScale(String environmentName, String platformName, String cloudId, Map<String, String> cloudMap) throws OneOpsClientAPIException {
		if (environmentName == null || environmentName.length() == 0) {
			String msg = String.format("Missing environment name to be updated");
			throw new OneOpsClientAPIException(msg);
		}

		if (platformName == null || platformName.length() == 0) {
			String msg = String.format("Missing platform name to be updated");
			throw new OneOpsClientAPIException(msg);
		}

		if (cloudId == null || cloudId.length() == 0) {
			String msg = String.format("Missing cloud ID to be updated");
			throw new OneOpsClientAPIException(msg);
		}

		if (cloudMap == null || cloudMap.size() == 0) {
			String msg = String.format("Missing cloud info to be updated");
			throw new OneOpsClientAPIException(msg);
		}

		RequestSpecification request = createRequest();
		JSONObject jo = new JSONObject();
		jo.put("cloud_id", cloudId);
		jo.put("attributes", cloudMap);
		Response response = request.body(jo.toString())
				.put(transitionEnvUri + environmentName + IConstants.PLATFORM_URI + platformName + "/cloud_configuration");
		if (response != null) {
			if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(CiResource.class);
			} else {
				String msg = String.format("Failed to update platforms %s cloud scale with cloud id %s for environment %s due to %s", platformName, cloudId, environmentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		}
		String msg = String.format("Failed to update platforms %s cloud scale with cloud id for %s environment %s due to null response", platformName, cloudId, environmentName);
		throw new OneOpsClientAPIException(msg);
	}
	
	
	
	/**
	 * List relays
	 * 
	 * @param environmentName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public List<CiResource> listRelays(String environmentName) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to get relays";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.get(transitionEnvUri + environmentName + "/relays/");
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<CiResource>>(){});
			} else {
				String msg = String.format("Failed to list relay for environment %s due to %s", environmentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to list relay for environment %s due to null response", environmentName);
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
	 
	public CiResource getRelay(String environmentName, String relayName) throws OneOpsClientAPIException {
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
				return response.getBody().as(CiResource.class);
			} else {
				String msg = String.format("Failed to get relay %s for environment %s due to %s", relayName, environmentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to get relay %s for environment %s due to null response", relayName, environmentName);
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
	public CiResource addRelay(String environmentName, String relayName, String severity, String emails, String source, String nsPaths, String regex, boolean correlation) throws OneOpsClientAPIException {
		if(environmentName == null || environmentName.length() == 0) {
			String msg = "Missing environment name to create relay";
			throw new OneOpsClientAPIException(msg);
		}
		
		ResourceObject ro = new ResourceObject();
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
		String path = "/" + instance.getOrgname() + "/" + assemblyName + "/" + environmentName;
		properties.put("nsPath", path );
		
		RequestSpecification request = createRequest();
		ro.setProperties(properties);
		
		Response newRelayResponse = request.get(transitionEnvUri + environmentName + "/relays/new");
		if(newRelayResponse != null) {
			JsonPath attachmentDetails = newRelayResponse.getBody().jsonPath();
			Map<String, String> attributes = attachmentDetails.getMap("ciAttributes");
			if(attributes == null) {
				attributes = Maps.newHashMap();
			}
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
		}
		
		JSONObject jsonObject = JsonUtil.createJsonObject(ro , "cms_ci");
		Response response = request.body(jsonObject.toString()).post(transitionEnvUri + environmentName + "/relays");
		
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(CiResource.class);
			} else {
				String msg = String.format("Failed to create relay %s for environment %s due to %s", relayName, environmentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to create relay with name %s for environment %s due to null response", relayName, environmentName);
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
	public CiResource updateRelay(String environmentName, String relayName, String severity, String emails, String source, String nsPaths, String regex, boolean correlation, boolean enable) throws OneOpsClientAPIException {
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
				return response.getBody().as(CiResource.class);
			} else {
				String msg = String.format("Failed to update relay %s for environment %s due to %s", relayName, environmentName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to update relay %s for environment %s due to null response", relayName, environmentName);
		throw new OneOpsClientAPIException(msg);
	}

	private CiResource doDeploy(String environmentName, String transitionEnvUri,
								RequestSpecification request, JSONObject jsonObject) throws OneOpsClientAPIException {

		Response response = request.body(jsonObject.toString()).post(transitionEnvUri + environmentName + "/deployments");
		if(response == null) {
			String msg = String.format("Failed to start deployment for environment %s " +
					"due to null response" ,environmentName);
			throw new OneOpsClientAPIException(msg);
		} else if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
			return response.as(CiResource.class);
		} else {
			String msg = String.format("Failed to start deployment for environment %s. %s" ,
					environmentName, getErrorMessageFromResponse(response));
			throw new OneOpsClientAPIException(msg);
		}
	}

	private String getErrorMessageFromResponse(Response response) {
		String errorMessage = "Error Status Code: " + response.getStatusCode() + ". Error: ";
		if (response.getBody() != null) {
			errorMessage = errorMessage + response.getBody().asString();
		} else {
			errorMessage = errorMessage + "n/a";
		}
		return errorMessage;
	}

	private void appendListToQueryString(List<Long> list, StringBuilder queryString, String queryParameter) {
		if(list != null && list.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < list.size(); i++) {
				sb.append(list.get(i));

				if (i < list.size() - 1)
					sb.append(",");
			}
			if (queryString.length() > 0)
				queryString.append("&");
			queryString.append(queryParameter).append("=").append(sb.toString());
		}
	}

	private void addStringToPropertyMap(List<Long> list, Map<String, String> properties, String parameter) {
		if (list != null && list.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < list.size(); i++) {
				sb.append(list.get(i));

				if (i < list.size() - 1)
					sb.append(",");
			}
			properties.put(parameter, sb.toString());
		}
	}
}
