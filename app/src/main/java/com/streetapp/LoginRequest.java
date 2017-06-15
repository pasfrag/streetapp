package com.streetapp;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;


public class LoginRequest extends StringRequest {

    private static final String REGISTER_REQUEST_URL = "https://sapp.000webhostapp.com/login.php";
    private Map<String, String> params;

    public LoginRequest(String email, String password, Response.Listener<String> listener,
                        Response.ErrorListener errorListener){

        super(Method.POST, REGISTER_REQUEST_URL, listener, errorListener);
        params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);

    }

    public Map<String, String> getParams(){
        return params;
    }
}
