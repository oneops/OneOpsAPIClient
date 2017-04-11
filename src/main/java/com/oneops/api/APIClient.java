package com.oneops.api;

import org.apache.commons.codec.binary.Base64;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.SSLConfig;
import com.jayway.restassured.specification.RequestSpecification;
import com.oneops.api.exception.OneOpsClientAPIException;

public abstract class APIClient {
	
	OOInstance instance;

	public APIClient(OOInstance instance) throws OneOpsClientAPIException {
		this.instance = instance;
		if(instance == null) {
			throw new OneOpsClientAPIException("Missing OneOps instance information to perform API invocation");
		}
		if(instance.getAuthtoken() == null) {
			throw new OneOpsClientAPIException("Missing OneOps authentication API key to perform API invocation");
		}
		if(instance.getEndpoint() == null) {
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
		if(instance.getOrgname() != null) {
			baseUri += instance.getOrgname();
		}
		rs.baseUri(baseUri);
		rs.config(RestAssured.config().sslConfig(
				new SSLConfig().relaxedHTTPSValidation()));
		return rs;
	}
	
	
}
