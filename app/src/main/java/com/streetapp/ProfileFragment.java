package com.streetapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.squareup.picasso.Picasso;
import com.streetapp.Classes.AndroidMultiPartEntity;
import com.streetapp.Classes.HttpRequest;
import com.streetapp.Classes.IImageCompressTaskListener;
import com.streetapp.Classes.ImageCompressTask;
import com.streetapp.Classes.Post;
import com.streetapp.Classes.PostsAdapter;
import com.streetapp.Classes.UploadPost;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.app.Activity.RESULT_OK;

//import com.streetapp.R;


public class ProfileFragment extends Fragment {

	private static final String PROFILE_URL = "http://sapp.000webhostapp.com/getuserdata.php";
	private static final String POST_URL = "http://sapp.000webhostapp.com/getuserposts.php";
	private static final String DOWNLOAD_PICTURE_URL = "http://sapp.000webhostapp.com/getimage.php";
	private static final String UPLOAD_PICTURE_URL = "http://sapp.000webhostapp.com/setprofilepicture.php";
	private static final String DESCRIPTION_URL = "http://sapp.000webhostapp.com/editdescription.php";
	private static final String FOLLOW_URL = "http://sapp.000webhostapp.com/follow.php";
	private static final String FAVOURITE_URL = "http://sapp.000webhostapp.com/favourite.php";
	private TextView usernameTV, categoryTV, descriptionTV, followersTV, favouritesTV, voidTV;
	private ImageView profileImageIV;
	private EditText descriptionET;
	private Button followBtn, favouriteBtn, sendDescriptionBtn;
	private long userId;
	private String username;
	private ArrayList<String> followers, favourites;

	private RecyclerView recyclerView;
	private ArrayList<Post> postsList;
	private PostsAdapter postsAdapter;

	private String picturePath;
	private Uri imageUri;

	private static final int RESULT_LOAD_IMG = 200;
	public static final int MEDIA_TYPE_IMAGE = 1;

	private ExecutorService executorService = Executors.newFixedThreadPool(1);
	private ImageCompressTask imageCompressTask;


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

		picturePath = "";

		usernameTV = (TextView) rootView.findViewById(R.id.profile_username_tv);
		categoryTV = (TextView) rootView.findViewById(R.id.profile_art_category_tv);
		descriptionTV = (TextView) rootView.findViewById(R.id.profile_description_tv);
		followersTV = (TextView) rootView.findViewById(R.id.profile_followers_tv);
		favouritesTV = (TextView) rootView.findViewById(R.id.profile_favourites_tv);
		profileImageIV = (ImageView) rootView.findViewById(R.id.profile_pic);
		descriptionET = (EditText) rootView.findViewById(R.id.enter_description_et);
		followBtn = (Button) rootView.findViewById(R.id.profile_follow_bt);
		favouriteBtn = (Button) rootView.findViewById(R.id.profile_favourite_bt);
		sendDescriptionBtn = (Button) rootView.findViewById(R.id.action_description_btn);
		voidTV = (TextView) rootView.findViewById(R.id.voidtextView3);

		if (isCurrentUser()){
			rootView.findViewById(R.id.profile_buttons).setVisibility(View.GONE);

			descriptionTV.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {

					descriptionTV.setVisibility(View.GONE);
					String text = descriptionTV.getText().toString();
					descriptionET.setText(text);
					descriptionET.setVisibility(View.VISIBLE);
					sendDescriptionBtn.setVisibility(View.VISIBLE);
					return true;

				}
			});

			profileImageIV.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {

					Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);



					photoPickerIntent.setType("image/*");
					startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);

					return true;
				}
			});

			sendDescriptionBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!descriptionET.getText().equals("")){
						String description = descriptionET.getText().toString();

						Response.Listener listener = new Response.Listener<String>() {
							@Override
							public void onResponse(String response) {
								String description = descriptionET.getText().toString();

								descriptionTV.setText(description);
								descriptionTV.setVisibility(View.VISIBLE);
								descriptionET.setVisibility(View.GONE);
								sendDescriptionBtn.setVisibility(View.GONE);
							}
						};

						Response.ErrorListener errorListener = new Response.ErrorListener() {
							@Override
							public void onErrorResponse(VolleyError error) {
								error.printStackTrace();
							}
						};

						ArrayList<String> names = new ArrayList<>();
						ArrayList<String> values = new ArrayList<>();

						names.add("description");
						names.add("user_id");

						values.add(description);
						values.add(Long.toString(userId));

						sendRequest(DESCRIPTION_URL, listener, errorListener, names, values);

					}
				}
			});

		}else{


			SharedPreferences preferences = getActivity().getBaseContext().getSharedPreferences("auth", Context.MODE_PRIVATE);
			final long curUserId = preferences.getLong("user_id", 0);
			final String curUsername = preferences.getString("username", "nouser");

			followBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Response.Listener listener = new Response.Listener<String>() {
						@Override
						public void onResponse(String response) {
							if (followers.contains(curUsername)){
								followers.remove(curUsername);
								followBtn.setBackgroundColor(Color.GRAY);
								favourites.remove(curUsername);
								favouriteBtn.setBackgroundColor(Color.GRAY);
							}else {
								followers.add(curUsername);
								followBtn.setBackgroundColor(Color.GREEN);
							}
							followersTV.setText("Followed by " + followers.size());
							favouritesTV.setText("Favourited by " + favourites.size());
						}
					};

					Response.ErrorListener errorListener = new Response.ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
							error.printStackTrace();
						}
					};

					ArrayList<String> names = new ArrayList<>();
					ArrayList<String> values = new ArrayList<>();

					names.add("follower_id");
					names.add("following_id");

					values.add(Long.toString(curUserId));
					values.add(Long.toString(userId));

					sendRequest(FOLLOW_URL, listener, errorListener, names, values);
				}
			});

			favouriteBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (followers.contains(curUsername)) {
						Response.Listener listener = new Response.Listener<String>() {
							@Override
							public void onResponse(String response) {
								if (favourites.contains(curUsername)) {
									favourites.remove(curUsername);
									favouriteBtn.setBackgroundColor(Color.GRAY);
								} else {
									favourites.add(curUsername);
									favouriteBtn.setBackgroundColor(Color.GREEN);
								}
								favouritesTV.setText("Favourited by " + favourites.size());
							}
						};

						Response.ErrorListener errorListener = new Response.ErrorListener() {
							@Override
							public void onErrorResponse(VolleyError error) {
								error.printStackTrace();
							}
						};

						ArrayList<String> names = new ArrayList<>();
						ArrayList<String> values = new ArrayList<>();

						names.add("follower_id");
						names.add("following_id");

						values.add(Long.toString(curUserId));
						values.add(Long.toString(userId));

						sendRequest(FAVOURITE_URL, listener, errorListener, names, values);
					}
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

		SharedPreferences preferences = getActivity().getBaseContext().getSharedPreferences("auth", Context.MODE_PRIVATE);
		final long curUserId = preferences.getLong("user_id", 0);
		final String curUsername = preferences.getString("username", "nouser");

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
						}else {
							if(isCurrentUser()){
								Toast.makeText(getActivity().getBaseContext(), "Long press on description to upload a new one!", Toast.LENGTH_LONG).show();
							}
						}
						descriptionTV.setText(description);

						if (!jsonData.isNull("picture_id")){
							downloadProfileImage(jsonData.getString("picture_id"));
						}else{
							if (isCurrentUser()){
								Toast.makeText(getActivity().getBaseContext(), "Long press on picture to upload a new one!", Toast.LENGTH_LONG).show();
							}
						}

						JSONArray followersArray = jsonData.getJSONArray("followers");
						JSONArray favouritesArray = jsonData.getJSONArray("favourites");
						for (int i = 0; i<followersArray.length(); i++){
							followers.add(followersArray.getString(i));
						}

						for (int i = 0; i<favouritesArray.length(); i++){
							favourites.add(favouritesArray.getString(i));
						}

						followersTV.setText("Followed by " + followers.size());
						favouritesTV.setText("Favourited by " + favourites.size());
						favouriteBtn.setBackgroundColor(Color.GRAY);
						followBtn.setBackgroundColor(Color.GRAY);
						if (!isCurrentUser() && followers.contains(curUsername)){
							followBtn.setBackgroundColor(Color.GREEN);
							if (favourites.contains(curUsername)){
								favouriteBtn.setBackgroundColor(Color.GREEN);
							}
						}

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
							ArrayList<String> userComments = new ArrayList<>();
							for (int j = 0; j < JSONcomments.length(); j++){
								JSONObject commentData = JSONcomments.getJSONObject(j);
								comments.add(commentData.getString("text"));
								userComments.add(commentData.getString("username"));
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
							post.setUserComments(userComments);
							postsList.add(post);
						}
						if (postsList.size() == 0){
							voidTV.setVisibility(View.VISIBLE);
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

		Uri builtUri = Uri.parse(DOWNLOAD_PICTURE_URL).buildUpon()
				.appendQueryParameter("user_id", String.valueOf(userId))
				.appendQueryParameter("image", imageName)
				.build();

		Picasso.get()
				.load(builtUri.toString())
				.into(profileImageIV);

	}

	private void sendRequest(String url, Response.Listener listener, Response.ErrorListener errorListener, ArrayList<String> names, ArrayList<String> values){

		HttpRequest request = new HttpRequest(url, listener, errorListener, names, values);

		RequestQueue requestQueue = Volley.newRequestQueue(this.getActivity().getBaseContext());
		int socketTimeout = 30000;
		RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

		request.setRetryPolicy(policy);
		requestQueue.add(request);

	}

	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);


		if (resultCode == RESULT_OK) {
			try {

				imageUri = data.getData();
				picturePath = getPath( getActivity( ).getApplicationContext( ), imageUri );

				final InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);
				final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
				profileImageIV.setImageBitmap(selectedImage);
				//new UpdateProfilePic().execute();
				imageCompressTask = new ImageCompressTask(getActivity().getBaseContext(), picturePath, iImageCompressTaskListener);
				executorService.execute(imageCompressTask);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		}else {
			Log.e("Activity result", " not ok");
		}
	}

	public static String getPath( Context context, Uri uri ) {
		String result = null;
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = context.getContentResolver( ).query( uri, proj, null, null, null );
		if(cursor != null){
			if ( cursor.moveToFirst( ) ) {
				int column_index = cursor.getColumnIndexOrThrow( proj[0] );
				result = cursor.getString( column_index );
			}
			cursor.close( );
		}
		if(result == null) {
			result = "Not found";
		}
		return result;
	}

	private IImageCompressTaskListener iImageCompressTaskListener = new IImageCompressTaskListener() {
		@Override
		public void onComplete(List<File> compressed) {

			File file = compressed.get(0);

			picturePath = file.getAbsolutePath();
			new UpdateProfilePic().execute();
		}

		@Override
		public void onError(Throwable error) {
			Log.e("ImageCompressor", "Error occurred", error);
		}
	};

	public void onDestroy(){

		super.onDestroy();

		executorService.shutdown();
		executorService = null;
		imageCompressTask = null;

	}

	private class UpdateProfilePic extends AsyncTask<Void, Void, Void>{
		public UpdateProfilePic(){

		}

		@Override
		protected Void doInBackground(Void... voids) {

			String responseString = null;

			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(UPLOAD_PICTURE_URL);

			try{
				AndroidMultiPartEntity entity = new AndroidMultiPartEntity(new AndroidMultiPartEntity.ProgressListener(){

					@Override
					public void transferred(long num) {

					}
				});

				File sourceFile = new File(picturePath);
				if (!imageUri.toString().equals("")){
					entity.addPart("user_id", new StringBody(Long.toString(userId)) );
					entity.addPart("image", new FileBody(sourceFile));

					httpPost.setEntity(entity);

					HttpResponse response = httpClient.execute(httpPost);
					HttpEntity responseEntity = response.getEntity();
					InputStream inputStream = responseEntity.getContent();
					responseString = convertStreamToString(inputStream);
					Log.e("HttpResponse", responseString);

				}else{
					Toast.makeText(getActivity(), "No image provided!", Toast.LENGTH_SHORT).show();
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}

		private String convertStreamToString(InputStream is) {

			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();

			String line = null;
			try {
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return sb.toString();
		}
	}

}
