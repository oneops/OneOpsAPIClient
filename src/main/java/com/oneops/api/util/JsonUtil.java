package com.oneops.api.util;

import java.util.Map.Entry;

import org.json.JSONObject;

import com.oneops.api.ResourceObject;

public class JsonUtil {

	
	public static JSONObject createJsonObject(ResourceObject ro, String root) {
		JSONObject rootObject = new JSONObject();
		JSONObject jsonObject = new JSONObject();
		if(ro != null) {
			if(ro.getProperties() != null && ro.getProperties().size() > 0) {
				for (Entry<String, String> entry : ro.getProperties().entrySet()) {
					jsonObject.put(entry.getKey(), entry.getValue());
				}
			}
			if(ro.getAttributes() != null && ro.getAttributes().size() > 0) {
				JSONObject attr = new JSONObject();
				for (Entry<String, String> entry : ro.getAttributes().entrySet()) {
					attr.put(entry.getKey(), entry.getValue());
				}
				jsonObject.put("ciAttributes", attr);
			}
			if(ro.getOwnerProps() != null && ro.getOwnerProps().size() > 0) {
				JSONObject attr = new JSONObject();
				for (Entry<String, String> entry : ro.getOwnerProps().entrySet()) {
					attr.put(entry.getKey(), entry.getValue());
				}
				JSONObject owner = new JSONObject();
				owner.put("owner", attr);
				jsonObject.put("ciAttrProps", owner);
			}
		}
		if(root != null) {
			rootObject.put(root, jsonObject);
		} else {
			rootObject = jsonObject;
		}
		return rootObject;
	}
	
	public static JSONObject createJsonObject(String str) {
		JSONObject jsonObject = new JSONObject(str);
		return jsonObject;
	}
	
	
}
