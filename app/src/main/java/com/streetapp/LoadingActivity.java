package com.streetapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.streetapp.Classes.HttpRequest;
import com.streetapp.Classes.InternetConnectivityTools;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoadingActivity extends AppCompatActivity {

	private String REMEMBERME_REQUEST_URL = "https://sapp.000webhostapp.com/rememberme.php";
	private static final int Permission_All = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loading);

		String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
				Manifest.permission.ACCESS_COARSE_LOCATION,
				Manifest.permission.CAMERA,
				Manifest.permission.WRITE_EXTERNAL_STORAGE,
				Manifest.permission.READ_EXTERNAL_STORAGE};

		if (!hasPermissions(this, permissions)){
			ActivityCompat.requestPermissions(this, permissions, Permission_All);
		}else {
			mainFunction();
		}

	}

	public void mainFunction(){
		InternetConnectivityTools tools = new InternetConnectivityTools(this);
		if (!tools.isOnline()){
			Intent intent = new Intent(LoadingActivity.this, LoginActivity.class);
			LoadingActivity.this.startActivity(intent);
		}

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

	public static boolean hasPermissions(Context context, String... permissions){

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			for (String permission: permissions ) {
				if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED){
					return false;
				}
			}

		}

		return true;
	}

	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
										   @NonNull int[] grantResults){
		switch (requestCode){
			case Permission_All:
				Map<String, Integer> perms = new HashMap<>();
				perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
				perms.put(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
				perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
				perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
				perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
				if (grantResults.length > 0) {
					for(int i=0;i<permissions.length;i++){
						perms.put(permissions[i], grantResults[i]);
					}
					if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
							perms.get(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
							perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
							perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
							perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
						mainFunction();
					}
					else {
						this.finish();
					}
				}else {
					this.finish();
				}
				break;
			default:
				break;
		}
	}
}
