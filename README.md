# OneOpsAPIClient
This library provides java client for OneOps APIs across all types of resources. Each of the resources client java class provides contextual APIs. The resources for which API client is available are:
- Account
- Assembly
- Cloud
- Design
- Transition
- Monitor
- Operation

Example code for sample routines are present in [`test`](https://gecgithub01.walmart.com/oneops/OneOpsAPIClient/tree/master/src/test/java/com/oneops/api/util) package

Routine for App life cycle is added in [`PackLifeCycleTests.java`](https://gecgithub01.walmart.com/oneops/OneOpsAPIClient/blob/master/src/test/java/com/oneops/api/util/PackLifeCycleTests.java) with steps involving
- create assembly
- create platform with or without yaml
- create environment
- start deployment
- execute actions across all component instances after successful deployment
- delete resources
- delete environment
- delete platforms
- delete assembly

Example code to commit and deploy
```
OOInstance instance = new OOInstance();
instance.setAuthtoken("myToken");
instance.setOrgname("myOrg");
instance.setEndpoint("https://localhost:9090/");

Transition transition = new Transition(instance , assembly);
Release latestRelease = transition.getLatestRelease(environment);
String releaseState = latestRelease.getReleaseState();
//commit if an open release id found
if("open".equals(releaseState)) {
	Deployment latestDeployment = transition.getLatestDeployment(environment);
	Long deploymentId = latestDeployment.getDeploymentId();
	waitForActiveDeployment(instance, assembly, environment, deploymentId);
	transition.commitEnvironment(environment, null, "test deployment");
} else {
	CiResource env = transition.getEnvironment(environment);
	String envState = env.getCiState();
	while("locked".equals(envState)) {
		Uninterruptibles.sleepUninterruptibly(20, TimeUnit.SECONDS);
		env = transition.getEnvironment(environment);
		envState = env.getCiState();
	}
}

//deploy
Deployment deploy = transition.deploy(environment, "test deploy");
Long deploymentId = deploy.getString("deploymentId");

transition.getDeploymentStatus(environment, deploymentId);
waitForActiveDeployment(instance, assembly, environment, deploymentId);
```

Example code to poll deployment state until it reaches end state
```
OOInstance instance = new OOInstance();
instance.setAuthtoken("myToken");
instance.setOrgname("myOrg");
instance.setEndpoint("https://localhost:9090/");

Transition transition = new Transition(instance , assembly);
Deployment deploymentStatus = transition.getDeploymentStatus(env, deploymentId);
String deploymentState = deploymentStatus.getDeploymentState();
while("active".equals(deploymentState)) {
	Uninterruptibles.sleepUninterruptibly(20, TimeUnit.SECONDS);
	deploymentStatus = transition.getDeploymentStatus(env, deploymentId);
	deploymentState = deploymentStatus.getDeploymentState();
	System.out.println(deploymentState);
} 

```

Example code to update variable value in Transition
```
OOInstance instance = new OOInstance();
instance.setAuthtoken("myToken");
instance.setOrgname("myOrg");
instance.setEndpoint("https://localhost:9090/");

Transition transition = new Transition(instance , assembly);

transition.updatePlatformVariable(environment, platform, "appVersion", "2.59", false);
transition.updateGlobalVariable(environment, "version", "2.59", false);
```
