package com.cyberegylet.antiDupeGallery.models;

import android.net.Uri;

public class ImageFolder extends ImageFile
{
	private int fileCount;

	public ImageFolder(Uri path, int fileCount, int id, String basename)
	{
		super(path, id, basename);
		this.fileCount = fileCount;
	}
	public ImageFolder(Uri path, int fileCount, String basename) { this(path, fileCount, -1, basename); }

	public void incrementFileCount() {
		this.fileCount++;
	}

	public int getFileCount()
	{
		return fileCount;
	}
}
