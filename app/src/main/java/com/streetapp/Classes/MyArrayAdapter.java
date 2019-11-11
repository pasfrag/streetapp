package com.streetapp.Classes;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.List;

public class MyArrayAdapter<T> extends ArrayAdapter<T> {

	private Filter filter = new MyNoFilter();
	public List<T> items;

	@Override
	public Filter getFilter() {
		return filter;
	}

	public MyArrayAdapter(Context context, int textViewResourceId,
						 List<T> objects) {
		super(context, textViewResourceId, objects);
		Log.v("Krzys", "Adapter created " + filter);
		items = objects;
	}

	public MyArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}

	private class MyNoFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence arg0) {
			FilterResults result = new FilterResults();
			result.values = items;
			result.count = items.size();
			return result;
		}

		@Override
		protected void publishResults(CharSequence arg0, FilterResults arg1) {
			notifyDataSetChanged();
		}
	}
}
