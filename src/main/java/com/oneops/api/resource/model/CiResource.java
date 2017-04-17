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
@JsonPropertyOrder({ "ciId", "ciName", "ciClassName", "impl", "nsPath", "ciGoid", "comments", "ciState",
		"lastAppliedRfcId", "createdBy", "updatedBy", "created", "updated", "nsId", "ciAttributes", "attrProps" })
public class CiResource {

	@JsonProperty("ciId")
	private Long ciId;
	@JsonProperty("ciName")
	private String ciName;
	@JsonProperty("ciClassName")
	private String ciClassName;
	@JsonProperty("impl")
	private String impl;
	@JsonProperty("nsPath")
	private String nsPath;
	@JsonProperty("ciGoid")
	private String ciGoid;
	@JsonProperty("comments")
	private String comments;
	@JsonProperty("ciState")
	private String ciState;
	@JsonProperty("lastAppliedRfcId")
	private Long lastAppliedRfcId;
	@JsonProperty("createdBy")
	private String createdBy;
	@JsonProperty("updatedBy")
	private Object updatedBy;
	@JsonProperty("created")
	private Long created;
	@JsonProperty("updated")
	private Long updated;
	@JsonProperty("nsId")
	private Long nsId;
	@JsonProperty("ciAttributes")
	private CiAttributes ciAttributes;
	@JsonProperty("ciAttrProps")
	private AttrProps attrProps;
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

	@JsonProperty("ciName")
	public String getCiName() {
		return ciName;
	}

	@JsonProperty("ciName")
	public void setCiName(String ciName) {
		this.ciName = ciName;
	}

	@JsonProperty("ciClassName")
	public String getCiClassName() {
		return ciClassName;
	}

	@JsonProperty("ciClassName")
	public void setCiClassName(String ciClassName) {
		this.ciClassName = ciClassName;
	}

	@JsonProperty("impl")
	public String getImpl() {
		return impl;
	}

	@JsonProperty("impl")
	public void setImpl(String impl) {
		this.impl = impl;
	}

	@JsonProperty("nsPath")
	public String getNsPath() {
		return nsPath;
	}

	@JsonProperty("nsPath")
	public void setNsPath(String nsPath) {
		this.nsPath = nsPath;
	}

	@JsonProperty("ciGoid")
	public String getCiGoid() {
		return ciGoid;
	}

	@JsonProperty("ciGoid")
	public void setCiGoid(String ciGoid) {
		this.ciGoid = ciGoid;
	}

	@JsonProperty("comments")
	public String getComments() {
		return comments;
	}

	@JsonProperty("comments")
	public void setComments(String comments) {
		this.comments = comments;
	}

	@JsonProperty("ciState")
	public String getCiState() {
		return ciState;
	}

	@JsonProperty("ciState")
	public void setCiState(String ciState) {
		this.ciState = ciState;
	}

	@JsonProperty("lastAppliedRfcId")
	public Long getLastAppliedRfcId() {
		return lastAppliedRfcId;
	}

	@JsonProperty("lastAppliedRfcId")
	public void setLastAppliedRfcId(Long lastAppliedRfcId) {
		this.lastAppliedRfcId = lastAppliedRfcId;
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
	public Object getUpdatedBy() {
		return updatedBy;
	}

	@JsonProperty("updatedBy")
	public void setUpdatedBy(Object updatedBy) {
		this.updatedBy = updatedBy;
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

	@JsonProperty("nsId")
	public Long getNsId() {
		return nsId;
	}

	@JsonProperty("nsId")
	public void setNsId(Long nsId) {
		this.nsId = nsId;
	}

	@JsonProperty("ciAttributes")
	public CiAttributes getCiAttributes() {
		return ciAttributes;
	}

	@JsonProperty("ciAttributes")
	public void setCiAttributes(CiAttributes ciAttributes) {
		this.ciAttributes = ciAttributes;
	}

	@JsonProperty("attrProps")
	public AttrProps getAttrProps() {
		return attrProps;
	}

	@JsonProperty("attrProps")
	public void setAttrProps(AttrProps attrProps) {
		this.attrProps = attrProps;
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