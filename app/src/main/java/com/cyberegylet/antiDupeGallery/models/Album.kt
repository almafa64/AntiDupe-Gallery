package com.cyberegylet.antiDupeGallery.models;

import com.cyberegylet.antiDupeGallery.backend.FileManager;

import java.io.File;
import java.util.Objects;

public class Album extends FileEntry
{
	private ImageFile indexImage;
	private long count;
	private static long countId = 0;

	public Album(File file)
	{
		super(file, countId);
		countId++;
	}

	public Album(Album folder, boolean copyImages)
	{
		this.name = folder.name;
		this.file = folder.file;
		this.modifiedDate = folder.modifiedDate;
		this.id = folder.id;
		if (copyImages)
		{
			indexImage = folder.indexImage;
			this.size = folder.size;
			this.count = folder.count;
		}
		else
		{
			this.size = 0;
			this.count = 0;
		}
	}

	public Album(String path) { this(new File(path)); }

	public void addImage(ImageFile imageFile)
	{
		if (count == 0) indexImage = imageFile;
		size += FileManager.getSize(imageFile.getFile());
		count++;
	}

	public ImageFile getIndexImage() { return indexImage; }

	public long getCount() { return count; }

	public void setFile(File file)
	{
		String stringPath = Objects.requireNonNull(file.getPath());
		this.file = file;
		this.name = file.getName();
		this.isHidden = stringPath.contains("/.");
		this.modifiedDate = file.lastModified();
	}
}