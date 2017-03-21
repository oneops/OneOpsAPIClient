package com.oneops.api;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class OOInstance {

	private String name;
	private String endpoint;
	private String orgname;
	private String authtoken;
	private String assembly;
	private String platform;
	private String environment;
	private String component;
	@JsonProperty("global-vars")
	private Map<String, String> globalVars;
	@JsonProperty("local-vars")
	private Map<String, String> localVars;
	private String comment;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEndpoint() {
		return endpoint;
	}
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	public String getOrgname() {
		return orgname;
	}
	public void setOrgname(String orgname) {
		this.orgname = orgname;
	}
	public String getAuthtoken() {
		return authtoken;
	}
	public void setAuthtoken(String authtoken) {
		this.authtoken = authtoken;
	}
	public String getAssembly() {
		return assembly;
	}
	public void setAssembly(String assembly) {
		this.assembly = assembly;
	}
	public String getPlatform() {
		return platform;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	public String getEnvironment() {
		return environment;
	}
	public void setEnvironment(String environment) {
		this.environment = environment;
	}
	public String getComponent() {
		return component;
	}
	public void setComponent(String component) {
		this.component = component;
	}
	public Map<String, String> getGlobalVars() {
		return globalVars;
	}
	public void setGlobalVars(Map<String, String> globalVars) {
		this.globalVars = globalVars;
	}
	public Map<String, String> getLocalVars() {
		return localVars;
	}
	public void setLocalVars(Map<String, String> localVars) {
		this.localVars = localVars;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}	
}
