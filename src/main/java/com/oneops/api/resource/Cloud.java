package com.oneops.api.resource;

import java.util.List;

import com.oneops.api.APIClient;
import com.oneops.api.OOInstance;
import com.oneops.api.exception.OneOpsClientAPIException;
import com.oneops.api.resource.model.CiResource;

@Deprecated
public class Cloud extends APIClient {

  @Deprecated
  public Cloud(OOInstance instance) throws OneOpsClientAPIException {
    super(instance);
  }

  @Deprecated
  public CiResource getCloud(String cloudName) throws OneOpsClientAPIException {
    return super.getCloud(cloudName);
  }

  @Deprecated
  public List<CiResource> listClouds() throws OneOpsClientAPIException {
    return super.listClouds();
  }

  public List<CiResource> listCloudVariables(String cloudName) throws OneOpsClientAPIException {
    return super.listCloudVariables(cloudName);
  }
}
