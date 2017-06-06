package com.streetapp.streetapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

public class RegisterActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_register);

      final EditText usernameET = (EditText) findViewById(R.id.usernameET);
      final EditText emailET = (EditText) findViewById(R.id.emailET);
      final EditText passwordET = (EditText) findViewById(R.id.passwordET);
      final EditText ageET = (EditText) findViewById(R.id.ageET);

      final Button registerBT = (Button) findViewById(R.id.registerBT);
  }
}
