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
@JsonPropertyOrder({"id", "name", "created_at", "updated_at", "cms_id", "assemblies", "catalogs", "services",
    "announcement", "full_name"})
public class Organization {

  @JsonProperty("id")
  private Long id;
  @JsonProperty("name")
  private String name;
  @JsonProperty("created_at")
  private String createdAt;
  @JsonProperty("updated_at")
  private String updatedAt;
  @JsonProperty("cms_id")
  private Long cmsId;
  @JsonProperty("assemblies")
  private Boolean assemblies;
  @JsonProperty("catalogs")
  private Boolean catalogs;
  @JsonProperty("services")
  private Boolean services;
  @JsonProperty("announcement")
  private Object announcement;
  @JsonProperty("full_name")
  private Object fullName;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("id")
  public Long getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(Long id) {
    this.id = id;
  }

  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  @JsonProperty("created_at")
  public String getCreatedAt() {
    return createdAt;
  }

  @JsonProperty("created_at")
  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }

  @JsonProperty("updated_at")
  public String getUpdatedAt() {
    return updatedAt;
  }

  @JsonProperty("updated_at")
  public void setUpdatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
  }

  @JsonProperty("cms_id")
  public Long getCmsId() {
    return cmsId;
  }

  @JsonProperty("cms_id")
  public void setCmsId(Long cmsId) {
    this.cmsId = cmsId;
  }

  @JsonProperty("assemblies")
  public Boolean getAssemblies() {
    return assemblies;
  }

  @JsonProperty("assemblies")
  public void setAssemblies(Boolean assemblies) {
    this.assemblies = assemblies;
  }

  @JsonProperty("catalogs")
  public Boolean getCatalogs() {
    return catalogs;
  }

  @JsonProperty("catalogs")
  public void setCatalogs(Boolean catalogs) {
    this.catalogs = catalogs;
  }

  @JsonProperty("services")
  public Boolean getServices() {
    return services;
  }

  @JsonProperty("services")
  public void setServices(Boolean services) {
    this.services = services;
  }

  @JsonProperty("announcement")
  public Object getAnnouncement() {
    return announcement;
  }

  @JsonProperty("announcement")
  public void setAnnouncement(Object announcement) {
    this.announcement = announcement;
  }

  @JsonProperty("full_name")
  public Object getFullName() {
    return fullName;
  }

  @JsonProperty("full_name")
  public void setFullName(Object fullName) {
    this.fullName = fullName;
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
