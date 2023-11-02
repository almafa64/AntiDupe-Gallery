package com.cyberegylet.antiDupeGallery;

import android.net.Uri;

public class Folder
{
	private final Uri path;
	private int fileCount;
	private final int id;
	private final String basename;

	public Folder(Uri path, int fileCount, int id, String basename)
	{
		this.path = path;
		this.fileCount = fileCount;
		this.id = id;
		this.basename = basename;
	}

	public void incrementFileCount() {
		this.fileCount++;
	}

	public Uri getPath()
	{
		return path;
	}

	public int getFileCount()
	{
		return fileCount;
	}

	public int getId()
	{
		return id;
	}

	public String getBasename()
	{
		return basename;
	}
}
