package com.streetapp.Classes;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.streetapp.R;

import java.util.List;

public class CommentAdapter extends ArrayAdapter<String> {

	private int resourceLayout;
	private Context mContext;
	private List<String> comments, userComments;

	public CommentAdapter(Context context, int resourceLayout, List<String> comments, List<String> userComments){
		super(context, resourceLayout, comments);
		this.resourceLayout = resourceLayout;
		this.mContext = context;
		this.comments = comments;
		this.userComments = userComments;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){

		View v = convertView;

		if (v == null) {
			LayoutInflater vi;
			vi = LayoutInflater.from(mContext);
			v = vi.inflate(resourceLayout, parent, false);
		}


		String text = getItem(position);//comments.get(position);
		String username = userComments.get(position);
		Log.e("Text", text);

		TextView usernameTV = (TextView) v.findViewById(R.id.comment_username);
		TextView textTV = (TextView) v.findViewById(R.id.comment_text);

		usernameTV.setText(username);
		textTV.setText(text);

		return v;
	}

	@Override
	public int getCount() {
		return comments.size();
	}
}
