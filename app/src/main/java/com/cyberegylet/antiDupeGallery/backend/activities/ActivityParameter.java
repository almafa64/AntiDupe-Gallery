package com.cyberegylet.antiDupeGallery.backend.activities;

import android.net.Uri;
import android.os.Parcelable;

import java.util.ArrayList;

public final class ActivityParameter
{
	public final String name;
	public final Object data;
	public final Type type;

	public enum Type
	{
		INT,
		STRING,
		STRING_ARR,
		BOOL,
		URI,
		PARCELABLE,
	}

	private ActivityParameter(String name, Object data, Type type)
	{
		this.name = name;
		this.data = data;
		this.type = type;
	}

	public ActivityParameter(String name, boolean data) { this(name, data, Type.BOOL); }

	public ActivityParameter(String name, int data) { this(name, data, Type.INT); }

	public ActivityParameter(String name, String data) { this(name, data, Type.STRING); }

	public ActivityParameter(String name, String[] data) { this(name, data, Type.STRING_ARR); }

	public ActivityParameter(String name, Uri data) { this(name, data, Type.URI); }

	public <T extends Parcelable> ActivityParameter(String name, ArrayList<T> data) { this(name, data, Type.PARCELABLE); }
}
