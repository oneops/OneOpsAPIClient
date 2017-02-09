package com.oneops.api.resource;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.oneops.api.APIClient;
import com.oneops.api.OOInstance;
import com.oneops.api.exception.OneOpsClientAPIException;
import com.oneops.api.util.IConstants;

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
	public JsonPath getCloud(String cloudName) throws OneOpsClientAPIException {
		if(cloudName == null || cloudName.length() == 0) {
			String msg = "Missing cloud name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.get(IConstants.CLOUDS_URI + cloudName);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
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
	public JsonPath listClouds() throws OneOpsClientAPIException {
		RequestSpecification request = createRequest();
		Response response = request.get(IConstants.CLOUDS_URI);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to get list of clouds due to %s", response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = "Failed to get list of clouds due to null response";
		throw new OneOpsClientAPIException(msg);
	}
}
