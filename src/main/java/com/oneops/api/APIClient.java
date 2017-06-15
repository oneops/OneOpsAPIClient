package com.oneops.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Uninterruptibles;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.SSLConfig;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.oneops.api.exception.OneOpsClientAPIException;
import com.oneops.api.resource.model.AttrProps;
import com.oneops.api.resource.model.CiAttributes;
import com.oneops.api.resource.model.CiResource;
import com.oneops.api.resource.model.Deployment;
import com.oneops.api.resource.model.DeploymentRFC;
import com.oneops.api.resource.model.Log;
import com.oneops.api.resource.model.Organization;
import com.oneops.api.resource.model.Procedure;
import com.oneops.api.resource.model.RedundancyConfig;
import com.oneops.api.resource.model.Release;
import com.oneops.api.util.IConstants;
import com.oneops.api.util.JsonUtil;

public abstract class APIClient {

  private final OOInstance instance;

  public APIClient(OOInstance instance) throws OneOpsClientAPIException {
    this.instance = instance;
    if (instance == null) {
      throw new OneOpsClientAPIException("Missing OneOps instance information to perform API invocation");
    }
    if (instance.getAuthtoken() == null) {
      throw new OneOpsClientAPIException("Missing OneOps authentication API key to perform API invocation");
    }
    if (instance.getEndpoint() == null) {
      throw new OneOpsClientAPIException("Missing OneOps endpoint to perform API invocation");
    }
  }

  protected RequestSpecification createRequest() {
    RequestSpecification rs = RestAssured.given();
    String basicAuth = "Basic " + new String(Base64.encodeBase64(instance.getAuthtoken().getBytes()));
    rs.header("Authorization", basicAuth);
    rs.header("User-Agent", "OneOpsAPIClient");
    rs.header("Accept", "application/json");
    rs.header("Content-Type", "application/json");
    String baseUri = instance.getEndpoint();
    if (instance.getOrgname() != null) {
      baseUri += instance.getOrgname();
    }
    rs.baseUri(baseUri);
    rs.config(RestAssured.config().sslConfig(new SSLConfig().relaxedHTTPSValidation()));
    return rs;
  }

  // Account

  /**
   * Lists all the organizations
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  public List<Organization> listOrganizations() throws OneOpsClientAPIException {
    RequestSpecification request = createRequest();
    Response response = request.get(IConstants.ACCOUNT_URI);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<Organization>>() {});
      } else {
        String msg = String.format("Failed to get list of organizations due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to get list of organizations due to null response";
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Fetches specific organization details
   * 
   * @param organizationName
   * @return
   * @throws OneOpsClientAPIException
   */
  public Organization getOrganization(String organizationName) throws OneOpsClientAPIException {
    if (organizationName == null || organizationName.length() == 0) {
      String msg = "Missing organization name to fetch details";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    Response response = request.get(IConstants.ACCOUNT_URI + organizationName);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(Organization.class);
      } else {
        String msg = String.format("Failed to get organization with name %s due to %s", organizationName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to get organization with name %s due to null response", organizationName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Creates organization for the given @organizationName
   * 
   * @param organizationName {mandatory} 
   * @return
   * @throws OneOpsClientAPIException
   */
  public Organization createOrganization(String organizationName) throws OneOpsClientAPIException {
    if (organizationName == null || organizationName.length() == 0) {
      String msg = "Missing organization name to create one";
      throw new OneOpsClientAPIException(msg);
    }
    RequestSpecification request = createRequest();
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("name", organizationName);
    Response response = request.body(jsonObject.toString()).post(IConstants.ACCOUNT_URI);

    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(Organization.class);
      } else {
        String msg = String.format("Failed to create organization with name %s due to %s", organizationName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to create organization with name %s due to null response", organizationName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Deletes the given organization
   * 
   * @param organizationName
   * @return
   * @throws OneOpsClientAPIException
   */
  public Organization deleteOrganization(String organizationName) throws OneOpsClientAPIException {
    if (organizationName == null || organizationName.length() == 0) {
      String msg = "Missing organization name to delete one";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    Organization org = getOrganization(organizationName);
    Long id = org.getId();
    if (id == null) {
      String msg = String.format("Failed to find organization with name %s to delete", organizationName);
      throw new OneOpsClientAPIException(msg);
    }

    Response response = request.delete(IConstants.ACCOUNT_URI + id);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(Organization.class);
      } else {
        String msg = String.format("Failed to delete organization with name %s due to %s", organizationName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to delete organization with name %s due to null response", organizationName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Lists all the Environment Profiles
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  public JsonPath listEnvironmentProfiles() throws OneOpsClientAPIException {
    RequestSpecification request = createRequest();
    Response response = request.get(IConstants.ORGANIZATION_URI + "environments");
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        System.out.println(response.getBody().asString());
        return response.getBody().jsonPath();
      } else {
        String msg = String.format("Failed to get list of Environment Profiles due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to get list of Environment Profiles due to null response";
    throw new OneOpsClientAPIException(msg);
  }

  // Assembly

  /**
   * Fetches specific assembly details
   * 
   * @param assemblyName
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource getAssembly(String assemblyName) throws OneOpsClientAPIException {
    if (assemblyName == null || assemblyName.length() == 0) {
      String msg = "Missing assembly name to fetch details";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    Response response = request.get(IConstants.ASSEMBLY_URI + assemblyName);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to get assembly with name %s due to %s", assemblyName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to get assembly with name %s due to null response", assemblyName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Lists all the assemblies
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  public List<CiResource> listAssemblies() throws OneOpsClientAPIException {
    RequestSpecification request = createRequest();
    Response response = request.get(IConstants.ASSEMBLY_URI);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<CiResource>>() {});
      } else {
        String msg = String.format("Failed to get list of assemblies due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to get list of assemblies due to null response";
    throw new OneOpsClientAPIException(msg);
  }


  /**
   * Creates assembly for the given @assemblyName
   * 
   * @param assemblyName {mandatory} 
   * @param ownerEmail a valid email address is {mandatory}
   * @param comments
   * @param description
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource createAssembly(String assemblyName, String ownerEmail, String comments, String description) throws OneOpsClientAPIException {
    return createAssembly(assemblyName, ownerEmail, comments, description, null);
  }

  /**
   * Creates assembly for the given @assemblyName
   * 
   * @param assemblyName {mandatory} 
   * @param ownerEmail a valid email address is {mandatory}
   * @param comments
   * @param description
   * @param tags
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource createAssembly(String assemblyName, String ownerEmail, String comments, String description, Map<String, String> tags) throws OneOpsClientAPIException {
    ResourceObject ro = new ResourceObject();
    Map<String, String> attributes = new HashMap<String, String>();
    Map<String, String> properties = new HashMap<String, String>();

    if (assemblyName != null && assemblyName.length() > 0) {
      properties.put("ciName", assemblyName);
    } else {
      String msg = "Missing assembly name to create one";
      throw new OneOpsClientAPIException(msg);
    }

    properties.put("comments", comments);
    ro.setProperties(properties);

    if (ownerEmail != null && ownerEmail.length() > 0) {
      attributes.put("owner", ownerEmail);
    } else {
      String msg = "Missing assembly owner email address";
      throw new OneOpsClientAPIException(msg);
    }

    attributes.put("description", description);

    if (tags != null && !tags.isEmpty()) {
      attributes.put("tags", JSONObject.valueToString(tags));
    }

    ro.setAttributes(attributes);

    RequestSpecification request = createRequest();
    JSONObject jsonObject = JsonUtil.createJsonObject(ro, "cms_ci");

    Response response = request.body(jsonObject.toString()).post(IConstants.ASSEMBLY_URI);

    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to create assembly with name %s due to %s", assemblyName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to create assembly with name %s due to null response", assemblyName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Updates assembly for the given @assemblyName
   * 
   * @param assemblyName {mandatory} 
   * @param ownerEmail a valid email address is {mandatory}
   * @param description
   * @param tags
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource updateAssembly(String assemblyName, String ownerEmail, String description, Map<String, String> tags) throws OneOpsClientAPIException {
    ResourceObject ro = new ResourceObject();
    Map<String, String> attributes = new HashMap<String, String>();
    Map<String, String> properties = new HashMap<String, String>();

    if (assemblyName != null && assemblyName.length() > 0) {
      properties.put("ciName", assemblyName);
    } else {
      String msg = "Missing assembly name to update one";
      throw new OneOpsClientAPIException(msg);
    }

    CiResource assembly = getAssembly(assemblyName);

    properties.put("ciId", String.valueOf(assembly.getCiId()));
    ro.setProperties(properties);

    if (ownerEmail != null && ownerEmail.length() > 0) {
      attributes.put("owner", ownerEmail);
    }

    if (description != null && description.length() > 0) {
      attributes.put("description", description);
    }

    if (tags != null && !tags.isEmpty()) {
      attributes.put("tags", JSONObject.valueToString(tags));
    }

    ro.setAttributes(attributes);

    RequestSpecification request = createRequest();
    JSONObject jsonObject = JsonUtil.createJsonObject(ro, "cms_ci");

    Response response = request.body(jsonObject.toString()).put(IConstants.ASSEMBLY_URI + assemblyName);

    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to update assembly with name %s due to %s", assemblyName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to update assembly with name %s due to null response", assemblyName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Deletes the given assembly
   * 
   * @param assemblyName
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource deleteAssembly(String assemblyName) throws OneOpsClientAPIException {
    if (assemblyName == null || assemblyName.length() == 0) {
      String msg = "Missing assembly name to delete one";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    Response response = request.delete(IConstants.ASSEMBLY_URI + assemblyName);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to delete assembly with name %s due to %s", assemblyName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to delete assembly with name %s due to null response", assemblyName);
    throw new OneOpsClientAPIException(msg);
  }

  // Cloud

  /**
   * Fetches specific cloud details
   * 
   * @param cloudName
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource getCloud(String cloudName) throws OneOpsClientAPIException {
    if (cloudName == null || cloudName.length() == 0) {
      String msg = "Missing cloud name to fetch details";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    Response response = request.get(IConstants.CLOUDS_URI + cloudName);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to get cloud with name %s due to %s", cloudName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to get cloud with name %s due to null response", cloudName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Lists all the clouds
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  public List<CiResource> listClouds() throws OneOpsClientAPIException {
    RequestSpecification request = createRequest();
    Response response = request.get(IConstants.CLOUDS_URI);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<CiResource>>() {});
      } else {
        String msg = String.format("Failed to get list of clouds due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to get list of clouds due to null response";
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Lists cloud variables given the cloudName
   *
   * @param cloudName
   * @return
   * @throws OneOpsClientAPIException
   */
  public List<CiResource> listCloudVariables(String cloudName) throws OneOpsClientAPIException {
    if (cloudName == null || cloudName.length() == 0) {
      String msg = "Missing cloud name to list cloud variables";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    Response response = request.get(IConstants.CLOUDS_URI + cloudName + IConstants.VARIABLES_URI);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<CiResource>>() {});
      } else {
        String msg = String.format("Failed to get list of cloud variables due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to get list of cloud variables due to null response";
    throw new OneOpsClientAPIException(msg);
  }  
  
  // Design

  /**
   * Fetches specific platform details
   * 
   * @param platformName
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource getPlatform(String assemblyName, String platformName) throws OneOpsClientAPIException {
    if (platformName == null || platformName.length() == 0) {
      String msg = "Missing platform name to fetch details";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    Response response = request.get(designURI(assemblyName) + IConstants.PLATFORM_URI + platformName);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to get platform with name %s due to %s", platformName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to get platform with name %s due to null response", platformName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Lists all the platforms
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  public List<CiResource> listPlatforms(String assemblyName) throws OneOpsClientAPIException {
    RequestSpecification request = createRequest();
    Response response = request.get(designURI(assemblyName) + IConstants.PLATFORM_URI);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<CiResource>>() {});
      } else {
        String msg = String.format("Failed to get list of platforms due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to get list of platforms due to null response";
    throw new OneOpsClientAPIException(msg);
  }


  /**
   * Creates platform within the given assembly
   * 
   * @param platformName {mandatory}
   * @param packname {mandatory}
   * @param packversion {mandatory}
   * @param packsource {mandatory}
   * @param comments
   * @param description
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource createPlatform(String assemblyName, String platformName, String packname, String packversion,
    String packsource, String comments, String description) throws OneOpsClientAPIException {

    ResourceObject ro = new ResourceObject();
    Map<String, String> attributes = new HashMap<String, String>();
    Map<String, String> properties = new HashMap<String, String>();

    if (platformName != null && platformName.length() > 0) {
      properties.put("ciName", platformName);
    } else {
      String msg = "Missing platform name to create platform";
      throw new OneOpsClientAPIException(msg);
    }

    if (packname != null && packname.length() > 0) {
      attributes.put("pack", packname);
    } else {
      String msg = "Missing pack name to create platform";
      throw new OneOpsClientAPIException(msg);
    }

    if (packversion != null && packversion.length() > 0) {
      attributes.put("version", packversion);
    } else {
      String msg = "Missing pack version to create platform";
      throw new OneOpsClientAPIException(msg);
    }

    if (packsource != null && packsource.length() > 0) {
      attributes.put("source", packsource);
    } else {
      String msg = "Missing platform name to create platform";
      throw new OneOpsClientAPIException(msg);
    }
    attributes.put("major_version", "1");

    properties.put("comments", comments);
    ro.setProperties(properties);

    attributes.put("description", description);
    ro.setAttributes(attributes);

    Map<String, String> ownerProps = Maps.newHashMap();
    ownerProps.put("description", "");
    ro.setOwnerProps(ownerProps);
    RequestSpecification request = createRequest();
    JSONObject jsonObject = JsonUtil.createJsonObject(ro, "cms_dj_ci");
    Response response = request.body(jsonObject.toString()).post(designURI(assemblyName) + IConstants.PLATFORM_URI);

    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to create platform with name %s due to %s", platformName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to create platform with name %s due to null response", platformName);
    throw new OneOpsClientAPIException(msg);
  }



  /**
   * Commits design open releases
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  public Release commitDesign(String assemblyName) throws OneOpsClientAPIException {
    RequestSpecification request = createRequest();
    Response response = request.get(designReleaseURI(assemblyName) + "latest");
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {

        String releaseState = response.getBody().jsonPath().get("releaseState");
        if ("open".equals(releaseState)) {
          int releaseId = response.getBody().jsonPath().get("releaseId");
          response = request.post(designReleaseURI(assemblyName) + releaseId + "/commit");
          if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
            return response.getBody().as(Release.class);
          } else {
            String msg = String.format("Failed to commit design due to %s", response.getStatusLine());
            throw new OneOpsClientAPIException(msg);
          }
        } else {
          String msg = String.format("No open release found to perform design commit");
          throw new OneOpsClientAPIException(msg);
        }

      } else {
        String msg = String.format("Failed to get latest release details due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to commit design due to null response";
    throw new OneOpsClientAPIException(msg);
  }


  /**
   * Commits specific platform with open release
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  public JsonPath commitPlatform(String assemblyName, String platformName) throws OneOpsClientAPIException {

    RequestSpecification request = createRequest();
    CiResource platform = getPlatform(assemblyName, platformName);
    if (platform != null) {
      Long platformId = platform.getCiId();
      Response response = request.post(designURI(assemblyName) + IConstants.PLATFORM_URI + platformId + "/commit");
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().jsonPath();
      } else {
        String msg = String.format("Failed to commit %s platform due to %s", platformName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }

    }
    String msg = String.format("Failed to commit %s due to null response", platformName);
    throw new OneOpsClientAPIException(msg);
  }

  public CiResource updatePlatformLinks(String assemblyName, String fromPlatformName, List<String> toPlatformNames) throws OneOpsClientAPIException {
    if (fromPlatformName == null || fromPlatformName.length() == 0) {
      String msg = "Missing from platform name to link";
      throw new OneOpsClientAPIException(msg);
    }
    if (toPlatformNames == null || toPlatformNames.size() == 0) {
      String msg = "Missing to platform name to link";
      throw new OneOpsClientAPIException(msg);
    }

    List<Long> toIds = new ArrayList<Long>();
    for (String toPlatformName : toPlatformNames) {
      CiResource toPlatform = getPlatform(assemblyName, toPlatformName);
      toIds.add(toPlatform.getCiId());
    }

    CiResource fromPlatform = getPlatform(assemblyName, fromPlatformName);

    ResourceObject ro = new ResourceObject();
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("ciId", fromPlatform.getCiId() + "");
    ro.setProperties(properties);
    JSONObject jsonObject = JsonUtil.createJsonObject(ro, "cms_dj_ci");
    jsonObject.put("links_to", toIds);

    RequestSpecification request = createRequest();
    Response response = request.body(jsonObject.toString()).put(designURI(assemblyName) + IConstants.PLATFORM_URI + fromPlatform.getCiId());

    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to update platform link to %s from %s due to %s response", toPlatformNames, fromPlatformName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }

    String msg = String.format("Failed to update platform link to %s from %s due to null response", toPlatformNames, fromPlatformName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Deletes the given platform
   * 
   * @param platformName
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource deletePlatform(String assemblyName, String platformName) throws OneOpsClientAPIException {
    if (platformName == null || platformName.length() == 0) {
      String msg = "Missing platform name to delete";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    Response response = request.delete(designURI(assemblyName) + IConstants.PLATFORM_URI + platformName);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to delete platform with name %s due to %s", platformName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to delete platform with name %s due to null response", platformName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * List platform components for a given assembly/design/platform
   * 
   * @param environmentName
   * @param platformName
   * @return
   * @throws OneOpsClientAPIException
   */
  public List<CiResource> listPlatformComponents(String assemblyName, String platformName) throws OneOpsClientAPIException {
    if (platformName == null || platformName.length() == 0) {
      String msg = "Missing platform name to list enviornment platform components";
      throw new OneOpsClientAPIException(msg);
    }
    RequestSpecification request = createRequest();
    Response response = request.get(designURI(assemblyName) + IConstants.PLATFORM_URI + platformName + IConstants.COMPONENT_URI);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<CiResource>>() {});
      } else {
        String msg = String.format("Failed to get list of platforms components due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to get list of platforms components due to null response";
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Get platform component details for a given assembly/design/platform
   * 
   * @param platformName
   * @param componentName
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource getPlatformComponent(String assemblyName, String platformName, String componentName) throws OneOpsClientAPIException {
    if (platformName == null || platformName.length() == 0) {
      String msg = "Missing platform name to get platform component details";
      throw new OneOpsClientAPIException(msg);
    }
    if (componentName == null || componentName.length() == 0) {
      String msg = "Missing component name to get platform component details";
      throw new OneOpsClientAPIException(msg);
    }
    RequestSpecification request = createRequest();
    Response response = request.get(designURI(assemblyName) + IConstants.PLATFORM_URI + platformName + IConstants.COMPONENT_URI + componentName);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to get platform component details due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to get platform component details due to null response";
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Add component to a given assembly/design/platform
   * 
   * @param platformName
   * @param componentName
   * @param attributes
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource addPlatformComponent(String assemblyName, String platformName, String componentName, String uniqueName, Map<String, String> attributes) throws OneOpsClientAPIException {
    if (platformName == null || platformName.length() == 0) {
      String msg = "Missing platform name to add component";
      throw new OneOpsClientAPIException(msg);
    }
    if (componentName == null || componentName.length() == 0) {
      String msg = "Missing component name to add component";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();

    Response newComponentResponse = request.queryParam("template_name", componentName).get(designURI(assemblyName) + IConstants.PLATFORM_URI + platformName + IConstants.COMPONENT_URI + "new.json");
    if (newComponentResponse != null) {
      ResourceObject ro = new ResourceObject();
      Map<String, String> properties = Maps.newHashMap();
      properties.put("ciName", uniqueName);
      properties.put("rfcAction", "add");

      JsonPath componentDetails = newComponentResponse.getBody().jsonPath();
      Map<String, String> attr = componentDetails.getMap("ciAttributes");
      if (attr == null) {
        attr = Maps.newHashMap();
      }
      if (attributes != null && attributes.size() > 0) {
        attr.putAll(attributes);

        Map<String, String> ownerProps = componentDetails.getMap("ciAttrProps.owner");
        if (ownerProps == null) {
          ownerProps = Maps.newHashMap();
        }
        for (Entry<String, String> entry : attributes.entrySet()) {
          ownerProps.put(entry.getKey(), "design");
        }
        ro.setOwnerProps(ownerProps);
      }
      ro.setAttributes(attr);
      ro.setProperties(properties);
      JSONObject jsonObject = JsonUtil.createJsonObject(ro, "cms_dj_ci");
      jsonObject.put("template_name", componentName);
      Response response = request.body(jsonObject.toString()).post(designURI(assemblyName) + IConstants.PLATFORM_URI + platformName + IConstants.COMPONENT_URI);
      if (response != null) {
        if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
          return response.getBody().as(CiResource.class);
        } else {
          String msg = String.format("Failed to get add component %s due to %s", componentName, response.getStatusLine());
          throw new OneOpsClientAPIException(msg);
        }
      }
    }

    String msg = String.format("Failed to get add component %s due to null response", componentName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Update component attributes for a given assembly/design/platform/component
   * 
   * @param platformName
   * @param componentName
   * @param attributes
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource updatePlatformComponent(String assemblyName, String platformName, String componentName, Map<String, String> attributes) throws OneOpsClientAPIException {
    if (platformName == null || platformName.length() == 0) {
      String msg = "Missing platform name to update component attributes";
      throw new OneOpsClientAPIException(msg);
    }
    if (componentName == null || componentName.length() == 0) {
      String msg = "Missing component name to update component attributes";
      throw new OneOpsClientAPIException(msg);
    }
    if (attributes == null || attributes.size() == 0) {
      String msg = "Missing attributes list to be updated";
      throw new OneOpsClientAPIException(msg);
    }

    CiResource componentDetails = getPlatformComponent(assemblyName, platformName, componentName);
    if (componentDetails != null) {
      ResourceObject ro = new ResourceObject();

      Long ciId = componentDetails.getCiId();
      RequestSpecification request = createRequest();

      Map<String, String> attr = Maps.newHashMap();
      //Add ciAttributes to be updated
      if (attributes != null && attributes.size() > 0)
        attr.putAll(attributes);

      Map<String, String> ownerProps = Maps.newHashMap();
      //Add existing attrProps to retain locking of attributes 
      AttrProps attrProps = componentDetails.getAttrProps();
      if (attrProps != null && attrProps.getAdditionalProperties() != null && attrProps.getAdditionalProperties().size() > 0 && attrProps.getAdditionalProperties().get("owner") != null) {
        Map<String, String> ownersMap = (Map<String, String>) attrProps.getAdditionalProperties().get("owner");
        for (Entry<String, String> entry : ownersMap.entrySet()) {
          ownerProps.put(entry.getKey(), entry.getValue());
        }
      }

      //Add updated attributes to attrProps to lock them
      for (Entry<String, String> entry : attributes.entrySet()) {
        ownerProps.put(entry.getKey(), "design");
      }

      ro.setOwnerProps(ownerProps);
      ro.setAttributes(attr);
      JSONObject jsonObject = JsonUtil.createJsonObject(ro, "cms_dj_ci");
      Response response = request.body(jsonObject.toString()).put(designURI(assemblyName) + IConstants.PLATFORM_URI + platformName + IConstants.COMPONENT_URI + ciId);
      if (response != null) {
        if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
          return response.getBody().as(CiResource.class);
        } else {
          String msg = String.format("Failed to get update component %s due to %s", componentName, response.getStatusLine());
          throw new OneOpsClientAPIException(msg);
        }
      }
    }
    String msg = String.format("Failed to get update component %s due to null response", componentName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Deletes the given platform component
   * 
   * @param platformName
   * @param componentName
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource deletePlatformComponent(String assemblyName, String platformName, String componentName) throws OneOpsClientAPIException {
    if (platformName == null || platformName.length() == 0) {
      String msg = "Missing platform name to delete component";
      throw new OneOpsClientAPIException(msg);
    }
    if (componentName == null || componentName.length() == 0) {
      String msg = "Missing component name to delete it";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    Response response = request.delete(designURI(assemblyName) + IConstants.PLATFORM_URI + platformName + IConstants.COMPONENT_URI + componentName);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to delete platform with name %s due to %s", platformName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to delete platform with name %s due to null response", platformName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * List attachments for a given assembly/design/platform/component
   * 
   * @param platformName
   * @return
   * @throws OneOpsClientAPIException
   */
  public List<CiResource> listPlatformComponentAttachments(String assemblyName, String platformName, String componentName) throws OneOpsClientAPIException {
    if (platformName == null || platformName.length() == 0) {
      String msg = "Missing platform name to list platform attachments";
      throw new OneOpsClientAPIException(msg);
    }
    RequestSpecification request = createRequest();
    Response response = request.get(designURI(assemblyName) + IConstants.PLATFORM_URI + platformName + IConstants.COMPONENT_URI + componentName + IConstants.ATTACHMENTS_URI);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<CiResource>>() {});
      } else {
        String msg = String.format("Failed to get list of design platforms attachments due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to get list of design platforms attachments due to null response";
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Get platform component attachment details 
   * 
   * @param platformName
   * @param componentName
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource getPlatformComponentAttachment(String assemblyName, String platformName, String componentName, String attachmentName) throws OneOpsClientAPIException {
    if (platformName == null || platformName.length() == 0) {
      String msg = "Missing platform name to get platform component attachment details";
      throw new OneOpsClientAPIException(msg);
    }
    if (componentName == null || componentName.length() == 0) {
      String msg = "Missing component name to get platform component attachment details";
      throw new OneOpsClientAPIException(msg);
    }
    RequestSpecification request = createRequest();
    Response response = request.get(designURI(assemblyName) + IConstants.PLATFORM_URI + platformName + IConstants.COMPONENT_URI + componentName + IConstants.ATTACHMENTS_URI + attachmentName);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to get platform component attachment details due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to get platform component attachment details due to null response";
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Update attachment
   * 
   * @param platformName
   * @param componentName
   * @param attachmentName
   * @param attributes
   * 
   *  Sample request for new attachment attributes
   *  attributes.put("content", "content");
    attributes.put("source", "source");
    attributes.put("path", "/tmp/my.sh");
    attributes.put("exec_cmd", "exec_cmd");
    attributes.put("run_on", "after-add,after-replace,after-update");
    attributes.put("run_on_action", "[\"after-restart\"]");
    
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource updatePlatformComponentAttachment(String assemblyName, String platformName, String componentName, String attachmentName, Map<String, String> attributes)
    throws OneOpsClientAPIException {
    if (platformName == null || platformName.length() == 0) {
      String msg = "Missing platform name to update attachment attributes";
      throw new OneOpsClientAPIException(msg);
    }
    if (componentName == null || componentName.length() == 0) {
      String msg = "Missing component name to update attachment attributes";
      throw new OneOpsClientAPIException(msg);
    }
    if (attachmentName == null || attachmentName.length() == 0) {
      String msg = "Missing attachment name to update attachment attributes";
      throw new OneOpsClientAPIException(msg);
    }
    if (attributes == null || attributes.size() == 0) {
      //nothing to be updated
      return null;
    }

    CiResource attachmentDetails = getPlatformComponentAttachment(assemblyName, platformName, componentName, attachmentName);
    if (attachmentDetails != null) {
      ResourceObject ro = new ResourceObject();

      Long ciId = attachmentDetails.getCiId();
      RequestSpecification request = createRequest();

      //Add existing ciAttributes 
      CiAttributes ciAttributes = attachmentDetails.getCiAttributes();
      Map<String, String> attr = Maps.newHashMap();
      if (ciAttributes != null && ciAttributes.getAdditionalProperties() != null && ciAttributes.getAdditionalProperties().size() > 0) {
        for (Entry<String, Object> entry : ciAttributes.getAdditionalProperties().entrySet()) {
          attr.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
      }

      //Add ciAttributes to be updated
      if (attributes != null && attributes.size() > 0)
        attr.putAll(attributes);

      Map<String, String> ownerProps = Maps.newHashMap();
      //Add existing attrProps to retain locking of attributes 
      AttrProps attrProps = attachmentDetails.getAttrProps();
      if (attrProps != null && attrProps.getAdditionalProperties() != null && attrProps.getAdditionalProperties().size() > 0 && attrProps.getAdditionalProperties().get("owner") != null) {
        Map<String, String> ownersMap = (Map<String, String>) attrProps.getAdditionalProperties().get("owner");
        for (Entry<String, String> entry : ownersMap.entrySet()) {
          ownerProps.put(entry.getKey(), entry.getValue());
        }
      }

      //Add updated attributes to attrProps to lock them
      for (Entry<String, String> entry : attributes.entrySet()) {
        ownerProps.put(entry.getKey(), "design");
      }

      ro.setOwnerProps(ownerProps);
      ro.setAttributes(attr);
      JSONObject jsonObject = JsonUtil.createJsonObject(ro, "cms_dj_ci");
      Response response = request.body(jsonObject.toString()).put(designURI(assemblyName) + IConstants.PLATFORM_URI + platformName
        + IConstants.COMPONENT_URI + componentName + IConstants.ATTACHMENTS_URI + ciId);
      if (response != null) {
        if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
          return response.getBody().as(CiResource.class);
        } else {
          String msg = String.format("Failed to get update attachment %s due to %s", componentName, response.getStatusLine());
          throw new OneOpsClientAPIException(msg);
        }
      }
    }
    String msg = String.format("Failed to get update attachment %s due to null response", componentName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Add attachment to a given assembly/design/platform/component
   * 
   * @param platformName
   * @param componentName
   * @param attributes
   * 
   * Sample request for new attachment attributes
   *  attributes.put("content", "content");
    attributes.put("source", "source");
    attributes.put("path", "/tmp/my.sh");
    attributes.put("exec_cmd", "exec_cmd");
    attributes.put("run_on", "after-add,after-replace,after-update");
    attributes.put("run_on_action", "[\"after-restart\"]");
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource addNewAttachment(String assemblyName, String platformName, String componentName, String uniqueName, Map<String, String> attributes) throws OneOpsClientAPIException {
    RequestSpecification request = createRequest();

    Response newAttachmentResponse = request.get(designURI(assemblyName) + IConstants.PLATFORM_URI + platformName + IConstants.COMPONENT_URI + componentName + IConstants.ATTACHMENTS_URI + "new.json");
    if (newAttachmentResponse != null) {
      ResourceObject ro = new ResourceObject();
      Map<String, String> properties = Maps.newHashMap();
      properties.put("ciName", uniqueName);
      properties.put("rfcAction", "add");

      JsonPath attachmentDetails = newAttachmentResponse.getBody().jsonPath();
      Map<String, String> attr = attachmentDetails.getMap("ciAttributes");
      if (attr == null) {
        attr = Maps.newHashMap();
      }
      if (attributes != null && attributes.size() > 0) {
        attr.putAll(attributes);

        Map<String, String> ownerProps = attachmentDetails.getMap("ciAttrProps.owner");
        if (ownerProps == null) {
          ownerProps = Maps.newHashMap();
        }
        for (Entry<String, String> entry : attributes.entrySet()) {
          ownerProps.put(entry.getKey(), "design");
        }
        ro.setOwnerProps(ownerProps);
      }
      ro.setAttributes(attr);
      ro.setProperties(properties);
      JSONObject jsonObject = JsonUtil.createJsonObject(ro, "cms_dj_ci");
      Response response =
        request.body(jsonObject.toString()).post(designURI(assemblyName) + IConstants.PLATFORM_URI + platformName + IConstants.COMPONENT_URI + componentName + IConstants.ATTACHMENTS_URI);
      if (response != null) {
        if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
          return response.getBody().as(CiResource.class);
        } else {
          String msg = String.format("Failed to get add attachment to %s due to %s", componentName, response.getStatusLine());
          throw new OneOpsClientAPIException(msg);
        }
      }
    }

    String msg = String.format("Failed to get add attachment to %s due to null response", componentName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * List local variables for a given assembly/design/platform
   * 
   * @param platformName
   * @return
   * @throws OneOpsClientAPIException
   */
  public List<CiResource> listPlatformVariables(String assemblyName, String platformName) throws OneOpsClientAPIException {
    if (platformName == null || platformName.length() == 0) {
      String msg = "Missing platform name to list platform variables";
      throw new OneOpsClientAPIException(msg);
    }
    RequestSpecification request = createRequest();
    Response response = request.get(designURI(assemblyName) + IConstants.PLATFORM_URI + platformName + IConstants.VARIABLES_URI);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<CiResource>>() {});
      } else {
        String msg = String.format("Failed to get list of design platforms variables due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to get list of design platforms variables due to null response";
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Add platform variables for a given assembly/design
   * 
   * @param environmentName
   * @param variables
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource addPlatformVariable(String assemblyName, String platformName, String variableName, String variableValue, boolean isSecure) throws OneOpsClientAPIException {
    if (platformName == null || platformName.length() == 0) {
      String msg = "Missing platform name to add variables";
      throw new OneOpsClientAPIException(msg);
    }
    if (variableName == null) {
      String msg = "Missing variable name to be added";
      throw new OneOpsClientAPIException(msg);
    }

    if (variableValue == null) {
      String msg = "Missing variable value to be added";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();

    Response variable = request.get(designURI(assemblyName) + IConstants.PLATFORM_URI + platformName + IConstants.VARIABLES_URI + variableName);
    if (variable != null && variable.getBody() != null && variable.getBody().jsonPath().getString("ciId") != null) {
      String msg = String.format("Global variables %s already exists", variableName);
      throw new OneOpsClientAPIException(msg);
    }
    ResourceObject ro = new ResourceObject();
    Response newVarResponse = request.get(designURI(assemblyName) + IConstants.PLATFORM_URI + platformName + IConstants.VARIABLES_URI + "new.json");
    if (newVarResponse != null) {
      JsonPath newVarJsonPath = newVarResponse.getBody().jsonPath();
      if (newVarJsonPath != null) {
        Map<String, String> attr = newVarJsonPath.getMap("ciAttributes");
        Map<String, String> properties = Maps.newHashMap();
        if (attr == null) {
          attr = Maps.newHashMap();
        }
        if (isSecure) {
          attr.put("secure", "true");
          attr.put("encrypted_value", variableValue);
        } else {
          attr.put("secure", "false");
          attr.put("value", variableValue);
        }

        properties.put("ciName", variableName);
        ro.setProperties(properties);
        ro.setAttributes(attr);
      }
    }

    JSONObject jsonObject = JsonUtil.createJsonObject(ro, "cms_dj_ci");

    Response response = request.body(jsonObject.toString()).post(designURI(assemblyName) + IConstants.PLATFORM_URI + platformName + IConstants.VARIABLES_URI);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to add platform variable %s due to %s", variableName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }

    String msg = String.format("Failed to add new variables %s due to null response", variableName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Update platform local variables for a given assembly/design/platform
   * 
   * @param platformName
   * @param variables
   * @return
   * @throws OneOpsClientAPIException
   */
  public Boolean updatePlatformVariable(String assemblyName, String platformName, String variableName, String variableValue, boolean isSecure) throws OneOpsClientAPIException {
    if (platformName == null || platformName.length() == 0) {
      String msg = "Missing platform name to update variables";
      throw new OneOpsClientAPIException(msg);
    }
    if (variableName == null) {
      String msg = "Missing variable name to be added";
      throw new OneOpsClientAPIException(msg);
    }

    if (variableValue == null) {
      String msg = "Missing variable value to be added";
      throw new OneOpsClientAPIException(msg);
    }

    Boolean success = false;
    RequestSpecification request = createRequest();

    Response variable = request.get(designURI(assemblyName) + IConstants.PLATFORM_URI + platformName + IConstants.VARIABLES_URI + variableName);
    if (variable == null || variable.getStatusCode() != 200 || variable.getBody() == null) {
      String msg = String.format("Failed to find local variables %s for platform %s", variableName, platformName);
      throw new OneOpsClientAPIException(msg);
    }

    JsonPath variableDetails = variable.getBody().jsonPath();
    String ciId = variableDetails.getString("ciId");
    Map<String, String> attr = variableDetails.getMap("ciAttributes");
    if (attr == null) {
      attr = new HashMap<String, String>();
    }

    if (isSecure) {
      attr.put("secure", "true");
      attr.put("encrypted_value", variableValue);
    } else {
      attr.put("secure", "false");
      attr.put("value", variableValue);
    }

    ResourceObject ro = new ResourceObject();
    ro.setAttributes(attr);

    JSONObject jsonObject = JsonUtil.createJsonObject(ro, "cms_dj_ci");

    Response response = request.body(jsonObject.toString()).put(designURI(assemblyName) + IConstants.PLATFORM_URI + platformName + IConstants.VARIABLES_URI + ciId);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        //return response.getBody().jsonPath();
        success = true;
      } else {
        String msg = String.format("Failed to get update variables %s due to %s", variableName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }

    return success;
  }

  /**
   * Update platform local variables for a given assembly/design/platform
   * 
   * @param platformName
   * @param variables
   * @return
   * @throws OneOpsClientAPIException
   */
  public Boolean updateOrAddPlatformVariables(String assemblyName, String platformName, String variableName, String variableValue, boolean isSecure) throws OneOpsClientAPIException {
    if (platformName == null || platformName.length() == 0) {
      String msg = "Missing platform name to update variables";
      throw new OneOpsClientAPIException(msg);
    }
    if (variableName == null) {
      String msg = "Missing variable name to be added";
      throw new OneOpsClientAPIException(msg);
    }

    if (variableValue == null) {
      String msg = "Missing variable value to be added";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();

    Response variable = request.get(designURI(assemblyName) + IConstants.PLATFORM_URI + platformName + IConstants.VARIABLES_URI + variableName);
    if (variable == null || variable.getStatusCode() != 200 || variable.getBody() == null) {
      return addPlatformVariable(assemblyName, platformName, variableName, variableValue, isSecure) == null ? false : true;
    } else {
      return updatePlatformVariable(assemblyName, platformName, variableName, variableValue, isSecure) == null ? false : true;
    }
  }

  /**
   * Deletes the given platform variable
   * 
   * @param platformName
   * @param variableName
   * @return
   * @throws OneOpsClientAPIException
   */

  public CiResource deletePlatformVariable(String assemblyName, String platformName, String variableName) throws OneOpsClientAPIException {
    if (platformName == null || platformName.length() == 0) {
      String msg = "Missing platform name to delete variable";
      throw new OneOpsClientAPIException(msg);
    }
    if (variableName == null || variableName.length() == 0) {
      String msg = "Missing variable name to delete it";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    Response response = request.delete(designURI(assemblyName) + IConstants.PLATFORM_URI + platformName + IConstants.VARIABLES_URI + variableName);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to delete platform with name %s due to %s", platformName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to delete platform with name %s due to null response", platformName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * List global variables for a given assembly/design
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  public List<CiResource> listGlobalVariables(String assemblyName) throws OneOpsClientAPIException {
    RequestSpecification request = createRequest();
    Response response = request.get(designURI(assemblyName) + IConstants.VARIABLES_URI);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<CiResource>>() {});
      } else {
        String msg = String.format("Failed to get list of design variables due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to get list of design variables due to null response";
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Add global variables for a given assembly/design
   * 
   * @param environmentName
   * @param variables
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource addGlobalVariable(String assemblyName, String variableName, String variableValue, boolean isSecure) throws OneOpsClientAPIException {
    if (variableName == null) {
      String msg = "Missing variable name to be added";
      throw new OneOpsClientAPIException(msg);
    }

    if (variableValue == null) {
      String msg = "Missing variable value to be added";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();

    Response variable = request.get(designURI(assemblyName) + IConstants.VARIABLES_URI + variableName);
    if (variable != null && variable.getBody() != null && variable.getBody().jsonPath().getString("ciId") != null) {
      String msg = String.format("Global variables %s already exists", variableName);
      throw new OneOpsClientAPIException(msg);
    }
    ResourceObject ro = new ResourceObject();
    Response newVarResponse = request.get(designURI(assemblyName) + IConstants.VARIABLES_URI + "new.json");
    if (newVarResponse != null) {
      JsonPath newVarJsonPath = newVarResponse.getBody().jsonPath();
      if (newVarJsonPath != null) {
        Map<String, String> attr = newVarJsonPath.getMap("ciAttributes");
        Map<String, String> properties = Maps.newHashMap();
        if (attr == null) {
          attr = Maps.newHashMap();
        }
        if (isSecure) {
          attr.put("secure", "true");
          attr.put("encrypted_value", variableValue);
        } else {
          attr.put("secure", "false");
          attr.put("value", variableValue);
        }

        properties.put("ciName", variableName);
        ro.setProperties(properties);
        ro.setAttributes(attr);
      }
    }

    JSONObject jsonObject = JsonUtil.createJsonObject(ro, "cms_dj_ci");

    Response response = request.body(jsonObject.toString()).post(designURI(assemblyName) + IConstants.VARIABLES_URI);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to get new global variable %s due to %s", variableName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }

    String msg = String.format("Failed to add new variables %s due to null response", variableName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Update global variables for a given assembly/design
   * 
   * @param environmentName
   * @param variables
   * @return
   * @throws OneOpsClientAPIException
   */
  public Boolean updateGlobalVariable(String assemblyName, String variableName, String variableValue, boolean isSecure) throws OneOpsClientAPIException {
    if (variableName == null) {
      String msg = "Missing variable name to be added";
      throw new OneOpsClientAPIException(msg);
    }

    if (variableValue == null) {
      String msg = "Missing variable value to be added";
      throw new OneOpsClientAPIException(msg);
    }

    Boolean success = false;
    RequestSpecification request = createRequest();

    Response variable = request.get(designURI(assemblyName) + IConstants.VARIABLES_URI + variableName);
    if (variable == null || variable.getBody() == null) {
      String msg = String.format("Failed to find global variables %s", variableName);
      throw new OneOpsClientAPIException(msg);
    }
    JsonPath variableDetails = variable.getBody().jsonPath();
    String ciId = variableDetails.getString("ciId");
    Map<String, String> attr = variableDetails.getMap("ciAttributes");
    if (attr == null) {
      attr = new HashMap<String, String>();
    }
    if (isSecure) {
      attr.put("secure", "true");
      attr.put("encrypted_value", variableValue);
    } else {
      attr.put("secure", "false");
      attr.put("value", variableValue);
    }

    ResourceObject ro = new ResourceObject();
    ro.setAttributes(attr);

    JSONObject jsonObject = JsonUtil.createJsonObject(ro, "cms_dj_ci");

    Response response = request.body(jsonObject.toString()).put(designURI(assemblyName) + IConstants.VARIABLES_URI + ciId);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        success = true;
      } else {
        String msg = String.format("Failed to get update global variable %s due to %s", variableName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }

    return success;
  }

  /**
   * Fetches specific platform details in Yaml format
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  public JsonPath extractYaml(String assemblyName) throws OneOpsClientAPIException {
    RequestSpecification request = createRequest();

    Response response = request.get(designURI(assemblyName) + "/extract.yaml");
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().jsonPath();
      } else {
        String msg = String.format("Failed to extract yaml content due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to extract yaml content due to null response";
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Adds specific platform from Yaml/Json file input
   * 
   * @param platformName
   * @return
   * @throws OneOpsClientAPIException
   */
  public JsonPath loadFile(String assemblyName, String filecontent) throws OneOpsClientAPIException {
    if (filecontent == null || filecontent.length() == 0) {
      String msg = "Missing input file content";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    request.header("Content-Type", "multipart/text");
    JSONObject jo = new JSONObject();
    jo.put("data", filecontent);

    Response response = request.parameter("data", filecontent).put(designURI(assemblyName) + "/load");
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().jsonPath();
      } else {
        String msg = String.format("Failed to load yaml content due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to load yaml content due to null response";
    throw new OneOpsClientAPIException(msg);
  }

  private String designURI(String assemblyName) {
    return IConstants.ASSEMBLY_URI + assemblyName + IConstants.DESIGN_URI;
  }

  private String designReleaseURI(String assemblyName) {
    return IConstants.ASSEMBLY_URI + assemblyName + IConstants.DESIGN_URI + IConstants.RELEASES_URI;
  }

  // Monitor

  /**
   * Fetches specific monitor details
   * 
   * @param monitorName
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource getMonitor(String assemblyName, String platform, String component, String environmentName, String monitorName) throws OneOpsClientAPIException {
    if (monitorName == null || monitorName.length() == 0) {
      String msg = "Missing monitor name to fetch details";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    Response response = request.get(transitionMonitorUri(assemblyName, platform, component, environmentName) + monitorName);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to get environment with name %s due to %s", environmentName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to get environment with name %s due to null response", environmentName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Update monitor
   * 
   * @param monitorName
   * @param cmdOptions
   * @param duration
   * @param sampleInterval
   * @param thresholds
   * @param heartbeat
   * @param enable
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource updateMonitor(String assemblyName, String platform, String component, String environmentName,
    String monitorName, String cmdOptions, Integer duration, Integer sampleInterval, JSONObject thresholds, boolean heartbeat, boolean enable) throws OneOpsClientAPIException {
    if (monitorName == null || monitorName.length() == 0) {
      String msg = "Missing monitor name to fetch details";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    Response response = request.get(transitionMonitorUri(assemblyName, platform, component, environmentName) + monitorName);

    ResourceObject ro = new ResourceObject();
    Map<String, String> attributes = new HashMap<String, String>();
    JSONObject monitor = JsonUtil.createJsonObject(response.getBody().asString());
    if (monitor != null && monitor.has("ciAttributes")) {
      JSONObject attrs = monitor.getJSONObject("ciAttributes");
      for (Object key : attrs.keySet()) {
        //based on you key types
        String keyStr = (String) key;
        String keyvalue = String.valueOf(attrs.get(keyStr));

        attributes.put(keyStr, keyvalue);

        if (cmdOptions != null && attributes.containsKey("cmd_options")) {
          attributes.put("cmd_options", String.valueOf(cmdOptions));
        }

        if (duration != null && attributes.containsKey("duration")) {
          attributes.put("duration", String.valueOf(duration));
        }

        if (sampleInterval != null && attributes.containsKey("sample_interval")) {
          attributes.put("sample_interval", String.valueOf(sampleInterval));
        }

        if (thresholds != null && attributes.containsKey("thresholds")) {
          attributes.put("thresholds", thresholds.toString());
        }

      }
      attributes.put("heartbeat", String.valueOf(heartbeat));
      attributes.put("enable", String.valueOf(enable));
    }
    ro.setAttributes(attributes);
    JSONObject jsonObject = JsonUtil.createJsonObject(ro, "cms_dj_ci");
    response = request.body(jsonObject.toString()).put(transitionMonitorUri(assemblyName, platform, component, environmentName) + monitorName);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to monitor definition for monitor %s due to %s", monitorName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }

    String msg = String.format("Failed to update monitor definition for %s due to null response", monitorName);
    throw new OneOpsClientAPIException(msg);
  }


  public JSONObject getThresholdJson(String name, String state, String metric, String bucket, String stat, JSONObject trigger, JSONObject reset) {
    JSONObject thresholdDef = new JSONObject();
    thresholdDef.put("bucket", bucket);
    thresholdDef.put("metric", metric);
    thresholdDef.put("state", state);
    thresholdDef.put("stat", stat);
    thresholdDef.put("trigger", trigger);
    thresholdDef.put("reset", reset);

    JSONObject threshold = new JSONObject();
    threshold.put(name, thresholdDef);

    return threshold;
  }

  public JSONObject createThresholdCondition(String operator, int value, int duration, int numOfOccurances) {
    JSONObject thresholdCondition = new JSONObject();
    thresholdCondition.put("operator", operator);
    thresholdCondition.put("value", value);
    thresholdCondition.put("duration", duration);
    thresholdCondition.put("numocc", numOfOccurances);

    return thresholdCondition;
  }

  public JSONObject addMetrics(String metricName, String unit, String dstype, String display, String description) {
    JSONObject metricinfo = new JSONObject();
    metricinfo.put("unit", unit);
    metricinfo.put("dstype", dstype);
    metricinfo.put("display", display);
    metricinfo.put("display_group", "");
    metricinfo.put("description", description);
    return metricinfo;
  }

  //list thresholds
  //add threshold
  //update threshold
  //delete threshold  

  private String transitionMonitorUri(String assemblyName, String platform, String component, String environmentName) {
    return IConstants.ASSEMBLY_URI + assemblyName + IConstants.TRANSITION_URI + IConstants.ENVIRONMENT_URI + environmentName
      + IConstants.PLATFORM_URI + platform
      + IConstants.COMPONENT_URI + component + IConstants.MONITORS_URI;
  }

  // Operation

  public CiResource updatePlatformAutoHealingStatus(String assemblyName, String environmentName, String platformName, String healingOption, boolean isEnabled) throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = String.format("Missing environment name to be updated");
      throw new OneOpsClientAPIException(msg);
    }

    if (platformName == null || platformName.length() == 0) {
      String msg = String.format("Missing platform name to be updated");
      throw new OneOpsClientAPIException(msg);
    }

    if (healingOption == null || healingOption.length() == 0) {
      String msg = String.format("No healing options available to be updated");
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();

    String enabled = "disable";
    if (isEnabled) {
      enabled = "enable";
    }

    Response response = request.body("").queryParam("status", enabled)
      .put(operationURI(assemblyName, environmentName) + IConstants.PLATFORM_URI + platformName + "/" + healingOption);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to update platforms due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to update platforms due to null response");
    throw new OneOpsClientAPIException(msg);
  }

  public CiResource updatePlatformAutoReplaceConfig(String assemblyName, String environmentName, String platformName, int repairCount, int repairTime) throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = String.format("Missing environment name to be updated");
      throw new OneOpsClientAPIException(msg);
    }

    if (platformName == null || platformName.length() == 0) {
      String msg = String.format("Missing platform name to be updated");
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    JSONObject jo = new JSONObject();
    jo.put("replace_after_minutes", String.valueOf(repairTime));
    jo.put("replace_after_repairs", String.valueOf(repairCount));

    Response response = request.body(jo.toString())
      .put(operationURI(assemblyName, environmentName) + IConstants.PLATFORM_URI + platformName + "/autoreplace");
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to update platforms due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to update platforms due to null response");
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Lists all instances for a given assembly, environment, platform and component
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  public List<CiResource> listInstances(String assemblyName, String environmentName, String platformName, String componentName) throws OneOpsClientAPIException {
    if (platformName == null || platformName.length() == 0) {
      String msg = "Missing platform name to fetch details";
      throw new OneOpsClientAPIException(msg);
    }
    if (componentName == null || componentName.length() == 0) {
      String msg = "Missing component name to fetch details";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    Response response = request.queryParam("instances_state", "all").get(operationURI(assemblyName, environmentName)
      + IConstants.PLATFORM_URI + platformName
      + IConstants.COMPONENT_URI + componentName
      + IConstants.INSTANCES_URI);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<CiResource>>() {});
      } else {
        String msg = String.format("Failed to get instances due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to get instances due to null response";
    throw new OneOpsClientAPIException(msg);
  }


  /**
   * Mark all instances for replacement for a given platform and component
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  public Boolean markInstancesForReplacement(String assemblyName, String environmentName, String platformName, String componentName) throws OneOpsClientAPIException {
    List<CiResource> instances = listInstances(assemblyName, environmentName, platformName, componentName);
    List<Long> instanceIds = Lists.newArrayList();
    for (CiResource ciResource : instances) {
      instanceIds.add(ciResource.getCiId());
    }
    return markInstancesForReplacement(assemblyName, platformName, componentName, instanceIds);
  }

  /**
   * Mark an instance for replacement for a given platform and component
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  public Boolean markInstanceForReplacement(String assemblyName, String platformName, String componentName, Long instanceId) throws OneOpsClientAPIException {
    List<Long> instanceIds = Lists.newArrayList();
    instanceIds.add(instanceId);
    return markInstancesForReplacement(assemblyName, platformName, componentName, instanceIds);
  }

  /**
   * Mark an instance for replacement for a given platform and component
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  Boolean markInstancesForReplacement(String assemblyName, String platformName, String componentName, List<Long> instanceIds) throws OneOpsClientAPIException {
    RequestSpecification request = createRequest();
    JSONObject jo = new JSONObject();
    jo.put("ids", instanceIds);
    jo.put("state", "replace");
    String uri = IConstants.ASSEMBLY_URI + assemblyName + IConstants.OPERATION_URI + IConstants.INSTANCES_URI + "state";

    Response response = request.body(jo.toString()).put(uri);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(Boolean.class);
      } else {
        String msg = String.format("Failed to set replace marker on instance(s) due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to set replace marker on instance(s) due to null response";
    throw new OneOpsClientAPIException(msg);
  }

  public JsonPath getLogData(String procedureId, List<String> actionIds) throws OneOpsClientAPIException {
    RequestSpecification request = createRequest();
    String uri = IConstants.OPERATION_URI + IConstants.PROCEDURES_URI + "log_data";
    request.queryParam("procedure_id", procedureId);

    if (actionIds != null) {
      for (String actionId : actionIds) {
        request.queryParam("action_ids", actionId);
      }
    }

    Response response = request.get(uri);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().jsonPath();
      } else {
        String msg = String.format("Failed to set replace marker on instance(s) due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to set replace marker on instance(s) due to null response";
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Lists all procedures for a given platform 
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  public List<CiResource> listProcedures(String assemblyName, String environmentName, String platformName) throws OneOpsClientAPIException {
    if (platformName == null || platformName.length() == 0) {
      String msg = "Missing platform name to fetch details";
      throw new OneOpsClientAPIException(msg);
    }
    RequestSpecification request = createRequest();
    Response response = request.get(operationURI(assemblyName, environmentName)
      + IConstants.PLATFORM_URI + platformName
      + IConstants.PROCEDURES_URI);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<CiResource>>() {});
      } else {
        String msg = String.format("Failed to get procedures due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to get procedures due to null response";
    throw new OneOpsClientAPIException(msg);
  }


  /**
   * Get procedure Id for a given platform, procedure name 
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  public Long getProcedureId(String assemblyName, String environmentName, String platformName, String procedureName) throws OneOpsClientAPIException {
    if (platformName == null || platformName.length() == 0) {
      String msg = "Missing platform name to fetch details";
      throw new OneOpsClientAPIException(msg);
    }
    if (procedureName == null || procedureName.length() == 0) {
      String msg = "Missing procedure name to fetch details";
      throw new OneOpsClientAPIException(msg);
    }

    List<CiResource> procs = listProcedures(assemblyName, environmentName, platformName);
    if (procs != null) {
      for (CiResource procedure : procs) {
        if (procedureName.equals(procedure.getCiName())) {
          return procedure.getCiId();
        }
      }
    }

    String msg = String.format("Failed to get procedure with the given name %s ", procedureName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Lists all actions for a given platform, component
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  public JsonPath listActions(String assemblyName, String environmentName, String platformName, String componentName) throws OneOpsClientAPIException {
    if (platformName == null || platformName.length() == 0) {
      String msg = "Missing platform name to fetch details";
      throw new OneOpsClientAPIException(msg);
    }
    if (componentName == null || componentName.length() == 0) {
      String msg = "Missing component name to fetch details";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    Response response = request.get(operationURI(assemblyName, environmentName)
      + IConstants.PLATFORM_URI + platformName
      + IConstants.COMPONENT_URI + componentName
      + IConstants.ACTIONS_URI);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().jsonPath();
      } else {
        String msg = String.format("Failed to get actions due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to get actions due to null response";
    throw new OneOpsClientAPIException(msg);
  }


  /**
   * Execute procedure for a given platform
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  public Procedure executeProcedure(String assemblyName, String environmentName, String platformName, String procedureName, String arglist) throws OneOpsClientAPIException {
    if (platformName == null || platformName.length() == 0) {
      String msg = "Missing platform name to fetch details";
      throw new OneOpsClientAPIException(msg);
    }
    if (procedureName == null || procedureName.length() == 0) {
      String msg = "Missing procedure name to fetch details";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    ResourceObject ro = new ResourceObject();
    Map<String, String> properties = new HashMap<String, String>();

    CiResource platform = getPlatform(environmentName, platformName);
    Long platformId = platform.getCiId();
    properties.put("procedureState", "active");
    properties.put("arglist", arglist);
    properties.put("definition", null);
    properties.put("ciId", "" + platformId);
    properties.put("procedureCiId", "" + getProcedureId(assemblyName, environmentName, platformName, procedureName));
    ro.setProperties(properties);

    JSONObject jsonObject = JsonUtil.createJsonObject(ro, "cms_procedure");
    Response response = request.body(jsonObject.toString()).post(IConstants.OPERATION_URI + IConstants.PROCEDURES_URI);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(Procedure.class);
      } else {
        String msg = String.format("Failed to execute procedures due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to execute procedures due to null response";
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Get procedure status for a given Id 
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  public Procedure getProcedureStatus(Long procedureId) throws OneOpsClientAPIException {
    if (procedureId == null) {
      String msg = "Missing procedure Id to fetch details";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    Response response = request.get(IConstants.OPERATION_URI + IConstants.PROCEDURES_URI + procedureId);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(Procedure.class);
      } else {
        String msg = String.format("Failed to get procedure status due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }

    String msg = String.format("Failed to get procedure status with the given Id " + procedureId);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Get procedure status for a given Id 
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  public Procedure cancelProcedure(Long procedureId) throws OneOpsClientAPIException {
    if (procedureId == null) {
      String msg = "Missing procedure Id to fetch details";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    ResourceObject ro = new ResourceObject();
    Map<String, String> properties = new HashMap<String, String>();

    properties.put("procedureState", "canceled");
    properties.put("definition", null);
    properties.put("actions", null);
    properties.put("procedureCiId", null);
    properties.put("procedureId", null);
    ro.setProperties(properties);

    JSONObject jsonObject = JsonUtil.createJsonObject(ro, "cms_procedure");
    Response response = request.body(jsonObject.toString()).put(IConstants.OPERATION_URI + IConstants.PROCEDURES_URI + procedureId);

    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(Procedure.class);
      } else {
        String msg = String.format("Failed to cancel procedure due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }

    String msg = String.format("Failed to cancel procedure with the given Id %s due to null response", procedureId);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Execute procedure for a given platform
   * 
   * @return
   * @throws OneOpsClientAPIException
   */
  public Procedure executeAction(String assemblyName, String environmentName, String platformName, String componentName, String actionName, List<Long> instanceList, String arglist, int rollingPercent)
    throws OneOpsClientAPIException {
    if (platformName == null || platformName.length() == 0) {
      String msg = "Missing platform name to fetch details";
      throw new OneOpsClientAPIException(msg);
    }
    if (componentName == null || componentName.length() == 0) {
      String msg = "Missing component name to fetch details";
      throw new OneOpsClientAPIException(msg);
    }
    if (actionName == null || actionName.length() == 0) {
      String msg = "Missing action name to fetch details";
      throw new OneOpsClientAPIException(msg);
    }
    if (instanceList == null || instanceList.size() == 0) {
      String msg = "Missing instances list to fetch details";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    ResourceObject ro = new ResourceObject();
    Map<String, String> properties = new HashMap<String, String>();

    CiResource component = getPlatformComponent(environmentName, platformName, componentName);
    Long componentId = component.getCiId();
    properties.put("procedureState", "active");
    properties.put("arglist", arglist);

    properties.put("ciId", "" + componentId);
    properties.put("force", "true");
    properties.put("procedureCiId", "0");

    Map<String, Object> flow = Maps.newHashMap();
    flow.put("targetIds", instanceList);
    flow.put("relationName", "base.RealizedAs");
    flow.put("direction", "from");

    Map<String, Object> action = Maps.newHashMap();
    action.put("isInheritable", null);
    action.put("actionName", "base.RealizedAs");
    action.put("inherited", null);
    action.put("isCritical", "true");
    action.put("stepNumber", "1");
    action.put("extraInfo", null);
    action.put("actionName", actionName);

    List<Map<String, Object>> actions = Lists.newArrayList();
    actions.add(action);
    flow.put("actions", actions);

    Map<String, Object> definition = new HashMap<String, Object>();
    List<Map<String, Object>> flows = Lists.newArrayList();
    flows.add(flow);
    definition.put("flow", flows);
    definition.put("name", actionName);

    properties.put("definition", definition.toString());
    ro.setProperties(properties);

    JSONObject jsonObject = JsonUtil.createJsonObject(ro, "cms_procedure");
    Response response = request.body(jsonObject.toString()).post(IConstants.OPERATION_URI + IConstants.PROCEDURES_URI);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(Procedure.class);
      } else {
        String msg = String.format("Failed to execute procedures due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to execute procedures due to null response";
    throw new OneOpsClientAPIException(msg);
  }

  private String operationURI(String assemblyName, String environmentName) {
    return IConstants.ASSEMBLY_URI + assemblyName + IConstants.OPERATION_URI + IConstants.ENVIRONMENT_URI + environmentName;
  }

  // Transition

  /**
   * Fetches specific environment details
   * 
   * @param environmentName
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource getEnvironment(String assemblyName, String environmentName) throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = "Missing environment name to fetch details";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    Response response = request.get(transitionEnvUri(assemblyName) + environmentName);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to get environment with name %s due to %s", environmentName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to get environment with name %s due to null response", environmentName);
    throw new OneOpsClientAPIException(msg);
  }


  /**
   * Lists all environments for a given assembly
   * @return
   * @throws OneOpsClientAPIException
   */
  public List<CiResource> listEnvironments(String assemblyName) throws OneOpsClientAPIException {

    RequestSpecification request = createRequest();
    Response response = request.get(transitionEnvUri(assemblyName));
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<CiResource>>() {});
      } else {
        String msg = String.format("Failed to list environments due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to list environments due to null response";
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Creates environment within the given assembly
   * 
   * @param environmentName {mandatory}
   * @param envprofile if exists
   * @param availability {mandatory}
   * @param platformAvailability
   * @param cloudMap {mandatory}
   * @param debugFlag {mandatory}
   * @param gdnsFlag {mandatory}
   * @param description
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource createEnvironment(String assemblyName, String environmentName, String envprofile, Map<String, String> attributes,
    Map<String, String> platformAvailability, Map<String, Map<String, String>> cloudMap, String description) throws OneOpsClientAPIException {

    ResourceObject ro = new ResourceObject();
    Map<String, String> properties = new HashMap<String, String>();

    if (environmentName != null && environmentName.length() > 0) {
      properties.put("ciName", environmentName);
    } else {
      String msg = "Missing environment name to create environment";
      throw new OneOpsClientAPIException(msg);
    }
    if (attributes == null) {
      String msg = "Missing availability in attributes map to create environment";
      throw new OneOpsClientAPIException(msg);
    }

    properties.put("nsPath", instance.getOrgname() + "/" + assemblyName);

    String availability = null;
    if (attributes.containsKey("availability")) {
      availability = attributes.get("availability");
    } else {
      String msg = "Missing availability in attributes map to create environment";
      throw new OneOpsClientAPIException(msg);
    }

    if (attributes.containsKey("global_dns")) {
      attributes.put("global_dns", String.valueOf(attributes.get("global_dns")));
    }

    ro.setProperties(properties);
    attributes.put("profile", envprofile);
    attributes.put("description", description);
    String subdomain = environmentName + "." + assemblyName + "." + instance.getOrgname();
    attributes.put("subdomain", subdomain);
    ro.setAttributes(attributes);

    RequestSpecification request = createRequest();
    JSONObject jsonObject = JsonUtil.createJsonObject(ro, "cms_ci");
    if (platformAvailability == null || platformAvailability.size() == 0) {
      List<CiResource> platforms = listPlatforms(assemblyName);
      if (platforms != null) {
        platformAvailability = new HashMap<String, String>();
        for (CiResource platform : platforms) {
          platformAvailability.put(platform.getCiId() + "", availability);
        }
      }
    }
    jsonObject.put("platform_availability", platformAvailability);

    if (cloudMap == null || cloudMap.size() == 0) {
      String msg = "Missing clouds map to create environment";
      throw new OneOpsClientAPIException(msg);
    }
    jsonObject.put("clouds", cloudMap);

    Response response = request.body(jsonObject.toString()).post(transitionEnvUri(assemblyName));

    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to create environment with name %s due to %s", environmentName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to create environment with name %s due to null response", environmentName);
    throw new OneOpsClientAPIException(msg);
  }


  /**
   * Commits environment open releases
   * 
   * @param environmentName {mandatory}
   * @param excludePlatforms
   * @return
   * @throws OneOpsClientAPIException
   */
  public Release commitEnvironment(String assemblyName, String environmentName, List<Long> excludePlatforms, String comment) throws OneOpsClientAPIException {

    RequestSpecification request = createRequest();
    JSONObject jo = new JSONObject();
    if (excludePlatforms != null && excludePlatforms.size() > 0) {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < excludePlatforms.size(); i++) {
        sb.append(excludePlatforms.get(i));
        if (i < (excludePlatforms.size() - 1)) {
          sb.append(",");
        }
      }
      jo.put("exclude_platforms", sb.toString());
    }
    if (comment != null)
      jo.put("desc", comment);
    Response response = request.body(jo.toString()).post(transitionEnvUri(assemblyName) + environmentName + "/commit");
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {

        response = request.get(transitionEnvUri(assemblyName) + environmentName);
        String envState = response.getBody().jsonPath().get("ciState");
        //wait for deployment plan to generate
        do {
          Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);
          response = request.get(transitionEnvUri(assemblyName) + environmentName);
          if (response == null) {
            String msg = String.format("Failed to commit environment due to null response");
            throw new OneOpsClientAPIException(msg);
          }
          envState = response.getBody().jsonPath().get("ciState");
        } while (response != null && "locked".equalsIgnoreCase(envState));

        String comments = response.getBody().jsonPath().getString("comments");
        if (comments != null && comments.startsWith("ERROR:")) {
          String msg = String.format("Failed to commit environment due to %s", comments);
          throw new OneOpsClientAPIException(msg);
        }

        return response.getBody().as(Release.class);

      } else {
        String msg = String.format("Failed to commit environment due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to commit environment due to null response";
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Deploy an already generated deployment plan
   * 
   * @param environmentName
   * @return
   * @throws OneOpsClientAPIException
   */
  public Deployment deploy(String assemblyName, String environmentName, String comments) throws OneOpsClientAPIException {

    RequestSpecification request = createRequest();

    Release bomRelease = getBomRelease(assemblyName, environmentName);
    Long releaseId = bomRelease.getReleaseId();
    String nsPath = bomRelease.getNsPath();
    if (releaseId != null && nsPath != null) {
      Map<String, String> properties = new HashMap<String, String>();
      properties.put("nsPath", nsPath);
      properties.put("releaseId", releaseId + "");
      if (comments != null) {
        properties.put("comments", comments);
      }
      ResourceObject ro = new ResourceObject();
      ro.setProperties(properties);
      JSONObject jsonObject = JsonUtil.createJsonObject(ro, "cms_deployment");
      Response response = request.body(jsonObject.toString()).post(transitionEnvUri(assemblyName) + environmentName + "/deployments/");
      if (response == null) {
        String msg = String.format("Failed to start deployment for environment %s due to null response", environmentName);
        throw new OneOpsClientAPIException(msg);
      }
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(Deployment.class);
      } else {
        String msg = String.format("Failed to start deployment for environment %s due to null response", environmentName);
        throw new OneOpsClientAPIException(msg);
      }
    } else {
      String msg = String.format("Failed to find release id to be deployed");
      throw new OneOpsClientAPIException(msg);
    }


  }


  /**
   * Fetches deployment status for the given assembly/environment
   * 
   * @param environmentName
   * @param deploymentId
   * @return
   * @throws OneOpsClientAPIException
   */
  public Deployment getDeploymentStatus(String assemblyName, String environmentName, Long deploymentId) throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = "Missing environment name to fetch details";
      throw new OneOpsClientAPIException(msg);
    }
    if (deploymentId == null) {
      String msg = "Missing deployment to fetch details";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    Response response = request.get(transitionEnvUri(assemblyName) + environmentName + IConstants.DEPLOYMENTS_URI + deploymentId + "/status");
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(Deployment.class);
      } else {
        String msg = String.format("Failed to get deployment status for environment %s with deployment Id %s due to %s", environmentName, deploymentId, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to get deployment status for environment %s with deployment Id %s due to null response", environmentName, deploymentId);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Fetches latest deployment for the given assembly/environment
   * 
   * @param environmentName
   * @return
   * @throws OneOpsClientAPIException
   */
  public Deployment getLatestDeployment(String assemblyName, String environmentName) throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = "Missing environment name to fetch details";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    Response response = request.get(transitionEnvUri(assemblyName) + environmentName + IConstants.DEPLOYMENTS_URI + "latest");
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(Deployment.class);
      } else {
        String msg = String.format("Failed to get latest deployment for environment %s due to %s", environmentName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to get latest deployment for environment %s due to null response", environmentName);
    throw new OneOpsClientAPIException(msg);
  }


  /**
   * Discard already generated deployment plan
   * 
   * @param environmentName
   * @return
   * @throws OneOpsClientAPIException
   */
  public Release discardDeploymentPlan(String assemblyName, String environmentName) throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = "Missing environment name to fetch details";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();

    Release bomRelease = getBomRelease(assemblyName, environmentName);
    if (bomRelease != null) {
      long releaseId = bomRelease.getReleaseId();
      Response response = request.body("").post(transitionEnvUri(assemblyName) + environmentName + IConstants.RELEASES_URI + releaseId + "/discard");
      if (response != null) {
        if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
          return response.getBody().as(Release.class);
        } else {
          String msg = String.format("Failed to discard deployment plan for environment %s due to %s", environmentName, response.getStatusLine());
          throw new OneOpsClientAPIException(msg);
        }
      }
    } else {
      String msg = String.format("Failed to discard deployment plan for environment %s due to no open bom", environmentName);
      throw new OneOpsClientAPIException(msg);
    }

    String msg = String.format("Failed to discard deployment plan for environment %s due to null response", environmentName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Discard uncommitted changes for the given {environmentName}
   * 
   * @param environmentName
   * @return
   * @throws OneOpsClientAPIException
   */
  public Release discardOpenRelease(String assemblyName, String environmentName) throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = "Missing environment name to fetch details";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    Response response = request.body("").post(transitionEnvUri(assemblyName) + environmentName + "/discard");
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(Release.class);
      } else {
        String msg = String.format("Failed to discard changes for environment %s due to %s", environmentName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }

    String msg = String.format("Failed to discard changes for environment %s due to null response", environmentName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Disable all platforms for the given assembly/environment
   * 
   * @param environmentName
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource disableAllPlatforms(String assemblyName, String environmentName) throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = "Missing environment name to disable all platforms";
      throw new OneOpsClientAPIException(msg);
    }

    List<CiResource> ps = listPlatforms(environmentName);
    List<Long> platformIds = Lists.newArrayList();
    for (CiResource ciResource : ps) {
      platformIds.add(ciResource.getCiId());
    }

    RequestSpecification request = createRequest();
    Response response = request.queryParam("platformCiIds[]", platformIds).put(transitionEnvUri(assemblyName) + environmentName + "/disable");
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to disable platforms for environment %s due to %s", environmentName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to disable platforms for environment %s due to null response", environmentName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Fetches latest release for the given assembly/environment
   * 
   * @param environmentName
   * @return
   * @throws OneOpsClientAPIException
   */
  public Release getLatestRelease(String assemblyName, String environmentName) throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = "Missing environment name to fetch details";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    Response response = request.get(transitionEnvUri(assemblyName) + environmentName + IConstants.RELEASES_URI + "latest");
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(Release.class);
      } else {
        String msg = String.format("Failed to get latest releases for environment %s due to %s", environmentName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to get latest releases for environment %s due to null response", environmentName);
    throw new OneOpsClientAPIException(msg);
  }


  /**
   * Fetches bom release for the given assembly/environment
   * 
   * @param environmentName
   * @return
   * @throws OneOpsClientAPIException
   */
  public Release getBomRelease(String assemblyName, String environmentName) throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = "Missing environment name to fetch details";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    Response response = request.get(transitionEnvUri(assemblyName) + environmentName + IConstants.RELEASES_URI + "bom");
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(Release.class);
      } else {
        String msg = String.format("No bom releases found for environment %s ", environmentName);
        System.out.println(msg);
        return null;
      }
    }
    String msg = String.format("Failed to get bom releases for environment %s due to null response", environmentName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Cancels a failed/paused deployment
   * 
   * @param environmentName
   * @param deploymentId
   * @param releaseId
   * @return
   * @throws OneOpsClientAPIException
   */
  public Deployment cancelDeployment(String assemblyName, String environmentName, Long deploymentId, Long releaseId) throws OneOpsClientAPIException {
    return updateDeploymentStatus(assemblyName, environmentName, deploymentId, releaseId, "canceled");
  }


  public DeploymentRFC getDeployment(String assemblyName, String environmentName, Long deploymentId) throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = "Missing environment name to fetch details";
      throw new OneOpsClientAPIException(msg);
    }

    if (deploymentId == null) {
      String msg = "Missing deployment Id to fetch details";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    Response response = request.get(transitionEnvUri(assemblyName) + environmentName + IConstants.DEPLOYMENTS_URI + deploymentId);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(DeploymentRFC.class);
      } else {
        String msg = String.format("Failed to get deployment details for environment %s for id %s due to %s", environmentName, deploymentId, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to get deployment details for environment %s for id %s due to null response", environmentName, deploymentId);
    throw new OneOpsClientAPIException(msg);
  }

  public Log getDeploymentRfcLog(String assemblyName, String environmentName, Long deploymentId, Long rfcId) throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = "Missing environment name to fetch details";
      throw new OneOpsClientAPIException(msg);
    }

    if (deploymentId == null) {
      String msg = "Missing deployment Id to fetch details";
      throw new OneOpsClientAPIException(msg);
    }

    if (rfcId == null) {
      String msg = "Missing rfc Id to fetch details";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    Response response = request.queryParam("rfcId", rfcId).get(transitionEnvUri(assemblyName) + environmentName + IConstants.DEPLOYMENTS_URI + deploymentId + "/log_data");
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(Log.class);
      } else {
        String msg = String.format("Failed to get deployment logs for environment %s, deployment id %s and rfcId %s due to %s", environmentName, deploymentId, rfcId, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to get deployment logs for environment %s, deployment id %s and rfcId %s due to null response", environmentName, deploymentId, rfcId);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Approve a deployment
   * 
   * @param environmentName
   * @param deploymentId
   * @param releaseId
   * @return
   * @throws OneOpsClientAPIException
   */
  public Deployment approveDeployment(String assemblyName, String environmentName, Long deploymentId, Long releaseId) throws OneOpsClientAPIException {
    return updateDeploymentStatus(assemblyName, environmentName, deploymentId, releaseId, "active");
  }

  /**
   * Retry a deployment
   * 
   * @param environmentName
   * @param deploymentId
   * @param releaseId
   * @return
   * @throws OneOpsClientAPIException
   */
  public Deployment retryDeployment(String assemblyName, String environmentName, Long deploymentId, Long releaseId) throws OneOpsClientAPIException {
    return updateDeploymentStatus(assemblyName, environmentName, deploymentId, releaseId, "active");
  }

  /**
   * Update deployment state
   * 
   * @param environmentName
   * @param deploymentId
   * @param releaseId
   * @return
   * @throws OneOpsClientAPIException
   */
  public Deployment updateDeploymentStatus(String assemblyName, String environmentName, Long deploymentId, Long releaseId, String newstate) throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = "Missing environment name to fetch details";
      throw new OneOpsClientAPIException(msg);
    }
    if (deploymentId == null) {
      String msg = "Missing deployment to fetch details";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();

    Map<String, String> properties = new HashMap<String, String>();
    properties.put("deploymentState", newstate);
    properties.put("releaseId", String.valueOf(releaseId));
    ResourceObject ro = new ResourceObject();
    ro.setProperties(properties);
    JSONObject jsonObject = JsonUtil.createJsonObject(ro, "cms_deployment");

    Response response = request.body(jsonObject.toString()).put(transitionEnvUri(assemblyName) + environmentName + IConstants.DEPLOYMENTS_URI + deploymentId);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(Deployment.class);
      } else {
        String msg = String.format("Failed to update deployment state to %s for environment %s with deployment Id %s due to %s", newstate, environmentName, deploymentId, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to update deployment state to %s for environment %s with deployment Id %s due to null response", newstate, environmentName, deploymentId);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Deletes the given environment
   * 
   * @param environmentName
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource deleteEnvironment(String assemblyName, String environmentName) throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = "Missing environment name to delete";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    Response response = request.delete(transitionEnvUri(assemblyName) + environmentName);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to delete environment with name %s due to %s", environmentName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to delete environment with name %s due to null response", environmentName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * List platforms for a given assembly/environment
   * 
   * @param environmentName
   * @return
   * @throws OneOpsClientAPIException
   */
  public List<CiResource> listPlatforms(String assemblyName, String environmentName) throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = "Missing environment name list platforms";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    Response response = request.get(transitionEnvUri(assemblyName) + environmentName + IConstants.PLATFORM_URI);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<CiResource>>() {});
      } else {
        String msg = String.format("Failed to get list of environment platforms due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to get list of environment platforms due to null response";
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Get platform details for a given assembly/environment
   * 
   * @param environmentName
   * @param platformName
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource getPlatform(String assemblyName, String environmentName, String platformName) throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = "Missing environment name to get details";
      throw new OneOpsClientAPIException(msg);
    }
    if (platformName == null || platformName.length() == 0) {
      String msg = "Missing platform name to get details";
      throw new OneOpsClientAPIException(msg);
    }
    RequestSpecification request = createRequest();
    Response response = request.get(transitionEnvUri(assemblyName) + environmentName + IConstants.PLATFORM_URI + platformName);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to get environment platform details due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to get environment platform details due to null response";
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * List platform components for a given assembly/environment/platform
   * 
   * @param environmentName
   * @param platformName
   * @return
   * @throws OneOpsClientAPIException
   */
  public List<CiResource> listPlatformComponents(String assemblyName, String environmentName, String platformName) throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = "Missing environment name to list enviornment platform components";
      throw new OneOpsClientAPIException(msg);
    }
    if (platformName == null || platformName.length() == 0) {
      String msg = "Missing platform name to list enviornment platform components";
      throw new OneOpsClientAPIException(msg);
    }
    RequestSpecification request = createRequest();
    Response response = request.get(transitionEnvUri(assemblyName) + environmentName + IConstants.PLATFORM_URI + platformName + IConstants.COMPONENT_URI);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<CiResource>>() {});
      } else {
        String msg = String.format("Failed to get list of environment platforms components due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to get list of environment platforms components due to null response";
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Get platform component details for a given assembly/environment/platform
   * 
   * @param environmentName
   * @param platformName
   * @param componentName
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource getPlatformComponent(String assemblyName, String environmentName, String platformName, String componentName) throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = "Missing environment name to get enviornment platform component details";
      throw new OneOpsClientAPIException(msg);
    }
    if (platformName == null || platformName.length() == 0) {
      String msg = "Missing platform name to get enviornment platform component details";
      throw new OneOpsClientAPIException(msg);
    }
    if (componentName == null || componentName.length() == 0) {
      String msg = "Missing component name to get enviornment platform component details";
      throw new OneOpsClientAPIException(msg);
    }
    RequestSpecification request = createRequest();
    Response response = request.get(transitionEnvUri(assemblyName) + environmentName + IConstants.PLATFORM_URI + platformName + IConstants.COMPONENT_URI + componentName);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to get enviornment platform component details due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to get enviornment platform component details due to null response";
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Update component attributes for a given assembly/environment/platform/component
   * 
   * @param environmentName
   * @param platformName
   * @param componentName
   * @param attributes
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource updatePlatformComponent(String assemblyName, String environmentName, String platformName, String componentName, Map<String, String> attributes) throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = "Missing environment name to update component attributes";
      throw new OneOpsClientAPIException(msg);
    }
    if (platformName == null || platformName.length() == 0) {
      String msg = "Missing platform name to update component attributes";
      throw new OneOpsClientAPIException(msg);
    }
    if (componentName == null || componentName.length() == 0) {
      String msg = "Missing component name to update component attributes";
      throw new OneOpsClientAPIException(msg);
    }
    if (attributes == null || attributes.size() == 0) {
      String msg = "Missing attributes list to be updated";
      throw new OneOpsClientAPIException(msg);
    }

    CiResource componentDetails = getPlatformComponent(environmentName, platformName, componentName);
    if (componentDetails != null) {
      ResourceObject ro = new ResourceObject();

      Long ciId = componentDetails.getCiId();

      Map<String, String> attr = Maps.newHashMap();
      //Add ciAttributes to be updated
      if (attributes != null && attributes.size() > 0)
        attr.putAll(attributes);

      Map<String, String> ownerProps = Maps.newHashMap();
      //Add existing attrProps to retain locking of attributes 
      AttrProps attrProps = componentDetails.getAttrProps();
      if (attrProps != null && attrProps.getAdditionalProperties() != null && attrProps.getAdditionalProperties().size() > 0 && attrProps.getAdditionalProperties().get("owner") != null) {
        @SuppressWarnings("unchecked")
        Map<String, String> ownersMap = (Map<String, String>) attrProps.getAdditionalProperties().get("owner");
        for (Entry<String, String> entry : ownersMap.entrySet()) {
          ownerProps.put(entry.getKey(), entry.getValue());
        }
      }

      //Add updated attributes to attrProps to lock them
      for (Entry<String, String> entry : attributes.entrySet()) {
        ownerProps.put(entry.getKey(), "manifest");
      }

      ro.setAttributes(attr);
      ro.setOwnerProps(ownerProps);

      RequestSpecification request = createRequest();
      JSONObject jsonObject = JsonUtil.createJsonObject(ro, "cms_dj_ci");
      Response response = request.body(jsonObject.toString()).put(transitionEnvUri(assemblyName) + environmentName + IConstants.PLATFORM_URI + platformName + IConstants.COMPONENT_URI + ciId);
      if (response != null) {
        if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
          return response.getBody().as(CiResource.class);
        } else {
          String msg = String.format("Failed to get update component %s due to %s", componentName, response.getStatusLine());
          throw new OneOpsClientAPIException(msg);
        }
      }
    }
    String msg = String.format("Failed to get update component %s due to null response", componentName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * touch component
   * 
   * @param environmentName
   * @param platformName
   * @param componentName
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource touchPlatformComponent(String assemblyName, String environmentName, String platformName, String componentName) throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = "Missing environment name to update component attributes";
      throw new OneOpsClientAPIException(msg);
    }
    if (platformName == null || platformName.length() == 0) {
      String msg = "Missing platform name to update component attributes";
      throw new OneOpsClientAPIException(msg);
    }
    if (componentName == null || componentName.length() == 0) {
      String msg = "Missing component name to update component attributes";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    JSONObject jo = new JSONObject();

    Response response =
      request.body(jo.toString()).post(transitionEnvUri(assemblyName) + environmentName + IConstants.PLATFORM_URI + platformName + IConstants.COMPONENT_URI + componentName + "/touch");

    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to touch component %s due to %s", componentName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to get touch component %s due to null response", componentName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Pull latest design commits
   * 
   * @param environmentName {mandatory}
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource pullDesign(String assemblyName, String environmentName) throws OneOpsClientAPIException {

    RequestSpecification request = createRequest();
    JSONObject jo = new JSONObject();

    Response response = request.body(jo.toString()).post(transitionEnvUri(assemblyName) + environmentName + "/pull");
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        CiResource env = response.getBody().as(CiResource.class);
        if (env == null || (env.getComments() != null && env.getComments().startsWith("ERROR:"))) {
          String msg = String.format("Failed to pull design for environment %s due to %s", environmentName, env.getComments());
          throw new OneOpsClientAPIException(msg);
        } else
          return env;
      } else {
        String msg = String.format("Failed to pull design for environment %s due to %s", environmentName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to pull design for environment %s due to null response", environmentName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Use this pull design when new platform(s) or newer version of platform(s) is being added to the environment
   *  
   * @param environmentName
   * @param platformAvailability
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource pullNewPlatform(String assemblyName, String environmentName, Map<String, String> platformAvailability) throws OneOpsClientAPIException {

    RequestSpecification request = createRequest();
    JSONObject jo = new JSONObject();

    if (platformAvailability == null || platformAvailability.size() == 0) {
      List<CiResource> platforms = listPlatforms(assemblyName);

      CiResource env = getEnvironment(assemblyName, environmentName);
      String availability = "single";
      if (env != null && env.getCiAttributes() != null) {
        CiAttributes attr = env.getCiAttributes();//.availability");
        if (attr.getAdditionalProperties() != null && attr.getAdditionalProperties().containsKey("availability")) {
          availability = String.valueOf(attr.getAdditionalProperties().get("availability"));
        }
      }
      if (platforms != null) {
        platformAvailability = new HashMap<String, String>();
        for (CiResource platform : platforms) {
          platformAvailability.put(platform.getCiId() + "", availability);
        }
      }
    }
    jo.put("platform_availability", platformAvailability);

    Response response = request.body(jo.toString()).post(transitionEnvUri(assemblyName) + environmentName + "/pull");
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        CiResource env = response.getBody().as(CiResource.class);
        if (env == null || (env.getComments() != null && env.getComments().startsWith("ERROR:"))) {
          String msg = String.format("Failed to pull design for environment %s due to %s", environmentName, env.getComments());
          throw new OneOpsClientAPIException(msg);
        } else
          return env;
      } else {
        String msg = String.format("Failed to pull design for environment %s due to %s", environmentName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to pull design for environment %s due to null response", environmentName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * List local variables for a given assembly/environment/platform
   * 
   * @param environmentName
   * @param platformName
   * @return
   * @throws OneOpsClientAPIException
   */
  public List<CiResource> listPlatformVariables(String assemblyName, String environmentName, String platformName) throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = "Missing environment name to list enviornment platform variables";
      throw new OneOpsClientAPIException(msg);
    }
    if (platformName == null || platformName.length() == 0) {
      String msg = "Missing platform name to list enviornment platform variables";
      throw new OneOpsClientAPIException(msg);
    }
    RequestSpecification request = createRequest();
    Response response = request.get(transitionEnvUri(assemblyName) + environmentName + IConstants.PLATFORM_URI + platformName + IConstants.VARIABLES_URI);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<CiResource>>() {});
      } else {
        String msg = String.format("Failed to get list of environment platforms variables due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to get list of environment platforms variables due to null response";
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Update platform local variables for a given assembly/environment/platform
   * 
   * @param environmentName
   * @param platformName
   * @param variables
   * @param isSecure
   * @return
   * @throws OneOpsClientAPIException
   */

  public Boolean updatePlatformVariable(String assemblyName, String environmentName, String platformName, String variableName, String variableValue, boolean isSecure) throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = "Missing environment name to update variables";
      throw new OneOpsClientAPIException(msg);
    }
    if (platformName == null || platformName.length() == 0) {
      String msg = "Missing platform name to update variables";
      throw new OneOpsClientAPIException(msg);
    }
    if (variableName == null) {
      String msg = "Missing variable name to be added";
      throw new OneOpsClientAPIException(msg);
    }
    if (variableValue == null) {
      String msg = "Missing variable value to be added";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    boolean success = false;
    ResourceObject ro = new ResourceObject();
    Map<String, String> attributes = new HashMap<String, String>();

    String uri = transitionEnvUri(assemblyName) + environmentName + IConstants.PLATFORM_URI + platformName + IConstants.VARIABLES_URI + variableName;
    Response response = request.get(uri);
    if (response != null) {
      JSONObject var = JsonUtil.createJsonObject(response.getBody().asString());
      if (var != null && var.has("ciAttributes")) {
        JSONObject attrs = var.getJSONObject("ciAttributes");
        for (Object key : attrs.keySet()) {
          //based on you key types
          String keyStr = (String) key;
          String keyvalue = String.valueOf(attrs.get(keyStr));

          attributes.put(keyStr, keyvalue);
        }
        if (isSecure) {
          attributes.put("secure", "true");
          attributes.put("encrypted_value", variableValue);
        } else {
          attributes.put("secure", "false");
          attributes.put("value", variableValue);
        }
        ro.setAttributes(attributes);

        JSONObject jsonObject = JsonUtil.createJsonObject(ro, "cms_dj_ci");
        if (response != null) {
          response = request.body(jsonObject.toString()).put(uri);
          if (response != null) {
            if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
              success = true;
            } else {
              String msg = String.format("Failed to get update variables %s due to %s", variableName, response.getStatusLine());
              throw new OneOpsClientAPIException(msg);
            }
          }
        }
      } else {
        success = true;
      }
    } else {
      String msg = String.format("Failed to get update variables %s due to null response", variableName);
      throw new OneOpsClientAPIException(msg);
    }


    return success;
  }



  /**
   * List global variables for a given assembly/environment
   * 
   * @param environmentName
   * @return
   * @throws OneOpsClientAPIException
   */
  public List<CiResource> listGlobalVariables(String assemblyName, String environmentName) throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = "Missing environment name to list enviornment variables";
      throw new OneOpsClientAPIException(msg);
    }
    RequestSpecification request = createRequest();
    Response response = request.get(transitionEnvUri(assemblyName) + environmentName + IConstants.VARIABLES_URI);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<CiResource>>() {});
      } else {
        String msg = String.format("Failed to get list of environment variables due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to get list of environment variables due to null response";
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Update global variables for a given assembly/environment
   * 
   * @param environmentName
   * @param variables
   * @return
   * @throws OneOpsClientAPIException
   */
  public Boolean updateGlobalVariable(String assemblyName, String environmentName, String variableName, String variableValue, boolean isSecure) throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = "Missing environment name to update component attributes";
      throw new OneOpsClientAPIException(msg);
    }
    if (variableName == null) {
      String msg = "Missing variable name to be added";
      throw new OneOpsClientAPIException(msg);
    }
    if (variableValue == null) {
      String msg = "Missing variable value to be added";
      throw new OneOpsClientAPIException(msg);
    }
    boolean success = false;
    RequestSpecification request = createRequest();
    ResourceObject ro = new ResourceObject();
    Map<String, String> attributes = new HashMap<String, String>();

    String uri = transitionEnvUri(assemblyName) + environmentName + IConstants.VARIABLES_URI + variableName;
    Response response = request.get(uri);
    if (response != null) {
      JSONObject var = JsonUtil.createJsonObject(response.getBody().asString());
      if (var != null && var.has("ciAttributes")) {
        JSONObject attrs = var.getJSONObject("ciAttributes");
        for (Object key : attrs.keySet()) {
          //based on you key types
          String keyStr = (String) key;
          String keyvalue = String.valueOf(attrs.get(keyStr));
          attributes.put(keyStr, keyvalue);
        }
        if (isSecure) {
          attributes.put("secure", "true");
          attributes.put("encrypted_value", variableValue);
        } else {
          attributes.put("secure", "false");
          attributes.put("value", variableValue);
        }

        ro.setAttributes(attributes);

        JSONObject jsonObject = JsonUtil.createJsonObject(ro, "cms_dj_ci");
        response = request.body(jsonObject.toString()).put(uri);
        if (response != null) {
          if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
            success = true;
          } else {
            String msg = String.format("Failed to get update variables %s due to %s", variableName, response.getStatusLine());
            throw new OneOpsClientAPIException(msg);
          }
        }
      } else {
        success = true;
      }
    } else {
      String msg = String.format("Failed to get update variables %s due to null response", variableName);
      throw new OneOpsClientAPIException(msg);
    }



    return success;
  }

  /**
   * Mark the input {#platformIdList} platforms for delete
   * 
   * @param environmentName
   * @param platformIdList
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource updateDisableEnvironment(String assemblyName, String environmentName, List<String> platformIdList) throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = "Missing environment name to disable platforms";
      throw new OneOpsClientAPIException(msg);
    }

    if (platformIdList == null || platformIdList.size() == 0) {
      String msg = "Missing platforms list to be disabled";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("platformCiIds", platformIdList);

    Response response = request.body(jsonObject.toString()).put(transitionEnvUri(assemblyName) + environmentName + "/disable");
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to disable platforms for environment with name %s due to %s", environmentName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to disable platforms for environment with name %s due to null response", environmentName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Update redundancy configuration for a given platform
   * 
   * @param environmentName
   * @param platformName
   * @param config update any 
   * @return
   * @throws OneOpsClientAPIException
   */
  public Boolean updatePlatformRedundancyConfig(String assemblyName, String environmentName, String platformName, String componentName, RedundancyConfig config) throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = "Missing environment name to be updated";
      throw new OneOpsClientAPIException(msg);
    }

    if (platformName == null || platformName.length() == 0) {
      String msg = "Missing platform name to be updated";
      throw new OneOpsClientAPIException(msg);
    }

    if (config == null) {
      String msg = "Missing redundancy config to be updated";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();

    JSONObject redundant = new JSONObject();
    redundant.put("max", config.getMax());
    redundant.put("pct_dpmt", config.getPercentDeploy());
    redundant.put("step_down", config.getStepDown());
    redundant.put("flex", "true");
    redundant.put("converge", "false");
    redundant.put("min", config.getMin());
    redundant.put("current", config.getCurrent());
    redundant.put("step_up", config.getStepUp());
    JSONObject rconfig = new JSONObject();
    rconfig.put("relationAttributes", redundant);

    redundant = new JSONObject();
    redundant.put("max", "manifest");
    redundant.put("pct_dpmt", "manifest");
    redundant.put("step_down", "manifest");
    redundant.put("flex", "manifest");
    redundant.put("converge", "manifest");
    redundant.put("min", "manifest");
    redundant.put("current", "manifest");
    redundant.put("step_up", "manifest");
    JSONObject owner = new JSONObject();
    owner.put("owner", redundant);

    rconfig.put("relationAttrProps", owner);
    if (componentName == null || componentName.isEmpty()) {
      componentName = "compute";
    }
    CiResource componentDetails = getPlatformComponent(environmentName, platformName, componentName);
    JSONObject jo = new JSONObject();
    jo.put(String.valueOf(componentDetails.getCiId()), rconfig);

    JSONObject dependsOn = new JSONObject();
    dependsOn.put("depends_on", jo);

    Response response = request.body(dependsOn.toString()).put(transitionEnvUri(assemblyName) + environmentName + IConstants.PLATFORM_URI + platformName);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return true;
      } else {
        String msg = String.format("Failed to update platforms redundancy for environment with name %s due to %s", environmentName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to update platforms redundancy for environment with name %s due to null response", environmentName);
    throw new OneOpsClientAPIException(msg);
  }

  public CiResource updatePlatformCloudScale(String assemblyName, String environmentName, String platformName, String cloudId, Map<String, String> cloudMap) throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = String.format("Missing environment name to be updated");
      throw new OneOpsClientAPIException(msg);
    }

    if (platformName == null || platformName.length() == 0) {
      String msg = String.format("Missing platform name to be updated");
      throw new OneOpsClientAPIException(msg);
    }

    if (cloudId == null || cloudId.length() == 0) {
      String msg = String.format("Missing cloud ID to be updated");
      throw new OneOpsClientAPIException(msg);
    }

    if (cloudMap == null || cloudMap.size() == 0) {
      String msg = String.format("Missing cloud info to be updated");
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    JSONObject jo = new JSONObject();
    jo.put("cloud_id", cloudId);
    jo.put("attributes", cloudMap);
    Response response = request.body(jo.toString())
      .put(transitionEnvUri(assemblyName) + environmentName + IConstants.PLATFORM_URI + platformName + "/cloud_configuration");
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to update platforms cloud scale with cloud id %s due to %s", cloudId,
          response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to update platforms cloud scale with cloud id %s due to null response",
      cloudId);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * List relays
   * 
   * @param environmentName
   * @return
   * @throws OneOpsClientAPIException
   */
  public List<CiResource> listRelays(String assemblyName, String environmentName) throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = "Missing environment name to get relays";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    Response response = request.get(transitionEnvUri(assemblyName) + environmentName + "/relays/");
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return JsonUtil.toObject(response.getBody().asString(), new TypeReference<List<CiResource>>() {});
      } else {
        String msg = String.format("Failed to list relay due to %s", response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = "Failed to list relay due to null response";
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Get relay details
   * 
   * @param environmentName
   * @param relayName
   * @return
   * @throws OneOpsClientAPIException
   */

  public CiResource getRelay(String assemblyName, String environmentName, String relayName) throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = "Missing environment name to get relay details";
      throw new OneOpsClientAPIException(msg);
    }

    if (relayName == null || relayName.length() == 0) {
      String msg = "Missing relay name to fetch details";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();
    Response response = request.get(transitionEnvUri(assemblyName) + environmentName + "/relays/" + relayName);
    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to get relay with name %s due to %s", relayName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to get relay with name %s due to null response", relayName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Add new Relay
   * 
   * @param relayName
   * @param severity
   * @param emails
   * @param source
   * @param nsPaths
   * @param regex
   * @param correlation
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource addRelay(String assemblyName, String environmentName, String relayName, String severity, String emails, String source, String nsPaths, String regex, boolean correlation)
    throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = "Missing environment name to create relay";
      throw new OneOpsClientAPIException(msg);
    }

    ResourceObject ro = new ResourceObject();
    Map<String, String> properties = Maps.newHashMap();

    if (relayName == null || relayName.length() == 0) {
      String msg = "Missing relay name to create one";
      throw new OneOpsClientAPIException(msg);
    }
    if (emails == null || emails.length() == 0) {
      String msg = "Missing emails addresses to create relay";
      throw new OneOpsClientAPIException(msg);
    }
    if (severity == null || severity.length() == 0) {
      String msg = "Missing severity to create relay";
      throw new OneOpsClientAPIException(msg);
    }
    if (source == null || source.length() == 0) {
      String msg = "Missing source to create relay";
      throw new OneOpsClientAPIException(msg);
    }


    properties.put("ciName", relayName);
    String path = "/" + instance.getOrgname() + "/" + assemblyName + "/" + environmentName;
    properties.put("nsPath", path);

    RequestSpecification request = createRequest();
    ro.setProperties(properties);

    Response newRelayResponse = request.get(transitionEnvUri(assemblyName) + environmentName + "/relays/new");
    if (newRelayResponse != null) {
      JsonPath attachmentDetails = newRelayResponse.getBody().jsonPath();
      Map<String, String> attributes = attachmentDetails.getMap("ciAttributes");
      if (attributes == null) {
        attributes = Maps.newHashMap();
      }
      attributes.put("enabled", "true");
      attributes.put("emails", emails);
      if (!Strings.isNullOrEmpty(severity))
        attributes.put("severity", severity);
      if (!Strings.isNullOrEmpty(source))
        attributes.put("source", source);
      if (!Strings.isNullOrEmpty(nsPaths))
        attributes.put("ns_paths", nsPaths);
      if (!Strings.isNullOrEmpty(regex))
        attributes.put("text_regex", regex);

      attributes.put("correlation", String.valueOf(correlation));
      ro.setAttributes(attributes);
    }

    JSONObject jsonObject = JsonUtil.createJsonObject(ro, "cms_ci");
    Response response = request.body(jsonObject.toString()).post(transitionEnvUri(assemblyName) + environmentName + "/relays");

    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to create relay with name %s due to %s", relayName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to create relay with name %s due to null response", relayName);
    throw new OneOpsClientAPIException(msg);
  }

  /**
   * Update relay
   * 
   * @param environmentName
   * @param relayName
   * @param severity
   * @param emails
   * @param source
   * @param nsPaths
   * @param regex
   * @param correlation
   * @param enable
   * @return
   * @throws OneOpsClientAPIException
   */
  public CiResource updateRelay(String assemblyName, String environmentName, String relayName, String severity, String emails, String source, String nsPaths, String regex, boolean correlation,
    boolean enable) throws OneOpsClientAPIException {
    if (environmentName == null || environmentName.length() == 0) {
      String msg = "Missing environment name to create relay";
      throw new OneOpsClientAPIException(msg);
    }

    ResourceObject ro = new ResourceObject();
    Map<String, String> attributes = Maps.newHashMap();

    if (relayName == null || relayName.length() == 0) {
      String msg = "Missing relay name to update";
      throw new OneOpsClientAPIException(msg);
    }

    RequestSpecification request = createRequest();

    Response relayResponse = request.get(transitionEnvUri(assemblyName) + environmentName + "/relays/" + relayName);
    if (relayResponse != null) {
      JsonPath newVarJsonPath = relayResponse.getBody().jsonPath();
      if (newVarJsonPath != null) {
        attributes = newVarJsonPath.getMap("ciAttributes");
        if (attributes == null) {
          attributes = Maps.newHashMap();
        } else {
          attributes.put("enabled", String.valueOf(enable));
          if (!Strings.isNullOrEmpty(emails))
            attributes.put("emails", emails);
          if (!Strings.isNullOrEmpty(severity))
            attributes.put("severity", severity);
          if (!Strings.isNullOrEmpty(source))
            attributes.put("source", source);
          if (!Strings.isNullOrEmpty(nsPaths))
            attributes.put("ns_paths", nsPaths);
          if (!Strings.isNullOrEmpty(regex))
            attributes.put("text_regex", regex);
        }
      }
    }
    ro.setAttributes(attributes);

    JSONObject jsonObject = JsonUtil.createJsonObject(ro, "cms_ci");

    Response response = request.body(jsonObject.toString()).put(transitionEnvUri(assemblyName) + environmentName + "/relays/" + relayName);

    if (response != null) {
      if (response.getStatusCode() == 200 || response.getStatusCode() == 302) {
        return response.getBody().as(CiResource.class);
      } else {
        String msg = String.format("Failed to update relay with name %s due to %s", relayName, response.getStatusLine());
        throw new OneOpsClientAPIException(msg);
      }
    }
    String msg = String.format("Failed to update relay with name %s due to null response", relayName);
    throw new OneOpsClientAPIException(msg);
  }

  private String transitionEnvUri(String assemblyName) {
    return IConstants.ASSEMBLY_URI + assemblyName + IConstants.TRANSITION_URI + IConstants.ENVIRONMENT_URI;
  }
}
