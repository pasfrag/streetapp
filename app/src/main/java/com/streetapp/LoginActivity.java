package com.streetapp;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class LoginActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_login);

      final EditText emailET = (EditText) findViewById(R.id.emailET);
      final EditText passwordET = (EditText) findViewById(R.id.passwordET);

      final Button loginBT = (Button) findViewById(R.id.loginBT);
      final TextView registerLinkTV = (TextView) findViewById(R.id.register_linkTV);

      registerLinkTV.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent registerIntent = new Intent(LoginActivity.this, com.streetapp.RegisterActivity.class);
            startActivity(registerIntent);
        }
      });

      loginBT.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {

              final String password = passwordET.getText().toString();
              //Log.e("Easy1", password);
              final String email = emailET.getText().toString();
              //Log.e("Easy1",email);

              Response.Listener<String> responseListener = new Response.Listener<String>(){

                  @Override
                  public void onResponse(String response) {

                      try {
                          Log.e("Working good",response);
                          JSONObject jsonResponse = new JSONObject(response);
                          Boolean success = jsonResponse.getBoolean("success");

                          if (success){
                              Intent intent = new Intent(LoginActivity.this, UserAreaActivity.class);
                              LoginActivity.this.startActivity(intent);
                          }else {

                              AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                              builder.setMessage("There is no such user.")
                                      .setNegativeButton("Retry", null)
                                      .create()
                                      .show();


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

              LoginRequest registerRequest = new LoginRequest( email, password, responseListener,
                      responseErrorListener);
              RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);

              int socketTimeout = 30000;//30 seconds - change to what you want
              RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
              registerRequest.setRetryPolicy(policy);

              queue.add(registerRequest);

          }
      });
  }
}
