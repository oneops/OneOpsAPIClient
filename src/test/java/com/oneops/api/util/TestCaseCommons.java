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

import com.google.common.collect.Lists;
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
import com.oneops.api.resource.model.CiResource;
import com.oneops.api.resource.model.Deployment;
import com.oneops.api.resource.model.DeploymentRFC;
import com.oneops.api.resource.model.Log;
import com.oneops.api.resource.model.LogDatum;
import com.oneops.api.resource.model.Procedure;
import com.oneops.api.resource.model.Release;
import com.oneops.api.resource.model.RfcCi;
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
		
		List<CiResource> ps = design.listPlatforms();
		for (CiResource ciResource : ps) {
			platformAvailability.put(String.valueOf(ciResource.getCiId()), context.getAvailability());
		}
		
		Cloud cloud = new Cloud(instance); //use clouds from context
		if(context.getCloudMap() != null && context.getCloudMap().size() > 0) {
			for(Entry<String, CLOUD_PRIORITY> entry : context.getCloudMap().entrySet()) {
				com.oneops.api.resource.model.CiResource cloudByName = cloud.getCloud(entry.getKey());
				Long cId = cloudByName.getCiId();
				cloudMap.put(String.valueOf(cId), String.valueOf(entry.getValue().getPriorityValue()));
			}
		} else {
			List<com.oneops.api.resource.model.CiResource> clouds = cloud.listClouds(); //randomly select one of the available clouds
			Random random = new Random();
			int randomNum = random.nextInt(clouds.size());
			
			cloudMap.put(String.valueOf(clouds.get(randomNum).getCiId()), "1");
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
			transition.pullDesign(context.getEnvName());
			
			System.out.println("fetched environment with latest design " + context.getEnvName());
		} catch (Exception e) {
			transition.createEnvironment(context.getEnvName(), profile, context.getAvailability(), platformAvailability, cloudMap, false, false, "test environment for " + context.getEnvName());
			System.out.println("created environment " + context.getEnvName());
		}
	}
	
	void deploy(OOInstance instance, String assemblyName, String envName) throws OneOpsClientAPIException {
		Transition transition = new Transition(instance, assemblyName);
		String deploymentComment = "initiated deployment";
		Release latestRelease = transition.getLatestRelease(envName);
		
		if("open".equalsIgnoreCase(latestRelease.getReleaseState())) {
             transition.commitEnvironment(envName, null, deploymentComment);
        } else {
			Release bomRelease = transition.getBomRelease(envName);
			if(bomRelease != null) {
				//deploy
				Deployment deploy = transition.deploy(envName, instance.getComment());
				Long deploymentId = deploy.getDeploymentId();
				Long releaseId = deploy.getReleaseId();
				transition.getDeploymentStatus(envName, deploymentId);
				System.out.println(deploymentComment);
				waitForActiveDeployment(instance, assemblyName, envName, deploymentId, releaseId);
			}
        }
	}
	
	public void teardownEnv(OOInstance instance, String assemblyName, boolean force) throws OneOpsClientAPIException {
		Assembly assembly = new Assembly(instance);
		Transition transition = new Transition(instance, assemblyName);
		
		List<CiResource> envs = transition.listEnvironments();
		for (CiResource ciResource : envs) {
			try {
				transition.disableAllPlatforms(ciResource.getCiName());
				deploy(instance, assemblyName, ciResource.getCiName());
			} catch (Exception e) {
				if(!force) {
//					throw e;
				}
			}
		}
		
		assembly.deleteAssembly(assemblyName);
		System.out.println("deleted all resources");
	}
	
	void waitForActiveDeployment(OOInstance instance, String assembly, String env, Long deploymentId, Long releaseId) throws OneOpsClientAPIException {
		Transition transition = new Transition(instance , assembly);
		
		
		Deployment deploymentStatus = transition.getDeploymentStatus(env, deploymentId);
		String deploymentState = deploymentStatus.getDeploymentState();
		String nsPath = deploymentStatus.getNsPath();
		
		System.out.println("path " + nsPath + " deployment state: " + deploymentState);
		while("active".equals(deploymentState)) {
			Uninterruptibles.sleepUninterruptibly(20, TimeUnit.SECONDS);
			deploymentStatus = transition.getDeploymentStatus(env, deploymentId);
			deploymentState = deploymentStatus.getDeploymentState();
			
			if("failed".equals(deploymentState)) {
				transition.cancelDeployment(env, deploymentId, releaseId);
				
				DeploymentRFC deploymentrfc = transition.getDeployment(env, deploymentId);
				Set<String> errorLogs = Sets.newHashSet();
				for(RfcCi rfcCi : deploymentrfc.getRfcCis()) {
					Deployment deployment = rfcCi.getDeployment();
					if(deployment != null) {
						String deploymentRecordState = deployment.getDeploymentState();
						Long rfcId = rfcCi.getRfcId();
						String rfcName = rfcCi.getCiName();
						
						if("failed".equalsIgnoreCase(deploymentRecordState)) {
							Log log = transition.getDeploymentRfcLog(env, deploymentId, rfcId);
							errorLogs.add(rfcName + " error log: ");
							for(LogDatum l : log.getLogData()) {
								String message = l.getMessage();
								if(message.contains("STDERR") || message.contains("FATAL")) {
									int fatalIndex = message.indexOf("FATAL");
									if(fatalIndex > 0) {
										errorLogs.add(message.substring(fatalIndex));
									} else {
										errorLogs.add(message);
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
		
		List<CiResource> envs = transition.listEnvironments();
		
		
		for (CiResource env : envs) {
			String envname = env.getCiName();
			List<CiResource> platforms = transition.listPlatforms(envname);
			for (CiResource platform : platforms) {
				String platformName = platform.getCiName();
				executeAllProcedures(instance, assemblyName, envname, platformName);
				
				List<CiResource> components = transition.listPlatformComponents(envname, platformName);
				for (CiResource component : components) {
					if(skipComponents != null && skipComponents.contains(component.getCiName())) {
						continue; //skip component for any action execution
					}
					
					executeAllActions(instance, assemblyName, envname, platformName, component.getCiName(), true);
				}
			}
			
		}
		return success;
	}
	
	boolean executeAllActions(OOInstance instance, String assemblyName, String envname, String platform, String component, boolean allInstances) throws OneOpsClientAPIException {
		boolean success = true;
		
		Operation operation = new Operation(instance, assemblyName, envname);
		JsonPath actionList = operation.listActions(platform, component);
		List<CiResource> instanceList = operation.listInstances(platform, component);
		List<Long> instances = Lists.newArrayList();
		
		for(CiResource ciResource : instanceList) {
			instances.add(ciResource.getCiId());
		}
		
		if(!allInstances && instances != null && instances.size() > 1) { //execute action on only first instance
			instances.clear();
			instances.add(instanceList.get(0).getCiId());
		}
		List<String> actions = actionList.getList("actionName");
		for (String action : actions) {
			System.out.println("executing action " + action + " on " + component);
			Procedure procedureExec = operation.executeAction(platform, component, action, instances, null);
			String state = procedureExec.getProcedureState();
			Long procedureId = procedureExec.getProcedureId();
			do {
				Uninterruptibles.sleepUninterruptibly(10, TimeUnit.SECONDS);
				
				procedureExec = operation.getProcedureStatus(procedureId);
				state = procedureExec.getProcedureState();
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
		List<CiResource> procedureList = operation.listProcedures(platform);
		for (CiResource procedure : procedureList) {
			System.out.println("executing action " + procedure + " on " + platform);
			
			Procedure procedureExec = operation.executeProcedure(platform, procedure.getCiName(), null);
			String state = procedureExec.getProcedureState();
			Long procedureId = procedureExec.getProcedureId();
			do {
				Uninterruptibles.sleepUninterruptibly(10, TimeUnit.SECONDS);
				
				procedureExec = operation.getProcedureStatus(procedureId);
				state = procedureExec.getProcedureState();
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
