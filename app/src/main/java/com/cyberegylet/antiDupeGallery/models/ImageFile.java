package com.cyberegylet.antiDupeGallery.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.cyberegylet.antiDupeGallery.backend.FileManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

public class ImageFile implements Parcelable
{
	private File file;
	private String name;
	private long size;
	private long modifiedDate;
	private long creationDate;
	private boolean isHidden;

	public ImageFile(String path) { this(new File(path)); }

	public ImageFile(File file)
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

	public File getFile() { return file; }
	public String getPath() { return file.getPath(); }

	public String getName() { return name; }

	public long getSize() { return size; }

	public long getModifiedDate() { return modifiedDate; }

	public long getCreationDate() { return creationDate; }

	public boolean isHidden() { return isHidden; }

	private final static int ParcelFileIndex = 0;
	private final static int ParcelNameIndex = 1;
	private final static int ParcelSizeIndex = 2;
	private final static int ParcelModDateIndex = 3;
	private final static int ParcelCreDateIndex = 4;
	private final static int ParcelHiddenIndex = 5;
	private final static int ParcelSize = ParcelHiddenIndex + 1;

	private ImageFile(Parcel in)
	{
		String[] data = new String[ParcelSize];
		in.readStringArray(data);
		this.file = new File(data[ParcelFileIndex]);
		this.name = data[ParcelNameIndex];
		this.size = Long.parseLong(data[ParcelSizeIndex]);
		this.modifiedDate = Long.parseLong(data[ParcelModDateIndex]);
		this.creationDate = Long.parseLong(data[ParcelCreDateIndex]);
		this.isHidden = Boolean.parseBoolean(data[ParcelHiddenIndex]);
	}

	public static final Creator<ImageFile> CREATOR = new Creator<ImageFile>()
	{
		@Override
		public ImageFile createFromParcel(Parcel in) { return new ImageFile(in); }

		@Override
		public ImageFile[] newArray(int size) { return new ImageFile[size]; }
	};

	@Override
	public int describeContents() { return 0; }

	@Override
	public void writeToParcel(@NonNull Parcel dest, int flags)
	{
		dest.writeStringArray(new String[]{ file.getPath(), name, String.valueOf(size), String.valueOf(modifiedDate),
				String.valueOf(creationDate), String.valueOf(isHidden) });
	}
}
