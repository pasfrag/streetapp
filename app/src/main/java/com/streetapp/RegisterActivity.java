package com.streetapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.*;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class RegisterActivity extends AppCompatActivity {

	private static final String REGISTER_REQUEST_URL = "https://sapp.000webhostapp.com/signup.php";
	private EditText usernameET,  emailET, passwordET, rePasswordET, birthdateET;
	private TextView messageTV;
	private Spinner categorySP;
	private Calendar calendar;

	private int category;
	private String username, email, password, rePassword, birthDate;

	private DatePickerDialog.OnDateSetListener date;

	@Override
  	protected void onCreate(Bundle savedInstanceState) {
	  	super.onCreate(savedInstanceState);
	  	setContentView(R.layout.activity_register);

	  	usernameET = (EditText) findViewById(R.id.usernameET);
	  	rePasswordET = (EditText) findViewById(R.id.re_passwordET);
	  	emailET = (EditText) findViewById(R.id.emailET);
	  	passwordET = (EditText) findViewById(R.id.passwordET);
	  	birthdateET = (EditText) findViewById(R.id.ageET);
		categorySP = (Spinner) findViewById(R.id.categorySP);
		messageTV = (TextView) findViewById(R.id.messageTV);

		calendar = Calendar.getInstance();

		date = new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

				calendar.set(Calendar.YEAR, year);
				calendar.set(Calendar.MONTH, month);
				calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				updateLabel();

			}
		};

		final Button registerBT = (Button) findViewById(R.id.registerBT);


	  	registerBT.setOnClickListener(new View.OnClickListener() {
		  	@Override
		  	public void onClick(View view) {

			  	Log.e("Easy1","Starts here");
			  	username = usernameET.getText().toString();
			  	Log.e("Easy1",username);
			  	rePassword = rePasswordET.getText().toString();
			  	Log.e("Easy1", rePassword);
			  	password = passwordET.getText().toString();
			  	Log.e("Easy1", password);
			  	email = emailET.getText().toString();
			  	Log.e("Easy1",email);
			  	birthDate = birthdateET.getText().toString();
			  	Log.e("Easy1", birthDate);
				//if (category!=0){
					int position = categorySP.getSelectedItemPosition();
					Log.e("Category", String.valueOf(position));
					String[] array = getResources().getStringArray(R.array.category_value_array);
					category = Integer.parseInt(array[position]);
					category--;
				//}

				ArrayList<String> names = new ArrayList<String>();
				names.add("username");
				names.add("email");
				names.add("password");
				names.add("password2");
				names.add("birthday");
				names.add("category");

				ArrayList<String> values = new ArrayList<String>();
				values.add(username);
				values.add(email);
				values.add(password);
				values.add(rePassword);
				values.add(birthDate);
				values.add(String.valueOf(category));


			  	Response.Listener<String> responseListener = new Response.Listener<String>(){

				  	@Override
				  	public void onResponse(String response) {

					  	try {

							messageTV.setVisibility(View.GONE);
						  	Log.e("Working good",response);
						  	JSONObject jsonResponse = new JSONObject(response);
						  	int code = jsonResponse.getInt("code");

						  	if (code == 200){
							  	Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
							  	RegisterActivity.this.startActivity(intent);
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

			  	HttpRequest registerRequest = new HttpRequest(REGISTER_REQUEST_URL, responseListener,
						responseErrorListener, names, values);
			  	RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);

			  	int socketTimeout = 30000;
			  	RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
						DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
						DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
			  	registerRequest.setRetryPolicy(policy);

			  	queue.add(registerRequest);

		  	}
	  	});
  	}

  	public void getBirthDate(View view){

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -12);
		Date min18 = calendar.getTime();
		DatePickerDialog dialog = new DatePickerDialog(RegisterActivity.this, date, calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
		dialog.getDatePicker().setMaxDate(min18.getTime());
		dialog.show();

	}

	private void updateLabel() {
		String myFormat = "yyyy-MM-dd"; //In which you need put here
		SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

		birthdateET.setText(sdf.format(calendar.getTime()));
	}
}
