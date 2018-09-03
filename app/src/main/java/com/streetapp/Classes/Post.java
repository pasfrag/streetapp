package com.streetapp.Classes;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class Post {

	private long id;
	private String url;
	private long userId;
	private String username;
	private String text;
	private String imageName;
	private Bitmap image;
	private String location;
	private Long timestamp;
	private ArrayList<String> tags;
	private ArrayList<String> likes;
	private ArrayList<String> comments;
	private long eventId;
	private boolean hasImage;
	private boolean hasLocation;
	private boolean hasEventId;

	public Post(long id, long userId, String username, String text, Long timestamp, ArrayList<String> tags, ArrayList<String> likes, ArrayList<String> comments) {
		this.id = id;
		this.userId = userId;
		this.username = username;
		this.text = text;
		this.timestamp = timestamp;
		this.tags = tags;
		this.likes = likes;
		this.comments = comments;
		this.hasImage = false;
		this.hasLocation = false;
		this.hasEventId = false;
	}

	public Post(long id, long userId, String username, String text, Long timestamp, ArrayList<String> tags, ArrayList<String> likes, ArrayList<String> comments, Long eventId) {
		this.id = id;
		this.userId = userId;
		this.username = username;
		this.text = text;
		this.timestamp = timestamp;
		this.tags = tags;
		this.likes = likes;
		this.comments = comments;
		this.eventId = eventId;
		this.hasImage = false;
		this.hasLocation = false;
		this.hasEventId = true;
	}

	public Post(long id, long userId, String username, String text, Long timestamp, ArrayList<String> tags, ArrayList<String> likes, ArrayList<String> comments, String location) {
		this.id = id;
		this.userId = userId;
		this.username = username;
		this.text = text;
		this.timestamp = timestamp;
		this.tags = tags;
		this.likes = likes;
		this.comments = comments;
		this.location = location;
		this.hasImage = false;
		this.hasLocation = true;
		this.hasEventId = false;
	}

	public Post(long id, long userId, String username, String text, Long timestamp, ArrayList<String> tags, ArrayList<String> likes, ArrayList<String> comments, String location, Long eventId) {
		this.id = id;
		this.userId = userId;
		this.username = username;
		this.text = text;
		this.timestamp = timestamp;
		this.tags = tags;
		this.likes = likes;
		this.comments = comments;
		this.location = location;
		this.eventId = eventId;
		this.hasImage = false;
		this.hasLocation = true;
		this.hasEventId = true;
	}

	public Post(long id, long userId, String username, String text, Long timestamp, String imageName, ArrayList<String> tags, ArrayList<String> likes, ArrayList<String> comments) {
		this.id = id;
		this.userId = userId;
		this.username = username;
		this.text = text;
		this.timestamp = timestamp;
		this.imageName = imageName;
		this.tags = tags;
		this.likes = likes;
		this.comments = comments;
		this.image = null;
		this.hasImage = true;
		this.hasLocation = false;
		this.hasEventId = false;
	}

	public Post(long id, long userId, String username, String text, Long timestamp, String imageName, ArrayList<String> tags, ArrayList<String> likes, ArrayList<String> comments, Long eventId) {
		this.id = id;
		this.userId = userId;
		this.username = username;
		this.text = text;
		this.timestamp = timestamp;
		this.imageName = imageName;
		this.tags = tags;
		this.likes = likes;
		this.comments = comments;
		this.eventId = eventId;
		this.image = null;
		this.hasImage = true;
		this.hasLocation = false;
		this.hasEventId = true;
	}

	public long getPostId() {
		return id;
	}

	public String getPostUrl() {
		return url;
	}

	public long getPostUserId() {
		return userId;
	}

	public String getPostText() {
		return text;
	}

	public Bitmap getPostImage() {
		return image;
	}

	public String getPostLocation() {
		return location;
	}

	public Long getPostTimestamp() {
		return timestamp;
	}

	public ArrayList<String> getPostTags() {
		return tags;
	}

	public long getPostEventId() {
		return eventId;
	}

	public String getPostImageName() {
		return imageName;
	}

	public void setPostImage(Bitmap image) {
		this.image = image;
	}

	public boolean isHasImage() {
		return hasImage;
	}

	public boolean isHasLocation() {
		return hasLocation;
	}

	public boolean isHasEventId() {
		return hasEventId;
	}

	public ArrayList<String> getLikes() {
		return likes;
	}

	public ArrayList<String> getComments() {
		return comments;
	}

	public String getPostUsername() {
		return username;
	}

	public int numberOfLikes(){
		return likes.size();
	}

	public int numberOfComments(){
		return comments.size();
	}

	public void addToLikes(String username){
		likes.add(username);
	}

	public void removeFromLikes(String username){
		likes.remove(username);
	}

	public void addToComments(String comment){
		comments.add(comment);
	}
}
