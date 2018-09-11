package com.streetapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class EventFragment extends Fragment implements OnMapReadyCallback{

	private static final String POST_URL = "http://sapp.000webhostapp.com/geteventposts.php";
	private static final String EVENT_URL = "http://sapp.000webhostapp.com/geteventdata.php";
	private static final String INTEREST_URL = "http://sapp.000webhostapp.com/interest.php";
	private static final String ATTEND_URL = "http://sapp.000webhostapp.com/attend.php";
	private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
	public static final int MEDIA_TYPE_IMAGE = 1;
	private long eventId;
	private TextView titleTV, descriptionTV, locationTV, attendTV, intererstTV, dateTV;
	private MapView eventMap;
	private Button postBtn, interestBtn, attendBtn;
	private RecyclerView postsRV;
	private LatLng locationLL;
	private ArrayList<Post> postsList;
	private PostsAdapter postsAdapter;
	private String username;
	private long userId;
	private Uri fileUri;
	private ImageView postImage;
	private PopupWindow popupWindow;
	private RelativeLayout relativeLayout;
	ArrayList<String> attend, interested;

	public EventFragment() {
		// Required empty public constructor
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		final  View rootView = inflater.inflate(R.layout.event, container, false);

		fileUri = null;
		if (savedInstanceState!=null && savedInstanceState.containsKey("file_uri")) {
			fileUri = savedInstanceState.getParcelable("file_uri");
		}

		Bundle arguments = getArguments();

		relativeLayout = (RelativeLayout) rootView.findViewById(R.id.event_RL);

		eventId = arguments.getLong("event_id");

		titleTV = (TextView) rootView.findViewById(R.id.evtitle);
		descriptionTV = (TextView) rootView.findViewById(R.id.evdescription);
		locationTV = (TextView) rootView.findViewById(R.id.evlocation);
		attendTV = (TextView) rootView.findViewById(R.id.attendTV);
		intererstTV = (TextView) rootView.findViewById(R.id.interestedTV);
		dateTV = (TextView) rootView.findViewById(R.id.evdate);
		eventMap = (MapView) rootView.findViewById(R.id.map_location);
		postBtn = (Button) rootView.findViewById(R.id.event_post_action);
		interestBtn = (Button) rootView.findViewById(R.id.yesbutnotsure);
		attendBtn = (Button) rootView.findViewById(R.id.iwillgo);

		SharedPreferences preferences = this.getActivity().getBaseContext().getSharedPreferences("auth", Context.MODE_PRIVATE);
		username = preferences.getString("username", "");
		userId = preferences.getLong("user_id", 0);

		postsRV = (RecyclerView) rootView.findViewById(R.id.event_posts_RV);
		LinearLayoutManager layoutManager = new LinearLayoutManager(this.getActivity().getBaseContext());
		postsRV.setLayoutManager(layoutManager);
		postsList = new ArrayList<>();
		postsAdapter = new PostsAdapter(this.getContext());
		postsRV.setAdapter(postsAdapter);

		postBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				postPopup();
			}
		});
		interestBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				Response.Listener listener = new Response.Listener() {
					@Override
					public void onResponse(Object response) {

						if (interested.contains(username)){
							interested.remove(username);
						}else {
							interested.add(username);
						}
						intererstTV.setText(interested.size() + "are interested");
					}
				};

				Response.ErrorListener errorListener = new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {

					}
				};

				final RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
				int socketTimeout = 30000;
				final RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

				ArrayList<String> names = new ArrayList<>();
				names.add("event_id");
				names.add("user_id");

				ArrayList<String> values = new ArrayList<>();
				values.add(String.valueOf(eventId));
				values.add(String.valueOf(userId));

				HttpRequest httpRequest = new HttpRequest(INTEREST_URL, listener, errorListener, names, values);
				httpRequest.setRetryPolicy(policy);
				requestQueue.add(httpRequest);
			}
		});

		attendBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				Response.Listener listener = new Response.Listener() {
					@Override
					public void onResponse(Object response) {

						if (attend.contains(username)){
							attend.remove(username);
						}else {
							attend.add(username);
						}
						attendTV.setText(attend.size() + "will go!");
					}
				};

				Response.ErrorListener errorListener = new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {

					}
				};

				final RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
				int socketTimeout = 30000;
				final RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

				ArrayList<String> names = new ArrayList<>();
				names.add("event_id");
				names.add("user_id");

				ArrayList<String> values = new ArrayList<>();
				values.add(String.valueOf(eventId));
				values.add(String.valueOf(userId));

				HttpRequest httpRequest = new HttpRequest(ATTEND_URL, listener, errorListener, names, values);
				httpRequest.setRetryPolicy(policy);
				requestQueue.add(httpRequest);
			}
		});

		populateEventData();
		populatePostData();

		return rootView;
	}

	public void  populateEventData(){

		Response.Listener listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {

				try {
					JSONObject jsonResponse = new JSONObject(response);
					if (!jsonResponse.getBoolean("error")){
						JSONObject jsonData = jsonResponse.getJSONObject("data");
						String location = jsonData.getString("location");
						long date = jsonData.getLong("date") * 1000;
						String title = jsonData.getString("title");
						String description = jsonData.getString("description");

						JSONArray JSONattend = jsonData.getJSONArray("attend");
						attend = new ArrayList<String>();
						for (int j = 0; j < JSONattend.length(); j++){
							attend.add(JSONattend.getString(j));
						}

						JSONArray JSONinterested = jsonData.getJSONArray("interested");
						interested = new ArrayList<String>();
						for (int j = 0; j < JSONinterested.length(); j++){
							interested.add(JSONinterested.getString(j));
						}

						titleTV.setText(title);
						descriptionTV.setText(description);
						attendTV.setText(attend.size() + " will attend.");
						intererstTV.setText(interested.size() + "are interested");
						android.text.format.DateFormat df = new android.text.format.DateFormat();
						dateTV.setText(df.format("dd/MM/yyyy hh:mm a", date));

						Geocoder geocoder = new Geocoder(getActivity().getBaseContext(), Locale.getDefault());
						String[] locArray = location.split("\\|");

						locationLL = new LatLng(Double.parseDouble(locArray[0]),Double.parseDouble(locArray[1]));

						List<Address> addresses = geocoder.getFromLocation(
								locationLL.latitude,
								locationLL.longitude,
								// In this sample, get just a single address.
								1);

						Address address = addresses.get(0);
						ArrayList<String> addressFragments = new ArrayList<String>();

						for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
							addressFragments.add(address.getAddressLine(i));
						}

						locationTV.setText(TextUtils.join(System.getProperty("line.separator"),
								addressFragments));
						addTheMarker(eventMap);

					}
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (IOException e) {
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
		names.add("event_id");

		ArrayList<String> values = new ArrayList<>();
		values.add(String.valueOf(eventId));

		HttpRequest httpRequest = new HttpRequest(EVENT_URL, listener, errorListener, names, values);
		httpRequest.setRetryPolicy(policy);
		requestQueue.add(httpRequest);

	}

	private void populatePostData(){

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
		names.add("event_id");

		ArrayList<String> values = new ArrayList<>();
		values.add(String.valueOf(eventId));

		HttpRequest httpRequest = new HttpRequest(POST_URL, listener, errorListener, names, values);
		httpRequest.setRetryPolicy(policy);
		requestQueue.add(httpRequest);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// save file url in bundle as it will be null on screen orientation
		// changes
		outState.putParcelable("file_uri", fileUri);
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
				postMap.setVisibility(View.GONE);
				captureImage();
			}
		});

		getLocationBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				fileUri = null;
				postImage.setVisibility(View.GONE);
				postImage.setImageBitmap(null);
				addTheMarker(postMap);

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
					if (locationLL != null) {
						locationString = Double.toString(locationLL.latitude) + "|" +
								Double.toString(locationLL.longitude);
					}
					Log.e("Edit text", text);
					UploadPost uploadPost = new UploadPost(filePath, userId, locationString, eventId, text);
					uploadPost.execute();
					popupWindow.dismiss();
				}
				else {
					Log.e("Edit text", "Empty");
				}
			}
		});

		popupWindow.showAtLocation(relativeLayout, Gravity.CENTER, 0, 0);

	}

	public void captureImage(){
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

		intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

		// start the image capture Intent
		startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);

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

	public void addTheMarker(MapView mapView){
		mapView.onCreate(null);
		mapView.onResume();
		mapView.getMapAsync(this);
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		MapsInitializer.initialize(getActivity().getBaseContext());

		googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

		googleMap.addMarker(new MarkerOptions().position(locationLL));

		CameraPosition cameraPosition = CameraPosition.builder().target(locationLL).zoom(16).build();
		googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
	}
}
