package com.jcg.xmljson;

import java.io.File;

import org.json.JSONObject;

public class SearchJSONObjectResult {
	private final File file;
	private final JSONObject jsonObject;

	public SearchJSONObjectResult(File file, JSONObject jsonObject) {
		super();
		this.file = file;
		this.jsonObject = jsonObject;
	}

	public File getFile() {
		return file;
	}

	public JSONObject getJsonObject() {
		return jsonObject;
	}
}