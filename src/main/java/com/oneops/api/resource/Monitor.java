package com.oneops.api.resource;

import org.json.JSONObject;

import com.oneops.api.APIClient;
import com.oneops.api.OOInstance;
import com.oneops.api.exception.OneOpsClientAPIException;
import com.oneops.api.resource.model.CiResource;

@Deprecated
public class Monitor extends APIClient {

  private String assemblyName;
  private String environmentName;
  private String platform;
  private String component;

  public Monitor(OOInstance instance, String assemblyName, String environment, String platform, String component) throws OneOpsClientAPIException {
    super(instance);
    if (assemblyName == null || assemblyName.length() == 0) {
      String msg = "Missing assembly name";
      throw new OneOpsClientAPIException(msg);
    }
    if (environment == null || environment.length() == 0) {
      String msg = "Missing environment name";
      throw new OneOpsClientAPIException(msg);
    }
    if (platform == null || platform.length() == 0) {
      String msg = "Missing platform name";
      throw new OneOpsClientAPIException(msg);
    }
    if (component == null || component.length() == 0) {
      String msg = "Missing component name";
      throw new OneOpsClientAPIException(msg);
    }

    this.environmentName = environment;
  }

  @Deprecated
  public CiResource getMonitor(String monitorName) throws OneOpsClientAPIException {
    return super.getMonitor(assemblyName, platform, component, environmentName, monitorName);
  }

  @Deprecated
  public CiResource updateMonitor(String monitorName, String cmdOptions, Integer duration, Integer sampleInterval, JSONObject thresholds, boolean heartbeat, boolean enable)
    throws OneOpsClientAPIException {
    return super.updateMonitor(assemblyName, platform, component, environmentName, monitorName, cmdOptions, duration, sampleInterval, thresholds, heartbeat, enable);
  }

  @Deprecated
  public JSONObject getThresholdJson(String name, String state, String metric, String bucket, String stat, JSONObject trigger, JSONObject reset) {
    return super.getThresholdJson(name, state, metric, bucket, stat, trigger, reset);
  }

  @Deprecated
  public JSONObject createThresholdCondition(String operator, int value, int duration, int numOfOccurances) {
    return super.createThresholdCondition(operator, value, duration, numOfOccurances);
  }

  @Deprecated
  public JSONObject addMetrics(String metricName, String unit, String dstype, String display, String description) {
    return super.addMetrics(metricName, unit, dstype, display, description);
  }

  //list thresholds
  //add threshold
  //update threshold
  //delete threshold
}
