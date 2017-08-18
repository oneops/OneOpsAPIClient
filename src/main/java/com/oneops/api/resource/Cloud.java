package com.oneops.api.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.oneops.api.APIClient;
import com.oneops.api.OOInstance;
import com.oneops.api.ResourceObject;
import com.oneops.api.exception.OneOpsClientAPIException;
import com.oneops.api.resource.model.CiResource;
import com.oneops.api.util.IConstants;
import com.oneops.api.util.JsonUtil;

public class Cloud extends APIClient {

	OOInstance instance;
	public Cloud(OOInstance instance) throws OneOpsClientAPIException {
		super(instance);
		this.instance = instance;
	}
	
	/**
	 * Fetches specific cloud details
	 * 
	 * @param cloudName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public CiResource getCloud(String cloudName) throws OneOpsClientAPIException {
		if(cloudName == null || cloudName.length() == 0) {
			String msg = "Missing cloud name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.get(IConstants.CLOUDS_URI + cloudName);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(CiResource.class);
			} else {
				String msg = String.format("Failed to get cloud with name %s due to %s", cloudName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to get cloud with name %s due to null response", cloudName);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Lists all the clouds
	 * 
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public List<CiResource> listClouds() throws OneOpsClientAPIException {
		RequestSpecification request = createRequest();
		Response response = request.get(IConstants.CLOUDS_URI);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<CiResource>>(){});
			} else {
				String msg = String.format("Failed to get list of clouds due to %s", response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = "Failed to get list of clouds due to null response";
		throw new OneOpsClientAPIException(msg);
	}
	
	
	/**
	 * Lists all the clouds
	 * 
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public List<CiResource> listCloudServices(String cloudName) throws OneOpsClientAPIException {
		RequestSpecification request = createRequest();
		Response response = request.get(IConstants.CLOUDS_URI + cloudName + "/services");
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<CiResource>>(){});
			} else {
				String msg = String.format("Failed to get list of cloud services due to %s", response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = "Failed to get list of cloud services due to null response";
		throw new OneOpsClientAPIException(msg);
	}
	

	/**
	 * Fetches specific cloud service details
	 * 
	 * @param cloudName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public CiResource getCloudService(String cloudName, String serviceName) throws OneOpsClientAPIException {
		if(cloudName == null || cloudName.length() == 0) {
			String msg = "Missing cloud name to fetch service details";
			throw new OneOpsClientAPIException(msg);
		}
		if(serviceName == null || serviceName.length() == 0) {
			String msg = "Missing cloud name to fetch service details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.get(IConstants.CLOUDS_URI + cloudName + IConstants.SERVICE_URI + serviceName);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(CiResource.class);
			} else {
				String msg = String.format("Failed to get cloud service with name %s for cloud %s due to %s", serviceName, cloudName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to get cloud service with name %s for cloud %s due to null response", serviceName, cloudName);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 *  Update specific cloud service attributes
	 *  
	 * @param cloudName
	 * @param serviceName
	 * @param attr
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	
	public CiResource updateCloudService(String cloudName, String serviceName, Map<String, String> attr) throws OneOpsClientAPIException {
		if(cloudName == null || cloudName.length() == 0) {
			String msg = "Missing cloud name to update service attributes";
			throw new OneOpsClientAPIException(msg);
		}
		if(serviceName == null || serviceName.length() == 0) {
			String msg = "Missing cloud name to update service attributes";
			throw new OneOpsClientAPIException(msg);
		}
		ResourceObject ro = new ResourceObject();
		ro.setAttributes(attr);
		JSONObject jsonObject = JsonUtil.createJsonObject(ro , "cms_ci");
		
		RequestSpecification request = createRequest();
		Response response = request.body(jsonObject.toString()).put(IConstants.CLOUDS_URI + cloudName + IConstants.SERVICE_URI + serviceName);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(CiResource.class);
			} else {
				String msg = String.format("Failed to update service attributes with name %s for cloud %s due to %s", serviceName, cloudName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to update service attributes with name %s for cloud %s due to null response", serviceName, cloudName);
		throw new OneOpsClientAPIException(msg);
	}
	
	
	/**
	 * Add specific service to the cloud
	 * 
	 * @param cloudName
	 * @param serviceName
	 * @param attr
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	 
	public CiResource addCloudService(String cloudName, String serviceType, String serviceName, Map<String, String> attr) throws OneOpsClientAPIException {
		if(cloudName == null || cloudName.length() == 0) {
			String msg = "Missing cloud name to add service";
			throw new OneOpsClientAPIException(msg);
		}
		if(serviceType == null || serviceType.length() == 0) {
			String msg = "Missing service type to add service";
			throw new OneOpsClientAPIException(msg);
		}
		if(serviceName == null || serviceName.length() == 0) {
			String msg = "Missing cloud name to add service";
			throw new OneOpsClientAPIException(msg);
		}
		Map<String, List<CiResource>> availableServices = listAvailableCloudServices(cloudName);
		ResourceObject ro = new ResourceObject();
		Map<String ,String> attributes = new HashMap<String, String>();
		Map<String ,String> properties= new HashMap<String, String>();
		String mgmtId = null;
				
		if(availableServices != null && availableServices.size() > 0) {
			for (Entry<String, List<CiResource>> entry : availableServices.entrySet()) {
				if(serviceType.equals(entry.getKey())) {
					for(CiResource aService : entry.getValue()) {
						if(serviceName.equals(aService.getCiName())) {
							mgmtId = String.valueOf(aService.getCiId());
							CiResource newServiceObj = getNewServiceObj(cloudName, serviceName, mgmtId);
							
							properties.put("ciName", newServiceObj.getCiName());
							properties.put("ciClassName", newServiceObj.getCiClassName());
							ro.setProperties(properties);
							
							for(Entry<String, Object> entry1 : newServiceObj.getCiAttributes().getAdditionalProperties().entrySet()) {
								attributes.put(entry1.getKey(), String.valueOf(entry1.getValue()));
							}
							if(attr != null) {
								for(Entry<String, String> entry1 : attr.entrySet()) {
									attributes.put(entry1.getKey(), entry1.getValue());
								}
							}
							break;
						}
					}
				}
				if(attributes.size() > 0) {
					ro.setAttributes(attributes);
					RequestSpecification request = createRequest();
					JSONObject jsonObject = JsonUtil.createJsonObject(ro , "cms_ci");
					jsonObject.put("mgmtCiId", mgmtId);
					Response response = request.body(jsonObject.toString()).post(IConstants.CLOUDS_URI + cloudName + IConstants.SERVICE_URI);
					if(response != null) {
						if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
							return response.getBody().as(CiResource.class);
						} else {
							String msg = String.format("Failed to add service %s to cloud %s due to %s", serviceName, cloudName, response.getStatusLine());
							throw new OneOpsClientAPIException(msg);
						}
					}
					break;
				}
			}
		}
		String msg = String.format("Failed to update service %s to cloud %s due to null response", serviceName, cloudName);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Fetches cloud service differences w.r.t template
	 * 
	 * @param cloudName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public List<CiResource> listCloudDifferences(String cloudName) throws OneOpsClientAPIException {
		if(cloudName == null || cloudName.length() == 0) {
			String msg = "Missing cloud name to fetch service details";
			throw new OneOpsClientAPIException(msg);
		}
		RequestSpecification request = createRequest();
		Response response = request.get(IConstants.CLOUDS_URI + cloudName + IConstants.SERVICE_URI + "diff");
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<CiResource>>(){});
			} else {
				String msg = String.format("Failed to get cloud %s diff due to %s", cloudName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to get cloud %s diff due to null response", cloudName);
		throw new OneOpsClientAPIException(msg);
	}
	
	
	/**
	 * Fetches cloud service differences w.r.t template
	 * 
	 * @param cloudName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public Map<String, List<CiResource>> listAvailableCloudServices(String cloudName) throws OneOpsClientAPIException {
		if(cloudName == null || cloudName.length() == 0) {
			String msg = "Missing cloud name to fetch service details";
			throw new OneOpsClientAPIException(msg);
		}
		RequestSpecification request = createRequest();
		Response response = request.get(IConstants.CLOUDS_URI + cloudName + IConstants.SERVICE_URI + "available");
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return JsonUtil.toObject(response.getBody().asString(), new TypeReference<Map<String, List<CiResource>>>(){});
			} else {
				String msg = String.format("Failed to get cloud %s available servcies due to %s", cloudName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to get cloud %s available servcies due to null response", cloudName);
		throw new OneOpsClientAPIException(msg);
	}
	
	private CiResource getNewServiceObj(String cloudName, String serviceName, String mgmtId) throws OneOpsClientAPIException {
		if(cloudName == null || cloudName.length() == 0) {
			String msg = "Missing cloud name to create new service";
			throw new OneOpsClientAPIException(msg);
		}
		if(serviceName == null || serviceName.length() == 0) {
			String msg = "Missing service name to create new service";
			throw new OneOpsClientAPIException(msg);
		}
		if(mgmtId == null || mgmtId.length() == 0) {
			String msg = "Missing mgmtId to create new service";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.get(IConstants.CLOUDS_URI + cloudName + IConstants.SERVICE_URI + "new?mgmtCiId=" + mgmtId);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(CiResource.class);
			} else {
				String msg = String.format("Failed to get new cloud service for org %s cloud %s due to %s", instance.getOrgname(), cloudName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		}
		
		String msg = "Failed to get new service object for cloud service : "+ serviceName + ". ";
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Get compute service for the given cloud
	 * 
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public CiResource getCloudComputeServices(String cloudName) {
		
		try {
			List<CiResource> services = listCloudServices(cloudName);
			for (CiResource service : services) {
				if("cloud.service.Openstack".equals(service.getCiClassName()) 
						|| "cloud.service.oneops.1.Azure".equals(service.getCiClassName())) {
					return service;
				}
			}
		} catch (OneOpsClientAPIException e) {
			System.out.println(instance.getOrgname() + " failure to getCloudComputeServices for cloud " + cloudName);
		}
		return null;
	}

	/**
	 * Lists cloud variables given the cloudName
	 *
	 * @param cloudName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public List<CiResource> listCloudVariables(String cloudName) throws OneOpsClientAPIException {
		if(cloudName == null || cloudName.length() == 0) {
			String msg = "Missing cloud name to list cloud variables";
			throw new OneOpsClientAPIException(msg);
		}

		RequestSpecification request = createRequest();
		Response response = request.get(IConstants.CLOUDS_URI + cloudName + IConstants.VARIABLES_URI );
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<CiResource>>(){});
			} else {
				String msg = String.format("Failed to get list of cloud variables due to %s", response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		}
		String msg = "Failed to get list of cloud variables due to null response";
		throw new OneOpsClientAPIException(msg);
	}
	
	private CiResource getNewOfferingObj(String cloudName, String serviceName, String nsId) throws OneOpsClientAPIException {
		if(cloudName == null || cloudName.length() == 0) {
			String msg = "Missing cloud name to create new offerings";
			throw new OneOpsClientAPIException(msg);
		}
		if(serviceName == null || serviceName.length() == 0) {
			String msg = "Missing service name to create new offerings";
			throw new OneOpsClientAPIException(msg);
		}
		if(nsId == null || nsId.length() == 0) {
			String msg = "Missing nsId to create new offerings";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		
		Response response = request.get(IConstants.CLOUDS_URI + cloudName + "/services/" + serviceName + "/offerings/" + "new?mgmtOfferingCiId=" + nsId);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(CiResource.class);
			} else {
				String msg = String.format("Failed to get new cloud offering for org %s cloud %s due to %s", instance.getOrgname(), cloudName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		}
		
		String msg = "Failed to get new offering object for cloud service : "+ serviceName + ". ";
		throw new OneOpsClientAPIException(msg);
	}
	
	private List<CiResource> listAvailableOfferings(String cloudName, String serviceName) throws OneOpsClientAPIException {
		if(cloudName == null || cloudName.length() == 0) {
			String msg = "Missing cloud name to list cloud offerings";
			throw new OneOpsClientAPIException(msg);
		}
		if(serviceName == null || serviceName.length() == 0) {
			String msg = "Missing service name to list cloud offerings";
			throw new OneOpsClientAPIException(msg);
		}

		RequestSpecification request = createRequest();
		Response response = request.get(IConstants.CLOUDS_URI + cloudName + "/services/" + serviceName + "/offerings/available");
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<CiResource>>(){});
			} else {
				String msg = String.format("Failed to get list of cloud offerings for cloud %s due to %s", cloudName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		}
		String msg = "Failed to get list of cloud offerings due to null response";
		throw new OneOpsClientAPIException(msg);
	}
	
	public void addMissingOffering(String cloudName, String serviceName) { 
		try {
		List<CiResource> availableOfferings = listAvailableOfferings(cloudName, serviceName);
		
		if(availableOfferings != null && availableOfferings.size() > 0) {
			for (CiResource aOffering : availableOfferings) {
				CiResource cloudOffering = getCloudOffering(cloudName, serviceName, aOffering.getCiName());
				if(cloudOffering == null) { //missing offering, add it now
					String nsId = String.valueOf(aOffering.getCiId());
					CiResource newOfferingObj = getNewOfferingObj(cloudName, serviceName, nsId);

					ResourceObject ro = new ResourceObject();
					Map<String ,String> attributes = new HashMap<String ,String>();
					Map<String ,String> properties= new HashMap<String ,String>();
					
					properties.put("ciName", newOfferingObj.getCiName());
					properties.put("comments", newOfferingObj.getComments());
					ro.setProperties(properties);
					
					for(Entry<String, Object> entry : newOfferingObj.getCiAttributes().getAdditionalProperties().entrySet()) {
						attributes.put(entry.getKey(), String.valueOf(entry.getValue()));
					}
					ro.setAttributes(attributes);
					
					RequestSpecification request = createRequest();
					JSONObject jsonObject = JsonUtil.createJsonObject(ro , "cms_ci");

					Response response = request.body(jsonObject.toString()).post(IConstants.CLOUDS_URI + cloudName + "/services/" + serviceName + "/offerings/");
					
					if(response != null) {
						if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
							System.out.println(instance.getOrgname() + " , " + cloudName + " , " + newOfferingObj.getCiName());
						}
					}
				}
			}
		}
		} catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	/**
	 * Fetches specific cloud offerings details
	 * 
	 * @param cloudName
	 * @return
	 */
	private CiResource getCloudOffering(String cloudName, String serviceName, String offeringName) {
		RequestSpecification request = createRequest();
		Response response = request.get(IConstants.CLOUDS_URI + cloudName + "/services/" + serviceName + "/offerings/" + offeringName);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(CiResource.class);
			} else {
				return null;
			}
		} 
		return null;
	}
	
}
