package com.cyberegylet.antiDupeGallery;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.cyberegylet.antiDupeGallery.backend.activities.ActivityManager;

public class SettingsActivity extends Activity
{
	private final ActivityManager activityManager = new ActivityManager(this);

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.settings_activity);

		findViewById(R.id.back_button).setOnClickListener(v -> activityManager.goBack());
	}
}
