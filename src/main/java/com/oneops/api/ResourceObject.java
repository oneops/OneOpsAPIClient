package com.oneops.api;

import java.util.Map;

public class ResourceObject {
	private Map<String ,String> attributes;
	private Map<String ,String> ownerProps;
	private Map<String ,String> properties;
	
	public Map<String, String> getProperties() {
		return properties;
	}
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	public Map<String, String> getOwnerProps() {
		return ownerProps;
	}
	public void setOwnerProps(Map<String, String> ownerProps) {
		this.ownerProps = ownerProps;
	}
	public Map<String, String> getAttributes() {
		return attributes;
	}
	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}
}
