package com.entryrise.lagassist.updater;

import java.util.Base64;

public class UpdateInfo {

	// INFO HOLDERS
	private String title;
	private String description;

	// IDENTIFIERS
	private String version;
	private long date;
	private long id;

	private boolean unsafe = false;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = html2text(new String(Base64.getDecoder().decode(description)));
		;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getLikes() {
		return likes;
	}

	public void setLikes(int likes) {
		this.likes = likes;
	}

	public int getDownloads() {
		return downloads;
	}

	public void setDownloads(int downloads) {
		this.downloads = downloads;
	}

	public float getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	// UPDATE QUALITY DATA
	private int likes;
	private int downloads;
	private int rating;

	public UpdateInfo() {
	}

	private static String html2text(String html) {
		return html.replaceAll("\\<[^>]*>", "");
	}

	public UpdateInfo(String title, String description, String version, long date, int id, int likes, int downloads,
			int rating) {
		// Show all the data required for analysis by the plugin.
		this.title = title;
		this.description = html2text(new String(Base64.getDecoder().decode(description)));
		this.version = version;
		this.date = date;
		this.id = id;

		// Data required for analysing databastanagaur.
		this.likes = likes;
		this.downloads = downloads;

		if (rating == 0) {
			rating = 5;
		}

		this.rating = rating;

		// Check if it is unsafe
		if (this.description.contains("(!) UNSAFE VERSION")) {
			unsafe = true;
		}
	}

	public boolean isUnsafe() {
		return unsafe;
	}

}
