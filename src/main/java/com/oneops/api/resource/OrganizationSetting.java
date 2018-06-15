package com.oneops.api.resource;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.oneops.api.APIClient;
import com.oneops.api.OOInstance;
import com.oneops.api.exception.OneOpsClientAPIException;
import com.oneops.api.resource.model.Member;
import com.oneops.api.resource.model.Team;
import com.oneops.api.util.IConstants;
import com.oneops.api.util.JsonUtil;

public class OrganizationSetting extends APIClient {

	public OrganizationSetting(OOInstance instance) throws OneOpsClientAPIException {
		super(instance);
	}
	

	/**
	 * Lists all the teams within an organization
	 * 
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public List<Team> listTeams() throws OneOpsClientAPIException {
		RequestSpecification request = createRequest();
		Response response = request.get(IConstants.ORGANIZATION_URI + IConstants.TEAM_URI);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<Team>>(){});
			} else {
				String msg = String.format("Failed to get list of teams due to %s", response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to get list of teams due to null response");
		throw new OneOpsClientAPIException(msg);
	}
	
	/**
	 * Lists all the users/groups[members] within an organization/team
	 * 
	 * @return
	 * @throws OneOpsClientAPIException
	 */
	public Member getTeamMembers(String teamName) throws OneOpsClientAPIException {
		RequestSpecification request = createRequest();
		Response response = request.get(IConstants.ORGANIZATION_URI + IConstants.TEAM_URI + teamName + IConstants.MEMBER_URI);
		if(response != null) {
			if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
				return response.getBody().as(Member.class);
			} else {
				String msg = String.format("Failed to get list of users for team %s due to %s", teamName, response.getStatusLine());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		String msg = String.format("Failed to get list of users for team %s due to null response", teamName);
		throw new OneOpsClientAPIException(msg);
	}
}
