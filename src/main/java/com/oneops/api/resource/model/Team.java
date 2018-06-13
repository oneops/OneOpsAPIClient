package com.oneops.api.resource.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Team {

	@JsonProperty("id")
	private String id;
	@JsonProperty("name")
	private String name;
	@JsonProperty("organization_id")
	private String orgId;
	@JsonProperty("created_at")
	private Date created;
	@JsonProperty("updated_at")
	private Date updated;
	@JsonProperty("description")
	private String description;
	@JsonProperty("design")
	private boolean design;
	@JsonProperty("transition")
	private boolean transition;
	@JsonProperty("operations")
	private boolean operations;
	@JsonProperty("org_scope")
	private boolean org_scope;
	@JsonProperty("manages_access")
	private boolean manages_access;
	@JsonProperty("cloud_services")
	private boolean cloud_services;
	@JsonProperty("cloud_compliance")
	private boolean cloud_compliance;
	@JsonProperty("cloud_support")
	private boolean cloud_support;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public Date getUpdated() {
		return updated;
	}
	public void setUpdated(Date updated) {
		this.updated = updated;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOrgId() {
		return orgId;
	}
	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean isDesign() {
		return design;
	}
	public void setDesign(boolean design) {
		this.design = design;
	}
	public boolean isTransition() {
		return transition;
	}
	public void setTransition(boolean transition) {
		this.transition = transition;
	}
	public boolean isOperations() {
		return operations;
	}
	public void setOperations(boolean operations) {
		this.operations = operations;
	}
	public boolean isOrg_scope() {
		return org_scope;
	}
	public void setOrg_scope(boolean org_scope) {
		this.org_scope = org_scope;
	}
	public boolean isManages_access() {
		return manages_access;
	}
	public void setManages_access(boolean manages_access) {
		this.manages_access = manages_access;
	}
	public boolean isCloud_services() {
		return cloud_services;
	}
	public void setCloud_services(boolean cloud_services) {
		this.cloud_services = cloud_services;
	}
	public boolean isCloud_compliance() {
		return cloud_compliance;
	}
	public void setCloud_compliance(boolean cloud_compliance) {
		this.cloud_compliance = cloud_compliance;
	}
	public boolean isCloud_support() {
		return cloud_support;
	}
	public void setCloud_support(boolean cloud_support) {
		this.cloud_support = cloud_support;
	}
	
}
