package com.oneops.api.util;

import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.oneops.api.exception.OneOpsClientAPIException;
import com.oneops.api.resource.model.CiResource;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class TransitionUtil {

    private static final Logger LOG = LoggerFactory.getLogger(TransitionUtil.class);

    public static void deployAndCheckStatus(String environmentName, String transitionEnvUri, RequestSpecification request,
                                            JSONObject jsonObject) throws OneOpsClientAPIException {
        // Start a new deployment
        Response response = doDeploy(environmentName, transitionEnvUri, request, jsonObject);
        checkForErrors(environmentName, response);

        // Wait for 5s before checking if the deployment started successfully
        try {
            Thread.sleep(10000);
        } catch(InterruptedException ie) {
            LOG.error("Error trying to delay: {}", ie.getMessage());
        }

        // Check for capacity issues and if deployment has started or failed due to capacity error.
        response = doDeploy(environmentName, transitionEnvUri, request, jsonObject);
        checkForErrors(environmentName, response);
    }

    public static Response doDeploy(String environmentName, String transitionEnvUri,
                                    RequestSpecification request, JSONObject jsonObject) throws OneOpsClientAPIException {

        Response response = request.body(jsonObject.toString()).post(transitionEnvUri + environmentName + "/deployments");
        if(response == null) {
            String msg = String.format("Failed to start deployment for environment %s " +
                    "due to null response" ,environmentName);
            throw new OneOpsClientAPIException(msg);
        }

        return response;
    }

    public static void checkForErrors(String environmentName, Response response) throws OneOpsClientAPIException {
        if(response.getStatusCode() == 200 || response.getStatusCode() == 302) {
            CiResource body = response.getBody().as(CiResource.class);
            String comment = body.getComments();
            if (comment.startsWith("ERROR:BOM:")) {
                String[] commentParts = comment.split(":");

                // When deployment API is called again to check if the deployment has started correctly
                // ignore the active deployment error message.
                // But if we are specifically checking for that error message, handle the error
                if (commentParts[2].startsWith(IConstants.EXISTING_DEPLOYMENT_MESSAGE)
                        && !IConstants.EXISTING_DEPLOYMENT_MESSAGE.equalsIgnoreCase(commentParts[2]))
                    return;

                throw new OneOpsClientAPIException(comment);
            }

        } else {
            String msg = String.format("Failed to start deployment for environment %s. %s" ,
                    environmentName, getErrorMessageFromResponse(response));
            throw new OneOpsClientAPIException(msg);
        }
    }

    public static String getErrorMessageFromResponse(Response response) {
        String errorMessage = "Error Status Code: " + response.getStatusCode() + ". Error: ";
        if (response.getBody() != null) {
            errorMessage = errorMessage + response.getBody().asString();
        } else {
            errorMessage = errorMessage + "n/a";
        }
        return errorMessage;
    }

    public static void appendListToQueryString(List<Long> list, StringBuilder queryString, String queryParameter) {
        if(list != null && list.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                sb.append(list.get(i));

                if (i < list.size() - 1)
                    sb.append(",");
            }
            if (queryString.length() > 0)
                queryString.append("&");
            queryString.append(queryParameter).append("=").append(sb.toString());
        }
    }

    public static void addStringToPropertyMap(List<Long> list, Map<String, String> properties, String parameter) {
        if (list != null && list.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                sb.append(list.get(i));

                if (i < list.size() - 1)
                    sb.append(",");
            }
            properties.put(parameter, sb.toString());
        }
    }
}
