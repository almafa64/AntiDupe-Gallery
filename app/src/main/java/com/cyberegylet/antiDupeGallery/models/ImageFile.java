package com.cyberegylet.antiDupeGallery.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class ImageFile implements Parcelable
{
	private final Uri path;
	private final String basename;

	public ImageFile(Uri path)
	{
		this.path = path;
		this.basename = path.getLastPathSegment();
	}

	public Uri getPath() { return path; }

	public String getBasename() { return basename; }

	private ImageFile(Parcel in)
	{
		String[] data = new String[2];
		in.readStringArray(data);
		this.path = Uri.parse(data[0]);
		this.basename = data[1];
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
		dest.writeStringArray(new String[]{ path.toString(), basename });
	}
}
