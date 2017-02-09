package com.oneops.api.util;

import java.util.List;

import com.jayway.restassured.path.json.JsonPath;
import com.oneops.api.OOInstance;
import com.oneops.api.exception.OneOpsClientAPIException;
import com.oneops.api.resource.Account;

public class AccountTest {

	void testOrglifecycle() {
		OOInstance instance = new OOInstance();
		instance.setAuthtoken("myToken");
		instance.setOrgname("");//Keep it blank for org APIs
		instance.setEndpoint("http://localhost:9090/");
		
		try {
			Account account = new Account(instance);
			String orgName = "org-test-1";
			JsonPath response = account.createOrganization(orgName);
			System.out.println(response.getString("id"));
			
			response = account.getOrganization(orgName);
			System.out.println(response.getString("id"));
			
			response = account.listOrganizations();
			List<Object> list = response.getList("name");
			System.out.println(list);
			
			response = account.deleteOrganization(orgName);
			System.out.println(response.getString("id"));
			
		} catch (OneOpsClientAPIException e) {
			e.printStackTrace();
		}
		
	}
}
