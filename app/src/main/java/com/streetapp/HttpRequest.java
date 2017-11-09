package com.streetapp;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class HttpRequest extends StringRequest {

	private Map<String, String> params;

	public HttpRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener, ArrayList<String> names, ArrayList<String> values) {
		super(Method.POST, url, listener, errorListener);

		params = new HashMap<>();
		for (int i = 0; i < names.size(); i++){
			params.put(names.get(i), values.get(i));
		}
	}

	@Override
	public Map<String, String> getParams() {
		return params;
	}
}
