package com.streetapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.support.v7.widget.Toolbar;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.streetapp.Classes.MyArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UserAreaActivity extends AppCompatActivity {

	private static final String SEARCH_URL = "http://sapp.000webhostapp.com/search.php";
	private DrawerLayout drawerLayout;
	private NavigationView navigationView;
	private AutoCompleteTextView searchACTV;
	private ArrayList<String> users, usersId, events, eventsId;
	private MyArrayAdapter<String> arrayAdapter;
	private long userId;
	private String username;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_user_area);

		SharedPreferences preferences = this.getSharedPreferences("auth", Context.MODE_PRIVATE);
		username = preferences.getString("username", "");
		userId = preferences.getLong("user_id", 0);

		Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
		setSupportActionBar(toolbar);
		ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

		searchACTV = (AutoCompleteTextView) findViewById(R.id.search_ACTV);
		arrayAdapter = new MyArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
		searchACTV.setAdapter(arrayAdapter);

		searchACTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				searchACTV.setVisibility(View.GONE);

				if (position < users.size()){
					ProfileFragment fragment = new ProfileFragment();

					Bundle bundle = new Bundle();
					bundle.putLong("user_id", Long.parseLong(usersId.get(position)));
					bundle.putString("username", users.get(position));

					fragment.setArguments(bundle);

					android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
					FragmentTransaction transaction = fragmentManager.beginTransaction();

					transaction
							.replace(R.id.fragment_container, fragment)
							.addToBackStack(null)
							.commit();

					Log.e("Fragment transaction", "Profile");
				}else {
					int correctPosition = position - users.size();
					EventFragment fragment = new EventFragment();

					Bundle bundle = new Bundle();
					bundle.putLong("event_id", Long.parseLong(eventsId.get(correctPosition)));

					fragment.setArguments(bundle);

					android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
					FragmentTransaction transaction = fragmentManager.beginTransaction();

					transaction
							.replace(R.id.fragment_container, fragment)
							.addToBackStack(null)
							.commit();

					Log.e("Fragment transaction", "Event");
				}

				uncheckMenuItems();
			}
		});

		searchACTV.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {

				String searchTerm = s.toString();

				Response.Listener listener = new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {

						try {
							Log.e("Volley response", response);
							JSONObject jsonResponse = new JSONObject(response);
							JSONArray usersJArray = jsonResponse.getJSONArray("users");
							JSONArray eventsJArray = jsonResponse.getJSONArray("events");
							JSONArray usersIDJArray = jsonResponse.getJSONArray("users_id");
							JSONArray eventsIDJArray = jsonResponse.getJSONArray("events_id");

							users = events = usersId = eventsId = null;

							users = getListFromJsonList(usersJArray);
							events= getListFromJsonList(eventsJArray);
							usersId  = getListFromJsonList(usersIDJArray);
							eventsId = getListFromJsonList(eventsIDJArray);

							arrayAdapter.clear();
							arrayAdapter.addAll(users);
							arrayAdapter.addAll(events);
							arrayAdapter.notifyDataSetChanged();

							//Log.e("Array adapter", arrayAdapter.getItem(0));

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

				final RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
				int socketTimeout = 30000;
				final RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

				ArrayList<String> names = new ArrayList<>();
				names.add("search");

				ArrayList<String> values = new ArrayList<>();
				values.add(String.valueOf(searchTerm));

				HttpRequest httpRequest = new HttpRequest(SEARCH_URL, listener, errorListener, names, values);
				httpRequest.setRetryPolicy(policy);
				requestQueue.add(httpRequest);

			}
		});

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.getMenu().getItem(0).setChecked(true);

		navigationView.setNavigationItemSelectedListener(
				new NavigationView.OnNavigationItemSelectedListener() {
					@Override
					public boolean onNavigationItemSelected(MenuItem menuItem) {

						searchACTV.setVisibility(View.GONE);
						//uncheck menu items
						uncheckMenuItems();
						// set item as selected to persist highlight
						menuItem.setChecked(true);
						// close drawer when item is tapped
						drawerLayout.closeDrawers();

						// Add code here to update the UI based on the item selected
						android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
						FragmentTransaction transaction = fragmentManager.beginTransaction();
						Fragment fragment;

						switch (menuItem.getItemId()){
							case R.id.logout:
								SharedPreferences preferences = UserAreaActivity.this.getSharedPreferences("auth", Context.MODE_PRIVATE);
								SharedPreferences.Editor editor = preferences.edit();
								editor.putString("remember_me_auth", "");
								editor.apply();
								Intent intent = new Intent(UserAreaActivity.this, LoginActivity.class);
								UserAreaActivity.this.startActivity(intent);
								break;
							case R.id.profile:
								Bundle arguments = new Bundle();
								arguments.putLong("user_id", userId);
								arguments.putString("username", username);

								searchACTV.setVisibility(View.GONE);
								fragment = new ProfileFragment();
								fragment.setArguments(arguments);
								transaction
										.replace(R.id.fragment_container, fragment)
										.addToBackStack(null)
										.commit();
								return true;
							case R.id.start_page:
								searchACTV.setVisibility(View.VISIBLE);
								fragment = new StartPageFragment();
								transaction
										.replace(R.id.fragment_container, fragment)
										.addToBackStack(null)
										.commit();
								return true;
							case R.id.map_page:
								searchACTV.setVisibility(View.GONE);
								fragment = new CloseMapFragment();
								transaction
										.replace(R.id.fragment_container, fragment)
										.addToBackStack(null)
										.commit();
								return true;
							case R.id.about_page:
								searchACTV.setVisibility(View.GONE);
								fragment = new AboutSappFragment();
								transaction
										.replace(R.id.fragment_container, fragment)
										.addToBackStack(null)
										.commit();
								return true;
							case R.id.settings_page:
								fragment = new SettingsFragment();
								transaction
										.replace(R.id.fragment_container, fragment)
										.addToBackStack(null)
										.commit();
								return true;

						}
						// For example, swap UI fragments here
						return true;
					}
				}
		);

		drawerLayout.addDrawerListener(
				new DrawerLayout.DrawerListener() {
					@Override
					public void onDrawerSlide(View drawerView, float slideOffset) {
						// Respond when the drawer's position changes
					}

					@Override
					public void onDrawerOpened(View drawerView) {
						// Respond when the drawer is opened
					}

					@Override
					public void onDrawerClosed(View drawerView) {
						// Respond when the drawer is closed
					}

					@Override
					public void onDrawerStateChanged(int newState) {
						// Respond when the drawer motion state changes
					}
				}
		);

		android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();

		StartPageFragment startPageFragment = new StartPageFragment();
		transaction
				.add(R.id.fragment_container, startPageFragment)
				.commit();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				drawerLayout.openDrawer(GravityCompat.START);
				return true;
		}
		return super.onOptionsItemSelected(item);

	}

	public void uncheckMenuItems(){
		int size = navigationView.getMenu().size();
		for (int i = 0; i < size; i++) {
			navigationView.getMenu().getItem(i).setChecked(false);
		}
	}

	private ArrayList<String> getListFromJsonList(JSONArray jsonArray){

		ArrayList<String> arrayList = new ArrayList<>();

		for (int i = 0; i < jsonArray.length(); i++){
			try {
				arrayList.add(jsonArray.getString(i));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return arrayList;

	}
}