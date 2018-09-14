package com.streetapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.streetapp.Classes.HttpRequest;
import com.streetapp.Classes.Post;
import com.streetapp.Classes.PostsAdapter;
import com.streetapp.Classes.UploadPost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.content.Context.LOCATION_SERVICE;

public class StartPageFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

	private static final String POST_URL = "http://sapp.000webhostapp.com/getposts.php";
	private static final String EVENT_URL = "http://sapp.000webhostapp.com/event.php";
	private String username;
	private long userId;
	private RecyclerView recyclerView;
	private ArrayList<Post> postsList;
	private PostsAdapter postsAdapter;
    private PopupWindow popupWindow;
    private LinearLayout linearLayout;
    private Button streetactionbutton;
    private Uri fileUri;
    private ImageView postImage;
    private Location location;
	private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
	public static final int MEDIA_TYPE_IMAGE = 1;

//	private OnFragmentInteractionListener mListener;

	public StartPageFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		final View rootView = inflater.inflate(R.layout.fragment_start_page, container, false);
		fileUri = null;
		if (savedInstanceState!=null && savedInstanceState.containsKey("file_uri")) {
			fileUri = savedInstanceState.getParcelable("file_uri");
		}

		linearLayout = (LinearLayout) rootView.findViewById(R.id.action_buttons);

		SharedPreferences preferences = this.getActivity().getBaseContext().getSharedPreferences("auth", Context.MODE_PRIVATE);
		username = preferences.getString("username", "");
		userId = preferences.getLong("user_id", 0);
		int category = preferences.getInt("category", 0);

		streetactionbutton = (Button) rootView.findViewById(R.id.streetaction);
		if (category > 0) {
			streetactionbutton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final LinearLayout buttonLayout = (LinearLayout) rootView.findViewById(R.id.action_buttons);
					if (buttonLayout.getVisibility() == View.VISIBLE) {
						buttonLayout.setVisibility(View.GONE);
					} else {
						buttonLayout.setVisibility(View.VISIBLE);

						Button postButton = (Button) rootView.findViewById(R.id.post_button);
						postButton.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								buttonLayout.setVisibility(View.GONE);
								postPopup();
							}
						});

						Button eventButton = (Button) rootView.findViewById(R.id.event_button);
						eventButton.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								buttonLayout.setVisibility(View.GONE);
								eventPopup();
							}
						});

					}
				}
			});
		}else{
			streetactionbutton.setVisibility(View.GONE);
		}

		recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
		LinearLayoutManager layoutManager = new LinearLayoutManager(this.getActivity().getBaseContext());
		recyclerView.setLayoutManager(layoutManager);
		postsList = new ArrayList<>();
		postsAdapter = new PostsAdapter(this.getContext());
		recyclerView.setAdapter(postsAdapter);


		recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
				if (!recyclerView.canScrollVertically(-1)){
					Log.e("Recycler", "Reached top");
				}
			}
		});

		populateData();

		return rootView;
	}

	private void populateData(){

		final RequestQueue requestQueue = Volley.newRequestQueue(this.getActivity().getBaseContext());
		int socketTimeout = 30000;
		final RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);


		Response.Listener listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				try{

					Log.e("json_response", response);

					JSONObject JSONResponse = new JSONObject(response);
					if (!JSONResponse.getBoolean("error")){
						JSONArray JSONPosts = JSONResponse.getJSONArray("posts");

						for (int i=0;i<JSONPosts.length();i++){
							Post post;
							JSONObject JSONData = JSONPosts.getJSONObject(i);
							JSONObject JSONPost = JSONData.getJSONObject("data");
							long postId = Long.parseLong(JSONPost.getString("post_id"));
							long userId = Long.parseLong(JSONPost.getString("user_id"));
							String postText = JSONPost.getString("text");
							String pictureId = JSONPost.getString("picture_id");
							String location = JSONPost.getString("location");
							long timestamp = Long.parseLong(JSONPost.getString("date"));
							String[] tagsList = JSONPost.getString("location").split(",");
							ArrayList<String> tags = new ArrayList<String>(Arrays.asList(tagsList));
							long eventId = Long.parseLong(JSONPost.getString("event_id"));
							String username = JSONPost.getString("username");

							JSONArray JSONlikes = JSONPost.getJSONArray("likes");
							ArrayList<String> likes = new ArrayList<String>();
							for (int j = 0; j < JSONlikes.length(); j++){
								likes.add(JSONlikes.getString(j));
							}

							JSONArray JSONcomments = JSONPost.getJSONArray("comments");
							ArrayList<String> comments = new ArrayList<String>();
							for (int j = 0; j < JSONcomments.length(); j++){
								JSONObject commentData = JSONcomments.getJSONObject(j);
								comments.add(commentData.getString("text"));
							}

							if (pictureId.equals("") && location.equals("")){
								if (eventId == 0){
									post = new Post(postId, userId, username, postText, timestamp, tags, likes, comments);
								}else{
									post = new Post(postId, userId, username, postText, timestamp, tags, likes, comments, eventId);
								}
							}else if (pictureId.equals("")){
								if (eventId == 0){
									post = new Post(postId, userId, username, postText, timestamp, tags, likes, comments, location);
								}else{
									post = new Post(postId, userId, username, postText, timestamp, tags, likes, comments, location ,eventId);
								}
							}else{
								if (eventId == 0){
									post = new Post(postId, userId, username, postText, timestamp, pictureId, tags, likes, comments);
								}else{
									post = new Post(postId, userId, username, postText, timestamp, pictureId, tags, likes, comments, eventId);
								}
							}
							postsList.add(post);
						}
					}else{
						Toast.makeText(getContext(),"There was some error in the server! " +
								"Please try again later", Toast.LENGTH_SHORT).show();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}finally {
					postsAdapter.setPostsList(postsList);
				}
			}
		};

		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if (error instanceof NetworkError) {
					Log.e("Volley Error", "Network error");
				} else if (error instanceof ServerError) {
					Log.e("Volley Error", "Server Error");
				} else if (error instanceof AuthFailureError) {
					Log.e("Volley Error", "Auth Failure Error");
				} else if (error instanceof ParseError) {
					Log.e("Volley Error", "Parse Error");
				} else if (error instanceof NoConnectionError) {
					Log.e("Volley Error", "No Connection Error");
				} else if (error instanceof TimeoutError) {
					Log.e("Volley Error", "Timeout Error");
				}
			}
		};

		ArrayList<String> names = new ArrayList<>();
		names.add("user_id");
		names.add("username");

		ArrayList<String> values = new ArrayList<>();
		values.add(String.valueOf(userId));
		values.add(username);

		HttpRequest httpRequest = new HttpRequest(POST_URL, listener, errorListener, names, values);
		httpRequest.setRetryPolicy(policy);
		requestQueue.add(httpRequest);
	}

    public void postPopup() {

        LayoutInflater layoutInflater = (LayoutInflater) getActivity()
                .getSystemService(LAYOUT_INFLATER_SERVICE);

        final View popupView = layoutInflater.inflate(R.layout.enter_post, null);
        popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.update();

		final EditText textET = (EditText) popupView.findViewById(R.id.post_text_ET);
		Button takeImageBtn = (Button) popupView.findViewById(R.id.choosepic);
		Button getLocationBtn = (Button) popupView.findViewById(R.id.chooselocation);
		Button enterPost = (Button) popupView.findViewById(R.id.enterpost);
		postImage = (ImageView) popupView.findViewById(R.id.image_upload_IV);
		postImage.setVisibility(View.GONE);
		postImage.setImageBitmap(null);

		final MapView postMap = (MapView) popupView.findViewById(R.id.map_post_MV);

		takeImageBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				postMap .setVisibility(View.GONE);
				location = null;
				captureImage();
			}
		});

		getLocationBtn.setOnClickListener(new View.OnClickListener() {
			@SuppressLint("MissingPermission")
			@Override
			public void onClick(View v) {
				String provider = Settings.Secure.getString(getActivity().getBaseContext().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

				if(!provider.contains("gps")){ //if gps is disabled
					enableGps();
				}else{

					postImage.setVisibility(View.GONE);
					postImage.setImageBitmap(null);
					fileUri = null;
					final LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, new android.location.LocationListener() {
						@Override
						public void onLocationChanged(Location location) {
							if (location.getAccuracy() < 20.0){
								locationManager.removeUpdates(this);
								addMarker(postMap, location);
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
				}
			}
		});

		enterPost.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String text = textET.getText().toString();

				if (!text.equals("")) {
					String filePath = "";
					if (fileUri != null) {
						filePath = fileUri.getPath();
					}
					String locationString = "";
					if (location != null) {
						locationString = Double.toString(location.getLatitude()) + "|" +
								Double.toString(location.getLongitude());
					}
					Log.e("Edit text", text);
					UploadPost uploadPost = new UploadPost(filePath,userId, locationString, 0, text);
					uploadPost.execute();
					popupWindow.dismiss();
				}
				else {
					Log.e("Edit text", "Empty");
				}
			}
		});

		popupWindow.showAtLocation(linearLayout, Gravity.CENTER, 0, 0);

    }

    public void eventPopup(){

		LayoutInflater layoutInflater = (LayoutInflater) getActivity()
				.getSystemService(LAYOUT_INFLATER_SERVICE);

		View popupView = layoutInflater.inflate(R.layout.enter_event, null);
		popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		popupWindow.setFocusable(true);
		popupWindow.update();

		final EditText titleET = (EditText) popupView.findViewById(R.id.enter_event_tittle);
		final EditText descriptionET = (EditText) popupView.findViewById(R.id.enter_event_description);
		final EditText locationET = (EditText) popupView.findViewById(R.id.enter_event_location);
		final LinearLayout mapLayout = (LinearLayout) popupView.findViewById(R.id.event_map_LL);
		final MapView eventMap = (MapView) popupView.findViewById(R.id.event_map);
		Button publishEvent = (Button) popupView.findViewById(R.id.publish_event);

		locationET.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {

				String location = s.toString();
				List<Address> addresses;
				Geocoder geocoder = new Geocoder(getActivity().getBaseContext(), Locale.getDefault());
				try {
					addresses = geocoder.getFromLocationName(location, 1);
					if (addresses.size() > 0) {
						Location eventLocation = new Location("event_provider");
						eventLocation.setLatitude(addresses.get(0).getLatitude());
						eventLocation.setLongitude(addresses.get(0).getLongitude());
						eventLocation.setAccuracy(0);
						mapLayout.setVisibility(View.VISIBLE);
						addMarker(eventMap, eventLocation);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		});


		publishEvent.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String title = titleET.getText().toString();
				String description = descriptionET.getText().toString();
				if (location == null || title.length() == 0 || description.length() == 0){
					Log.e("Event","Do nothing");
				}
				else{
					Log.e("Event","Upload post");
					double latitude = location.getLatitude();
					double longtitude = location.getLongitude();
					String location = latitude + "|" + longtitude;
					uploadEvent( location, description, title);
					popupWindow.dismiss();
				}
			}
		});

		popupWindow.showAtLocation(linearLayout, Gravity.CENTER, 0, 0);

	}

	public void uploadEvent(String location, String description, String title){

		this.location = null;
		long date = System.currentTimeMillis()/1000;
		ArrayList<String> names = new ArrayList<>();
		ArrayList<String> values = new ArrayList<>();

		names.add("users_id");
		names.add("location");
		names.add("date");
		names.add("description");
		names.add("title");

		values.add(Long.toString(userId));
		values.add(location);
		values.add(Long.toString(date));
		values.add(description);
		values.add(title);

		Response.Listener listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				Log.e("Event request", response);
			}
		};

		Response.ErrorListener errorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {

			}
		};

		HttpRequest request = new HttpRequest(EVENT_URL, listener, errorListener, names, values);

		RequestQueue requestQueue = Volley.newRequestQueue(this.getActivity().getBaseContext());
		int socketTimeout = 30000;
		RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

		request.setRetryPolicy(policy);
		requestQueue.add(request);

	}

	public void captureImage(){
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

		intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

		// start the image capture Intent
		startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// save file url in bundle as it will be null on screen orientation
		// changes
		outState.putParcelable("file_uri", fileUri);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// if the result is capturing Image
		if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {

				// successfully captured the image
				BitmapFactory.Options options = new BitmapFactory.Options();

				options.inSampleSize = 10;

				final Bitmap image = BitmapFactory.decodeFile(fileUri.getPath(), options);
				postImage.setImageBitmap(image);
				postImage.setVisibility(View.VISIBLE);

			} else if (resultCode == RESULT_CANCELED) {

				// user cancelled Image capture
				//Toast.makeText(getApplicationContext(),
						//"User cancelled image capture", Toast.LENGTH_SHORT)
						//.show();

			} else {
				// failed to capture image
				//Toast.makeText(getApplicationContext(),
						//"Sorry! Failed to capture image", Toast.LENGTH_SHORT)
						//.show();
			}

		}
	}


	public Uri getOutputMediaFileUri(int type) {
		return Uri.fromFile(getOutputMediaFile(type));
	}

	private static File getOutputMediaFile(int type) {

		// External sdcard location
		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"Streetapp pictures");

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("", "Oops! Failed create "
						+ "Streetapp pictures directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
				Locale.getDefault()).format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
		}else {
			return null;
		}

		return mediaFile;
	}

	public void enableGps(){
		GoogleApiClient googleApiClient = new GoogleApiClient.Builder(getActivity().getBaseContext())
				.addApi(LocationServices.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this).build();
		googleApiClient.connect();

		LocationRequest locationRequest = LocationRequest.create();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setInterval(5 * 1000);
		locationRequest.setFastestInterval(2 * 1000);
		LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
				.addLocationRequest(locationRequest);

		//**************************
		builder.setAlwaysShow(true); //this is the key ingredient
		//**************************

		PendingResult<LocationSettingsResult> result =
				LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
		result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
			@Override
			public void onResult(@NonNull LocationSettingsResult result) {
				final Status status = result.getStatus();
//                final LocationSettingsStates state = result.getLocationSettingsStates();

				switch (status.getStatusCode()) {
					case LocationSettingsStatusCodes.SUCCESS:


						break;
					case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
						// Location settings are not satisfied. But could be fixed by showing the user
						// a dialog.
						try {
							// Show the dialog by calling startResolutionForResult(),
							// and check the result in onActivityResult().
							status.startResolutionForResult(
									getActivity(), 1000);
						} catch (IntentSender.SendIntentException e) {
							// Ignore the error.
						}
						break;
					case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
						break;
				}
			}
		});
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {

	}

	@Override
	public void onConnectionSuspended(int i) {

	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

	}

	public void addMarker(MapView mapView, Location location){
		this.location = location;
		mapView.onCreate(null);
		mapView.onResume();
		mapView.setVisibility(View.VISIBLE);
		mapView.getMapAsync(this);
	}

	@Override
	public void onMapReady(final GoogleMap googleMap) {
		MapsInitializer.initialize(getActivity().getBaseContext());

		googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

		LatLng latLng = new LatLng(this.location.getLatitude(), this.location.getLongitude());

		Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng));

		CameraPosition cameraPosition = CameraPosition.builder().target(latLng).zoom(16).build();
		googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
	}

}
