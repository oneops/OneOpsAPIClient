package com.oneops.api.resource;

import java.util.List;
import java.util.Map;

import com.oneops.api.APIClient;
import com.oneops.api.OOInstance;
import com.oneops.api.exception.OneOpsClientAPIException;
import com.oneops.api.resource.model.CiResource;

@Deprecated
public class Assembly extends APIClient {

  @Deprecated
  public Assembly(OOInstance instance) throws OneOpsClientAPIException {
    super(instance);
  }

  @Deprecated
  public CiResource getAssembly(String assemblyName) throws OneOpsClientAPIException {
    return super.getAssembly(assemblyName);
  }

  @Deprecated
  public List<CiResource> listAssemblies() throws OneOpsClientAPIException {
    return super.listAssemblies();
  }

  @Deprecated
  public CiResource createAssembly(String assemblyName, String ownerEmail, String comments, String description) throws OneOpsClientAPIException {
    return super.createAssembly(assemblyName, ownerEmail, comments, description);
  }

  @Deprecated
  public CiResource createAssembly(String assemblyName, String ownerEmail, String comments, String description, Map<String, String> tags) throws OneOpsClientAPIException {
    return super.createAssembly(assemblyName, ownerEmail, comments, description, tags);
  }

  @Deprecated
  public CiResource updateAssembly(String assemblyName, String ownerEmail, String description, Map<String, String> tags) throws OneOpsClientAPIException {
    return super.updateAssembly(assemblyName, ownerEmail, description, tags);
  }

  @Deprecated
  public CiResource deleteAssembly(String assemblyName) throws OneOpsClientAPIException {
    return super.deleteAssembly(assemblyName);
  }
}
