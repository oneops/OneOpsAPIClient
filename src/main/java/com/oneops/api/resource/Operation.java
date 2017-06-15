package com.oneops.api.resource;

import java.util.List;

import com.jayway.restassured.path.json.JsonPath;
import com.oneops.api.APIClient;
import com.oneops.api.OOInstance;
import com.oneops.api.exception.OneOpsClientAPIException;
import com.oneops.api.resource.model.CiResource;
import com.oneops.api.resource.model.Procedure;

@Deprecated
public class Operation extends APIClient {

  private String assemblyName;
  private String environmentName;

  public Operation(OOInstance instance, String assemblyName, String environmentName) throws OneOpsClientAPIException {
    super(instance);
    if (assemblyName == null || assemblyName.length() == 0) {
      String msg = "Missing assembly name";
      throw new OneOpsClientAPIException(msg);
    }

    if (environmentName == null || environmentName.length() == 0) {
      String msg = "Missing environment name";
      throw new OneOpsClientAPIException(msg);
    }

    this.assemblyName = assemblyName;
    this.environmentName = environmentName;
  }

  @Deprecated
  public List<CiResource> listInstances(String platformName, String componentName) throws OneOpsClientAPIException {
    return super.listInstances(assemblyName, environmentName, platformName, componentName);
  }

  @Deprecated
  public Boolean markInstancesForReplacement(String platformName, String componentName) throws OneOpsClientAPIException {
    return super.markInstancesForReplacement(assemblyName, environmentName, platformName, componentName);
  }

  @Deprecated
  public Boolean markInstanceForReplacement(String platformName, String componentName, Long instanceId) throws OneOpsClientAPIException {
    return super.markInstanceForReplacement(assemblyName, platformName, componentName, instanceId);
  }

  @Deprecated
  public JsonPath getLogData(String procedureId, List<String> actionIds) throws OneOpsClientAPIException {
    return super.getLogData(procedureId, actionIds);
  }

  @Deprecated
  public List<CiResource> listProcedures(String platformName) throws OneOpsClientAPIException {
    return super.listProcedures(assemblyName, environmentName, platformName);
  }

  @Deprecated
  public Long getProcedureId(String platformName, String procedureName) throws OneOpsClientAPIException {
    return super.getProcedureId(assemblyName, environmentName, platformName, procedureName);
  }

  @Deprecated
  public JsonPath listActions(String platformName, String componentName) throws OneOpsClientAPIException {
    return super.listActions(assemblyName, environmentName, platformName, componentName);
  }

  @Deprecated
  public Procedure executeProcedure(String platformName, String procedureName, String arglist) throws OneOpsClientAPIException {
    return super.executeProcedure(assemblyName, environmentName, platformName, procedureName, arglist);
  }

  @Deprecated
  public Procedure getProcedureStatus(Long procedureId) throws OneOpsClientAPIException {
    return super.getProcedureStatus(procedureId);
  }

  @Deprecated
  public Procedure cancelProcedure(Long procedureId) throws OneOpsClientAPIException {
    return super.cancelProcedure(procedureId);
  }

  @Deprecated
  public Procedure executeAction(String platformName, String componentName, String actionName, List<Long> instanceList, String arglist, int rollingPercent) throws OneOpsClientAPIException {
    return super.executeAction(assemblyName, environmentName, platformName, componentName, actionName, instanceList, arglist, rollingPercent);
  }

  public CiResource updatePlatformAutoHealingStatus(String environmentName, String platformName, String healingOption, boolean isEnabled) throws OneOpsClientAPIException {
    return super.updatePlatformAutoHealingStatus(assemblyName, environmentName, platformName, healingOption, isEnabled);
  }

  public CiResource updatePlatformAutoReplaceConfig(String environmentName, String platformName, int repairCount, int repairTime) throws OneOpsClientAPIException {
    return super.updatePlatformAutoReplaceConfig(assemblyName, environmentName, platformName, repairCount, repairTime);
  }
}
