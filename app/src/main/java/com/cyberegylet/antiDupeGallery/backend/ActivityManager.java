package com.cyberegylet.antiDupeGallery.backend;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import java.util.Objects;

public class ActivityManager
{
	public static class Parameter
	{
		private String name;
		private Object data;
		private final Type type;

		private enum Type
		{
			INT,
			STRING,
			BOOL,
			URI,
		}

		private void innit(String name, Object data)
		{
			this.data = data;
			this.name = name;
		}

		public Parameter(String name, boolean data)
		{
			innit(name, data);
			type = Type.BOOL;
		}

		public Parameter(String name, int data)
		{
			innit(name, data);
			type = Type.INT;
		}

		public Parameter(String name, String data)
		{
			innit(name, data);
			type = Type.STRING;
		}

		public Parameter(String name, Uri data)
		{
			innit(name, data);
			type = Type.URI;
		}
	}

	public static void switchActivity(Activity activity, Class<? extends Activity> newActivity, Parameter... params)
	{
		Intent intent = new Intent(activity, newActivity);
		for (Parameter param : params)
		{
			switch (param.type)
			{
				case INT:
					intent.putExtra(param.name, (Integer) param.data); break;
				case STRING:
					intent.putExtra(param.name, (String) param.data); break;
				case BOOL:
					intent.putExtra(param.name, (Boolean) param.data); break;
				case URI:
					intent.putExtra(param.name, (Uri) param.data); break;
			}
		}
		activity.startActivity(intent);
	}

	public static void goBack(Activity activity)
	{
		activity.finish();
	}

	public static Object getParam(Activity activity, String name)
	{
		return Objects.requireNonNull(activity.getIntent().getExtras()).get(name);
	}
}
