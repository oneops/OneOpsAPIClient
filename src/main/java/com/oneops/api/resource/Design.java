package com.oneops.api.resource;

import java.util.List;
import java.util.Map;

import com.jayway.restassured.path.json.JsonPath;
import com.oneops.api.APIClient;
import com.oneops.api.OOInstance;
import com.oneops.api.exception.OneOpsClientAPIException;
import com.oneops.api.resource.model.CiResource;
import com.oneops.api.resource.model.Release;

@Deprecated
public class Design extends APIClient {

  private final String assemblyName;

  public Design(OOInstance instance, String assemblyName) throws OneOpsClientAPIException {
    super(instance);
    if (assemblyName == null || assemblyName.length() == 0) {
      String msg = "Missing assembly name";
      throw new OneOpsClientAPIException(msg);
    }
    this.assemblyName = assemblyName;
  }

  @Deprecated
  public CiResource getPlatform(String platformName) throws OneOpsClientAPIException {
    return super.getPlatform(assemblyName, platformName);
  }

  @Deprecated
  public List<CiResource> listPlatforms() throws OneOpsClientAPIException {
    return super.listPlatforms(assemblyName);
  }

  @Deprecated
  public CiResource createPlatform(String platformName, String packname, String packversion,
    String packsource, String comments, String description) throws OneOpsClientAPIException {
    return super.createPlatform(assemblyName, platformName, packname, packversion, packsource, comments, description);
  }

  @Deprecated
  public Release commitDesign() throws OneOpsClientAPIException {
    return super.commitDesign(assemblyName);
  }

  @Deprecated
  public JsonPath commitPlatform(String platformName) throws OneOpsClientAPIException {
    return super.commitPlatform(assemblyName, platformName);
  }

  @Deprecated
  public CiResource updatePlatformLinks(String fromPlatformName, List<String> toPlatformNames) throws OneOpsClientAPIException {
    return super.updatePlatformLinks(fromPlatformName, fromPlatformName, toPlatformNames);
  }

  @Deprecated
  public CiResource deletePlatform(String platformName) throws OneOpsClientAPIException {
    return super.deletePlatform(platformName, platformName);
  }

  @Deprecated
  public List<CiResource> listPlatformComponents(String platformName) throws OneOpsClientAPIException {
    return super.listPlatformComponents(platformName, platformName);
  }

  @Deprecated
  public CiResource getPlatformComponent(String platformName, String componentName) throws OneOpsClientAPIException {
    return super.getPlatformComponent(componentName, platformName, componentName);
  }

  @Deprecated
  public CiResource addPlatformComponent(String platformName, String componentName, String uniqueName, Map<String, String> attributes) throws OneOpsClientAPIException {
    return super.addPlatformComponent(uniqueName, platformName, componentName, uniqueName, attributes);
  }

  @Deprecated
  public CiResource updatePlatformComponent(String platformName, String componentName, Map<String, String> attributes) throws OneOpsClientAPIException {
    return super.updatePlatformComponent(assemblyName, platformName, componentName, attributes);
  }

  @Deprecated
  public CiResource deletePlatformComponent(String platformName, String componentName) throws OneOpsClientAPIException {
    return super.deletePlatformComponent(assemblyName, platformName, componentName);
  }

  @Deprecated
  public List<CiResource> listPlatformComponentAttachments(String platformName, String componentName) throws OneOpsClientAPIException {
    return super.listPlatformComponentAttachments(assemblyName, platformName, componentName);
  }

  @Deprecated
  public CiResource getPlatformComponentAttachment(String platformName, String componentName, String attachmentName) throws OneOpsClientAPIException {
    return super.getPlatformComponentAttachment(assemblyName, platformName, componentName, attachmentName);
  }

  @Deprecated
  public CiResource updatePlatformComponentAttachment(String platformName, String componentName, String attachmentName, Map<String, String> attributes) throws OneOpsClientAPIException {
    return super.updatePlatformComponentAttachment(assemblyName, platformName, componentName, attachmentName, attributes);
  }

  @Deprecated
  public CiResource addNewAttachment(String platformName, String componentName, String uniqueName, Map<String, String> attributes) throws OneOpsClientAPIException {
    return super.addNewAttachment(assemblyName, platformName, componentName, uniqueName, attributes);
  }

  @Deprecated
  public List<CiResource> listPlatformVariables(String platformName) throws OneOpsClientAPIException {
    return super.listPlatformVariables(assemblyName, platformName);
  }

  @Deprecated
  public CiResource addPlatformVariable(String platformName, String variableName, String variableValue, boolean isSecure) throws OneOpsClientAPIException {
    return super.addPlatformVariable(assemblyName, platformName, variableName, variableValue, isSecure);
  }

  @Deprecated
  public Boolean updatePlatformVariable(String platformName, String variableName, String variableValue, boolean isSecure) throws OneOpsClientAPIException {
    return super.updatePlatformVariable(assemblyName, platformName, variableName, variableValue, isSecure);
  }

  @Deprecated
  public Boolean updateOrAddPlatformVariables(String platformName, String variableName, String variableValue, boolean isSecure) throws OneOpsClientAPIException {
    return super.updateOrAddPlatformVariables(assemblyName, platformName, variableName, variableValue, isSecure);
  }

  @Deprecated
  public CiResource deletePlatformVariable(String platformName, String variableName) throws OneOpsClientAPIException {
    return super.deletePlatformVariable(assemblyName, platformName, variableName);
  }

  @Deprecated
  public List<CiResource> listGlobalVariables() throws OneOpsClientAPIException {
    return super.listGlobalVariables(assemblyName);
  }

  @Deprecated
  public CiResource addGlobalVariable(String variableName, String variableValue, boolean isSecure) throws OneOpsClientAPIException {
    return super.addGlobalVariable(assemblyName, variableName, variableValue, isSecure);
  }

  @Deprecated
  public Boolean updateGlobalVariable(String variableName, String variableValue, boolean isSecure) throws OneOpsClientAPIException {
    return super.updateGlobalVariable(assemblyName, variableName, variableValue, isSecure);
  }

  @Deprecated
  public JsonPath extractYaml() throws OneOpsClientAPIException {
    return super.extractYaml(assemblyName);
  }

  @Deprecated
  public JsonPath loadFile(String filecontent) throws OneOpsClientAPIException {
    return super.loadFile(assemblyName, filecontent);
  }
}
