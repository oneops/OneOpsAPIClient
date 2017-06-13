package com.oneops.api.resource;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oneops.api.APIClient;
import com.oneops.api.OOInstance;
import com.oneops.api.exception.OneOpsClientAPIException;
import com.oneops.api.resource.model.CiResource;
import com.oneops.api.resource.model.Deployment;
import com.oneops.api.resource.model.DeploymentRFC;
import com.oneops.api.resource.model.Log;
import com.oneops.api.resource.model.RedundancyConfig;
import com.oneops.api.resource.model.Release;

@Deprecated
public class Transition extends APIClient {

  private static final Logger LOG = LoggerFactory.getLogger(Transition.class);
  private String transitionEnvUri;
  private OOInstance instance;
  private final String assemblyName;

  public Transition(OOInstance instance, String assemblyName) throws OneOpsClientAPIException {
    super(instance);
    if (assemblyName == null || assemblyName.length() == 0) {
      String msg = "Missing assembly name";
      throw new OneOpsClientAPIException(msg);
    }
    this.assemblyName = assemblyName;
  }

  @Deprecated
  public CiResource getEnvironment(String environmentName) throws OneOpsClientAPIException {
    return super.getEnvironment(assemblyName, environmentName);
  }

  @Deprecated
  public List<CiResource> listEnvironments() throws OneOpsClientAPIException {
    return super.listEnvironments(assemblyName);
  }

  @Deprecated
  public CiResource createEnvironment(String environmentName, String envprofile, Map<String, String> attributes,
    Map<String, String> platformAvailability, Map<String, Map<String, String>> cloudMap, String description) throws OneOpsClientAPIException {
    return super.createEnvironment(assemblyName, environmentName, envprofile, attributes, platformAvailability, cloudMap, description);
  }

  @Deprecated
  public Release commitEnvironment(String environmentName, List<Long> excludePlatforms, String comment) throws OneOpsClientAPIException {
    return super.commitEnvironment(assemblyName, environmentName, excludePlatforms, comment);
  }

  @Deprecated
  public Deployment deploy(String environmentName, String comments) throws OneOpsClientAPIException {
    return super.deploy(assemblyName, environmentName, comments);
  }

  @Deprecated
  public Deployment getDeploymentStatus(String environmentName, Long deploymentId) throws OneOpsClientAPIException {
    return super.getDeploymentStatus(assemblyName, environmentName, deploymentId);
  }

  @Deprecated
  public Deployment getLatestDeployment(String environmentName) throws OneOpsClientAPIException {
    return super.getLatestDeployment(assemblyName, environmentName);
  }

  @Deprecated
  public Release discardDeploymentPlan(String environmentName) throws OneOpsClientAPIException {
    return super.discardDeploymentPlan(assemblyName, environmentName);
  }

  @Deprecated
  public Release discardOpenRelease(String environmentName) throws OneOpsClientAPIException {
    return super.discardOpenRelease(assemblyName, environmentName);
  }

  @Deprecated
  public CiResource disableAllPlatforms(String environmentName) throws OneOpsClientAPIException {
    return super.disableAllPlatforms(assemblyName, environmentName);
  }

  @Deprecated
  public Release getLatestRelease(String environmentName) throws OneOpsClientAPIException {
    return super.getLatestRelease(assemblyName, environmentName);
  }

  @Deprecated
  public Release getBomRelease(String environmentName) throws OneOpsClientAPIException {
    return super.getBomRelease(assemblyName, environmentName);
  }

  @Deprecated
  public Deployment cancelDeployment(String environmentName, Long deploymentId, Long releaseId) throws OneOpsClientAPIException {
    return super.cancelDeployment(assemblyName, environmentName, deploymentId, releaseId);
  }

  @Deprecated
  public DeploymentRFC getDeployment(String environmentName, Long deploymentId) throws OneOpsClientAPIException {
    return super.getDeployment(assemblyName, environmentName, deploymentId);
  }

  @Deprecated
  public Log getDeploymentRfcLog(String environmentName, Long deploymentId, Long rfcId) throws OneOpsClientAPIException {
    return super.getDeploymentRfcLog(assemblyName, environmentName, deploymentId, rfcId);
  }

  @Deprecated
  public Deployment approveDeployment(String environmentName, Long deploymentId, Long releaseId) throws OneOpsClientAPIException {
    return super.approveDeployment(assemblyName, environmentName, deploymentId, releaseId);
  }

  @Deprecated
  public Deployment retryDeployment(String environmentName, Long deploymentId, Long releaseId) throws OneOpsClientAPIException {
    return super.updateDeploymentStatus(assemblyName, environmentName, deploymentId, releaseId, "active");
  }

  @Deprecated
  private Deployment updateDeploymentStatus(String environmentName, Long deploymentId, Long releaseId, String newstate) throws OneOpsClientAPIException {
    return super.updateDeploymentStatus(assemblyName, environmentName, deploymentId, releaseId, newstate);
  }

  @Deprecated
  public CiResource deleteEnvironment(String environmentName) throws OneOpsClientAPIException {
    return super.deleteEnvironment(assemblyName, environmentName);
  }

  @Deprecated
  public List<CiResource> listPlatforms(String environmentName) throws OneOpsClientAPIException {
    return super.listPlatforms(assemblyName, environmentName);
  }

  @Deprecated
  public CiResource getPlatform(String environmentName, String platformName) throws OneOpsClientAPIException {
    return super.getPlatformComponent(assemblyName, environmentName, platformName);
  }

  @Deprecated
  public List<CiResource> listPlatformComponents(String environmentName, String platformName) throws OneOpsClientAPIException {
    return super.listPlatformComponents(assemblyName, environmentName, platformName);
  }

  @Deprecated
  public CiResource getPlatformComponent(String environmentName, String platformName, String componentName) throws OneOpsClientAPIException {
    return super.getPlatformComponent(assemblyName, environmentName, platformName, componentName);
  }

  @Deprecated
  public CiResource updatePlatformComponent(String environmentName, String platformName, String componentName, Map<String, String> attributes) throws OneOpsClientAPIException {
    return super.updatePlatformComponent(assemblyName, environmentName, platformName, componentName, attributes);
  }

  @Deprecated
  public CiResource touchPlatformComponent(String environmentName, String platformName, String componentName) throws OneOpsClientAPIException {
    return super.touchPlatformComponent(assemblyName, environmentName, platformName, componentName);
  }

  @Deprecated
  public CiResource pullDesign(String environmentName) throws OneOpsClientAPIException {
    return super.pullDesign(assemblyName, environmentName);
  }

  @Deprecated
  public CiResource pullNewPlatform(String environmentName, Map<String, String> platformAvailability) throws OneOpsClientAPIException {
    return super.pullNewPlatform(assemblyName, environmentName, platformAvailability);
  }

  @Deprecated
  public List<CiResource> listPlatformVariables(String environmentName, String platformName) throws OneOpsClientAPIException {
    return super.listPlatformVariables(assemblyName, platformName);
  }

  @Deprecated
  public Boolean updatePlatformVariable(String environmentName, String platformName, String variableName, String variableValue, boolean isSecure) throws OneOpsClientAPIException {
    return super.updatePlatformVariable(assemblyName, platformName, variableName, variableValue, isSecure);
  }

  @Deprecated
  public List<CiResource> listGlobalVariables(String environmentName) throws OneOpsClientAPIException {
    return super.listGlobalVariables(assemblyName, environmentName);
  }

  @Deprecated
  public Boolean updateGlobalVariable(String environmentName, String variableName, String variableValue, boolean isSecure) throws OneOpsClientAPIException {
    return super.updateGlobalVariable(assemblyName, environmentName, variableName, variableValue, isSecure);
  }

  @Deprecated
  public CiResource updateDisableEnvironment(String environmentName, List<String> platformIdList) throws OneOpsClientAPIException {
    return super.updateDisableEnvironment(assemblyName, environmentName, platformIdList);
  }

  @Deprecated
  public Boolean updatePlatformRedundancyConfig(String environmentName, String platformName, String componentName, RedundancyConfig config) throws OneOpsClientAPIException {
    return super.updatePlatformRedundancyConfig(assemblyName, environmentName, platformName, componentName, config);
  }

  @Deprecated
  public CiResource updatePlatformCloudScale(String environmentName, String platformName, String cloudId, Map<String, String> cloudMap) throws OneOpsClientAPIException {
    return super.updatePlatformCloudScale(assemblyName, environmentName, platformName, cloudId, cloudMap);
  }

  public List<CiResource> listRelays(String environmentName) throws OneOpsClientAPIException {
    return super.listRelays(assemblyName, environmentName);
  }

  @Deprecated
  public CiResource getRelay(String environmentName, String relayName) throws OneOpsClientAPIException {
    return super.getRelay(assemblyName, environmentName, relayName);
  }

  @Deprecated
  public CiResource addRelay(String environmentName, String relayName, String severity, String emails, String source, String nsPaths, String regex, boolean correlation)
    throws OneOpsClientAPIException {
    return super.addRelay(assemblyName, environmentName, relayName, severity, emails, source, nsPaths, regex, correlation);
  }

  @Deprecated
  public CiResource updateRelay(String environmentName, String relayName, String severity, String emails, String source, String nsPaths, String regex, boolean correlation, boolean enable)
    throws OneOpsClientAPIException {
    return super.updateRelay(assemblyName, environmentName, relayName, severity, emails, source, nsPaths, regex, correlation, enable);
  }
}
