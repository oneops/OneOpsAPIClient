package com.oneops.api.resource;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.oneops.api.APIClient;
import com.oneops.api.OOInstance;
import com.oneops.api.exception.OneOpsClientAPIException;
import com.oneops.api.resource.model.CiResource;
import com.oneops.api.util.IConstants;
import com.oneops.api.util.JsonUtil;


public class Cloud extends APIClient {

	public Cloud(OOInstance instance) throws OneOpsClientAPIException {
		super(instance);
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
}
