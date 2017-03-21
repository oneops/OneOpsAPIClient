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
@JsonPropertyOrder({ "actionId", "actionName", "ciId", "actionState", "execOrder", "isCritical", "extraInfo", "arglist",
		"payLoadDef", "createdBy", "created", "updated", "procedureId" })
public class Action {

	@JsonProperty("actionId")
	private Long actionId;
	@JsonProperty("actionName")
	private String actionName;
	@JsonProperty("ciId")
	private Long ciId;
	@JsonProperty("actionState")
	private String actionState;
	@JsonProperty("execOrder")
	private Long execOrder;
	@JsonProperty("isCritical")
	private Boolean isCritical;
	@JsonProperty("extraInfo")
	private Object extraInfo;
	@JsonProperty("arglist")
	private String arglist;
	@JsonProperty("payLoadDef")
	private Object payLoadDef;
	@JsonProperty("createdBy")
	private Object createdBy;
	@JsonProperty("created")
	private Long created;
	@JsonProperty("updated")
	private Long updated;
	@JsonProperty("procedureId")
	private Long procedureId;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("actionId")
	public Long getActionId() {
		return actionId;
	}

	@JsonProperty("actionId")
	public void setActionId(Long actionId) {
		this.actionId = actionId;
	}

	@JsonProperty("actionName")
	public String getActionName() {
		return actionName;
	}

	@JsonProperty("actionName")
	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	@JsonProperty("ciId")
	public Long getCiId() {
		return ciId;
	}

	@JsonProperty("ciId")
	public void setCiId(Long ciId) {
		this.ciId = ciId;
	}

	@JsonProperty("actionState")
	public String getActionState() {
		return actionState;
	}

	@JsonProperty("actionState")
	public void setActionState(String actionState) {
		this.actionState = actionState;
	}

	@JsonProperty("execOrder")
	public Long getExecOrder() {
		return execOrder;
	}

	@JsonProperty("execOrder")
	public void setExecOrder(Long execOrder) {
		this.execOrder = execOrder;
	}

	@JsonProperty("isCritical")
	public Boolean getIsCritical() {
		return isCritical;
	}

	@JsonProperty("isCritical")
	public void setIsCritical(Boolean isCritical) {
		this.isCritical = isCritical;
	}

	@JsonProperty("extraInfo")
	public Object getExtraInfo() {
		return extraInfo;
	}

	@JsonProperty("extraInfo")
	public void setExtraInfo(Object extraInfo) {
		this.extraInfo = extraInfo;
	}

	@JsonProperty("arglist")
	public String getArglist() {
		return arglist;
	}

	@JsonProperty("arglist")
	public void setArglist(String arglist) {
		this.arglist = arglist;
	}

	@JsonProperty("payLoadDef")
	public Object getPayLoadDef() {
		return payLoadDef;
	}

	@JsonProperty("payLoadDef")
	public void setPayLoadDef(Object payLoadDef) {
		this.payLoadDef = payLoadDef;
	}

	@JsonProperty("createdBy")
	public Object getCreatedBy() {
		return createdBy;
	}

	@JsonProperty("createdBy")
	public void setCreatedBy(Object createdBy) {
		this.createdBy = createdBy;
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

	@JsonProperty("procedureId")
	public Long getProcedureId() {
		return procedureId;
	}

	@JsonProperty("procedureId")
	public void setProcedureId(Long procedureId) {
		this.procedureId = procedureId;
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