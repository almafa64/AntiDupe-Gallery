package com.cyberegylet.antiDupeGallery.activities;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.cyberegylet.antiDupeGallery.R;
import com.cyberegylet.antiDupeGallery.backend.activities.ActivityManager;

public class FilterActivity extends Activity
{
	private static final String TAG = "FilterActivity";

	private final ActivityManager activityManager = new ActivityManager(this);

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filter_activity);

		findViewById(R.id.back_button).setOnClickListener(v -> activityManager.goBack());
	}
}
