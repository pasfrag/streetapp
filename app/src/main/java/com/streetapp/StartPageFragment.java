package com.streetapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.TextView;
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
import com.streetapp.Classes.Post;
import com.streetapp.Classes.PostsAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;

import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class StartPageFragment extends Fragment{

	private static final String POST_URL = "http://sapp.000webhostapp.com/getposts.php";
	private String username;
	private Long userId;
	private RecyclerView recyclerView;
	private ArrayList<Post> postsList;
	private PostsAdapter postsAdapter;
    private PopupWindow popupWindow;
    private LinearLayout linearLayout;
    private Button streetactionbutton;

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

		linearLayout = (LinearLayout) rootView.findViewById(R.id.linearstreetaction);
		streetactionbutton = (Button) rootView.findViewById(R.id.streetaction);
		streetactionbutton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//StreetActionPopup(v);
				final LinearLayout buttonLayout = (LinearLayout) rootView.findViewById(R.id.action_buttons);
				if(buttonLayout.getVisibility() == View.VISIBLE){
					buttonLayout.setVisibility(View.GONE);
				}else{
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

		SharedPreferences preferences = this.getActivity().getBaseContext().getSharedPreferences("auth", Context.MODE_PRIVATE);
		username = preferences.getString("username", "");
		userId = preferences.getLong("user_id", 0);
		Log.e("username", username);
		Log.e("user_id", String.valueOf(userId));

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
								likes.add(JSONlikes.getString(i));
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

        View popupView = layoutInflater.inflate(R.layout.enter_post, null);
        popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, 650);
        popupWindow.setFocusable(true);
        popupWindow.update();

        TextView EnterPost = (TextView) popupView.findViewById(R.id.streetaction);

		popupWindow.showAtLocation(linearLayout, Gravity.CENTER, 0, 0);

    }

    public void eventPopup(){

		LayoutInflater layoutInflater = (LayoutInflater) getActivity()
				.getSystemService(LAYOUT_INFLATER_SERVICE);

		View popupView = layoutInflater.inflate(R.layout.enter_post, null);
		popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, 650);
		popupWindow.setFocusable(true);
		popupWindow.update();

		TextView EnterPost = (TextView) popupView.findViewById(R.id.streetaction);

		popupWindow.showAtLocation(linearLayout, Gravity.CENTER, 0, 0);

	}

}
