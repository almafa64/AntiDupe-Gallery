package com.cyberegylet.antiDupeGallery.models;

import androidx.annotation.NonNull;

import com.cyberegylet.antiDupeGallery.backend.FileManager;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

public class Album
{
	private ImageFile indexImage;
	@NotNull
	private String name;
	private File file;
	private long modifiedDate;
	private long creationDate;
	private long size;
	private long count;
	private boolean isHidden;

	public Album(File file)
	{
		String stringPath = Objects.requireNonNull(file.getPath());
		this.file = file;
		this.name = file.getName();
		this.isHidden = stringPath.contains("/.");
		try
		{
			BasicFileAttributes attr = Files.readAttributes(Paths.get(stringPath), BasicFileAttributes.class);
			modifiedDate = attr.lastModifiedTime().toMillis();
			creationDate = attr.creationTime().toMillis();
		}
		catch (IOException e)
		{
			throw new RuntimeException();
		}
	}

	public Album(Album folder, boolean copyImages)
	{
		this.name = folder.name;
		this.file = folder.file;
		this.modifiedDate = folder.modifiedDate;
		this.creationDate = folder.creationDate;
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

	public void addImage(File imageFile)
	{
		if (count == 0) indexImage = new ImageFile(imageFile);
		size += FileManager.getSize(imageFile);
		count++;
	}

	public ImageFile getIndexImage() { return indexImage; }

	@NonNull
	public String getName() { return name; }

	public File getFile() { return file; }

	public String getPath() { return file.getPath(); }

	public long getSize() { return size; }

	public long getCount() { return count; }

	public long getModifiedDate() { return modifiedDate; }

	public long getCreationDate() { return creationDate; }

	public boolean isHidden() { return isHidden; }
}