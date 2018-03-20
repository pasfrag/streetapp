package com.streetapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class InternetConnectivityTools {

	private Context myContext;

	public InternetConnectivityTools(Context myContext){

		this.myContext = myContext;

	}

	public boolean isOnline(){
		ConnectivityManager cm =
				(ConnectivityManager) myContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnectedOrConnecting();
	}
}
