package com.cyberegylet.antiDupeGallery.models;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Objects;

public class Folder
{
	public final ArrayList<ImageFile> images = new ArrayList<>();
	private File file;
	@NotNull
	private String name;
	private long modifiedDate;
	private long creationDate;
	private long size;
	private boolean isHidden;

	public Folder(File file)
	{
		String stringPath = Objects.requireNonNull(file.getPath());
		this.file = file;
		this.name = file.getName();
		this.isHidden = stringPath.contains("/.");
		//this.size = FileManager.getSize(file); // ToDo remove this and use the ToDo below
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

	public Folder(Folder folder, boolean copyImages)
	{
		this.name = folder.name;
		this.file = folder.file;
		this.modifiedDate = folder.modifiedDate;
		this.creationDate = folder.creationDate;
		if (copyImages)
		{
			images.addAll(folder.images);
			this.size = folder.size;
		}
		else this.size = 0;
	}

	public Folder(String path) { this(new File(path)); }
	public Folder(Folder folder) { this(folder, false); }

	public void addImage(ImageFile file)
	{
		size += file.getSize(); // ToDo make this work somehow
		images.add(file);
	}

	@NonNull
	public String getName() { return name; }

	public File getFile() { return file; }

	public String getPath() { return file.getPath(); }

	public long getSize() { return size; }

	public long getModifiedDate() { return modifiedDate; }

	public long getCreationDate() { return creationDate; }

	public boolean isHidden() { return isHidden; }
}