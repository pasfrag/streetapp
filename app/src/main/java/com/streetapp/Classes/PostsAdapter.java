package com.streetapp.Classes;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.streetapp.R;

import java.util.ArrayList;
import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.MyViewHolder> {

	private ArrayList<Post> postsList;
	private Context context;
	private GoogleMap mGoogleMap;
	private VolleySingleton volleySingleton;
	private ImageLoader imageLoader;
	private static final String IMAGE_URL = "http://sapp.000webhostapp.com/getimage.php";

	public PostsAdapter(Context context){
		this.postsList = new ArrayList<Post>();
		this.context = context;
		volleySingleton = VolleySingleton.getInstance(context);
		imageLoader = volleySingleton.getImageLoader();
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
	public void onBindViewHolder(final MyViewHolder holder, int position) {

		final Post post = postsList.get(position);

		holder.mapView.setClickable(false);

		long timestamp = post.getPostTimestamp() * 1000;
		android.text.format.DateFormat df = new android.text.format.DateFormat();
		holder.pTimeTV.setText(df.format("dd/MM/yyyy hh:mm a", timestamp));

		holder.pInfoTV.setText(post.getPostUsername() + " made a post");

		holder.pTextTV.setText(post.getPostText());

		if (post.isHasLocation()){
			holder.mapLL.setVisibility(View.VISIBLE);
			holder.addTheMarker(post);
		}

		if (post.isHasImage()){
			Uri builtUri = Uri.parse(IMAGE_URL).buildUpon()
					.appendQueryParameter("user_id", String.valueOf(post.getPostUserId()))
					.appendQueryParameter("image", post.getPostImageName())
					.build();
			Log.e("image_post1", post.getPostUsername() + " " + post.getPostId());
			imageLoader.get(builtUri.toString(), new ImageLoader.ImageListener() {
				@Override
				public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
					post.setPostImage(response.getBitmap());
					holder.pImageView.setImageBitmap(response.getBitmap());
					holder.imageLL.setVisibility(View.VISIBLE);
					Log.e("image_post2", post.getPostUsername() + " " + post.getPostId());
				}

				@Override
				public void onErrorResponse(VolleyError error) {

				}
			});
		}

		holder.loveItButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//send like/dislike
			}
		});

		holder.likesTV.setText(post.numberOfLikes() + " hearts");

		holder.commentButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!holder.commentET.getText().equals("")) {
					//send comment
				}
			}
		});

		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context,
				android.R.layout.simple_list_item_1, post.getComments());
		holder.commentsLV.setAdapter(arrayAdapter);

	}

	@Override
	public int getItemCount() {
		return postsList.size();
	}

	public class MyViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback{

		TextView pTimeTV, pInfoTV, pTextTV, likesTV;
		Spinner toolsSp;
		MapView mapView;
		ImageView pImageView;
		Button loveItButton, commentButton;
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
			toolsSp = (Spinner) itemView.findViewById(R.id.tools);
			mapView = (MapView) itemView.findViewById(R.id.mapView);
			pImageView = (ImageView) itemView.findViewById(R.id.photoupload);
			loveItButton = (Button) itemView.findViewById(R.id.loveit);
			commentButton = (Button) itemView.findViewById(R.id.comment_send);
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
