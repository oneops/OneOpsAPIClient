package com.oneops.api.resource.model;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "rfcs", "release", "capacity" })
public class DeploymentBom {
    @JsonProperty("rfcs")
    private ReleaseRFC rfcs;
    @JsonProperty("release")
    private Release release;
    @JsonProperty("capacity")
    private Capacity capacity;
    @JsonProperty("errors")
    private List<String> errors = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("rfcs")
    public ReleaseRFC getRfcs() {
        return rfcs;
    }

    @JsonProperty("rfcs")
    public void setRfcs(ReleaseRFC rfcs) {
        this.rfcs = rfcs;
    }

    @JsonProperty("release")
    public Release getRelease() {
        return release;
    }

    @JsonProperty("release")
    public void setRelease(Release release) {
        this.release = release;
    }

    @JsonProperty("capacity")
    public Capacity getCapacity() {
        return capacity;
    }

    @JsonProperty("capacity")
    public void setCapacity(Capacity capacity) {
        this.capacity = capacity;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String key, Object value) {
        this.additionalProperties.put(key, value);
    }
}
