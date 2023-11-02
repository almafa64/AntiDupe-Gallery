package com.cyberegylet.antiDupeGallery.models;

import android.net.Uri;

public class ImageFile
{
	private final int id;
	private final Uri path;
	private final String basename;

	public ImageFile(Uri path, int id, String name)
	{
		this.path = path;
		this.id = id;
		this.basename = name;
	}
	public ImageFile(Uri uri, String name) { this(uri, -1, name); }
	public Uri getPath() { return path; }
	public int getId() { return id; }
	public String getBasename() { return basename; }
}
