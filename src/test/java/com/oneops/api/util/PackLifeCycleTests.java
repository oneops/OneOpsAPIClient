package com.oneops.api.util;

import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.oneops.api.OOInstance;
import com.oneops.api.exception.OneOpsClientAPIException;
import com.oneops.api.util.TestContext;
import com.oneops.api.util.TestContext.CLOUD_PRIORITY;

public class PackLifeCycleTests {

	public static void main(String[] args) {
		OOInstance instance = new OOInstance();
		instance.setAuthtoken("myToken");
		instance.setOrgname("myOrg");
		instance.setEndpoint("http://localhost:9000/");
		
		TestContext context = new TestContext();
		context.setPack("tomcat");
		context.setSource("oneops");
		context.setVersion("1");
		context.setAvailability("redundant");
		Map<String, CLOUD_PRIORITY> cloudMap = Maps.newHashMap();
		cloudMap.put("stub-cloud", CLOUD_PRIORITY.PRIMARY);
		context.setCloudMap(cloudMap );
		
		PackLifeCycleTests plct = new PackLifeCycleTests();
		plct.packTest(instance, context);
	}
	
	public void packTest(OOInstance instance, TestContext context) {
		TestCaseCommons tcc = new TestCaseCommons();
		
		String assemblyName = String.format("%s-%s-%s", context.getSource(), context.getVersion(), context.getPack());
		if(assemblyName.length() > 32) {
			assemblyName = assemblyName.substring(0, 31);
		}
		context.setAssemblyName(assemblyName);
		String fileName = String.format("%s.%s.%s.yaml", context.getSource(), context.getVersion(), context.getPack());
		
		try {
			String fileContents = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(fileName));
			if(Strings.isNullOrEmpty(fileContents)) {
				tcc.executeTestCase(instance, context);
				tcc.allActions(instance, assemblyName);
				tcc.teardownEnv(instance, assemblyName, true);
			} else {
				tcc.executeTestCase(instance, context, fileContents);
				tcc.allActions(instance, assemblyName);
				tcc.teardownEnv(instance, assemblyName, true);
			}
		} catch(Exception e) {
			try {
				tcc.executeTestCase(instance, context);
				tcc.allActions(instance, assemblyName);
				tcc.teardownEnv(instance, assemblyName, true);
			} catch (OneOpsClientAPIException e1) {
				e1.printStackTrace();
			}
			
		}
	}
}
