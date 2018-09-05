package com.streetapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class LoginActivity extends Activity {

	private static final String LOGIN_REQUEST_URL = "https://sapp.000webhostapp.com/login.php";
	private TextView messageTV;
	private EditText emailET, passwordET;
	private CheckBox rememberCB;

  	@Override
  	protected void onCreate(Bundle savedInstanceState) {
      	super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
      	setContentView(R.layout.activity_login);


      	emailET = (EditText) findViewById(R.id.emailET);
      	passwordET = (EditText) findViewById(R.id.passwordET);
      	rememberCB = (CheckBox) findViewById(R.id.rememberme);

		final Button loginBT = (Button) findViewById(R.id.loginBT);
		messageTV = (TextView) findViewById(R.id.display_messageTV);

		loginBT.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				final String password = passwordET.getText().toString();
				boolean flag = true;
				if (password.equals("")){
					passwordET.setError("This field cannot be blank!");
					flag = false;
				}
				final String email = emailET.getText().toString();
				if (email.equals("")){
					emailET.setError("This field cannot be blank!");
                    flag = false;
				}

				if (!flag){
					return;
				}
				ArrayList<String> names = new ArrayList<>();
				names.add("email");
				names.add("password");

				ArrayList<String> values = new ArrayList<>();
				values.add(email);
				values.add(password);

				if (rememberCB.isChecked()){
					names.add("rememberme");
					values.add("checked");
				}

				Response.Listener<String> responseListener = new Response.Listener<String>(){

					@Override
					public void onResponse(String response) {

						try {
							messageTV.setVisibility(View.GONE);
							Log.e("Working good",response);
							JSONObject jsonResponse = new JSONObject(response);
							int code = jsonResponse.getInt("code");

							if (code == 200){
								SharedPreferences preferences = LoginActivity.this.getSharedPreferences("auth", Context.MODE_PRIVATE);
								SharedPreferences.Editor editor = preferences.edit();

								long id = jsonResponse.getLong("id");
								editor.putLong("user_id", id);

								String username = jsonResponse.getString("username");
								editor.putString("username", username);

								String email = jsonResponse.getString("email");
								editor.putString("email", email);

								int category = jsonResponse.getInt("category");
								editor.putInt("category", category);

								Boolean flag = jsonResponse.getBoolean("rememberme_exists");

								if (flag) {

									String auth = jsonResponse.getString("auth");
									editor.putString("remember_me_auth", auth);
								}

								editor.apply();

								Intent intent = new Intent(LoginActivity.this, UserAreaActivity.class);
								LoginActivity.this.startActivity(intent);
							}else {

								messageTV.setVisibility(View.VISIBLE);
								messageTV.setText(jsonResponse.getString("message"));

							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				};

				Response.ErrorListener responseErrorListener = new Response.ErrorListener(){

					public void onErrorResponse(VolleyError error) {
						if( error instanceof NetworkError) {
							Log.e("Volley Error", "Network error");
						} else if( error instanceof ServerError) {
							Log.e("Volley Error", "Server Error");
						} else if( error instanceof AuthFailureError) {
							Log.e("Volley Error", "Auth Failure Error");
						} else if( error instanceof ParseError) {
							Log.e("Volley Error", "Parse Error");
						} /*else if( error instanceof NoConnectionError) {
							Log.e("Volley Error", "No Connection Error");
						}*/ else if( error instanceof TimeoutError) {
							Log.e("Volley Error", "Timeout Error");
						}

					}
				};

				HttpRequest registerRequest = new HttpRequest( LOGIN_REQUEST_URL , responseListener,
						  responseErrorListener, names, values);
				RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);

				int socketTimeout = 60000;//30 seconds - change to what you want
				RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
				registerRequest.setRetryPolicy(policy);

				queue.add(registerRequest);

			}
      	});
  	}

  	public void registerClick(View view){

		Intent registerIntent = new Intent(LoginActivity.this, com.streetapp.RegisterActivity.class);
		startActivity(registerIntent);

	}

}
