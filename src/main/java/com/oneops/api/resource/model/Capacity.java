package com.oneops.api.resource.model;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"increase", "decrease", "reservationCheck"})
public class Capacity {
    @JsonProperty("increase")
    private Map<String, Object> increase;
    @JsonProperty("decrease")
    private Map<String, Object> decrease;
    @JsonProperty("reservationCheck")
    private String reservationCheck;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("increase")
    public Map<String, Object> getIncrease() {
        return increase;
    }

    @JsonProperty("increase")
    public void setIncrease(Map<String, Object> increase) {
        this.increase = increase;
    }

    @JsonProperty("decrease")
    public Map<String, Object> getDecrease() {
        return decrease;
    }

    @JsonProperty("decrease")
    public void setDecrease(Map<String, Object> decrease) {
        this.decrease = decrease;
    }

    @JsonProperty("reservationCheck")
    public String getReservationCheck() {
        return reservationCheck;
    }

    @JsonProperty("reservationCheck")
    public void setReservationCheck(String reservationCheck) {
        this.reservationCheck = reservationCheck;
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
