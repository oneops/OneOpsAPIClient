package com.oneops.api.resource.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "ciId", "procedureCiId", "procedureState", "arglist", "definition", "force", "procedureId",
		"procedureName", "maxExecOrder", "createdBy", "created", "updated", "nsPath", "forceExecution", "actions" })
public class Procedure {

	@JsonProperty("ciId")
	private Long ciId;
	@JsonProperty("procedureCiId")
	private Long procedureCiId;
	@JsonProperty("procedureState")
	private String procedureState;
	@JsonProperty("arglist")
	private String arglist;
	@JsonProperty("definition")
	private Object definition;
	@JsonProperty("force")
	private String force;
	@JsonProperty("procedureId")
	private Long procedureId;
	@JsonProperty("procedureName")
	private String procedureName;
	@JsonProperty("maxExecOrder")
	private Long maxExecOrder;
	@JsonProperty("createdBy")
	private String createdBy;
	@JsonProperty("created")
	private Long created;
	@JsonProperty("updated")
	private Long updated;
	@JsonProperty("nsPath")
	private Object nsPath;
	@JsonProperty("forceExecution")
	private Boolean forceExecution;
	@JsonProperty("actions")
	private List<Action> actions = null;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("ciId")
	public Long getCiId() {
		return ciId;
	}

	@JsonProperty("ciId")
	public void setCiId(Long ciId) {
		this.ciId = ciId;
	}

	@JsonProperty("procedureCiId")
	public Long getProcedureCiId() {
		return procedureCiId;
	}

	@JsonProperty("procedureCiId")
	public void setProcedureCiId(Long procedureCiId) {
		this.procedureCiId = procedureCiId;
	}

	@JsonProperty("procedureState")
	public String getProcedureState() {
		return procedureState;
	}

	@JsonProperty("procedureState")
	public void setProcedureState(String procedureState) {
		this.procedureState = procedureState;
	}

	@JsonProperty("arglist")
	public String getArglist() {
		return arglist;
	}

	@JsonProperty("arglist")
	public void setArglist(String arglist) {
		this.arglist = arglist;
	}

	@JsonProperty("definition")
	public Object getDefinition() {
		return definition;
	}

	@JsonProperty("definition")
	public void setDefinition(Object definition) {
		this.definition = definition;
	}

	@JsonProperty("force")
	public String getForce() {
		return force;
	}

	@JsonProperty("force")
	public void setForce(String force) {
		this.force = force;
	}

	@JsonProperty("procedureId")
	public Long getProcedureId() {
		return procedureId;
	}

	@JsonProperty("procedureId")
	public void setProcedureId(Long procedureId) {
		this.procedureId = procedureId;
	}

	@JsonProperty("procedureName")
	public String getProcedureName() {
		return procedureName;
	}

	@JsonProperty("procedureName")
	public void setProcedureName(String procedureName) {
		this.procedureName = procedureName;
	}

	@JsonProperty("maxExecOrder")
	public Long getMaxExecOrder() {
		return maxExecOrder;
	}

	@JsonProperty("maxExecOrder")
	public void setMaxExecOrder(Long maxExecOrder) {
		this.maxExecOrder = maxExecOrder;
	}

	@JsonProperty("createdBy")
	public String getCreatedBy() {
		return createdBy;
	}

	@JsonProperty("createdBy")
	public void setCreatedBy(String createdBy) {
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

	@JsonProperty("nsPath")
	public Object getNsPath() {
		return nsPath;
	}

	@JsonProperty("nsPath")
	public void setNsPath(Object nsPath) {
		this.nsPath = nsPath;
	}

	@JsonProperty("forceExecution")
	public Boolean getForceExecution() {
		return forceExecution;
	}

	@JsonProperty("forceExecution")
	public void setForceExecution(Boolean forceExecution) {
		this.forceExecution = forceExecution;
	}

	@JsonProperty("actions")
	public List<Action> getActions() {
		return actions;
	}

	@JsonProperty("actions")
	public void setActions(List<Action> actions) {
		this.actions = actions;
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