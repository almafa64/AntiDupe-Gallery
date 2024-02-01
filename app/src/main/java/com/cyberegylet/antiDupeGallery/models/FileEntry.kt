package com.cyberegylet.antiDupeGallery.models;

import java.io.File;

public abstract class FileEntry
{
	protected File file;
	protected String name;
	protected long size;
	protected long modifiedDate;
	protected boolean isHidden;
	protected long id;

	public FileEntry(File file) { this(file, -1); }

	public FileEntry(File file, long id)
	{
		setFile(file);
		this.id = id;
	}

	protected FileEntry() { }

	public File getFile() { return file; }

	public String getPath() { return file.getPath(); }

	public String getName() { return name; }

	public long getSize() { return size; }

	public long getModifiedDate() { return modifiedDate; }

	public boolean isHidden() { return isHidden; }

	public long getId() { return id; }

	public abstract void setFile(File file);
}
