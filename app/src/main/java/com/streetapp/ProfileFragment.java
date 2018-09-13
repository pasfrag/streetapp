package com.streetapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.streetapp.Classes.Post;
import com.streetapp.Classes.PostsAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

//import com.streetapp.R;


public class ProfileFragment extends Fragment {

	private static final String PROFILE_URL = "http://sapp.000webhostapp.com/getuserdata.php";
	private static final String POST_URL = "http://sapp.000webhostapp.com/getuserposts.php";
	private static final String DOWNLOAD_PICTURE_URL = "";
	private static final String UPLOAD_PICTURE_URL = "";
	private static final String DESCRIPTION_URL = "";
	private static final String FOLLOW_URL = "";
	private static final String FAVOURITE_URL = "";
	private TextView usernameTV, /*fullnameTV,*/ categoryTV, descriptionTV, followersTV, favouritesTV;
	private ImageView profileImageIV;
	private EditText descriptionET;
	private Button followBtn, favouriteBtn;
	private long userId;
	private String username;
	private ArrayList<String> followers, favourites;

	private RecyclerView recyclerView;
	private ArrayList<Post> postsList;
	private PostsAdapter postsAdapter;


	public ProfileFragment() {
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
		View rootView = inflater.inflate(R.layout.profile, container, false);

		Bundle arguments = getArguments();
		userId = arguments.getLong("user_id");
		username = arguments.getString("username");

		followers =  new ArrayList<>();
		favourites = new ArrayList<>();

		usernameTV = (TextView) rootView.findViewById(R.id.profile_username_tv);
		//fullnameTV = (TextView) rootView.findViewById(R.id.profile_name_tv);
		categoryTV = (TextView) rootView.findViewById(R.id.profile_art_category_tv);
		descriptionTV = (TextView) rootView.findViewById(R.id.profile_description_tv);
		followersTV = (TextView) rootView.findViewById(R.id.profile_followers_tv);
		favouritesTV = (TextView) rootView.findViewById(R.id.profile_favourites_tv);
		profileImageIV = (ImageView) rootView.findViewById(R.id.profile_pic);
		descriptionET = (EditText) rootView.findViewById(R.id.enter_description_et);
		followBtn = (Button) rootView.findViewById(R.id.profile_follow_bt);
		favouriteBtn = (Button) rootView.findViewById(R.id.profile_favourite_bt);

		if (isCurrentUser()){
			rootView.findViewById(R.id.profile_buttons).setVisibility(View.GONE);

			descriptionTV.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {

					descriptionTV.setVisibility(View.GONE);
					String text = descriptionTV.getText().toString();
					descriptionET.setText(text);
					descriptionET.setVisibility(View.VISIBLE);
					return true;

				}
			});

		}else{
			followBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

				}
			});

			favouriteBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

				}
			});
		}

		recyclerView = (RecyclerView) rootView.findViewById(R.id.profile_posts_rv);
		LinearLayoutManager layoutManager = new LinearLayoutManager(this.getActivity().getBaseContext());
		recyclerView.setLayoutManager(layoutManager);
		postsList = new ArrayList<>();
		postsAdapter = new PostsAdapter(this.getContext());
		recyclerView.setAdapter(postsAdapter);

		populateProfile();
		populatePostData();

		return rootView;
	}

	private boolean isCurrentUser(){
		SharedPreferences preferences = this.getActivity().getBaseContext().getSharedPreferences("auth", Context.MODE_PRIVATE);
		long userId = preferences.getLong("user_id", 0);
		return this.userId == userId;
	}

	private void populateProfile(){
		Response.Listener listener = new Response.Listener<String>(){

			@Override
			public void onResponse(String response) {
				try {
					JSONObject jsonResponse = new JSONObject(response);
					if (!jsonResponse.getBoolean("error")){
						JSONObject jsonData = jsonResponse.getJSONObject("data");
						usernameTV.setText(jsonData.getString("username"));
						String[] array = getResources().getStringArray(R.array.category_name_array);
						categoryTV.setText(array[jsonData.getInt("category") + 1]);

						String description = "There is no description yet! Please add one";
						if(!jsonData.isNull("description")){
							description = jsonData.getString("description");
						}
						descriptionTV.setText(description);

						if (!jsonData.isNull("picture_id")){
							downloadProfileImage(jsonData.getString("picture_id"));
						}

						JSONArray followersArray = jsonData.getJSONArray("followers");
						JSONArray favouritesArray = jsonData.getJSONArray("favourites");
						for (int i = 0; i<followersArray.length(); i++){
							followers.add(followersArray.getString(i));
						}

						for (int i = 0; i<favouritesArray.length(); i++){
							favourites.add(favouritesArray.getString(i));
						}

						followersTV.setText(followers.size() + " follow you");
						favouritesTV.setText("Favourited by " + favourites.size());

					}
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
		names.add("user_id");

		ArrayList<String> values = new ArrayList<>();
		values.add(Long.toString(userId));

		HttpRequest httpRequest = new HttpRequest(PROFILE_URL, listener, errorListener, names, values);
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
		names.add("user_id");
		names.add("username");

		ArrayList<String> values = new ArrayList<>();
		values.add(String.valueOf(userId));
		values.add(username);

		HttpRequest httpRequest = new HttpRequest(POST_URL, listener, errorListener, names, values);
		httpRequest.setRetryPolicy(policy);
		requestQueue.add(httpRequest);
	}

	private void downloadProfileImage(String imageName){

	}

}
