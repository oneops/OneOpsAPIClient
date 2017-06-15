package com.oneops.api.resource;

import java.util.List;

import com.jayway.restassured.path.json.JsonPath;
import com.oneops.api.APIClient;
import com.oneops.api.OOInstance;
import com.oneops.api.exception.OneOpsClientAPIException;
import com.oneops.api.resource.model.Organization;

@Deprecated
public class Account extends APIClient {

  @Deprecated
  public Account(OOInstance instance) throws OneOpsClientAPIException {
    super(instance);
  }

  @Deprecated
  public List<Organization> listOrganizations() throws OneOpsClientAPIException {
    return super.listOrganizations();
  }

  @Deprecated
  public Organization getOrganization(String organizationName) throws OneOpsClientAPIException {
    return super.getOrganization(organizationName);
  }

  @Deprecated
  public Organization createOrganization(String organizationName) throws OneOpsClientAPIException {
    return super.createOrganization(organizationName);
  }

  @Deprecated
  public Organization deleteOrganization(String organizationName) throws OneOpsClientAPIException {
    return super.deleteOrganization(organizationName);
  }

  @Deprecated
  public JsonPath listEnvironmentProfiles() throws OneOpsClientAPIException {
    return super.listEnvironmentProfiles();
  }
}
