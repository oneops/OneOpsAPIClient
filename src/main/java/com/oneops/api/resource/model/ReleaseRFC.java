package com.oneops.api.resource.model;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"cis"})
public class ReleaseRFC {
    @JsonProperty("cis")
    private List<RfcCi> cis = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("cis")
    public List<RfcCi> getCis() {
        return cis;
    }

    @JsonProperty("cis")
    public void setCis(List<RfcCi> cis) {
        this.cis = cis;
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
