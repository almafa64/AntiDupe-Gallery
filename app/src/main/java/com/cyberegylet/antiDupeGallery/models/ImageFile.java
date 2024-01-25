package com.cyberegylet.antiDupeGallery.models;

import com.cyberegylet.antiDupeGallery.backend.FileManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

public class ImageFile
{
	private File file;
	private String name;
	private long size;
	private long modifiedDate;
	private long creationDate;
	private boolean isHidden;
	private String mime;
	private long id;

	public ImageFile(File file) { this(file, "*/*", -1); }

	public ImageFile(File file, String mime, long id)
	{
		String stringPath = file.getPath();
		this.file = file;
		this.name = file.getName();
		this.isHidden = Objects.requireNonNull(name).charAt(0) == '.';
		this.mime = mime;
		this.id = id;
		try
		{
			BasicFileAttributes attr = Files.readAttributes(Paths.get(stringPath), BasicFileAttributes.class);
			size = FileManager.getSize(file);
			modifiedDate = attr.lastModifiedTime().toMillis();
			creationDate = attr.creationTime().toMillis();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public File getFile() { return file; }

	public String getPath() { return file.getPath(); }

	public String getName() { return name; }

	public long getSize() { return size; }

	public long getModifiedDate() { return modifiedDate; }

	public long getCreationDate() { return creationDate; }

	public boolean isHidden() { return isHidden; }

	public String getMime() { return mime; }

	public long getId() { return id; }
}
