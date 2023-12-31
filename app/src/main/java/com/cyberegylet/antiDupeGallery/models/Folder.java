package com.cyberegylet.antiDupeGallery.models;

import androidx.annotation.NonNull;

import com.cyberegylet.antiDupeGallery.backend.FileManager;

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
	private final String name;
	private final long size;
	private final long modifiedDate;
	private final long creationDate;
	private boolean isHidden;

	public Folder(File file)
	{
		String stringPath = Objects.requireNonNull(file.getPath());
		this.file = file;
		this.name = file.getName();
		this.isHidden = stringPath.contains("/.");
		try
		{
			BasicFileAttributes attr = Files.readAttributes(Paths.get(stringPath), BasicFileAttributes.class);
			size = FileManager.getSize(file);
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
		this.size = folder.size;
		this.modifiedDate = folder.modifiedDate;
		this.creationDate = folder.creationDate;
		if (copyImages) images.addAll(folder.images);
	}

	public Folder(String path) { this(new File(path)); }
	public Folder(Folder folder) { this(folder, false); }

	@NonNull
	public String getName() { return name; }

	public File getFile() { return file; }

	public String getPath() { return file.getPath(); }

	public long getSize() { return size; }

	public long getModifiedDate() { return modifiedDate; }

	public long getCreationDate() { return creationDate; }

	public boolean isHidden() { return isHidden; }
}