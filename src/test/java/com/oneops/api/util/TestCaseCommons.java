package com.oneops.api.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Uninterruptibles;
import com.jayway.restassured.path.json.JsonPath;
import com.oneops.api.OOInstance;
import com.oneops.api.exception.OneOpsClientAPIException;
import com.oneops.api.resource.Account;
import com.oneops.api.resource.Assembly;
import com.oneops.api.resource.Cloud;
import com.oneops.api.resource.Design;
import com.oneops.api.resource.Operation;
import com.oneops.api.resource.Transition;
import com.oneops.api.util.TestContext.CLOUD_PRIORITY;

public class TestCaseCommons {
	
	private static final String GIT_AUTH_KEY = null;

	void executeTestCase(OOInstance instance, TestContext context) throws OneOpsClientAPIException {
		setupAssembly(instance, context);
		setupPlatform(instance, context);
		setupEnv(instance, context);
		deploy(instance, context.getAssemblyName(), context.getEnvName());
	}
	
	void executeTestCase(OOInstance instance, TestContext context, String fileContents) throws OneOpsClientAPIException {
		setupAssembly(instance, context);
		setupPlatform(instance, context, fileContents);
		setupEnv(instance, context);
		deploy(instance, context.getAssemblyName(), context.getEnvName());
	}
	
	void setupAssembly(OOInstance instance, TestContext context) throws OneOpsClientAPIException {
		Assembly assembly = new Assembly(instance);
		String assemblyName = context.getAssemblyName();
		try {
			assembly.getAssembly(assemblyName);
			System.out.println("fetched assembly " + assemblyName);
		} catch (Exception e1) {
			assembly.createAssembly(assemblyName, "temp@wlm.com", "test assembly for " + assemblyName, "test assembly for " + assemblyName);
			System.out.println("created assembly " + assemblyName);
		}
	}
	
	void setupPlatform(OOInstance instance, TestContext context) throws OneOpsClientAPIException {
		Design design = new Design(instance , context.getAssemblyName());
		String comment = String.format("Added platform %s", context.getPack());
		String platformName = String.format("%s-%s", context.getPack(), context.getVersion());
		design.createPlatform(platformName, context.getPack(), context.getVersion(), context.getSource(), comment, comment);
		design.commitDesign();
	}
	
	void setupPlatform(OOInstance instance, TestContext context, String fileContents) throws OneOpsClientAPIException {
		Design design = new Design(instance , context.getAssemblyName());
		design.loadFile(fileContents);
		design.commitDesign();
		System.out.println("loaded design from file ");
		
	}
	
	void setupEnv(OOInstance instance, TestContext context) throws OneOpsClientAPIException {
		Transition transition = new Transition(instance, context.getAssemblyName());
		Design design = new Design(instance , context.getAssemblyName());
		
		Map<String, String> platformAvailability = Maps.newHashMap();
		Map<String, String> cloudMap = Maps.newHashMap();
		
		JsonPath ps = design.listPlatforms();
		List<Integer> platformIds = ps.getList("ciId");
		for (Integer pid : platformIds) {
			platformAvailability.put(String.valueOf(pid), context.getAvailability());
		}
		
		Cloud cloud = new Cloud(instance); //use clouds from context
		if(context.getCloudMap() != null && context.getCloudMap().size() > 0) {
			for(Entry<String, CLOUD_PRIORITY> entry : context.getCloudMap().entrySet()) {
				JsonPath cloudByName = cloud.getCloud(entry.getKey());
				Integer cId = cloudByName.getInt("ciId");
				cloudMap.put(String.valueOf(cId), String.valueOf(entry.getValue().getPriorityValue()));
			}
		} else {
			JsonPath clouds = cloud.listClouds(); //randomly select one of the available clouds
			List<Integer> cloudIds = clouds.getList("ciId");
			Random random = new Random();
			int randomNum = random.nextInt(cloudIds.size());
			
			cloudMap.put(String.valueOf(cloudIds.get(randomNum)), "1");
		}
		
		String profile = null;
		
		try {
			Account account = new Account(instance);
			JsonPath profileList = account.listEnvironmentProfiles();
			
			List<String> profiles = profileList.getList("ciName");
			
			if(profiles != null && profiles.size() > 0) {
				profile = profiles.get(0);
			}
		} catch (Exception e1) {
			System.out.println("no env profile found");
		}
		
		try {
			transition.getEnvironment(context.getEnvName());
			transition.pullDesin(context.getEnvName());
			
			System.out.println("fetched environment with latest design " + context.getEnvName());
		} catch (Exception e) {
			transition.createEnvironment(context.getEnvName(), profile, context.getAvailability(), platformAvailability, cloudMap, false, false, "test environment for " + context.getEnvName());
			System.out.println("created environment " + context.getEnvName());
		}
	}
	
	void deploy(OOInstance instance, String assemblyName, String envName) throws OneOpsClientAPIException {
		Transition transition = new Transition(instance, assemblyName);
		String deploymentComment = "initiated deployment";
		
		transition.commitEnvironment(envName, null, deploymentComment);
		JsonPath bomRelease = transition.getBomRelease(envName);
		if(bomRelease != null) {
			//deploy
			JsonPath deploy = transition.deploy(envName, instance.getComment());
			String deploymentId = deploy.getString("deploymentId");
			String releaseId = deploy.getString("releaseId");
			transition.getDeploymentStatus(envName, deploymentId);
			System.out.println(deploymentComment);
			waitForActiveDeployment(instance, assemblyName, envName, deploymentId, releaseId);
		}
	}
	
	void teardownEnv(OOInstance instance, String assemblyName, boolean force) throws OneOpsClientAPIException {
		Assembly assembly = new Assembly(instance);
		Transition transition = new Transition(instance, assemblyName);
		
		JsonPath envs = transition.listEnvironments();
		List<String> envList = envs.getList("ciName");
		for (String envname : envList) {
			
			try {
				transition.disableAllPlatforms(envname);
				deploy(instance, assemblyName, envname);
			} catch (Exception e) {
				if(!force) {
//					throw e;
				}
			}
		}
		
		assembly.deleteAssembly(assemblyName);
		System.out.println("deleted all resources");
	}
	
	void waitForActiveDeployment(OOInstance instance, String assembly, String env, String deploymentId, String releaseId) throws OneOpsClientAPIException {
		Transition transition = new Transition(instance , assembly);
		JsonPath deploymentStatus = transition.getDeploymentStatus(env, deploymentId);
		String deploymentState = deploymentStatus.getString("deploymentState");
		String nsPath = deploymentStatus.getString("nsPath");
		
		System.out.println("path " + nsPath + " deployment state: " + deploymentState);
		while("active".equals(deploymentState)) {
			Uninterruptibles.sleepUninterruptibly(20, TimeUnit.SECONDS);
			deploymentStatus = transition.getDeploymentStatus(env, deploymentId);
			deploymentState = deploymentStatus.getString("deploymentState");
			
			if("failed".equals(deploymentState)) {
				transition.cancelDeployment(env, deploymentId, releaseId);
				
				JsonPath deployment = transition.getDeployment(env, deploymentId);
				List<Map<String, String>> rfclist = deployment.getList("rfc_cis.deployment");
				List<String> ciNames = deployment.getList("rfc_cis.ciName");
				Set<String> errorLogs = Sets.newHashSet();
				for (int r = 0; r < rfclist.size(); r++) {
					String dpmtRecordState = rfclist.get(r).get("dpmtRecordState");
					String rfcId = String.valueOf(rfclist.get(r).get("rfcId"));
					
					if("failed".equalsIgnoreCase(dpmtRecordState)) {
						errorLogs.add(ciNames.get(r) + " error log: ");
						JsonPath logs = transition.getDeploymentRfcLog(env, deploymentId, rfcId);
						List<List<String>> messages = logs.getList("logData.message");
						//parse error log for failed instances
						for (int i = 0; i < messages.size(); i++) {
							for (int j = 0; j < messages.get(i).size(); j++) {
								String log = messages.get(i).get(j);
								if(log.contains("STDERR") || log.contains("FATAL")) {
									int fatalIndex = log.indexOf("FATAL");
									if(fatalIndex > 0) {
										errorLogs.add(log.substring(fatalIndex));
									} else {
										errorLogs.add(log);
									}
									
								}
							}
						}
						
					}
				}
				
				String msg = String.format("Deployment failed for %s due to %s", nsPath, errorLogs.toString());
				throw new OneOpsClientAPIException(msg);
			}
		} 
		System.out.println("path " + nsPath + " deployment state: " + deploymentState);
	}
	
	private Set<String> getBaseComponents(String giturl, String token) {
		Set<String> baseComponentList = Sets.newHashSet();
		
		try {
			String baseFilePath = giturl + "/master/packs/base.rb";
			URL url = new URL(baseFilePath);
			URLConnection uc = url.openConnection();
			uc.setRequestProperty("Authorization", "token " + token);
			InputStream content = (InputStream) uc.getInputStream();
			BufferedReader r = new BufferedReader(new InputStreamReader(content));
			String line;
			while ((line = r.readLine()) != null) {
				if (line.startsWith("resource \"") || line.startsWith("resource '")) {
					String component = line.replace("resource ", "").replace(",", "").trim();
					baseComponentList.add(component.replace("\"", "").replace("'", ""));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return baseComponentList;
	}
	
	Set<String> getBaseComponentList() {
		if(GIT_AUTH_KEY == null) {
			return null;
		}
		return getBaseComponents("https://raw.githubusercontent.com/oneops/circuit-oneops-1", GIT_AUTH_KEY);
	}
	
	boolean allActions(OOInstance instance, String assemblyName) throws OneOpsClientAPIException {
		boolean success = true;
		Set<String> skipComponents = getBaseComponentList();
		Transition transition = new Transition(instance, assemblyName);
		
		JsonPath envs = transition.listEnvironments();
		List<String> envList = envs.getList("ciName");
		
		for (String envname : envList) {
			JsonPath platforms = transition.listPlatforms(envname);
			List<String> platformList = platforms.getList("ciName");
			for (String platform : platformList) {
				
				executeAllProcedures(instance, assemblyName, envname, platform);
				
				JsonPath components = transition.listPlatformComponents(envname, platform);
				List<String> componentList = components.getList("ciName");
				for (String component : componentList) {
					if(skipComponents != null && skipComponents.contains(component)) {
						continue; //skip component for any action execution
					}
					
					executeAllActions(instance, assemblyName, envname, platform, component, true);
				}
			}
			
		}
		return success;
	}
	
	boolean executeAllActions(OOInstance instance, String assemblyName, String envname, String platform, String component, boolean allInstances) throws OneOpsClientAPIException {
		boolean success = true;
		
		Operation operation = new Operation(instance, assemblyName, envname);
		JsonPath actionList = operation.listActions(platform, component);
		JsonPath instanceList = operation.listInstances(platform, component);
		List<String> instances = instanceList.getList("ciId");
		if(!allInstances && instances != null && instances.size() > 1) { //execute action on only first instance
			instances.clear();
			instances.add(String.valueOf(instanceList.getList("ciId").get(0)));
		}
		List<String> actions = actionList.getList("actionName");
		for (String action : actions) {
			System.out.println("executing action " + action + " on " + component);
			JsonPath procedureExec = operation.executeAction(platform, component, action, instances, null);
			String state = procedureExec.getString("procedureState");
			String procedureId = procedureExec.getString("procedureId");
			do {
				Uninterruptibles.sleepUninterruptibly(10, TimeUnit.SECONDS);
				
				procedureExec = operation.getProcedureStatus(procedureId);
				state = procedureExec.getString("procedureState");
			}
			while("active".equals(state));
			System.out.println("action " + action + " on " + component + " has state " + state);
			if("failed".equalsIgnoreCase(state)) {
				operation.cancelProcedure(procedureId);
				success &= false;
			}
		}
		
		return success;
	}
	
	boolean executeAllProcedures(OOInstance instance, String assemblyName, String envname, String platform) throws OneOpsClientAPIException {
		boolean success = true;
		
		Operation operation = new Operation(instance, assemblyName, envname);
		JsonPath procedureList = operation.listProcedures(platform);
		List<String> procedures = procedureList.getList("ciName");
		for (String procedure : procedures) {
			System.out.println("executing action " + procedure + " on " + platform);
			
			JsonPath procedureExec = operation.executeProcedure(platform, procedure, null);
			String state = procedureExec.getString("procedureState");
			String procedureId = procedureExec.getString("procedureId");
			do {
				Uninterruptibles.sleepUninterruptibly(10, TimeUnit.SECONDS);
				
				procedureExec = operation.getProcedureStatus(procedureId);
				state = procedureExec.getString("procedureState");
			}
			while("active".equals(state));
			System.out.println("procedure " + procedure + " on " + platform + " has state " + state);
			if("failed".equalsIgnoreCase(state)) {
				operation.cancelProcedure(procedureId);
				success &= false;
			}
		}
		
		return success;
	}
}
