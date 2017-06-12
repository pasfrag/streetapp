package com.streetapp;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.*;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


public class RegisterActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_register);

      final EditText usernameET = (EditText) findViewById(R.id.usernameET);
      final EditText firstNameET = (EditText) findViewById(R.id.firstnameET);
      final EditText lastNameET = (EditText) findViewById(R.id.lastnameET);
      final EditText emailET = (EditText) findViewById(R.id.emailET);
      final EditText passwordET = (EditText) findViewById(R.id.passwordET);
      final EditText ageET = (EditText) findViewById(R.id.ageET);

      final Button registerBT = (Button) findViewById(R.id.registerBT);


      registerBT.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {

              Log.e("Easy1","Starts here");
              final AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
              final String username = usernameET.getText().toString();
              Log.e("Easy1",username);
              final String firstName = firstNameET.getText().toString();
              Log.e("Easy1", firstName);
              final String lastName = lastNameET.getText().toString();
              Log.e("Easy1", lastName);
              final String password = passwordET.getText().toString();
              Log.e("Easy1", password);
              final String email = emailET.getText().toString();
              Log.e("Easy1",email);
              final int age = Integer.parseInt(ageET.getText().toString());
              Log.e("Easy1",age + "");

              Response.Listener<String> responseListener = new Response.Listener<String>(){

                  @Override
                  public void onResponse(String response) {

                      try {
                          JSONObject jsonResponse = new JSONObject(response);
                          Boolean success = jsonResponse.getBoolean("sucess");

                          Log.e("Easy", "IT 's ok but not really" + success);
                          if (success){
                              Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                              RegisterActivity.this.startActivity(intent);
                              Log.e("Easy", "IT 's ok but not really" + success);
                          }else {

                              Log.e("Easy", "IT 's not ok at all: " + success);
                              builder.setMessage("Register failed")
                                      .setNegativeButton("Retry", null)
                                      .create()
                                      .show();
                          }
                      } catch (JSONException e) {
                          e.printStackTrace();
                      }

                  }
                  public void onErrorResponse(VolleyError error) {
                      // Handle your error types accordingly.For Timeout & No connection error, you can show 'retry' button.
                      // For AuthFailure, you can re login with user credentials.
                      // In this case you can check how client is forming the api and debug accordingly.
                      // For ServerError 5xx, you can do retry or handle accordingly.
                      if( error instanceof NetworkError) {
                          Log.e("Volley Error", "Network error");
                      } else if( error instanceof ServerError) {
                          Log.e("Volley Error", "Server Error");
                      } else if( error instanceof AuthFailureError) {
                          Log.e("Volley Error", "Auth Failure Error");
                      } else if( error instanceof ParseError) {
                          Log.e("Volley Error", "Parse Error");
                      } else if( error instanceof NoConnectionError) {
                          Log.e("Volley Error", "No Connection Error");
                      } else if( error instanceof TimeoutError) {
                          Log.e("Volley Error", "Timeout Error");
                      }

                  }
              };

              RegisterRequest registerRequest = new RegisterRequest(username, firstName, lastName,
                            email, password, age, responseListener);
              RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
              queue.add(registerRequest);

          }
      });
  }
}
