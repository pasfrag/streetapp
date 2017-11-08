package com.streetapp;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterRequest extends StringRequest{

    private static final String REGISTER_REQUEST_URL = "https://sapp.000webhostapp.com/signup.php";
    private Map<String, String> params;

    public RegisterRequest(String username, String firstName, String lastName, String email,
                           String password, int age, Response.Listener<String> listener, Response.ErrorListener errorListener){


        super(Method.POST, REGISTER_REQUEST_URL, listener, errorListener);
        Log.e("Easy",username);
        params = new HashMap<>();
        params.put("username", username);
        params.put("first_name", firstName);
        params.put("last_name", lastName);
        params.put("email", email);
        params.put("password", password);
        params.put("age", Integer.toString(age));

    }

    public Map<String, String> getParams(){
        return params;
    }

}
