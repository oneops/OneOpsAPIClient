package com.oneops.api.util;

import java.util.List;

import com.oneops.api.OOInstance;
import com.oneops.api.exception.OneOpsClientAPIException;
import com.oneops.api.resource.Account;
import com.oneops.api.resource.model.Organization;

public class AccountTest {

	public static void main(String[] args) {
		AccountTest at = new AccountTest();
		at.testOrglifecycle();
	}
	void testOrglifecycle() {
		OOInstance instance = new OOInstance();
		instance.setAuthtoken("myToken");
		instance.setOrgname("");//Keep it blank for org APIs
		instance.setEndpoint("http://localhost:9000/");
		
		try {
			Account account = new Account(instance);
			String orgName = "org-test-1";
			Organization response = account.createOrganization(orgName);
			System.out.println(response.getId());
			
			response = account.getOrganization(orgName);
			System.out.println(response.getId());
			
			List<Organization> list = account.listOrganizations();
			System.out.println(list.size());
			
			response = account.deleteOrganization(orgName);
			System.out.println(response.getId());
			
		} catch (OneOpsClientAPIException e) {
			e.printStackTrace();
		}
		
	}
}
