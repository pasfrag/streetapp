package com.streetapp.Classes;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class UploadPost extends AsyncTask<Void, Void, String>{

	private static String POST_URL = "http://sapp.000webhostapp.com/post.php";
	private String filePath;
	private String text;
	private long userId;
	private String location;
	private long eventId;

	public UploadPost(String filePath, long userId, String location, long eventId, String text) {
		this.filePath = filePath;
		this.text = text;
		this.userId = userId;
		this.location = location;
		this.eventId = eventId;
	}

	@Override
	protected String doInBackground(Void... voids) {

		String responseString = null;

		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(UploadPost.POST_URL);

		try {
			AndroidMultiPartEntity entity = new AndroidMultiPartEntity(new AndroidMultiPartEntity.ProgressListener() {
				@Override
				public void transferred(long num) {

				}
			});
			File sourceFile = new File(filePath);

			if (!filePath.equals("")) {
				entity.addPart("image", new FileBody(sourceFile));
			}
			if (!location.equals("")){
				entity.addPart("location", new StringBody(location));
			}
			entity.addPart("user_id", new StringBody(Long.toString(userId)));
			entity.addPart("event_id", new StringBody(Long.toString(eventId)));
			entity.addPart("text", new StringBody(text));
			long date = System.currentTimeMillis()/1000;
			entity.addPart("date", new StringBody(Long.toString(date)));

			httpPost.setEntity(entity);

			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity responseEntity = response.getEntity();
			InputStream inputStream = responseEntity.getContent();
			responseString = convertStreamToString(inputStream);
			Log.e("HttpResponse", responseString);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String convertStreamToString(InputStream is) {

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
