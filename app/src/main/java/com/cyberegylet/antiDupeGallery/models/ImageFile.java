package com.cyberegylet.antiDupeGallery.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

public class ImageFile implements Parcelable
{
	private Uri path;
	private String name;
	private long size;
	private long modifiedDate;
	private long creationDate;
	private boolean isHidden;

	public ImageFile(Uri path)
	{
		String stringPath = Objects.requireNonNull(path.getPath());
		this.path = path;
		this.name = path.getLastPathSegment();
		this.isHidden = Objects.requireNonNull(name).charAt(0) == '.';
		try
		{
			BasicFileAttributes attr = Files.readAttributes(Paths.get(stringPath), BasicFileAttributes.class);
			size = attr.size();
			modifiedDate = attr.lastModifiedTime().toMillis();
			creationDate = attr.creationTime().toMillis();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e.getMessage());
		}
	}

	public Uri getPath() { return path; }

	public String getName() { return name; }

	public long getSize() { return size; }

	public long getModifiedDate() { return modifiedDate; }

	public long getCreationDate() { return creationDate; }
	public boolean isHidden() { return isHidden; }

	private ImageFile(Parcel in)
	{
		String[] data = new String[6];
		in.readStringArray(data);
		this.path = Uri.parse(data[0]);
		this.name = data[1];
		this.size = Long.parseLong(data[2]);
		this.modifiedDate = Long.parseLong(data[3]);
		this.creationDate = Long.parseLong(data[4]);
		this.isHidden = Boolean.parseBoolean(data[5]);
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
		dest.writeStringArray(new String[]{ path.toString(), name, String.valueOf(size), String.valueOf(modifiedDate),
				String.valueOf(creationDate), String.valueOf(isHidden) });
	}
}
