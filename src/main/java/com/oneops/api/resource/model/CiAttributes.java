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
@JsonPropertyOrder({"adminstatus", "auth", "description", "location"})
public class CiAttributes {

  @JsonProperty("adminstatus")
  private String adminstatus;
  @JsonProperty("auth")
  private Object auth;
  @JsonProperty("description")
  private String description;
  @JsonProperty("location")
  private String location;
  @JsonIgnore
  private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("adminstatus")
  public String getAdminstatus() {
    return adminstatus;
  }

  @JsonProperty("adminstatus")
  public void setAdminstatus(String adminstatus) {
    this.adminstatus = adminstatus;
  }

  @JsonProperty("auth")
  public Object getAuth() {
    return auth;
  }

  @JsonProperty("auth")
  public void setAuth(Object auth) {
    this.auth = auth;
  }

  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  @JsonProperty("description")
  public void setDescription(String description) {
    this.description = description;
  }

  @JsonProperty("location")
  public String getLocation() {
    return location;
  }

  @JsonProperty("location")
  public void setLocation(String location) {
    this.location = location;
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
