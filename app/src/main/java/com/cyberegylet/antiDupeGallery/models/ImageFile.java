package com.cyberegylet.antiDupeGallery.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

public class ImageFile implements Parcelable
{
	private final Uri path;
	private final String basename;
	private final long size;
	private final long modifiedDate;
	private final long creationDate;

	public ImageFile(Uri path)
	{
		this.path = path;
		this.basename = path.getLastPathSegment();
		try
		{
			Path p = Paths.get(path.getPath());
			BasicFileAttributes attr = Files.readAttributes(p, BasicFileAttributes.class);
			size = attr.size();
			modifiedDate = attr.lastModifiedTime().toMillis();
			creationDate = attr.creationTime().toMillis();
		}
		catch (IOException e)
		{
			throw new RuntimeException();
		}
	}

	public Uri getPath() { return path; }

	public String getBasename() { return basename; }

	public long getSize() { return size; }

	public long getModifiedDate() { return modifiedDate; }

	public long getCreationDate() { return creationDate; }

	private ImageFile(Parcel in)
	{
		String[] data = new String[5];
		in.readStringArray(data);
		this.path = Uri.parse(data[0]);
		this.basename = data[1];
		this.size = Long.parseLong(data[2]);
		this.modifiedDate = Long.parseLong(data[3]);
		this.creationDate = Long.parseLong(data[4]);
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
		dest.writeStringArray(new String[]{ path.toString(), basename, String.valueOf(size), String.valueOf(modifiedDate),
				String.valueOf(creationDate) });
	}
}
