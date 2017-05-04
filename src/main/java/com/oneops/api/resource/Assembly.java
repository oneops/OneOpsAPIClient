package com.oneops.api.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class Assembly extends APIClient {

	public Assembly(OOInstance instance) throws OneOpsClientAPIException {
		super(instance);
	}
	
	/**
	 * Fetches specific assembly details
	 * 
	 * @param assemblyName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public CiResource getAssembly(String assemblyName) throws OneOpsClientAPIException {
		if(assemblyName == null || assemblyName.length() == 0) {
			String msg = "Missing assembly name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.get(IConstants.ASSEMBLY_URI + assemblyName);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(CiResource.class);
			} else {
				String msg = String.format("Failed to get assembly with name %s due to %s", assemblyName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to get assembly with name %s due to null response", assemblyName);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Lists all the assemblies
	 * 
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public List<CiResource> listAssemblies() throws OneOpsClientAPIException {
		RequestSpecification request = createRequest();
		Response response = request.get(IConstants.ASSEMBLY_URI);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<CiResource>>(){});
			} else {
				String msg = String.format("Failed to get list of assemblies due to %s", response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = "Failed to get list of assemblies due to null response";
		throw new OneOpsClientAPIException(msg);
	}
	
	
	/**
	 * Creates assembly for the given @assemblyName
	 * 
	 * @param assemblyName {mandatory} 
	 * @param ownerEmail a valid email address is {mandatory}
	 * @param comments
	 * @param description
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public CiResource createAssembly(String assemblyName, String ownerEmail, String comments, String description) throws OneOpsClientAPIException {
		return createAssembly(assemblyName, ownerEmail, comments, description, null);
	}
	
	/**
	 * Creates assembly for the given @assemblyName
	 * 
	 * @param assemblyName {mandatory} 
	 * @param ownerEmail a valid email address is {mandatory}
	 * @param comments
	 * @param description
	 * @param tags
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public CiResource createAssembly(String assemblyName, String ownerEmail, String comments, String description, Map<String, String> tags) throws OneOpsClientAPIException {
		ResourceObject ro = new ResourceObject();
		Map<String ,String> attributes = new HashMap<String ,String>();
		Map<String ,String> properties= new HashMap<String ,String>();
		
		if(assemblyName != null && assemblyName.length() > 0) {
			properties.put("ciName", assemblyName);
		} else {
			String msg = "Missing assembly name to create one";
			throw new OneOpsClientAPIException(msg);
		}
		
		properties.put("comments", comments);
		ro.setProperties(properties);
		
		if(ownerEmail != null && ownerEmail.length() > 0) {
			attributes.put("owner", ownerEmail);
		} else {
			String msg = "Missing assembly owner email address";
			throw new OneOpsClientAPIException(msg);
		}
		
		attributes.put("description", description);
		
		if( tags != null && !tags.isEmpty() ){
			attributes.put("tags", JSONObject.valueToString(tags));
		}
		
		ro.setAttributes(attributes);
		
		RequestSpecification request = createRequest();
		JSONObject jsonObject = JsonUtil.createJsonObject(ro , "cms_ci");

		Response response = request.body(jsonObject.toString()).post(IConstants.ASSEMBLY_URI);
		
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(CiResource.class);
			} else {
				String msg = String.format("Failed to create assembly with name %s due to %s", assemblyName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to create assembly with name %s due to null response", assemblyName);
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Deletes the given assembly
	 * 
	 * @param assemblyName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public CiResource deleteAssembly(String assemblyName) throws OneOpsClientAPIException {
		if(assemblyName == null || assemblyName.length() == 0) {
			String msg = "Missing assembly name to delete one";
			throw new OneOpsClientAPIException(msg);
		}
		
		RequestSpecification request = createRequest();
		Response response = request.delete(IConstants.ASSEMBLY_URI + assemblyName);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(CiResource.class);
			} else {
				String msg = String.format("Failed to delete assembly with name %s due to %s", assemblyName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to delete assembly with name %s due to null response", assemblyName);
		throw new OneOpsClientAPIException(msg);
	}
}
