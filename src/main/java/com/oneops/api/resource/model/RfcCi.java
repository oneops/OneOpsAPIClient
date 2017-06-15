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
@JsonPropertyOrder({"rfcId", "releaseId", "ciId", "nsPath", "ciClassName", "impl", "ciName", "ciGoid", "ciState",
    "rfcAction", "releaseType", "createdBy", "updatedBy", "rfcCreatedBy", "rfcUpdatedBy", "execOrder",
    "lastAppliedRfcId", "comments", "isActiveInRelease", "rfcCreated", "rfcUpdated", "created", "updated",
    "ciAttributes", "ciAttrProps", "deployment"})
public class RfcCi {

  @JsonProperty("rfcId")
  private Long rfcId;
  @JsonProperty("releaseId")
  private Long releaseId;
  @JsonProperty("ciId")
  private Long ciId;
  @JsonProperty("nsPath")
  private String nsPath;
  @JsonProperty("ciClassName")
  private String ciClassName;
  @JsonProperty("impl")
  private String impl;
  @JsonProperty("ciName")
  private String ciName;
  @JsonProperty("ciGoid")
  private String ciGoid;
  @JsonProperty("ciState")
  private Object ciState;
  @JsonProperty("rfcAction")
  private String rfcAction;
  @JsonProperty("releaseType")
  private Object releaseType;
  @JsonProperty("createdBy")
  private String createdBy;
  @JsonProperty("updatedBy")
  private Object updatedBy;
  @JsonProperty("rfcCreatedBy")
  private String rfcCreatedBy;
  @JsonProperty("rfcUpdatedBy")
  private Object rfcUpdatedBy;
  @JsonProperty("execOrder")
  private Long execOrder;
  @JsonProperty("lastAppliedRfcId")
  private Object lastAppliedRfcId;
  @JsonProperty("comments")
  private Object comments;
  @JsonProperty("isActiveInRelease")
  private Boolean isActiveInRelease;
  @JsonProperty("rfcCreated")
  private Long rfcCreated;
  @JsonProperty("rfcUpdated")
  private Long rfcUpdated;
  @JsonProperty("created")
  private Long created;
  @JsonProperty("updated")
  private Long updated;
  @JsonProperty("ciAttributes")
  private CiAttributes ciAttributes;
  @JsonProperty("ciAttrProps")
  private AttrProps ciAttrProps;
  @JsonProperty("deployment")
  private Deployment deployment;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("rfcId")
  public Long getRfcId() {
    return rfcId;
  }

  @JsonProperty("rfcId")
  public void setRfcId(Long rfcId) {
    this.rfcId = rfcId;
  }

  @JsonProperty("releaseId")
  public Long getReleaseId() {
    return releaseId;
  }

  @JsonProperty("releaseId")
  public void setReleaseId(Long releaseId) {
    this.releaseId = releaseId;
  }

  @JsonProperty("ciId")
  public Long getCiId() {
    return ciId;
  }

  @JsonProperty("ciId")
  public void setCiId(Long ciId) {
    this.ciId = ciId;
  }

  @JsonProperty("nsPath")
  public String getNsPath() {
    return nsPath;
  }

  @JsonProperty("nsPath")
  public void setNsPath(String nsPath) {
    this.nsPath = nsPath;
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

  @JsonProperty("ciName")
  public String getCiName() {
    return ciName;
  }

  @JsonProperty("ciName")
  public void setCiName(String ciName) {
    this.ciName = ciName;
  }

  @JsonProperty("ciGoid")
  public String getCiGoid() {
    return ciGoid;
  }

  @JsonProperty("ciGoid")
  public void setCiGoid(String ciGoid) {
    this.ciGoid = ciGoid;
  }

  @JsonProperty("ciState")
  public Object getCiState() {
    return ciState;
  }

  @JsonProperty("ciState")
  public void setCiState(Object ciState) {
    this.ciState = ciState;
  }

  @JsonProperty("rfcAction")
  public String getRfcAction() {
    return rfcAction;
  }

  @JsonProperty("rfcAction")
  public void setRfcAction(String rfcAction) {
    this.rfcAction = rfcAction;
  }

  @JsonProperty("releaseType")
  public Object getReleaseType() {
    return releaseType;
  }

  @JsonProperty("releaseType")
  public void setReleaseType(Object releaseType) {
    this.releaseType = releaseType;
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

  @JsonProperty("rfcCreatedBy")
  public String getRfcCreatedBy() {
    return rfcCreatedBy;
  }

  @JsonProperty("rfcCreatedBy")
  public void setRfcCreatedBy(String rfcCreatedBy) {
    this.rfcCreatedBy = rfcCreatedBy;
  }

  @JsonProperty("rfcUpdatedBy")
  public Object getRfcUpdatedBy() {
    return rfcUpdatedBy;
  }

  @JsonProperty("rfcUpdatedBy")
  public void setRfcUpdatedBy(Object rfcUpdatedBy) {
    this.rfcUpdatedBy = rfcUpdatedBy;
  }

  @JsonProperty("execOrder")
  public Long getExecOrder() {
    return execOrder;
  }

  @JsonProperty("execOrder")
  public void setExecOrder(Long execOrder) {
    this.execOrder = execOrder;
  }

  @JsonProperty("lastAppliedRfcId")
  public Object getLastAppliedRfcId() {
    return lastAppliedRfcId;
  }

  @JsonProperty("lastAppliedRfcId")
  public void setLastAppliedRfcId(Object lastAppliedRfcId) {
    this.lastAppliedRfcId = lastAppliedRfcId;
  }

  @JsonProperty("comments")
  public Object getComments() {
    return comments;
  }

  @JsonProperty("comments")
  public void setComments(Object comments) {
    this.comments = comments;
  }

  @JsonProperty("isActiveInRelease")
  public Boolean getIsActiveInRelease() {
    return isActiveInRelease;
  }

  @JsonProperty("isActiveInRelease")
  public void setIsActiveInRelease(Boolean isActiveInRelease) {
    this.isActiveInRelease = isActiveInRelease;
  }

  @JsonProperty("rfcCreated")
  public Long getRfcCreated() {
    return rfcCreated;
  }

  @JsonProperty("rfcCreated")
  public void setRfcCreated(Long rfcCreated) {
    this.rfcCreated = rfcCreated;
  }

  @JsonProperty("rfcUpdated")
  public Long getRfcUpdated() {
    return rfcUpdated;
  }

  @JsonProperty("rfcUpdated")
  public void setRfcUpdated(Long rfcUpdated) {
    this.rfcUpdated = rfcUpdated;
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

  @JsonProperty("ciAttributes")
  public CiAttributes getCiAttributes() {
    return ciAttributes;
  }

  @JsonProperty("ciAttributes")
  public void setCiAttributes(CiAttributes ciAttributes) {
    this.ciAttributes = ciAttributes;
  }

  @JsonProperty("ciAttrProps")
  public AttrProps getCiAttrProps() {
    return ciAttrProps;
  }

  @JsonProperty("ciAttrProps")
  public void setCiAttrProps(AttrProps ciAttrProps) {
    this.ciAttrProps = ciAttrProps;
  }

  @JsonProperty("deployment")
  public Deployment getDeployment() {
    return deployment;
  }

  @JsonProperty("deployment")
  public void setDeployment(Deployment deployment) {
    this.deployment = deployment;
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
