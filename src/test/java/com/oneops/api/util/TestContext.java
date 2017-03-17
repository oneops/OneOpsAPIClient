package com.oneops.api.util;

import java.util.Map;

public class TestContext {

	private String assemblyName;
	private String envName = "qa";
	private String source = "oneops";
	private String pack = "custom";
	private String version = "1";
	private String availability = "redundant";
	private Map<String, CLOUD_PRIORITY> cloudMap;
	
	public enum CLOUD_PRIORITY {
		PRIMARY(1),
		SECONDARY(2);
		
		private int priorityValue;
		CLOUD_PRIORITY(int val) {
			priorityValue = val;
		}
		
		public int getPriorityValue() {
			return this.priorityValue;
		}
	}
	
	public String getAssemblyName() {
		return assemblyName;
	}
	public void setAssemblyName(String assemblyName) {
		this.assemblyName = assemblyName;
	}
	public String getEnvName() {
		return envName;
	}
	public void setEnvName(String envName) {
		this.envName = envName;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getPack() {
		return pack;
	}
	public void setPack(String pack) {
		this.pack = pack;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getAvailability() {
		return availability;
	}
	public void setAvailability(String availability) {
		this.availability = availability;
	}
	public Map<String, CLOUD_PRIORITY> getCloudMap() {
		return cloudMap;
	}
	public void setCloudMap(Map<String, CLOUD_PRIORITY> cloudMap) {
		this.cloudMap = cloudMap;
	}
}
