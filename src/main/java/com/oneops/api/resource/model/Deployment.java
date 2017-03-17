package com.oneops.api.resource.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "deploymentId", "releaseId", "maxExecOrder", "nsPath", "deploymentState", "processId", "createdBy",
		"updatedBy", "description", "comments", "ops", "autoPauseExecOrders", "created", "updated", "flags",
		"continueOnFailure" })
public class Deployment {

	@JsonProperty("deploymentId")
	private Long deploymentId;
	@JsonProperty("releaseId")
	private Long releaseId;
	@JsonProperty("maxExecOrder")
	private Integer maxExecOrder;
	@JsonProperty("nsPath")
	private String nsPath;
	@JsonProperty("deploymentState")
	private String deploymentState;
	@JsonProperty("processId")
	private String processId;
	@JsonProperty("createdBy")
	private String createdBy;
	@JsonProperty("updatedBy")
	private String updatedBy;
	@JsonProperty("description")
	private Object description;
	@JsonProperty("comments")
	private String comments;
	@JsonProperty("ops")
	private Object ops;
	@JsonProperty("autoPauseExecOrders")
	private Object autoPauseExecOrders;
	@JsonProperty("created")
	private Long created;
	@JsonProperty("updated")
	private Long updated;
	@JsonProperty("flags")
	private Integer flags;
	@JsonProperty("continueOnFailure")
	private Boolean continueOnFailure;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("deploymentId")
	public Long getDeploymentId() {
		return deploymentId;
	}

	@JsonProperty("deploymentId")
	public void setDeploymentId(Long deploymentId) {
		this.deploymentId = deploymentId;
	}

	@JsonProperty("releaseId")
	public Long getReleaseId() {
		return releaseId;
	}

	@JsonProperty("releaseId")
	public void setReleaseId(Long releaseId) {
		this.releaseId = releaseId;
	}

	@JsonProperty("maxExecOrder")
	public Integer getMaxExecOrder() {
		return maxExecOrder;
	}

	@JsonProperty("maxExecOrder")
	public void setMaxExecOrder(Integer maxExecOrder) {
		this.maxExecOrder = maxExecOrder;
	}

	@JsonProperty("nsPath")
	public String getNsPath() {
		return nsPath;
	}

	@JsonProperty("nsPath")
	public void setNsPath(String nsPath) {
		this.nsPath = nsPath;
	}

	@JsonProperty("deploymentState")
	public String getDeploymentState() {
		return deploymentState;
	}

	@JsonProperty("deploymentState")
	public void setDeploymentState(String deploymentState) {
		this.deploymentState = deploymentState;
	}

	@JsonProperty("processId")
	public String getProcessId() {
		return processId;
	}

	@JsonProperty("processId")
	public void setProcessId(String processId) {
		this.processId = processId;
	}

	@JsonProperty("createdBy")
	public String getCreatedBy() {
		return createdBy;
	}

	@JsonProperty("createdBy")
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	@JsonProperty("updatedBy")
	public String getUpdatedBy() {
		return updatedBy;
	}

	@JsonProperty("updatedBy")
	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	@JsonProperty("description")
	public Object getDescription() {
		return description;
	}

	@JsonProperty("description")
	public void setDescription(Object description) {
		this.description = description;
	}

	@JsonProperty("comments")
	public String getComments() {
		return comments;
	}

	@JsonProperty("comments")
	public void setComments(String comments) {
		this.comments = comments;
	}

	@JsonProperty("ops")
	public Object getOps() {
		return ops;
	}

	@JsonProperty("ops")
	public void setOps(Object ops) {
		this.ops = ops;
	}

	@JsonProperty("autoPauseExecOrders")
	public Object getAutoPauseExecOrders() {
		return autoPauseExecOrders;
	}

	@JsonProperty("autoPauseExecOrders")
	public void setAutoPauseExecOrders(Object autoPauseExecOrders) {
		this.autoPauseExecOrders = autoPauseExecOrders;
	}

	@JsonProperty("created")
	public Long getCreated() {
		return created;
	}

	@JsonProperty("created")
	public void setCreated(Long created) {
		this.created = created;
	}

	@JsonProperty("updated")
	public Long getUpdated() {
		return updated;
	}

	@JsonProperty("updated")
	public void setUpdated(Long updated) {
		this.updated = updated;
	}

	@JsonProperty("flags")
	public Integer getFlags() {
		return flags;
	}

	@JsonProperty("flags")
	public void setFlags(Integer flags) {
		this.flags = flags;
	}

	@JsonProperty("continueOnFailure")
	public Boolean getContinueOnFailure() {
		return continueOnFailure;
	}

	@JsonProperty("continueOnFailure")
	public void setContinueOnFailure(Boolean continueOnFailure) {
		this.continueOnFailure = continueOnFailure;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}