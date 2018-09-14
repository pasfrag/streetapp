package com.streetapp;


import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.streetapp.Classes.HttpRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

//import com.streetapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class CloseMapFragment extends Fragment implements OnMapReadyCallback {

	private static final String EVENT_URL = "http://sapp.000webhostapp.com/geteventmap.php";
	private MapView closeMap;
	private GoogleMap googleMap;
	private ArrayList<String> titles, locations;
	private ArrayList<Long> dates;


	public CloseMapFragment() {
		// Required empty public constructor
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.fragment_close_map, container, false);
		titles = new ArrayList<>();
		locations = new ArrayList<>();
		dates = new ArrayList<>();

		closeMap = (MapView) rootView.findViewById(R.id.close_events_map);

		getEventLocations();

		return rootView;
	}

	public void getEventLocations(){
		Response.Listener listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					Log.e("Map response", response);
					JSONObject jsonResponse = new JSONObject(response);
					JSONArray locationArray = jsonResponse.getJSONArray("locations");
					JSONArray titleArray = jsonResponse.getJSONArray("titles");
					JSONArray dateArray = jsonResponse.getJSONArray("dates");
					for (int i = 0; i<locationArray.length(); i++){
						locations.add(locationArray.getString(i));
						titles.add(titleArray.getString(i));
						dates.add(Long.parseLong(dateArray.getString(i)));
					}

					addTheMarker();

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

		final RequestQueue requestQueue = Volley.newRequestQueue(this.getActivity().getBaseContext());
		int socketTimeout = 30000;
		final RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

		ArrayList<String> names = new ArrayList<>();

		ArrayList<String> values = new ArrayList<>();

		HttpRequest httpRequest = new HttpRequest(EVENT_URL, listener, errorListener, names, values);
		httpRequest.setRetryPolicy(policy);
		requestQueue.add(httpRequest);

	}

	public void addTheMarker(){
		closeMap.onCreate(null);
		closeMap.onResume();
		closeMap.getMapAsync(this);
	}

	@SuppressLint("MissingPermission")
	@Override
	public void onMapReady(GoogleMap googleMap) {
		MapsInitializer.initialize(getActivity().getBaseContext());

		googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		this.googleMap = googleMap;

		for (int i = 0; i<locations.size(); i++){
			String[] locArray = locations.get(i).split("\\|");
			android.text.format.DateFormat df = new android.text.format.DateFormat();
			String snippet = df.format("dd/MM/yyyy hh:mm a", dates.get(i)*1000).toString();

			LatLng latLng = new LatLng(Double.parseDouble(locArray[0]), Double.parseDouble(locArray[1]));
			googleMap.addMarker(new MarkerOptions()
				.position(latLng)
				.anchor(0.5f, 0.5f)
				.title(titles.get(i))
				.snippet(snippet));
		}

		final LocationManager locationManager = (LocationManager) getActivity().getBaseContext().getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				if (location.getAccuracy() < 20.0){
					locationManager.removeUpdates(this);
				}
			}

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {

			}

			@Override
			public void onProviderEnabled(String provider) {

			}

			@Override
			public void onProviderDisabled(String provider) {

			}
		});

		Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

		if (location != null){
			LatLng latLng1 = new LatLng(location.getLatitude(), location.getLongitude());
			googleMap.addMarker(new MarkerOptions()
					.position(latLng1)
					.anchor(0.5f, 0.5f)
					.title("You are here"));


			CameraPosition cameraPosition = CameraPosition.builder().target(latLng1).zoom(16).build();
			googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		}

	}
}
