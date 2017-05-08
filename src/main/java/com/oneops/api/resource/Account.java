package com.oneops.api.resource;

import java.util.List;

import org.json.JSONObject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.oneops.api.APIClient;
import com.oneops.api.OOInstance;
import com.oneops.api.exception.OneOpsClientAPIException;
import com.oneops.api.resource.model.Organization;
import com.oneops.api.util.IConstants;
import com.oneops.api.util.JsonUtil;

public class Account extends APIClient {

	public Account(OOInstance instance) throws OneOpsClientAPIException {
		super(instance);
	}
	
	/**
	 * Lists all the organizations
	 * 
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public List<Organization> listOrganizations() throws OneOpsClientAPIException {
		RequestSpecification request = createRequest();
		Response response = request.get(IConstants.ACCOUNT_URI);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<Organization>>(){});
			} else {
				String msg = String.format("Failed to get list of organizations due to %s", response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = "Failed to get list of organizations due to null response";
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Fetches specific organization details
	 * 
	 * @param organizationName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public Organization getOrganization(String organizationName) throws OneOpsClientAPIException {
		if(organizationName == null || organizationName.length() == 0) {
			String msg = "Missing organization name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.get(IConstants.ACCOUNT_URI + organizationName);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(Organization.class);
			} else {
				String msg = String.format("Failed to get organization with name %s due to %s", organizationName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to get organization with name %s due to null response", organizationName);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Creates organization for the given @organizationName
	 * 
	 * @param organizationName {mandatory} 
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public Organization createOrganization(String organizationName) throws OneOpsClientAPIException {
		if(organizationName == null || organizationName.length() == 0) {
			String msg = "Missing organization name to create one";
			throw new OneOpsClientAPIException(msg);
		}
		RequestSpecification request = createRequest();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("name", organizationName);
		Response response = request.body(jsonObject.toString()).post(IConstants.ACCOUNT_URI);
		
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(Organization.class);
			} else {
				String msg = String.format("Failed to create organization with name %s due to %s", organizationName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to create organization with name %s due to null response", organizationName);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Deletes the given organization
	 * 
	 * @param organizationName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public Organization deleteOrganization(String organizationName) throws OneOpsClientAPIException {
		if(organizationName == null || organizationName.length() == 0) {
			String msg = "Missing organization name to delete one";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Organization org = getOrganization(organizationName);
		Long id =  org.getId();
		if(id == null) {
			String msg = String.format("Failed to find organization with name %s to delete", organizationName);
			throw new OneOpsClientAPIException(msg);
		} 
		
		Response response = request.delete(IConstants.ACCOUNT_URI + id);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(Organization.class);
			} else {
				String msg = String.format("Failed to delete organization with name %s due to %s", organizationName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to delete organization with name %s due to null response", organizationName);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Lists all the Environment Profiles
	 * 
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public JsonPath listEnvironmentProfiles() throws OneOpsClientAPIException {
		RequestSpecification request = createRequest();
		Response response = request.get(IConstants.ORGANIZATION_URI + "environments");
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().jsonPath();
			} else {
				String msg = String.format("Failed to get list of Environment Profiles due to %s", response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = "Failed to get list of Environment Profiles due to null response";
		throw new OneOpsClientAPIException(msg);
	}

}
