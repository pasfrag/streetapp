package com.streetapp;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class RegisterRequest extends StringRequest{

    private static final String REGISTER_REQUEST_URL = "https://sapp.000webhostapp.com/register.php";
    private Map<String, String> params;

    public RegisterRequest(String username, String firstName, String lastName, String email,
                           String password, int age, Response.Listener<String> listener){


        super(Method.POST, REGISTER_REQUEST_URL, listener, null);
        Log.e("Easy",username);
        params = new HashMap<>();
        params.put("username", username);
        params.put("firstname", firstName);
        params.put("lastname", lastName);
        params.put("email", email);
        params.put("password", password);
        params.put("age", Integer.toString(age));

    }

    public Map<String, String> getParams(){
        return params;
    }

}
