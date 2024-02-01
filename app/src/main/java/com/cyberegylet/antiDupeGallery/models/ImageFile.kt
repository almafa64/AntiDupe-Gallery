package com.cyberegylet.antiDupeGallery.models;

import com.cyberegylet.antiDupeGallery.backend.FileManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

public class ImageFile extends FileEntry
{
	private long creationDate;
	private final String mime;

	public ImageFile(File file) { this(file, "*/*", -1); }

	public ImageFile(File file, String mime, long id)
	{
		super(file, id);
		this.mime = mime;
	}

	public long getCreationDate() { return creationDate; }

	public String getMime() { return mime; }

	public void setFile(File file)
	{
		String stringPath = file.getPath();
		this.file = file;
		this.name = file.getName();
		this.isHidden = Objects.requireNonNull(name).charAt(0) == '.';
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
}
