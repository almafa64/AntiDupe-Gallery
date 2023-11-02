package com.cyberegylet.antiDupeGallery.Models;

import android.net.Uri;

public class ImageFile
{
	public final int id;
	public final Uri uri;
	public final String name;
	public final int fileCount;

	public ImageFile(Uri uri, int id, int fileCount, String name)
	{
		this.uri = uri;
		this.id = id;
		this.fileCount = fileCount;
		this.name = name;
	}
	public ImageFile(Uri uri, String name) { this(uri, -1, -1, name); }
}
