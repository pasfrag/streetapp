package com.streetapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class LoadingActivity extends AppCompatActivity {

	private String REMEMBERME_REQUEST_URL = "https://sapp.000webhostapp.com/rememberme.php";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loading);

		final SharedPreferences preferences = this.getSharedPreferences("auth", Context.MODE_PRIVATE);
		String auth = preferences.getString("remember_me_auth", "0");
		ArrayList<String> names = new ArrayList<String>();
		names.add("rememberme");

		ArrayList<String> values = new ArrayList<String>();
		values.add(auth);

		Response.Listener<String> listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					JSONObject jsonResponse = new JSONObject(response);
					int code = jsonResponse.getInt("code");
					if (code == 200){

						String jsonAuth = jsonResponse.getString("auth");

						SharedPreferences.Editor editor = preferences.edit();
						editor.putString("remember_me_auth", jsonAuth);
						editor.apply();

						Intent intent = new Intent(LoadingActivity.this, UserAreaActivity.class);
						LoadingActivity.this.startActivity(intent);
					} else{
						Intent intent = new Intent(LoadingActivity.this, LoginActivity.class);
						LoadingActivity.this.startActivity(intent);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};

		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {

			}
		};

		HttpRequest request = new HttpRequest(REMEMBERME_REQUEST_URL, listener, errorListener, names, values);
		RequestQueue queue = Volley.newRequestQueue(LoadingActivity.this);

		int socketTimeout = 60000;//30 seconds - change to what you want
		RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
		request.setRetryPolicy(policy);

		queue.add(request);
	}
}
