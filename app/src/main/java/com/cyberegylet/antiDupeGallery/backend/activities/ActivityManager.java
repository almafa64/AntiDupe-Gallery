package com.cyberegylet.antiDupeGallery.backend.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import java.util.Objects;

public class ActivityManager
{
	public static void switchActivity(Activity activity, Class<? extends Activity> newActivity, ActivityParameter... params)
	{
		Intent intent = new Intent(activity, newActivity);
		for (ActivityParameter param : params)
		{
			switch (param.type)
			{
				case INT:
					intent.putExtra(param.name, (Integer) param.data);
					break;
				case STRING:
					intent.putExtra(param.name, (String) param.data);
					break;
				case BOOL:
					intent.putExtra(param.name, (Boolean) param.data);
					break;
				case URI:
					intent.putExtra(param.name, (Uri) param.data);
					break;
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
