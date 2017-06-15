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
@JsonPropertyOrder({"releaseId", "nsPath", "releaseName", "createdBy", "commitedBy", "releaseState", "releaseType",
    "description", "revision", "parentReleaseId", "created", "updated", "nsId", "releaseStateId", "ciRfcCount",
    "relationRfcCount"})
public class Release {

  @JsonProperty("releaseId")
  private Long releaseId;
  @JsonProperty("nsPath")
  private String nsPath;
  @JsonProperty("releaseName")
  private String releaseName;
  @JsonProperty("createdBy")
  private String createdBy;
  @JsonProperty("commitedBy")
  private String commitedBy;
  @JsonProperty("releaseState")
  private String releaseState;
  @JsonProperty("releaseType")
  private Object releaseType;
  @JsonProperty("description")
  private String description;
  @JsonProperty("revision")
  private Integer revision;
  @JsonProperty("parentReleaseId")
  private Object parentReleaseId;
  @JsonProperty("created")
  private Long created;
  @JsonProperty("updated")
  private Long updated;
  @JsonProperty("nsId")
  private Integer nsId;
  @JsonProperty("releaseStateId")
  private Integer releaseStateId;
  @JsonProperty("ciRfcCount")
  private Integer ciRfcCount;
  @JsonProperty("relationRfcCount")
  private Integer relationRfcCount;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("releaseId")
  public Long getReleaseId() {
    return releaseId;
  }

  @JsonProperty("releaseId")
  public void setReleaseId(Long releaseId) {
    this.releaseId = releaseId;
  }

  @JsonProperty("nsPath")
  public String getNsPath() {
    return nsPath;
  }

  @JsonProperty("nsPath")
  public void setNsPath(String nsPath) {
    this.nsPath = nsPath;
  }

  @JsonProperty("releaseName")
  public String getReleaseName() {
    return releaseName;
  }

  @JsonProperty("releaseName")
  public void setReleaseName(String releaseName) {
    this.releaseName = releaseName;
  }

  @JsonProperty("createdBy")
  public String getCreatedBy() {
    return createdBy;
  }

  @JsonProperty("createdBy")
  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  @JsonProperty("commitedBy")
  public String getCommitedBy() {
    return commitedBy;
  }

  @JsonProperty("commitedBy")
  public void setCommitedBy(String commitedBy) {
    this.commitedBy = commitedBy;
  }

  @JsonProperty("releaseState")
  public String getReleaseState() {
    return releaseState;
  }

  @JsonProperty("releaseState")
  public void setReleaseState(String releaseState) {
    this.releaseState = releaseState;
  }

  @JsonProperty("releaseType")
  public Object getReleaseType() {
    return releaseType;
  }

  @JsonProperty("releaseType")
  public void setReleaseType(Object releaseType) {
    this.releaseType = releaseType;
  }

  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
  }

  @JsonProperty("revision")
  public Integer getRevision() {
    return revision;
  }

  @JsonProperty("revision")
  public void setRevision(Integer revision) {
    this.revision = revision;
  }

  @JsonProperty("parentReleaseId")
  public Object getParentReleaseId() {
    return parentReleaseId;
  }

  @JsonProperty("parentReleaseId")
  public void setParentReleaseId(Object parentReleaseId) {
    this.parentReleaseId = parentReleaseId;
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
  public Integer getNsId() {
    return nsId;
  }

  @JsonProperty("nsId")
  public void setNsId(Integer nsId) {
    this.nsId = nsId;
  }

  @JsonProperty("releaseStateId")
  public Integer getReleaseStateId() {
    return releaseStateId;
  }

  @JsonProperty("releaseStateId")
  public void setReleaseStateId(Integer releaseStateId) {
    this.releaseStateId = releaseStateId;
  }

  @JsonProperty("ciRfcCount")
  public Integer getCiRfcCount() {
    return ciRfcCount;
  }

  @JsonProperty("ciRfcCount")
  public void setCiRfcCount(Integer ciRfcCount) {
    this.ciRfcCount = ciRfcCount;
  }

  @JsonProperty("relationRfcCount")
  public Integer getRelationRfcCount() {
    return relationRfcCount;
  }

  @JsonProperty("relationRfcCount")
  public void setRelationRfcCount(Integer relationRfcCount) {
    this.relationRfcCount = relationRfcCount;
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
