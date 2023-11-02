package com.cyberegylet.antiDupeGallery.backend.Activites;

import android.net.Uri;

public final class ActivityParameter
{
	public final String name;
	public final Object data;
	public final Type type;

	public enum Type
	{
		INT,
		STRING,
		BOOL,
		URI,
	}

	public ActivityParameter(String name, boolean data)
	{
		this.name = name;
		this.data = data;
		this.type = Type.BOOL;
	}

	public ActivityParameter(String name, int data)
	{
		this.name = name;
		this.data = data;
		this.type = Type.INT;
	}

	public ActivityParameter(String name, String data)
	{
		this.name = name;
		this.data = data;
		this.type = Type.STRING;
	}

	public ActivityParameter(String name, Uri data)
	{
		this.name = name;
		this.data = data;
		this.type = Type.URI;
	}
}
