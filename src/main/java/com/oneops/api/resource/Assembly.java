package com.oneops.api.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.oneops.api.APIClient;
import com.oneops.api.OOInstance;
import com.oneops.api.ResourceObject;
import com.oneops.api.exception.OneOpsClientAPIException;
import com.oneops.api.resource.model.CiResource;
import com.oneops.api.resource.model.Team;
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
	 * List teams for the given assembly
	 *
	 * @param assemblyName
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public List<Team> listAssemblyTeams(String assemblyName) throws OneOpsClientAPIException {
		if(assemblyName == null || assemblyName.length() == 0) {
			String msg = "Missing assembly name to fetch details";
			throw new OneOpsClientAPIException(msg);
		}

		RequestSpecification request = createRequest();
		Response response = request.get(IConstants.ASSEMBLY_URI + assemblyName + "/teams");
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<Team>>(){});
			} else {
				String msg = String.format("Failed to get assembly team list with name %s due to %s", assemblyName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		}
		String msg = String.format("Failed to get assembly team list with name %s due to null response", assemblyName);
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
	 * Clones assembly @toAssembly from a given @fromAssembly
	 *
	 * @param fromOrg {mandatory}
	 * @param toOrg
	 * @param fromAssembly {mandatory}
	 * @param toAssembly {mandatory}
	 * @param description
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public CiResource cloneAssembly(String fromOrg, String toOrg, String fromAssembly, String toAssembly, String description) throws OneOpsClientAPIException{
		ResourceObject ro = new ResourceObject();
		Map<String ,String> properties= new HashMap<String ,String>();

		if(Strings.isNullOrEmpty(fromOrg)){
			String msg = "Missing organization name to clone assembly from";
			throw new OneOpsClientAPIException(msg);
		}
		if(Strings.isNullOrEmpty(toOrg)) {
			toOrg = fromOrg;
		}
		if(Strings.isNullOrEmpty(fromAssembly)){
			String msg = "Missing assembly name to be cloned";
			throw new OneOpsClientAPIException(msg);
		}
		if(Strings.isNullOrEmpty(toAssembly)){
			String msg = "Missing assembly name to be created";
			throw new OneOpsClientAPIException(msg);
		}

		properties.put("ciName", toAssembly);
		properties.put("to_org", toOrg);
		properties.put("description", description);
		properties.put("org_name", fromOrg);
		properties.put("id", fromAssembly);
		ro.setProperties(properties);

		RequestSpecification request = createRequest();
		JSONObject jsonObject = JsonUtil.createJsonObject(ro,null);

		Response response = request.body(jsonObject.toString()).post(IConstants.ASSEMBLY_URI  + fromAssembly +  "/clone");

		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(CiResource.class);
			} else {
				String msg = String.format("Failed to clone assembly with name %s due to %s", fromAssembly, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		}
		String msg = String.format("Failed to clone assembly with name %s due to null response", fromAssembly);
		throw new OneOpsClientAPIException(msg);
	}

	/**
	 * Updates assembly for the given @assemblyName
	 *
	 * @param assemblyName {mandatory}
	 * @param ownerEmail a valid email address is {mandatory}
	 * @param description
	 * @param tags
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public CiResource updateAssembly(String assemblyName, String ownerEmail, String description, Map<String, String> tags) throws OneOpsClientAPIException {
		ResourceObject ro = new ResourceObject();
		Map<String ,String> attributes = new HashMap<String ,String>();
		Map<String ,String> properties= new HashMap<String ,String>();

		if(assemblyName != null && assemblyName.length() > 0) {
			properties.put("ciName", assemblyName);
		} else {
			String msg = "Missing assembly name to update one";
			throw new OneOpsClientAPIException(msg);
		}

		CiResource assembly = getAssembly(assemblyName);

		properties.put("ciId", String.valueOf(assembly.getCiId()));
		ro.setProperties(properties);

		if(ownerEmail != null && ownerEmail.length() > 0) {
			attributes.put("owner", ownerEmail);
		}

		if(description != null && description.length() > 0) {
			attributes.put("description", description);
		}

		if( tags != null && !tags.isEmpty() ){
			attributes.put("tags", JSONObject.valueToString(tags));
		}

		ro.setAttributes(attributes);

		RequestSpecification request = createRequest();
		JSONObject jsonObject = JsonUtil.createJsonObject(ro , "cms_ci");

		Response response = request.body(jsonObject.toString()).put(IConstants.ASSEMBLY_URI + assemblyName);

		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(CiResource.class);
			} else {
				String msg = String.format("Failed to update assembly with name %s due to %s", assemblyName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		}
		String msg = String.format("Failed to update assembly with name %s due to null response", assemblyName);
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