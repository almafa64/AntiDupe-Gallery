package com.cyberegylet.antiDupeGallery.backend.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import java.util.Objects;

public class ActivityManager
{
	private final Activity currentActivity;

	public ActivityManager(Activity currentActivity) { this.currentActivity = currentActivity; }

	public void switchActivity(Class<? extends Activity> newActivity, ActivityParameter... params)
	{
		switchActivity(currentActivity, newActivity, params);
	}

	public static void switchActivity(Activity currentActivity, Class<? extends Activity> newActivity, ActivityParameter... params)
	{
		Intent intent = new Intent(currentActivity, newActivity);
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
		currentActivity.startActivity(intent);
	}

	public void goBack() { goBack(currentActivity); }

	public static void goBack(Activity currentActivity) { currentActivity.finish(); }

	public Object getParam(String name) { return getParam(currentActivity, name); }

	public static Object getParam(Activity currentActivity, String name)
	{
		return Objects.requireNonNull(currentActivity.getIntent().getExtras()).get(name);
	}
}