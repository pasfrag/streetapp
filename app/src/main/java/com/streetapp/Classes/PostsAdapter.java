package com.streetapp.Classes;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import com.streetapp.R;

import java.util.ArrayList;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.MyViewHolder> {

	private ArrayList<Post> postsList;
	private Context context;
	private GoogleMap mGoogleMap;
	private static final String IMAGE_URL = "http://sapp.000webhostapp.com/getimage.php";
	private static final String LIKE_REQUEST_URL = "https://sapp.000webhostapp.com/like.php";
	private static final String COMMENT_REQUEST_URL = "https://sapp.000webhostapp.com/comment.php";

	public PostsAdapter(Context context){
		this.postsList = new ArrayList<Post>();
		this.context = context;
	}

	public void setPostsList(ArrayList<Post> postsList){
		this.postsList = postsList;
		notifyItemRangeChanged(0, getItemCount());
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

		View itemView = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.post, parent, false);
		return new MyViewHolder(itemView);

	}

	@Override
	public void onBindViewHolder(final MyViewHolder holder, final int position) {

		final Post post = postsList.get(holder.getAdapterPosition());

		holder.mapView.setClickable(false);

		long timestamp = post.getPostTimestamp() * 1000;
		android.text.format.DateFormat df = new android.text.format.DateFormat();
		holder.pTimeTV.setText(df.format("dd/MM/yyyy hh:mm a", timestamp));

		holder.pInfoTV.setText(post.getPostUsername() + " made a post");

		holder.pTextTV.setText(post.getPostText());

		SharedPreferences preferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
		String username = preferences.getString("username", "");
		if (post.getLikes().contains(username)) {
			holder.loveItButton.setBackgroundColor(Color.RED);
		}else {
			holder.loveItButton.setBackgroundColor(Color.GRAY);
		}

		if (post.isHasLocation()){
			holder.imageLL.setVisibility(View.GONE);
			holder.pImageView.setVisibility(View.GONE);
			holder.mapLL.setVisibility(View.VISIBLE);
			holder.addTheMarker(post);
		}else{
			if (post.getPostImage() == null) {
				Uri builtUri = Uri.parse(IMAGE_URL).buildUpon()
						.appendQueryParameter("user_id", String.valueOf(post.getPostUserId()))
						.appendQueryParameter("image", post.getPostImageName())
						.build();
				Picasso.get()
						.load(builtUri.toString())
						.into(holder.pImageView);

				holder.mapLL.setVisibility(View.GONE);
				holder.mapView.setVisibility(View.GONE);
				holder.imageLL.setVisibility(View.VISIBLE);
				holder.pImageView.setVisibility(View.VISIBLE);

			}else{
				holder.mapLL.setVisibility(View.GONE);
				holder.mapView.setVisibility(View.GONE);
				holder.pImageView.setImageBitmap(post.getPostImage());
				holder.imageLL.setVisibility(View.VISIBLE);
			}
		}

		holder.loveItButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//send like/dislike
				likePressed(holder, holder.getAdapterPosition());
			}
		});

		holder.likesTV.setText(post.numberOfLikes() + " hearts");

		holder.commentButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!holder.commentET.getText().equals("")) {
					//send comment
					commentPressed(holder, holder.getAdapterPosition());
				}
			}
		});

		holder.allCommentsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				allCommentsPressed(post, holder);
			}
		});

		ArrayList<String> comments = post.getComments();
		ArrayList<String> usernames = post.getUserComments();

		CommentAdapter commentAdapter = new CommentAdapter(context, R.layout.comments,
				comments, usernames);
		//ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context,
		//		android.R.layout.simple_list_item_1, post.getComments());
		holder.commentsLV.setAdapter(commentAdapter);

	}

	@Override
	public int getItemCount() {
		return postsList.size();
	}

	public void likePressed(MyViewHolder viewHolder, int position){
		Post post = postsList.get(position);
		SharedPreferences preferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
		String username = preferences.getString("username", "");
		if (!post.getLikes().contains(username)) {
			post.addToLikes(username);
			viewHolder.loveItButton.setBackgroundColor(Color.RED);
		}else {
			post.removeFromLikes(username);
			viewHolder.loveItButton.setBackgroundColor(Color.GRAY);
		}
		ArrayList<String> names = new ArrayList<>();
		names.add("post_id");
		names.add("user_id");

		ArrayList<String> values = new ArrayList<>();
		values.add(String.valueOf(post.getPostId()));
		values.add(String.valueOf(preferences.getLong("user_id", 0)));

		HttpRequest request = new HttpRequest(LIKE_REQUEST_URL, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {

			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {

			}
		}, names, values);
		RequestQueue queue = Volley.newRequestQueue(context);
		int socketTimeout = 60000;//30 seconds - change to what you want
		RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
		request.setRetryPolicy(policy);
		queue.add(request);

		viewHolder.likesTV.setText(post.numberOfLikes() + " hearts");
	}

	public void commentPressed(MyViewHolder viewHolder, int position){

		String text = viewHolder.commentET.getText().toString();
		if (text.length()!=0) {
			Post post = postsList.get(position);
			long timestamp = System.currentTimeMillis()/1000;
			String ts = String.valueOf(timestamp);

			SharedPreferences preferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
			long userId = preferences.getLong("user_id", 0);
			String username = preferences.getString("username", "");

			ArrayList<String> names = new ArrayList<>();
			names.add("post_id");
			names.add("user_id");
			names.add("date");
			names.add("text");

			ArrayList<String> values = new ArrayList<>();
			values.add(String.valueOf(post.getPostId()));
			values.add(String.valueOf(userId));
			values.add(ts);
			values.add(text);


			HttpRequest request = new HttpRequest(COMMENT_REQUEST_URL, new Response.Listener<String>() {
				@Override
				public void onResponse(String response) {

				}
			}, new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {

				}
			}, names, values);
			RequestQueue queue = Volley.newRequestQueue(context);
			int socketTimeout = 60000;//30 seconds - change to what you want
			RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
			request.setRetryPolicy(policy);
			queue.add(request);

			post.addToComments(text);
			post.addToUserComments(username);
			CommentAdapter adapter = (CommentAdapter) viewHolder.commentsLV.getAdapter();
			adapter.notifyDataSetChanged();

			viewHolder.commentET.setText("");
			Toast.makeText(context, "Comment uploaded!", Toast.LENGTH_LONG).show();
		}else {
			Toast.makeText(context, "You can't live an empty comment!", Toast.LENGTH_LONG).show();
		}
	}

	public void allCommentsPressed(Post post, MyViewHolder holder){

		if (post.getComments().size()>0) {
			LayoutInflater layoutInflater = (LayoutInflater) context
					.getSystemService(LAYOUT_INFLATER_SERVICE);

			final View popupView = layoutInflater.inflate(R.layout.all_comments, null);
			PopupWindow popupWindow = new PopupWindow(popupView, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			popupWindow.setFocusable(true);
			if (post.getComments().size()>5){
				popupWindow.setHeight(600);
			}
			popupWindow.update();

			ListView listView = (ListView) popupView.findViewById(R.id.comment_all_list);
			CommentAdapter commentAdapter = new CommentAdapter(context, R.layout.comments, post.getComments(), post.getUserComments());
			listView.setAdapter(commentAdapter);

			popupWindow.showAtLocation(holder.mapLL, Gravity.CENTER, 0, 0);
		}
	}

	public class MyViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback{

		TextView pTimeTV, pInfoTV, pTextTV, likesTV;
		//Spinner toolsSp;
		MapView mapView;
		ImageView pImageView;
		Button loveItButton, commentButton, allCommentsButton;
		EditText commentET;
		ListView commentsLV;
		LinearLayout mapLL, imageLL;

		Post post;

		public MyViewHolder(View itemView) {
			super(itemView);

			pTimeTV = (TextView) itemView.findViewById(R.id.posttime);
			pInfoTV = (TextView) itemView.findViewById(R.id.postinfo);
			pTextTV = (TextView) itemView.findViewById(R.id.posttext);
			likesTV = (TextView) itemView.findViewById(R.id.likesTV);
			//toolsSp = (Spinner) itemView.findViewById(R.id.tools);
			mapView = (MapView) itemView.findViewById(R.id.mapView);
			pImageView = (ImageView) itemView.findViewById(R.id.photoupload);
			loveItButton = (Button) itemView.findViewById(R.id.loveit);
			commentButton = (Button) itemView.findViewById(R.id.comment_send);
			allCommentsButton = (Button) itemView.findViewById(R.id.comment_btn);
			commentET = (EditText) itemView.findViewById(R.id.commit_et);
			commentsLV = (ListView) itemView.findViewById(R.id.comment_list);
			mapLL = (LinearLayout) itemView.findViewById(R.id.map_ll);
			imageLL = (LinearLayout) itemView.findViewById(R.id.image_ll);
		}

		public void addTheMarker(Post post){
			this.post = post;
			mapView.onCreate(null);
			mapView.onResume();
			mapView.getMapAsync(this);
		}

		@Override
		public void onMapReady(GoogleMap googleMap) {
			MapsInitializer.initialize(context);

			googleMap.getUiSettings().setAllGesturesEnabled(false);
			mGoogleMap = googleMap;
			googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

			Log.e("location_string", post.getPostLocation());
			String[] locArray = post.getPostLocation().split("\\|");
			Log.e("location_string0", locArray[0]);

			LatLng location = new LatLng(Double.parseDouble(locArray[0]),Double.parseDouble(locArray[1]));

			googleMap.addMarker(new MarkerOptions().position(location));

			CameraPosition cameraPosition = CameraPosition.builder().target(location).zoom(16).tilt(45).build();
			googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		}
	}
}
