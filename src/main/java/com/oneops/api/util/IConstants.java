package com.oneops.api.util;

public interface IConstants {
	static String ASSEMBLY_URI = "/assemblies/";
	static String DESIGN_URI = "/design";
	static String RELEASES_URI = "/releases/";
	static String ENVIRONMENT_URI = "/environments/";
	static String TRANSITION_URI = "/transition";
	static String OPERATION_URI = "/operations";
	static String PLATFORM_URI = "/platforms/";
	static String COMPONENT_URI = "/components/";
	static String CLOUDS_URI = "/clouds/";
	static String SERVICE_URI = "/services/";
	static String ACCOUNT_URI = "/account/organizations/";
	static String ORGANIZATION_URI = "/organization/";
	static String TEAM_URI = "teams/";
	static String MEMBER_URI = "/members/";
	static String VARIABLES_URI = "/variables/";
	static String ATTACHMENTS_URI = "/attachments/";
	static String ACTIONS_URI = "/actions/";
	static String PROCEDURES_URI = "/procedures/";
	static String INSTANCES_URI = "/instances/";
	static String MONITORS_URI = "/monitors/";
	static String DEPLOYMENTS_URI = "/deployments/";

	static String DEFAULT_COMMIT_QUERY_STRING = "commit=true&capacity=true";
	static String DEFAULT_ERROR_COMMENT_PREFIX = "ERROR:BOM:";
	static String DEFAULT_CI_STATE = "default";
	static String DEFAULT_ERROR_MESSAGE = "Unable to start deployment for environment %s." +
			"Please check OneOps UI for the exact reason.";
}
