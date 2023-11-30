package com.cyberegylet.antiDupeGallery.models;

import android.net.Uri;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Objects;

public class Folder
{
	public final ArrayList<ImageFile> images = new ArrayList<>();
	private Uri path;
	@NotNull
	private String name;
	private long size;
	private long modifiedDate;
	private long creationDate;
	private boolean isHidden;

	public Folder(Uri path)
	{
		String stringPath = Objects.requireNonNull(path.getPath());
		this.path = path;
		this.name = Objects.requireNonNull(path.getLastPathSegment());
		this.isHidden = stringPath.contains("/.");
		try
		{
			BasicFileAttributes attr = Files.readAttributes(Paths.get(stringPath), BasicFileAttributes.class);
			size = attr.size();
			modifiedDate = attr.lastModifiedTime().toMillis();
			creationDate = attr.creationTime().toMillis();
		}
		catch (IOException e)
		{
			throw new RuntimeException();
		}
	}

	public Folder(Folder folder, boolean copyImages)
	{
		this.name = folder.name;
		this.path = folder.path;
		this.size = folder.size;
		this.modifiedDate = folder.modifiedDate;
		this.creationDate = folder.creationDate;
		if (copyImages) images.addAll(folder.images);
	}

	public Folder(Folder folder) { this(folder, false); }

	@NonNull
	public String getName() { return name; }

	public Uri getPath() { return path; }

	public long getSize() { return size; }

	public long getModifiedDate() { return modifiedDate; }

	public long getCreationDate() { return creationDate; }

	public boolean isHidden() { return isHidden; }
}