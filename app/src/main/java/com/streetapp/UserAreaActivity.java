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
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.support.v7.widget.Toolbar;

public class UserAreaActivity extends AppCompatActivity {

	private DrawerLayout drawerLayout;
	private NavigationView navigationView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_user_area);

		Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
		setSupportActionBar(toolbar);
		ActionBar actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.getMenu().getItem(0).setChecked(true);

		navigationView.setNavigationItemSelectedListener(
				new NavigationView.OnNavigationItemSelectedListener() {
					@Override
					public boolean onNavigationItemSelected(MenuItem menuItem) {

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
								fragment = new ProfileFragment();
								transaction
										.replace(R.id.fragment_container, fragment)
										.addToBackStack(null)
										.commit();
								return true;
							case R.id.start_page:
								fragment = new StartPageFragment();
								transaction
										.replace(R.id.fragment_container, fragment)
										.addToBackStack(null)
										.commit();
								return true;
							case R.id.map_page:
								fragment = new CloseMapFragment();
								transaction
										.replace(R.id.fragment_container, fragment)
										.addToBackStack(null)
										.commit();
								return true;
							case R.id.about_page:
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
						/*if (menuItem.getItemId() == R.id.logout){
							SharedPreferences preferences = UserAreaActivity.this.getSharedPreferences("auth", Context.MODE_PRIVATE);
							SharedPreferences.Editor editor = preferences.edit();
							editor.putString("remember_me_auth", "");
							editor.apply();
							Intent intent = new Intent(UserAreaActivity.this, LoginActivity.class);
							UserAreaActivity.this.startActivity(intent);
						}*/
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
}