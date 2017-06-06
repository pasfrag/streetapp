package com.streetapp.streetapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

            Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(registerIntent);
        }
      });
  }
}
