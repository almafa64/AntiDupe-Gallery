package com.cyberegylet.antiDupeGallery.models;

import android.net.Uri;

public class ImageFile
{
	private final Uri path;
	private final String basename;

	public ImageFile(Uri path)
	{
		this.path = path;
		this.basename = path.getLastPathSegment();
	}
	public Uri getPath() { return path; }
	public String getBasename() { return basename; }
}
