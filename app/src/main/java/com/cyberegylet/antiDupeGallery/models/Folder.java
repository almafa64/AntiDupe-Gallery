package com.cyberegylet.antiDupeGallery.models;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class Folder
{
	public final List<ImageFile> images = new ArrayList<>();
	public final Uri path;
	public final String name;

	public String getName()
	{
		return name;
	}

	public Folder(Uri path)
	{
		this.path = path;
		this.name = path.getLastPathSegment();
	}
}